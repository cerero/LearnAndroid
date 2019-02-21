#include <jni.h>
#include <android/bitmap.h>

#include <unistd.h>

#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>

#define LOG_LEVEL 10
/*for android logs*/
#include <android/log.h>
#define LOG_TAG "libframegrabber"
#define LOGI(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__);}
#define LOGE(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__);}


typedef struct VideoState {
	AVFormatContext *pFormatCtx;
	AVStream *pVideoStream;
	int videoStreamIdx;
}VideoState;

char *gVideoFileName;
VideoState *gvs;

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
	//get the decoder from the video stream
	pcodecctx = vs->pFormatCtx->streams[vs->videoStreamIdx]->codec;
	AVCodec *pcodec;
	pcodec = avcodec_find_decoder(pcodecctx->codec_id);
	if (NULL == pcodec) {
		LOGE(1, "unsupported codec");
		return -1;
	}
//	LOGI(10, "codec type %s, bit rate = %d codec = %d", pcodecctx->codec_name, pcodecctx->bit_rate, pcodecctx->codec);
	//open the codec
	if (0 > avcodec_open2(pcodecctx, pcodec, NULL)) {
		LOGE(1, "could not open codec");
		return -1;
	}
//	LOGI(10, "codec type %s, bit rate = %d codec=%d", pcodecctx->codec_name, pcodecctx->bit_rate, pcodecctx->codec);
	return 0;
}

int naFinish(JNIEnv *pEnv, jobject pObj) {
	VideoState* vs = gvs;
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

jint naGetDuration(JNIEnv *pEnv, jobject pObj) {
	if (NULL != gvs->pFormatCtx) {
		return (gvs->pFormatCtx->duration / AV_TIME_BASE);
	} else {
		return -1;
	}
}

jint naGetFrameRate(JNIEnv *pEnv, jobject pObj) {
	//find the frame rate
	int fr;
	VideoState *vs = gvs;
	if(vs->pVideoStream->avg_frame_rate.den && vs->pVideoStream->avg_frame_rate.num) {
		fr = av_q2d(vs->pVideoStream->avg_frame_rate);
	} else if(vs->pVideoStream->r_frame_rate.den && vs->pVideoStream->r_frame_rate.num) {
		fr = av_q2d(vs->pVideoStream->r_frame_rate);
	} else if(vs->pVideoStream->time_base.den && vs->pVideoStream->time_base.num) {
		fr = 1/av_q2d(vs->pVideoStream->time_base);
	} else if(vs->pVideoStream->codec->time_base.den && vs->pVideoStream->codec->time_base.num) {
		fr = 1/av_q2d(vs->pVideoStream->codec->time_base);
	}
	return fr;
}

jint JNI_OnLoad(JavaVM* pVm, void* reserved) {
	JNIEnv* env;
	if ((*pVm)->GetEnv(pVm, (void **)&env, JNI_VERSION_1_6) != JNI_OK) {
		 return -1;
	}
	JNINativeMethod nm[5];
	nm[0].name = "naInit";
	nm[0].signature = "(Ljava/lang/String;)I";
	nm[0].fnPtr = (void*)naInit;
	nm[1].name = "naGetVideoRes";
	nm[1].signature = "()[I";
	nm[1].fnPtr = (void*)naGetVideoRes;
	nm[2].name = "naFinish";
	nm[2].signature = "()I";
	nm[2].fnPtr = (void*)naFinish;
	nm[3].name = "naGetDuration";
	nm[3].signature = "()I";
	nm[3].fnPtr = (void*)naGetDuration;
	nm[4].name = "naGetFrameRate";
	nm[4].signature = "()I";
	nm[4].fnPtr = (void*)naGetFrameRate;
	jclass cls = (*env)->FindClass(env, "cookbook/chapter10/framegrabber/VideoLibraryFragment");
	//Register methods with env->RegisterNatives.
	(*env)->RegisterNatives(env, cls, nm, 5);
	return JNI_VERSION_1_6;
}
