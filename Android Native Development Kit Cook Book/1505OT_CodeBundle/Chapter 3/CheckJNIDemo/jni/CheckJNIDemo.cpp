#include <jni.h>

jintArray getNewIntArray(JNIEnv *pEnv) {
	jintArray jintArr = pEnv->NewIntArray(-1);
	return jintArr;
}

extern "C" {
	JNIEXPORT jintArray JNICALL Java_cookbook_chapter3_CheckJNIDemoActivity_CheckJNIDemo(JNIEnv *pEnv, jobject pObj) {
		return getNewIntArray(pEnv);
	}
}
