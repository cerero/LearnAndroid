#include <jni.h>
#include <android/log.h>

#include "arraytest.h"

JNIEXPORT void JNICALL Java_cookbook_chapter2_ArrayManipulationActivity_IllegalDirectAccessDemo(JNIEnv *pEnv, jobject pObj, jintArray pInts) {
	jsize len = (*pEnv)->GetArrayLength(pEnv, pInts);
	int i;
	int *ints = (int*)pInts;
	for (i = 0; i < len; ++i) {
		__android_log_print(ANDROID_LOG_INFO, "IllegalDirectAccessDemo", "%d", ints[i]);
	}
}

JNIEXPORT void JNICALL Java_cookbook_chapter2_ArrayManipulationActivity_GetArrayLengthDemo(JNIEnv *pEnv, jobject pObj,
		jdoubleArray pDoubles, jobjectArray pObjs, jobjectArray pArrays){
//	jsize GetArrayLength(JNIEnv *env, jarray array);
	jsize len = (*pEnv)->GetArrayLength(pEnv, pDoubles);
	__android_log_print(ANDROID_LOG_INFO, "GetArrayLengthDemo", "length: %d", len);
	len = (*pEnv)->GetArrayLength(pEnv, pObjs);
	__android_log_print(ANDROID_LOG_INFO, "GetArrayLengthDemo", "length: %d", len);
	len = (*pEnv)->GetArrayLength(pEnv, pArrays);
	__android_log_print(ANDROID_LOG_INFO, "GetArrayLengthDemo", "length: %d", len);
}

JNIEXPORT jobjectArray JNICALL Java_cookbook_chapter2_ArrayManipulationActivity_NewObjectArrayDemo(JNIEnv *pEnv, jobject pObj, jobject pDummyInit) {
	//jarray NewObjectArray(JNIEnv *env, jsize length, jclass elementType, jobject initialElement);
	jclass contactCls = (*pEnv)->FindClass(pEnv, "cookbook/chapter2/Dummy");
	jarray objArray = (*pEnv)->NewObjectArray(pEnv, 10, contactCls, pDummyInit);
	return objArray;
}

JNIEXPORT jintArray JNICALL Java_cookbook_chapter2_ArrayManipulationActivity_NewIntArrayDemo(JNIEnv *pEnv, jobject pObj) {
	//<ArrayType> New<Type>Array(JNIEnv *env, jsize length);
	jintArray jintArr = (*pEnv)->NewIntArray(pEnv, 10);
	jboolean isCopy;
	jint *intArr = (*pEnv)->GetIntArrayElements(pEnv, jintArr, &isCopy);
	int i;
	for (i = 0; i < 10; ++i) {
		intArr[i] = i;
	}
	(*pEnv)->ReleaseIntArrayElements(pEnv, jintArr, intArr, 0);
	return jintArr;
}

JNIEXPORT jobject JNICALL Java_cookbook_chapter2_ArrayManipulationActivity_GetSetObjectArrayDemo(JNIEnv *pEnv, jobject pObj, jobjectArray pArr, jobject pDummy) {
//jobject GetObjectArrayElement(JNIEnv *env,jobjectArray array, jsize index);
	//void SetObjectArrayElement(JNIEnv *env,jobjectArray array, jsize index,jobject value);
	jobject dummy1 = (*pEnv)->GetObjectArrayElement(pEnv, pArr, 1);
	(*pEnv)->SetObjectArrayElement(pEnv, pArr, 1, pDummy);
	return dummy1;
}

