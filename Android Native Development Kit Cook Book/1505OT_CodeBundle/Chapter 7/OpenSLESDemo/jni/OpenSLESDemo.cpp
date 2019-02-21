#include <jni.h>

#include "mylog.h"
#include "common.h"

void naCreateEngine(JNIEnv* env, jclass clazz);
void naShutdown(JNIEnv* env, jclass clazz);
jboolean naCreateAudioRecorder(JNIEnv* env, jclass clazz);
void naStartRecording(JNIEnv* env, jclass clazz);
void naCreateBufferQueueAudioPlayer(JNIEnv* env, jclass clazz);
void naStartPlaying(JNIEnv* env, jclass clazz);

#ifdef __cplusplus
extern "C" {
#endif
	jboolean createAudioRecorder();
	void startRecording();
	void createBufferQueueAudioPlayer();
	jboolean startPlaying();
#ifdef __cplusplus
};
#endif

jint JNI_OnLoad(JavaVM* pVm, void* reserved)
{
	JNIEnv* env;
	if (pVm->GetEnv((void **)&env, JNI_VERSION_1_6) != JNI_OK) {
		 return -1;
	}
	JNINativeMethod nm[6];
	nm[0].name = "naCreateEngine";
	nm[0].signature = "()V";
	nm[0].fnPtr = (void*)naCreateEngine;
	nm[1].name = "naShutdown";
	nm[1].signature = "()V";
	nm[1].fnPtr = (void*)naShutdown;
	nm[2].name = "naCreateAudioRecorder";
	nm[2].signature = "()Z";
	nm[2].fnPtr = (void*)naCreateAudioRecorder;
	nm[3].name = "naStartRecording";
	nm[3].signature = "()V";
	nm[3].fnPtr = (void*)naStartRecording;
	nm[4].name = "naCreateBufferQueueAudioPlayer";
	nm[4].signature = "()V";
	nm[4].fnPtr = (void*)naCreateBufferQueueAudioPlayer;
	nm[5].name = "naStartPlaying";
	nm[5].signature = "()V";
	nm[5].fnPtr = (void*)naStartPlaying;
	jclass cls = env->FindClass("cookbook/chapter7/opensles/MainActivity");
	// Register methods with env->RegisterNatives.
	env->RegisterNatives(cls, nm, 6);
	return JNI_VERSION_1_6;
}

void naCreateBufferQueueAudioPlayer(JNIEnv* env, jclass clazz) {
	createBufferQueueAudioPlayer();
}

void naStartPlaying(JNIEnv* env, jclass clazz) {
	startPlaying();
}

jboolean naCreateAudioRecorder(JNIEnv* env, jclass clazz) {
	return createAudioRecorder();
}

void naStartRecording(JNIEnv* env, jclass clazz) {
	startRecording();
}

// create the engine and output mix objects
void naCreateEngine(JNIEnv* env, jclass clazz) {
    SLresult result;

    // create engine
    result = slCreateEngine(&engineObject, 0, NULL, 0, NULL, NULL);
    assert(SL_RESULT_SUCCESS == result);

    // realize the engine
    result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
    assert(SL_RESULT_SUCCESS == result);

    // get the engine interface, which is needed in order to create other objects
    result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);
    assert(SL_RESULT_SUCCESS == result);

    // create output mix, with environmental reverb specified as a non-required interface
    const SLInterfaceID ids[1] = {SL_IID_ENVIRONMENTALREVERB};
    const SLboolean req[1] = {SL_BOOLEAN_FALSE};
    result = (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 1, ids, req);
    assert(SL_RESULT_SUCCESS == result);

    // realize the output mix
    result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
    assert(SL_RESULT_SUCCESS == result);

    // get the environmental reverb interface
    // this could fail if the environmental reverb effect is not available,
    // either because the feature is not present, excessive CPU load, or
    // the required MODIFY_AUDIO_SETTINGS permission was not requested and granted
    result = (*outputMixObject)->GetInterface(outputMixObject, SL_IID_ENVIRONMENTALREVERB,
            &outputMixEnvironmentalReverb);
    if (SL_RESULT_SUCCESS == result) {
        result = (*outputMixEnvironmentalReverb)->SetEnvironmentalReverbProperties(
                outputMixEnvironmentalReverb, &reverbSettings);
    }
}

// shut down the native audio system
void naShutdown(JNIEnv* env, jclass clazz) {
    // destroy buffer queue audio player object, and invalidate all associated interfaces
    if (bqPlayerObject != NULL) {
        (*bqPlayerObject)->Destroy(bqPlayerObject);
        bqPlayerObject = NULL;
        bqPlayerPlay = NULL;
        bqPlayerBufferQueue = NULL;
        bqPlayerEffectSend = NULL;
        bqPlayerVolume = NULL;
    }

    // destroy audio recorder object, and invalidate all associated interfaces
    if (recorderObject != NULL) {
        (*recorderObject)->Destroy(recorderObject);
        recorderObject = NULL;
        recorderRecord = NULL;
        recorderBufferQueue = NULL;
    }

    // destroy output mix object, and invalidate all associated interfaces
    if (outputMixObject != NULL) {
        (*outputMixObject)->Destroy(outputMixObject);
        outputMixObject = NULL;
        outputMixEnvironmentalReverb = NULL;
    }

    // destroy engine object, and invalidate all associated interfaces
    if (engineObject != NULL) {
        (*engineObject)->Destroy(engineObject);
        engineObject = NULL;
        engineEngine = NULL;
    }

}






