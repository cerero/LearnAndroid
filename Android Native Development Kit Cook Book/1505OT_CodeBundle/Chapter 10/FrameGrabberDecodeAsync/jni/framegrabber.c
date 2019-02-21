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
	int status;
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

void *decode_video_thread(void *arg) {
	VideoState* vs = (VideoState*)arg;
	AVPacket packet;
	int framefinished;
	while ((!vs->status) && 0 == av_read_frame(vs->pFormatCtx, &packet)) {
		if (vs->videoStreamIdx == packet.stream_index) {
			LOGI(10, "start decoding a new frame");
			sem_wait(&vs->fqemptysem);	//take an empty slot
			AVFrame *pframe = vs->frameq[vs->frameqEnqueueIdx];
			avcodec_decode_video2(vs->pVideoStream->codec, pframe, &framefinished, &packet);
			if (framefinished) {
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

int naGetVideoFrame(JNIEnv *pEnv, jobject pObj) {
	VideoState* vs = gvs;
	VideoDisplayUtil *vdu = gvdu;
	if (!vs->status || (vs->frameqDequeueIdx != vs->frameqEnqueueIdx)) {	
		int filledSlots = 0;
		sem_getvalue(&vs->fqfullsem, &filledSlots);
		LOGI(10, "start display a new frame: %d", filledSlots);
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
		return vdu->frameNum;
	} else {
		LOGI(10, "no more frames to display");
		vs->status = 3;	//no more frame to display
		return 0;
	}
}

int naStartDecodeVideo(JNIEnv *pEnv, jobject pObj) {
	//start two threads to run the decode and display
	VideoState *vs = gvs;
	init_queue(vs);
	vs->status = 0;
	pthread_t decodeThread;
	pthread_create(&decodeThread, NULL, decode_video_thread, (void*)vs);
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
	//for android, we use the bitmap buffer as the buffer for pFrameRGBA
	avpicture_fill((AVPicture*)vdu->pFrameRGBA, vdu->pBitmap, PIX_FMT_RGBA, width, height);
	vdu->img_resample_ctx = sws_getContext(vs->pVideoStream->codec->width, vs->pVideoStream->codec->height, vs->pVideoStream->codec->pix_fmt, width, height, PIX_FMT_RGBA, SWS_BICUBIC, NULL, NULL, NULL);
	if (NULL == vdu->img_resample_ctx) {
		LOGE(1, "error initialize the video frame conversion context");
		return -1;
	}
	vs->nextFrameTime = av_gettime() + 50*1000;	//introduce 50 milliseconds of initial delay
	return 0;
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
	vs->status = 0;
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

jint JNI_OnLoad(JavaVM* pVm, void* reserved) {
	JNIEnv* env;
	if ((*pVm)->GetEnv(pVm, (void **)&env, JNI_VERSION_1_6) != JNI_OK) {
		 return -1;
	}
	JNINativeMethod nm[9];
	nm[0].name = "naInit";
	nm[0].signature = "(Ljava/lang/String;)I";
	nm[0].fnPtr = (void*)naInit;
	nm[1].name = "naGetVideoRes";
	nm[1].signature = "()[I";
	nm[1].fnPtr = (void*)naGetVideoRes;
	nm[2].name = "naPrepareDisplay";
	nm[2].signature = "(Landroid/graphics/Bitmap;II)I";
	nm[2].fnPtr = (void*)naPrepareDisplay;
	nm[3].name = "naFinish";
	nm[3].signature = "(Landroid/graphics/Bitmap;)I";
	nm[3].fnPtr = (void*)naFinish;
	nm[4].name = "naGetVideoFrame";
	nm[4].signature = "()I";
	nm[4].fnPtr = (void*)naGetVideoFrame;
	nm[5].name = "naQuit";
	nm[5].signature = "()V";
	nm[5].fnPtr = (void*)naQuit;
	nm[6].name = "naGetDuration";
	nm[6].signature = "()I";
	nm[6].fnPtr = (void*)naGetDuration;
	nm[7].name = "naGetFrameRate";
	nm[7].signature = "()I";
	nm[7].fnPtr = (void*)naGetFrameRate;
	nm[8].name = "naStartDecodeVideo";
	nm[8].signature = "()I";
	nm[8].fnPtr = (void*)naStartDecodeVideo;
	jclass cls = (*env)->FindClass(env, "cookbook/chapter10/framegrabber/SimpleVideoSurfaceView");
	//Register methods with env->RegisterNatives.
	(*env)->RegisterNatives(env, cls, nm, 9);
	return JNI_VERSION_1_6;
}
