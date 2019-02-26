#include <jni.h>
#include <string>
extern "C" {
#include <libavutil/log.h>
#include "my_log.h"
}

void stringFromJNI(JNIEnv *env,jobject obj) {
//    std::string hello = "Hello from C++";
//    return env->NewStringUTF(hello.c_str());
    LOGD("AVNative", "stringFromJNI call frome native: %s\n", "Hello World!");
    av_log(NULL, AV_LOG_ERROR, "this is ffmpeg log func: %s\n", "hello ffmpeg!");
}

JNIEXPORT jint JNI_OnLoad(JavaVM* pVm, void* reserved){
    JNIEnv* env;
    if (pVm->GetEnv((void **)&env, JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }

    JNINativeMethod nm[1];
    nm[0].name = "stringFromJNI";
    nm[0].signature = "()V";
    nm[0].fnPtr = (void*)stringFromJNI;
    jclass cls = env->FindClass("com/kugou/audiovideoplayersample/MainActivity");
    env->RegisterNatives(cls, nm, 1);

    init_my_log();
    return JNI_VERSION_1_6;
}