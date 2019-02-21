#include <jni.h>

JNIEXPORT jint JNICALL Java_cookbook_chapter2_AssemblyInJNIActivity_AssemblyMultiplyDemo(JNIEnv *pEnv, jobject pObj, jint a, jint b) {
    int rv = a*b;
	return rv;
}
