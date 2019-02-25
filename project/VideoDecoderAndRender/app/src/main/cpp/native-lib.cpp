#include <jni.h>
#include <string>
#include "dm01/TwoDG1.h"
#include "dm02/CubeG2.h"
#include "dm03/EGLDemo.h"

//extern "C" JNIEXPORT jstring JNICALL
//Java_com_example_videodecoderandrender_MainActivity_stringFromJNI(JNIEnv *env,jobject /* this */) {
//    std::string hello = "Hello from C++";
//    return env->NewStringUTF(hello.c_str());
//}

void stringFromJNI(JNIEnv *env,jobject /* this */) {
//    std::string hello = "Hello from C++";
//    return env->NewStringUTF(hello.c_str());
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
    jclass cls = env->FindClass("com/example/videodecoderandrender/MainActivity");
    env->RegisterNatives(cls, nm, 1);

    TwoDG1::OnLoad(env, reserved);
    CubeG2::OnLoad(env, reserved);
    EGLDemo::OnLoad(env, reserved);
    return JNI_VERSION_1_6;
}