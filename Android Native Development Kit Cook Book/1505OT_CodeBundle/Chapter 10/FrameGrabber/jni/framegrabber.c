#include <jni.h>
#include <android/bitmap.h>

#include <unistd.h>
#include <pthread.h>
#include <semaphore.h>

#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libswscale/swscale.h>
#include <libavutil/avutil.h>
#include <libavutil/mathematics.h>

#define LOG_LEVEL 10
/*for android logs*/
#include <android/log.h>
#define LOG_TAG "libframegrabber"
#define LOGI(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__);}
#define LOGE(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__);}

#define VIDEO_FR_QUEUE_SIZE 5

typedef struct VideoState {
	AVFormatContext *pFormatCtx;
	AVStream *pVideoStream;
	int videoStreamIdx;

	AVFrame* frameq[VIDEO_FR_QUEUE_SIZE];
	int frameqDequeueIdx, frameqEnqueueIdx;	

	int fint;
	int64_t nextFrameTime;		//track the next frame display time

	sem_t fqfullsem, fqemptysem;
	int status;		//0: continue; 1: quit; 2: end of decoding; 3: end of display

	int ifSeek;
	int seekTargetTime;		//in milliseconds
	pthread_mutex_t mux;
	pthread_cond_t cond;
}VideoState;

typedef struct VideoDisplayUtil {
	struct SwsContext *img_resample_ctx;
	AVFrame *pFrameRGBA;
	int width, height;

	void* pBitmap;	//use the bitmap as the buffer for pFrameRGBA
	int frameNum;
} VideoDisplayUtil;

char *gVideoFileName;
VideoState *gvs;
VideoDisplayUtil *gvdu;

//initialize the picq frameq queue
void init_queue(VideoState *vs) {
	int i;
	for (i = 0; i < VIDEO_FR_QUEUE_SIZE; ++i) {
		vs->frameq[i] = avcodec_alloc_frame();
	} 
	vs->frameqDequeueIdx = 0;
	vs->frameqEnqueueIdx = 0;
	sem_init(&vs->fqemptysem, 0, VIDEO_FR_QUEUE_SIZE);
	sem_init(&vs->fqfullsem, 0, 0);
} 
//destroy the queue
void destroy_queue(VideoState *vs) {
	int i;
	for (i = 0; i < VIDEO_FR_QUEUE_SIZE; ++i) {
		avcodec_free_frame(&vs->frameq[i]);
	}
	sem_destroy(&vs->fqemptysem);
	sem_destroy(&vs->fqfullsem);
}

//function run by a separate thread to decode frames and put them into the queue
void *decode_video_thread(void *arg) {
	VideoState* vs = (VideoState *)arg;
	//read frames and decode them 
	AVPacket packet;
	int framefinished;
	while ((!vs->status) && 0 <= av_read_frame(vs->pFormatCtx, &packet)) {
		if (vs->ifSeek) {
			//convert time from seconds to AV_TIME_BASE
			int64_t targetTime = (int64_t)(vs->seekTargetTime * (AV_TIME_BASE / 1000));	//seekTargetTime is in milliseconds
			LOGI(10, "sizeof(int64_t) = %d, long int = %d, long long = %d", sizeof(int64_t), sizeof(long int), sizeof(long long));
			//convert time from AV_TIME_BASE to AVStream.time_base
			LOGI(10, "decode thread seek to %d*%d / 1000=%lld %d:%d %d:%d", vs->seekTargetTime, AV_TIME_BASE, targetTime, AV_TIME_BASE_Q.num, AV_TIME_BASE_Q.den, vs->pVideoStream->time_base.num, vs->pVideoStream->time_base.den);
			targetTime = av_rescale_q(targetTime, AV_TIME_BASE_Q, vs->pVideoStream->time_base);
			LOGI(10, "decode thread seek to %d", targetTime);
			if (av_seek_frame(gvs->pFormatCtx, vs->videoStreamIdx, targetTime, 0) < 0) {
				LOGE(1, "seek error");
			}
			//reset enqueue index to 0
			vs->frameqEnqueueIdx = 0;
			pthread_mutex_lock(&vs->mux);
			//wait for decoding thread to clear the queue
			while (2!=vs->ifSeek) {
				pthread_cond_wait(&vs->cond, &vs->mux);
			}
			vs->ifSeek = 0;
			pthread_mutex_unlock(&vs->mux);
			LOGI(10, "decode thread finish seeking");
		} else if (vs->videoStreamIdx == packet.stream_index) {
			LOGI(10, "start decoding a new frame");
			//get a video frame from the frameq
			sem_wait(&vs->fqemptysem);		//take an empty slot
			AVFrame *pframe = vs->frameq[vs->frameqEnqueueIdx];
			avcodec_decode_video2(vs->pVideoStream->codec, pframe, &framefinished, &packet);
			if (framefinished) {
				LOGI(10, "decode thread decoded another frame");
				vs->frameqEnqueueIdx++;
				if (VIDEO_FR_QUEUE_SIZE == vs->frameqEnqueueIdx) {
					vs->frameqEnqueueIdx = 0;		//round to the first 
				}
				sem_post(&vs->fqfullsem);	//signal a frame is enqueued
			}
		}
		av_free_packet(&packet);
	}
	LOGI(10, "end of decoding");
	vs->status = 2;	//end of decoding
	return 0;
}

