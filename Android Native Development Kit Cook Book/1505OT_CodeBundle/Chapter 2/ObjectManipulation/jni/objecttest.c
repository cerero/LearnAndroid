#include <jni.h>
#include <android/log.h>
#include <stdio.h>

#include "objecttest.h"

JNIEXPORT jobject JNICALL Java_cookbook_chapter2_ObjectManipulationActivity_AllocObjectDemo(JNIEnv *pEnv, jobject pObj) {
	jclass contactCls = (*pEnv)->FindClass(pEnv, "cookbook/chapter2/Contact");
	jobject allocObj = (*pEnv)->AllocObject(pEnv, contactCls);
	return allocObj;
}

JNIEXPORT jobject JNICALL Java_cookbook_chapter2_ObjectManipulationActivity_NewObjectDemo(JNIEnv *pEnv, jobject pObj, jstring pName, jint pAge) {
	jclass contactCls = (*pEnv)->FindClass(pEnv, "cookbook/chapter2/Contact");
	jmethodID method = (*pEnv)->GetMethodID(pEnv, contactCls, "<init>","(Ljava/lang/String;I)V");
	jobject newObj = (*pEnv)->NewObject(pEnv, contactCls, method, pName, pAge);
	return newObj;
}

JNIEXPORT jobject JNICALL Java_cookbook_chapter2_ObjectManipulationActivity_NewObjectADemo(JNIEnv *pEnv, jobject pObj, jstring pName, jint pAge) {
	jclass contactCls = (*pEnv)->FindClass(pEnv, "cookbook/chapter2/Contact");
	jmethodID method = (*pEnv)->GetMethodID(pEnv, contactCls, "<init>","(Ljava/lang/String;I)V");
	jvalue args[2];
	args[0].l = pName;
	args[1].i = pAge;
	jobject newObjA = (*pEnv)->NewObjectA(pEnv, contactCls, method, args);
	return newObjA;
}

static jobject newObjectVUtil(JNIEnv *pEnv, ...) {
	jclass contactCls = (*pEnv)->FindClass(pEnv, "cookbook/chapter2/Contact");
	jmethodID method = (*pEnv)->GetMethodID(pEnv, contactCls, "<init>","(Ljava/lang/String;I)V");
	va_list arList;
	va_start(arList, pEnv);
	jobject newObjV = (*pEnv)->NewObjectV(pEnv, contactCls, method, arList);
	va_end(arList);
	return newObjV;
}

JNIEXPORT jobject JNICALL Java_cookbook_chapter2_ObjectManipulationActivity_NewObjectVDemo(JNIEnv *pEnv, jobject pObj, jstring pName, jint pAge) {
	return newObjectVUtil(pEnv, pName, pAge);
}

JNIEXPORT jobject JNICALL Java_cookbook_chapter2_ObjectManipulationActivity_GetObjectClassDemo(JNIEnv *pEnv, jobject pObj, jobject pContact) {
	//jclass cls1 = (*pEnv)->GetObjectClass(pEnv, NULL);
	jclass cls = (*pEnv)->GetObjectClass(pEnv, pContact);
	jmethodID method = (*pEnv)->GetMethodID(pEnv, cls, "<init>","(Ljava/lang/String;I)V");
	jobject allocObj = (*pEnv)->AllocObject(pEnv, cls);
	return allocObj;
}

JNIEXPORT void JNICALL Java_cookbook_chapter2_ObjectManipulationActivity_IsInstanceOfDemo(JNIEnv *pEnv, jobject pObj) {
	jclass dummyCls = (*pEnv)->FindClass(pEnv, "cookbook/chapter2/Dummy");
	jclass dummySubCls = (*pEnv)->FindClass(pEnv, "cookbook/chapter2/DummySub");
	jmethodID dummyConstructor = (*pEnv)->GetMethodID(pEnv, dummyCls, "<init>","()V");
	jmethodID dummySubConstructor = (*pEnv)->GetMethodID(pEnv, dummySubCls, "<init>","()V");
	jobject dummyObj = (*pEnv)->NewObject(pEnv, dummyCls, dummyConstructor);
	jobject dummySubObj = (*pEnv)->NewObject(pEnv, dummySubCls, dummySubConstructor);
	__android_log_print(ANDROID_LOG_INFO, "IsInstanceOfDemo", "dummyObj, dummyCls: %d", (*pEnv)->IsInstanceOf(pEnv, dummyObj, dummyCls));
	__android_log_print(ANDROID_LOG_INFO, "IsInstanceOfDemo", "dummyObj, dummySubCls: %d", (*pEnv)->IsInstanceOf(pEnv, dummyObj, dummySubCls));
	__android_log_print(ANDROID_LOG_INFO, "IsInstanceOfDemo", "dummySubObj, dummyCls: %d", (*pEnv)->IsInstanceOf(pEnv, dummySubObj, dummyCls));
	__android_log_print(ANDROID_LOG_INFO, "IsInstanceOfDemo", "dummySubObj, dummySubCls: %d", (*pEnv)->IsInstanceOf(pEnv, dummySubObj, dummySubCls));
}