JNIEXPORT void JNICALL Java_cookbook_chapter2_ArrayManipulationActivity_GetReleaseIntArrayDemo(JNIEnv *pEnv, jobject pObj, jintArray pArr) {
//	<NativeType> *Get<Type>ArrayElements(JNIEnv *env, <ArrayType> array, jboolean *isCopy);
//  void Release<Type>ArrayElements(JNIEnv *env, <ArrayType> array, <NativeType> *elems, jint mode);
//	if isCopy is true, then Release operation has the following modes:
//	0: copy back and free the elems buffer
//	JNI_COMMIT: copy back but do not free the elems buffer
//	JNI_ABORT: free the buffer without copying back the possible changes in the elems buffer

	jboolean isCopy;
	jint *intArr = (*pEnv)->GetIntArrayElements(pEnv, pArr, &isCopy);
	__android_log_print(ANDROID_LOG_INFO, "GetReleaseIntArrayDemo", "a new copy is created: %d", isCopy);
	int i;
	jsize len = (*pEnv)->GetArrayLength(pEnv, pArr);
	for (i = 0; i < len; ++i) {
		__android_log_print(ANDROID_LOG_INFO, "GetReleaseIntArrayDemo", "%d: %d", i, intArr[i]);
		intArr[i] *= 2;
	}
	(*pEnv)->ReleaseIntArrayElements(pEnv, pArr, intArr, 0);
}

JNIEXPORT void JNICALL Java_cookbook_chapter2_ArrayManipulationActivity_GetSetIntArrayRegionDemo(JNIEnv *pEnv, jobject pObj, jintArray pArr) {
//	void Get<Type>ArrayRegion(JNIEnv *env,<ArrayType> array, jsize start, jsize len, <NativeType> *buf);
//	void Set<Type>ArrayRegion(JNIEnv *env, <ArrayType> array, jsize start, jsize len, <NativeType> *buf);
	int buf[100];
	(*pEnv)->GetIntArrayRegion(pEnv, pArr, 1, 3, buf);
	int i;
	for (i = 0; i < 3; ++i) {
		buf[i] *= 2;
	}
	(*pEnv)->SetIntArrayRegion(pEnv, pArr, 0, 3, buf);
}

JNIEXPORT void JNICALL Java_cookbook_chapter2_ArrayManipulationActivity_GetReleasePrimitiveArrayCriticalDemo(JNIEnv *pEnv, jobject pObj, jintArray pArr) {
//	void * GetPrimitiveArrayCritical(JNIEnv *env, jarray array, jboolean *isCopy);
//	void ReleasePrimitiveArrayCritical(JNIEnv *env,jarray array, void *carray, jint mode);
//	Inside a critical region, native code must not call other JNI functions, nor may the native code make any system call that may cause the current thread to block and wait for another thread in the virtual machine instance.
//These restrictions make it more likely that native code will obtain an uncopied version of the array, even if the virtual machine implementation does not support pinning.
//	GetPrimitiveArrayCritical might still make a copy of the array if the virtual machine implementation internally represents arrays in a different format (noncontiguously, for example). Therefore, it is important to check its return value against NULL for possible out-of-memory situations.
	jboolean isCopy;
	jint *intArr = (jint*)(*pEnv)->GetPrimitiveArrayCritical(pEnv, pArr, &isCopy);
	__android_log_print(ANDROID_LOG_INFO, "GetReleasePrimitiveArrayCriticalDemo", "a new copy is created: %d", isCopy);
	int i;
	jsize len = (*pEnv)->GetArrayLength(pEnv, pArr);
	for (i = 0; i < len; ++i) {
		__android_log_print(ANDROID_LOG_INFO, "GetReleasePrimitiveArrayCriticalDemo", "%d: %d", i, intArr[i]);
		intArr[i] *= 2;
	}
	(*pEnv)->ReleasePrimitiveArrayCritical(pEnv, pArr, intArr, 0);
}
//GetPrimitiveArrayCritical and ReleasePrimitiveArrayCritical ac-cess elements in an array of primitive types with minimum overhead. Both functions are new in Java 2 SDK release 1.2.
//jint len = (*env)->GetArrayLength(env, arr1);
//                jbyte *a1 = (*env)->
//                    GetPrimitiveArrayCritical(env, arr1 0);
//                if (a1 == NULL) {
//                    ... /* out of memory error */
//                }
//                jbyte *a2 = (*env)->
//                    GetPrimitiveArrayCritical(env, arr2, 0);
//                if (a2 == NULL) {
//                    ... /* out of memory error */
//                }
//                memcpy(a1, a2, len);
//                (*env)->ReleasePrimitiveArrayCritical(
//                     env, arr2, a2, 0);
//                (*env)->ReleasePrimitiveArrayCritical(
//                     env, arr1, a1, 0);