int naPrepareDisplay(JNIEnv *pEnv, jobject pObj, jobject pBitmap, jint width, jint height) {
	VideoState* vs = gvs;
	VideoDisplayUtil* vdu = av_mallocz(sizeof(VideoDisplayUtil));
	gvdu = vdu;
	vdu->frameNum = 0;
	vdu->width = width;
	vdu->height = height;
	vdu->pFrameRGBA = avcodec_alloc_frame();
	AndroidBitmapInfo linfo;
	int lret;
	//1. retrieve information about the bitmap
	if ((lret = AndroidBitmap_getInfo(pEnv, pBitmap, &linfo)) < 0) {
		LOGE(1, "AndroidBitmap_getinfo failed! error = %d", lret);
		return -1;
	}
	if (linfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
		LOGE(1, "bitmap format is not rgba_8888!");
		return- 1;
	}
	//2. lock the pixel buffer and retrieve a pointer to it
	if ((lret = AndroidBitmap_lockPixels(pEnv, pBitmap, &vdu->pBitmap)) < 0) {
		LOGE(1, "AndroidBitmap_lockpixels() failed! error = %d", lret);
		return -1;
	}
	//if android, we use the bitmap buffer as the buffer for pFrameRGBA
	avpicture_fill((AVPicture*)vdu->pFrameRGBA, vdu->pBitmap, PIX_FMT_RGBA, width, height);
	vdu->img_resample_ctx = sws_getContext(vs->pVideoStream->codec->width, vs->pVideoStream->codec->height, vs->pVideoStream->codec->pix_fmt, width, height, PIX_FMT_RGBA, SWS_BICUBIC, NULL, NULL, NULL);
	if (NULL == vdu->img_resample_ctx) {
		LOGE(1, "error initialize the video frame conversion context");
		return -1;
	}
	vs->nextFrameTime = av_gettime() + 50*1000;	//introduce 50 milliseconds of initial delay
	return 0;
}
	
//function run by a separate thread to read AVFrame from queue, scale the frame and dump it to pictures
int naGetVideoFrame(JNIEnv *pEnv, jobject pObj) {
	VideoState *vs = gvs;
	VideoDisplayUtil *vdu = gvdu;
	if (!vs->status || (vs->frameqDequeueIdx != vs->frameqEnqueueIdx)) {	
		if (vs->ifSeek) {
			//clear the queue without displaying
			int numOfItemsInQueue = 0;
			sem_getvalue(&vs->fqfullsem, &numOfItemsInQueue);
			LOGI(10, "display thread No. of items to clear in queue: %d", numOfItemsInQueue);
			while (numOfItemsInQueue--) {
				sem_wait(&vs->fqfullsem);	//wait until there's frame in the queue
				sem_post(&vs->fqemptysem);
			}
			while (vs->ifSeek) {
				//signal in a loop to ensure the decoding thread can receive it
				pthread_mutex_lock(&vs->mux);
				vs->ifSeek = 2;
				pthread_cond_signal(&vs->cond);	//signal that display thread has cleared the queue for seeking operation, so decoding thread can continue
				pthread_mutex_unlock(&vs->mux);
			}
			//reset dequeue idx to 0
			vs->frameqDequeueIdx = 0;
			//adjust display clock
			vdu->frameNum = vs->seekTargetTime / vs->fint;
			vs->nextFrameTime += vs->fint * 1000;
			//seeking is done, clear the flag and signal display decoding thread to continue
			LOGI(10, "display thread finish seeking to frame %d", vdu->frameNum);
		} else {
			LOGI(10, "start display a new frame");
			sem_wait(&vs->fqfullsem);	//wait until there's frame in the queue
			AVFrame *pframe = vs->frameq[vs->frameqDequeueIdx];
			//scale the frame
        	sws_scale(vdu->img_resample_ctx, pframe->data, pframe->linesize, 0, vs->pVideoStream->codec->height, vdu->pFrameRGBA->data, vdu->pFrameRGBA->linesize);
			int64_t curtime = av_gettime();
			if (vs->nextFrameTime - curtime > 20*1000) {		//only sleep if the delay bigger than 20 ms
				LOGI(10, "sleep for %lld microseconds", vs->nextFrameTime - curtime);
				usleep(vs->nextFrameTime-curtime);
			}
			++vdu->frameNum;
			vs->nextFrameTime += vs->fint*1000;			//update the next frame play time, fint in milliseconds, convert to macro seconds
			LOGI(10, "display thread processed frame %d", vdu->frameNum);
			vs->frameqDequeueIdx++;
			if (VIDEO_FR_QUEUE_SIZE == vs->frameqDequeueIdx) {
				vs->frameqDequeueIdx = 0;
			}
			sem_post(&vs->fqemptysem);
		}
		return vdu->frameNum;
	} else {
		LOGI(10, "no more frames to display");
		vs->status = 3;	//no more frame to display
		return 0;
	}
}

