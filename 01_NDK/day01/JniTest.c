#include "JniTest.h"

JNIEXPORT jstring JNICALL Java_JniTest_getStringFromC(JNIEnv *env, jclass cls) {
    return (*env)->NewStringUTF(env, "Hello CPP From JNI");
}
