#include <jni.h>
#include <android/log.h>
#include <stdio.h>

#include "cachingtest.h"

jmethodID mid;
jfieldID fid;

JNIEXPORT void JNICALL Java_cookbook_chapter2_CachingActivity_InitIDs(JNIEnv *pEnv, jobject pObj) {
	fid = (*pEnv)->GetFieldID(pEnv, pObj, "value", "I");
	mid = (*pEnv)->GetMethodID(pEnv, pObj, "getValue", "()I");
}

JNIEXPORT void JNICALL Java_cookbook_chapter2_CachingActivity_CachingFieldMethodIDDemo1(JNIEnv *pEnv, jobject pObj) {
	jint rvalue = (*pEnv)->CallIntMethod(pEnv, pObj, mid);
	jint fvalue = (*pEnv)->GetIntField(pEnv, pObj, fid);
	__android_log_print(ANDROID_LOG_INFO, "CachingFieldMethodIDDemo1", "getValue returned %d: value field %d", rvalue, fvalue);
}

JNIEXPORT void JNICALL Java_cookbook_chapter2_CachingActivity_CachingFieldMethodIDDemo2(JNIEnv *pEnv, jobject pObj) {
	jclass cls = (*pEnv)->GetObjectClass(pEnv, pObj);
	if (NULL == fid) {
		__android_log_print(ANDROID_LOG_INFO, "CachingFieldMethodIDDemo2", "fid is null, get it");
		fid = (*pEnv)->GetFieldID(pEnv, cls, "value", "I");
	}
	if (NULL == mid) {
		__android_log_print(ANDROID_LOG_INFO, "CachingFieldMethodIDDemo2", "mid is null, get it");
		mid = (*pEnv)->GetMethodID(pEnv, cls, "getValue", "()I");
	}
	jint rvalue = (*pEnv)->CallIntMethod(pEnv, pObj, mid);
	jint fvalue = (*pEnv)->GetIntField(pEnv, pObj, fid);
	__android_log_print(ANDROID_LOG_INFO, "CachingFieldMethodIDDemo2", "getValue returned %d: value field %d", rvalue, fvalue);

}

JNIEXPORT void JNICALL Java_cookbook_chapter2_CachingActivity_CachingReferencesDemo(JNIEnv *pEnv, jobject pObj, jboolean pDelete) {
	static jstring cachedStr;
	if (NULL == cachedStr) {
		__android_log_print(ANDROID_LOG_INFO, "CachingReferencesDemo", "cached string is null, cache it");
		jstring str = (*pEnv)->NewStringUTF(pEnv, "caching string");
		cachedStr = (*pEnv)->NewGlobalRef(pEnv, str);
		(*pEnv)->DeleteLocalRef(pEnv, str);
	}
	//print the cached string value
	const jbyte *strChars = (*pEnv)->GetStringUTFChars(pEnv, cachedStr, NULL);
	__android_log_print(ANDROID_LOG_INFO, "CachingReferencesDemo", "%s", strChars);
	 (*pEnv)->ReleaseStringUTFChars(pEnv, cachedStr, strChars);
	if (pDelete) {
		//delete the global reference
		(*pEnv)->DeleteGlobalRef(pEnv, cachedStr);
		cachedStr = NULL;
		__android_log_print(ANDROID_LOG_INFO, "CachingReferencesDemo", "deleted the global reference");
	}
	__android_log_print(ANDROID_LOG_INFO, "CachingReferencesDemo", "--------------");
}