//initialize the context for an input video
int naInit(JNIEnv *pEnv, jobject pObj, jstring pfilename) {
	gVideoFileName = (char *)(*pEnv)->GetStringUTFChars(pEnv, pfilename, NULL);
	LOGI(10, "video file name is %s", gVideoFileName);
	av_register_all();
	VideoState *vs;
	vs = av_mallocz(sizeof(VideoState));
	gvs = vs;
	av_register_all();	 
	//open the video file
	if (0 != avformat_open_input(&vs->pFormatCtx, gVideoFileName, NULL, NULL)) {
		LOGE(1, "could not open video file: %s", gVideoFileName);
		return -1;  
	}
	LOGI(10, "opened file %s", gVideoFileName);
	//retrieve stream info
	if (0 > avformat_find_stream_info(vs->pFormatCtx, NULL)) {
		LOGE(1, "could not find stream info");	
		return -1;
	}
	//find the video stream
	int i;
	AVCodecContext *pcodecctx;
	//find the first video stream
	vs->videoStreamIdx = -1;
	for (i = 0; i < vs->pFormatCtx->nb_streams; ++i) {
		if (AVMEDIA_TYPE_VIDEO == vs->pFormatCtx->streams[i]->codec->codec_type) {
			vs->videoStreamIdx = i;
			vs->pVideoStream = vs->pFormatCtx->streams[i];
			break;
		}
	}	
	if (-1 == vs->videoStreamIdx) {
		LOGI(1, "could not find a video stream");
		return -1;
	}
	//find the frame rate
	if(vs->pVideoStream->avg_frame_rate.den && vs->pVideoStream->avg_frame_rate.num) {
		vs->fint = 1000/av_q2d(vs->pVideoStream->avg_frame_rate);
    } else if(vs->pVideoStream->r_frame_rate.den && vs->pVideoStream->r_frame_rate.num) {
		vs->fint = 1000/av_q2d(vs->pVideoStream->r_frame_rate);
	} else if(vs->pVideoStream->time_base.den && vs->pVideoStream->time_base.num) {
		vs->fint = 1000*av_q2d(vs->pVideoStream->time_base);
	} else if(vs->pVideoStream->codec->time_base.den && vs->pVideoStream->codec->time_base.num) {
		vs->fint = 1000*av_q2d(vs->pVideoStream->codec->time_base);
	}
	if (vs->fint < 20) {
		vs->fint = 20;	//min interval 20 => max frame rate 1000/20=50;
	} else if (vs->fint > 100) {
		vs->fint = 100;	//max interval 100=> min frame rate 1000/100=10;
	}
	LOGI(1, "frame display interval: %d ms", vs->fint);
	//get the decoder from the video stream
	pcodecctx = vs->pFormatCtx->streams[vs->videoStreamIdx]->codec;
	AVCodec *pcodec;
	pcodec = avcodec_find_decoder(pcodecctx->codec_id);
	if (NULL == pcodec) {
		LOGE(1, "unsupported codec");
		return -1;
	}
	//open the codec
	if (0 > avcodec_open2(pcodecctx, pcodec, NULL)) {
		LOGE(1, "could not open codec");
		return -1;
	}
	vs->ifSeek = 0;
	vs->seekTargetTime = 0;
	pthread_mutex_init(&vs->mux, NULL);
	pthread_cond_init(&vs->cond, NULL);
	return 0;
}

