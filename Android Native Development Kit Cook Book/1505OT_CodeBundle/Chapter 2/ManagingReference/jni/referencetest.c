#include <jni.h>
#include <android/log.h>
#include <stdio.h>

#include "referencetest.h"

JNIEXPORT void JNICALL Java_cookbook_chapter2_ManagingReferenceActivity_referenceAssignmentAndNew(JNIEnv *pEnv, jobject pObj, jstring pStringP) {
	jstring globalRefNew = (*pEnv)->NewGlobalRef(pEnv, pStringP);
	jstring globalRefNew2 = (*pEnv)->NewGlobalRef(pEnv, pStringP);
	jstring globalRefAssignment = globalRefNew;

	__android_log_print(ANDROID_LOG_INFO, "ReferenceAssignmentAndNew", "pStringP and globalRefNew: %d", (*pEnv)->IsSameObject(pEnv, pStringP, globalRefNew));
	__android_log_print(ANDROID_LOG_INFO, "ReferenceAssignmentAndNew", "globalRefNew and globalRefNew2: %d", (*pEnv)->IsSameObject(pEnv, globalRefNew, globalRefNew2));
	__android_log_print(ANDROID_LOG_INFO, "ReferenceAssignmentAndNew", "globalRefAssignment and globalRefNew: %d", (*pEnv)->IsSameObject(pEnv, globalRefAssignment, globalRefNew));

	//jstring and jobject are actually a pointer to void (void*), so we can print the location they're pointing at
	__android_log_print(ANDROID_LOG_INFO, "ReferenceAssignmentAndNew", "pointer size: %u", sizeof(void*));
	__android_log_print(ANDROID_LOG_INFO, "ReferenceAssignmentAndNew", "pStringP: %u", pStringP);
	__android_log_print(ANDROID_LOG_INFO, "ReferenceAssignmentAndNew", "globalRefNew: %u", globalRefNew);
	__android_log_print(ANDROID_LOG_INFO, "ReferenceAssignmentAndNew", "globalRefNew2: %u", globalRefNew2);
	__android_log_print(ANDROID_LOG_INFO, "ReferenceAssignmentAndNew", "globalRefAssignment: %u", globalRefAssignment);

	__android_log_print(ANDROID_LOG_INFO, "ReferenceAssignmentAndNew", "call DeleteGlobalRef 1");
	(*pEnv)->DeleteGlobalRef(pEnv, globalRefNew);
	__android_log_print(ANDROID_LOG_INFO, "ReferenceAssignmentAndNew", "call DeleteGlobalRef 2");
	(*pEnv)->DeleteGlobalRef(pEnv, globalRefNew);
	__android_log_print(ANDROID_LOG_INFO, "ReferenceAssignmentAndNew", "call DeleteGlobalRef 3");
	(*pEnv)->DeleteGlobalRef(pEnv, globalRefNew);
}

JNIEXPORT void JNICALL Java_cookbook_chapter2_ManagingReferenceActivity_localReference(JNIEnv *pEnv, jobject pObj, jstring pStringP, jboolean pDelete){
    jstring stStr;
	int i;
	__android_log_print(ANDROID_LOG_INFO, "localReference", "delete after allocation: %d", pDelete);
	for (i = 0; i < 10000; ++i) {
		stStr = (*pEnv)->NewLocalRef(pEnv, pStringP);
		if (pDelete) {
			(*pEnv)->DeleteLocalRef(pEnv, stStr);
		}
	}
	__android_log_print(ANDROID_LOG_INFO, "localReference", "finished");
}

JNIEXPORT void JNICALL Java_cookbook_chapter2_ManagingReferenceActivity_globalReference(JNIEnv *pEnv, jobject pObj, jstring pStringP, jboolean pDelete){
	static jstring stStr;
	const jbyte *str;
	jboolean *isCopy;
	if (NULL == stStr) {
		stStr = (*pEnv)->NewGlobalRef(pEnv, pStringP);
		__android_log_print(ANDROID_LOG_INFO, "globalReference", "create global reference");
		if (NULL == stStr) {
			__android_log_print(ANDROID_LOG_INFO, "globalReference", "cannot create global reference");
			return;
		}
	}
	str = (*pEnv)->GetStringUTFChars(pEnv, stStr, isCopy);
	if (NULL == str) {
		__android_log_print(ANDROID_LOG_INFO, "globalReference", "cannot convert jstring to utf-8 string");
		return;
	}
	__android_log_print(ANDROID_LOG_INFO, "globalReference", "print UTF-8 string: %s", str);
	if (pDelete) {
		(*pEnv)->DeleteGlobalRef(pEnv, stStr);
		stStr = NULL;
		__android_log_print(ANDROID_LOG_INFO, "globalReference", "global reference deleted");
	}
}

JNIEXPORT void JNICALL Java_cookbook_chapter2_ManagingReferenceActivity_weakReference(JNIEnv *pEnv, jobject pObj, jstring pStringP, jboolean pDelete){
	static jstring stStr;
	const jbyte *str;
	jboolean *isCopy;
	if (NULL == stStr) {
		stStr = (*pEnv)->NewWeakGlobalRef(pEnv, pStringP);
		__android_log_print(ANDROID_LOG_INFO, "weakReference", "create weak reference");
		if (NULL == stStr) {
			__android_log_print(ANDROID_LOG_INFO, "weakReference", "cannot create weak reference");
			return;
		}
	}
	str = (*pEnv)->GetStringUTFChars(pEnv, stStr, isCopy);
	if (NULL == str)  {
		__android_log_print(ANDROID_LOG_INFO, "weakReference", "cannot convert jstring to utf-8 string");
		return;
	}
	__android_log_print(ANDROID_LOG_INFO, "weakReference", "print UTF-8 string: %s", str);
	if (pDelete) {
		(*pEnv)->DeleteWeakGlobalRef(pEnv, stStr);
		stStr = NULL;
		__android_log_print(ANDROID_LOG_INFO, "weakReference", "weak reference deleted");
	}
}
