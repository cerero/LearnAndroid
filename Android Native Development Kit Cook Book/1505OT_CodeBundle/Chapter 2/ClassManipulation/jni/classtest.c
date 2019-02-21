#include <jni.h>
#include <android/log.h>
#include <stdio.h>

#include <pthread.h>

#include "classtest.h"


JavaVM *cachedVm;
jclass cachedClassDummy;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved)
{
	 JNIEnv *env;
	 jclass cls;
	 cachedVm = jvm;  //cache the VM pointer

	 if ((*jvm)->GetEnv(jvm, (void **)&env, JNI_VERSION_1_6) != JNI_OK) {
	     return JNI_ERR;
	 }
 	 return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL Java_cookbook_chapter2_ClassManipulationActivity_findClassDemo(JNIEnv *pEnv, jobject pObj) {
	jclass strClass = (*pEnv)->FindClass(pEnv, "java/lang/String");
	if (strClass == NULL) {
		__android_log_print(ANDROID_LOG_INFO, "findClassDemo", "find String class failed");
	} else {
		__android_log_print(ANDROID_LOG_INFO, "findClassDemo", "find String class succeeded");
		(*pEnv)->DeleteLocalRef(pEnv, strClass);
	}
	jclass dummyClass = (*pEnv)->FindClass(pEnv, "cookbook/chapter2/Dummy");
	if (dummyClass == NULL) {
		__android_log_print(ANDROID_LOG_INFO, "findClassDemo", "find Dummy class failed");
	} else {
		__android_log_print(ANDROID_LOG_INFO, "findClassDemo", "find Dummy class succeeded");
		(*pEnv)->DeleteLocalRef(pEnv, dummyClass);
	}
}

static void *pthread_dummy(void *arg) {
	JNIEnv *env;
	(*cachedVm)->AttachCurrentThread(cachedVm, &env, NULL);
	jclass strClass = (*env)->FindClass(env, "java/lang/String");
	if (strClass == NULL) {
		__android_log_print(ANDROID_LOG_INFO, "pthread_dummy", "after attach: find String class failed");
	} else {
		__android_log_print(ANDROID_LOG_INFO, "pthread_dummy", "after attach: find String class succeeded");
		(*env)->DeleteLocalRef(env, strClass);
	}
	jclass dummyClass = (*env)->FindClass(env, "cookbook/chapter2/Dummy");
	if (dummyClass == NULL) {
		__android_log_print(ANDROID_LOG_INFO, "pthread_dummy", "after attach: find Dummy class failed");
	} else {
		__android_log_print(ANDROID_LOG_INFO, "pthread_dummy", "after attach: find Dummy class succeeded");
		(*env)->DeleteLocalRef(env, dummyClass);
	}
	(*cachedVm)->DetachCurrentThread(cachedVm);
}

JNIEXPORT void JNICALL Java_cookbook_chapter2_ClassManipulationActivity_findClassDemo2(JNIEnv *pEnv, jobject pObj) {
	pthread_t th;
	if (pthread_create(&th, NULL, pthread_dummy, NULL)) {
		__android_log_print(ANDROID_LOG_INFO, "findClassDemo2", "failed to create thread");
	}
	pthread_join(th, NULL);
}

static void nativeGetClassName(JNIEnv *pEnv, jclass pClass) {
	jmethodID midGetName = (*pEnv)->GetMethodID(pEnv, pClass, "toString", "()Ljava/lang/String;");
	if (midGetName != NULL) {
		jstring jclassName = (jstring) (*pEnv)->CallObjectMethod(pEnv, pClass, midGetName);
		if (jclassName != NULL)
		{
			const char* className = (*pEnv)->GetStringUTFChars(pEnv, jclassName, NULL);
			__android_log_print(ANDROID_LOG_INFO, "nativeGetName", "%s", className);
			(*pEnv)->ReleaseStringUTFChars(pEnv, jclassName, className);
			(*pEnv)->DeleteLocalRef(pEnv, jclassName);
		}
	}
}

JNIEXPORT void JNICALL Java_cookbook_chapter2_ClassManipulationActivity_GetSuperclassDemo(JNIEnv *pEnv, jobject pObj) {
	jclass objectClass = (*pEnv)->FindClass(pEnv, "java/lang/Object");
	jclass dummySubInterface = (*pEnv)->FindClass(pEnv, "cookbook/chapter2/DummySubInterface");
	jclass dummySubClass = (*pEnv)->FindClass(pEnv, "cookbook/chapter2/DummySubClass");
	jclass superOfObject = (*pEnv)->GetSuperclass(pEnv, objectClass);
	jclass superOfDummySubInterface = (*pEnv)->GetSuperclass(pEnv, dummySubInterface);
	jclass superOfDummySubClass = (*pEnv)->GetSuperclass(pEnv, dummySubClass);
	if (superOfObject == NULL) {
		__android_log_print(ANDROID_LOG_INFO, "GetSuperclassDemo", "superOfObject is NULL");
	} else {
		__android_log_print(ANDROID_LOG_INFO, "GetSuperclassDemo", "superOfObject is not NULL");
		nativeGetClassName(pEnv, superOfObject);
	}
	if (superOfDummySubClass == NULL) {
		__android_log_print(ANDROID_LOG_INFO, "GetSuperclassDemo", "superOfDummySubClass is NULL");
	} else {
		__android_log_print(ANDROID_LOG_INFO, "GetSuperclassDemo", "superOfDummySubClass is not NULL");
		nativeGetClassName(pEnv, superOfDummySubClass);
	}
	if (superOfDummySubInterface == NULL) {
		__android_log_print(ANDROID_LOG_INFO, "GetSuperclassDemo", "superOfDummySubInterface is NULL");
	} else {
		__android_log_print(ANDROID_LOG_INFO, "GetSuperclassDemo", "superOfDummySubInterface is not NULL");
		nativeGetClassName(pEnv, superOfDummySubInterface);
	}
}

JNIEXPORT void JNICALL Java_cookbook_chapter2_ClassManipulationActivity_IsAssignableFromDemo(JNIEnv *pEnv, jobject pObj) {
	jclass dummySubClass = (*pEnv)->FindClass(pEnv, "cookbook/chapter2/DummySubClass");
	jclass superOfDummySubClass = (*pEnv)->GetSuperclass(pEnv, dummySubClass);
	__android_log_print(ANDROID_LOG_INFO, "IsAssignableFromDemo", "From sub to super: %d",
			(*pEnv)->IsAssignableFrom(pEnv, dummySubClass, superOfDummySubClass));
	__android_log_print(ANDROID_LOG_INFO, "IsAssignableFromDemo", "From super to sub: %d",
			(*pEnv)->IsAssignableFrom(pEnv, superOfDummySubClass, dummySubClass));
}