int naStartDecodeVideo(JNIEnv *pEnv, jobject pObj) {
	//start two threads to run the decode and display
	VideoState *vs = gvs;
	init_queue(vs);
	vs->status = 0;
	pthread_t decodeThread, displayThread;
	pthread_create(&decodeThread, NULL, decode_video_thread, (void*)vs);
	return 0;	
}

int naFinish(JNIEnv *pEnv, jobject pObj, jobject pBitmap) {
	VideoDisplayUtil* vdu = gvdu;
	//free RGBA images
	AndroidBitmap_unlockPixels(pEnv, pBitmap);
	avcodec_free_frame(&vdu->pFrameRGBA);
	VideoState* vs = gvs;
	destroy_queue(vs);
	//close codec
	avcodec_close(vs->pVideoStream->codec);
	//close video file
	avformat_close_input(&vs->pFormatCtx);
	av_free(vs);
	pthread_mutex_destroy(&vs->mux);
	pthread_cond_destroy(&vs->cond);
	return 0;
}

jintArray naGetVideoRes(JNIEnv *pEnv, jobject pObj) {
    jintArray lRes;
	AVCodecContext* vCodecCtx = gvs->pVideoStream->codec;
	if (NULL == vCodecCtx) {
		return NULL;
	}
	lRes = (*pEnv)->NewIntArray(pEnv, 2);
	if (lRes == NULL) {
		LOGI(1, "cannot allocate memory for video size");
		return NULL;
	}
	jint lVideoRes[2];
	lVideoRes[0] = vCodecCtx->width;
	lVideoRes[1] = vCodecCtx->height;
	(*pEnv)->SetIntArrayRegion(pEnv, lRes, 0, 2, lVideoRes);
	return lRes;
}

void naQuit(JNIEnv *pEnv, jobject pObj) {
	//quit the playback
	gvs->status = 1;
}

jint naGetDuration(JNIEnv *pEnv, jobject pObj) {
	if (NULL != gvs->pFormatCtx) {
		return (gvs->pFormatCtx->duration / AV_TIME_BASE);
	} else {
		return -1;
	}
}

jint naGetFrameRate(JNIEnv *pEnv, jobject pObj) {
	return 1000/gvs->fint;
}

void naSeekToTime(JNIEnv *pEnv, jobject pObj, float pSec) {
	gvs->seekTargetTime = (int)(pSec*1000);
	gvs->ifSeek = 1;	
}

jint JNI_OnLoad(JavaVM* pVm, void* reserved) {
	JNIEnv* env;
	if ((*pVm)->GetEnv(pVm, (void **)&env, JNI_VERSION_1_6) != JNI_OK) {
		 return -1;
	}
	JNINativeMethod nm[10];
	nm[0].name = "naInit";
	nm[0].signature = "(Ljava/lang/String;)I";
	nm[0].fnPtr = (void*)naInit;
	nm[1].name = "naStartDecodeVideo";
	nm[1].signature = "()I";
	nm[1].fnPtr = (void*)naStartDecodeVideo;
	nm[2].name = "naGetVideoRes";
	nm[2].signature = "()[I";
	nm[2].fnPtr = (void*)naGetVideoRes;
	nm[3].name = "naPrepareDisplay";
	nm[3].signature = "(Landroid/graphics/Bitmap;II)I";
	nm[3].fnPtr = (void*)naPrepareDisplay;
	nm[4].name = "naFinish";
	nm[4].signature = "(Landroid/graphics/Bitmap;)I";
	nm[4].fnPtr = (void*)naFinish;
	nm[5].name = "naGetVideoFrame";
	nm[5].signature = "()I";
	nm[5].fnPtr = (void*)naGetVideoFrame;
	nm[6].name = "naQuit";
	nm[6].signature = "()V";
	nm[6].fnPtr = (void*)naQuit;
	nm[7].name = "naGetDuration";
	nm[7].signature = "()I";
	nm[7].fnPtr = (void*)naGetDuration;
	nm[8].name = "naSeekToTime";
	nm[8].signature = "(F)V";
	nm[8].fnPtr = (void*)naSeekToTime;
	nm[9].name = "naGetFrameRate";
	nm[9].signature = "()I";
	nm[9].fnPtr = (void*)naGetFrameRate;
	jclass cls = (*env)->FindClass(env, "cookbook/chapter10/framegrabber/SimpleVideoSurfaceView");
	//Register methods with env->RegisterNatives.
	(*env)->RegisterNatives(env, cls, nm, 10);
	return JNI_VERSION_1_6;
}
