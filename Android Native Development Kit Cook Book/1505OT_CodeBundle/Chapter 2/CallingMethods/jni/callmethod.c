#include <jni.h>
#include <android/log.h>
#include <stdio.h>

#include "callmethod.h"

static void callStaticVoidMethodVUtil(JNIEnv *pEnv, jclass dummyCls, jmethodID mid, ...) {
	va_list arList;
	va_start(arList, mid);
	(*pEnv)->CallStaticVoidMethodV(pEnv, dummyCls, mid, arList);
	va_end(arList);
}

JNIEXPORT void JNICALL Java_cookbook_chapter2_CallingMethodsActivity_AccessStaticMethodDemo(JNIEnv *pEnv, jobject pObj, jobject pDummy) {
	//	jmethodID GetStaticMethodID(JNIEnv *env, jclass clazz, const char *name, const char *sig);
	//	<NativeType> CallStatic<Type>Method(JNIEnv *env, jclass clazz, jmethodID methodID, ...);
	//	<NativeType> CallStatic<Type>MethodA(JNIEnv *env, jclass clazz, jmethodID methodID, jvalue *args);
	//	<NativeType> CallStatic<Type>MethodV(JNIEnv *env, jclass clazz,jmethodID methodID, va_list args);
	jmethodID sGetValue2MID, sSetValue2MID;
	jclass dummyCls = (*pEnv)->GetObjectClass(pEnv, pDummy);

	sGetValue2MID = (*pEnv)->GetStaticMethodID(pEnv, dummyCls, "getValue2", "()I");
	sSetValue2MID = (*pEnv)->GetStaticMethodID(pEnv, dummyCls, "setValue2", "(I)V");

	//call the get method
	jint value2 = (*pEnv)->CallStaticIntMethod(pEnv, dummyCls, sGetValue2MID);
	__android_log_print(ANDROID_LOG_INFO, "AccessStaticMethodDemo", "value2: %d", value2);
	//call the set method
	(*pEnv)->CallStaticVoidMethod(pEnv, dummyCls, sSetValue2MID, 123);
	//call get method again
	value2 = (*pEnv)->CallStaticIntMethod(pEnv, dummyCls, sGetValue2MID);
	__android_log_print(ANDROID_LOG_INFO, "AccessStaticMethodDemo", "value2 after set to 123: %d", value2);

	//call the set method A
	jvalue args[1];
	args[0].i = 124;
	(*pEnv)->CallStaticVoidMethodA(pEnv, dummyCls, sSetValue2MID, args);
	//call get method again
	value2 = (*pEnv)->CallStaticIntMethod(pEnv, dummyCls, sGetValue2MID);
	__android_log_print(ANDROID_LOG_INFO, "AccessStaticMethodDemo", "value2 after set to 124: %d", value2);

	//call the set method V
	callStaticVoidMethodVUtil(pEnv, dummyCls, sSetValue2MID, 125);
	//call get method again
	value2 = (*pEnv)->CallStaticIntMethod(pEnv, dummyCls, sGetValue2MID);
	__android_log_print(ANDROID_LOG_INFO, "AccessStaticMethodDemo", "value2 after set to 125: %d", value2);
}

static void callVoidMethodVUtil(JNIEnv *pEnv, jobject pDummySub, jmethodID mid, ...) {
	va_list arList;
	va_start(arList, mid);
	(*pEnv)->CallVoidMethodV(pEnv, pDummySub, mid, arList);
}

JNIEXPORT void JNICALL Java_cookbook_chapter2_CallingMethodsActivity_AccessInstanceMethodDemo(JNIEnv *pEnv, jobject pObj, jobject pDummySub) {
	//	jmethodID GetMethodID(JNIEnv *env, jclass clazz, const char *name, const char *sig);
	//	<NativeType> Call<Type>Method(JNIEnv *env, jobject obj, jmethodID methodID, ...);
	//	<NativeType> Call<Type>MethodA(JNIEnv *env,jobject obj, jmethodID methodID, jvalue *args);
	//	<NativeType> Call<Type>MethodV(JNIEnv *env, jobject obj, jmethodID methodID, va_list args);
	//	<NativeType> CallNonvirtual<Type>Method(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, ...);
	//	<NativeType> CallNonvirtual<Type>MethodA(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, jvalue *args);
	//	<NativeType> CallNonvirtual<Type>MethodV(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, va_list args);
	jmethodID sGetNameMID, sSetNameMID;
	jclass dummySubCls = (*pEnv)->GetObjectClass(pEnv, pDummySub);

	sGetNameMID = (*pEnv)->GetMethodID(pEnv, dummySubCls, "getName", "()Ljava/lang/String;");
	sSetNameMID = (*pEnv)->GetMethodID(pEnv, dummySubCls, "setName", "(Ljava/lang/String;)V");

	//call the get method
	jstring name = (*pEnv)->CallObjectMethod(pEnv, pDummySub, sGetNameMID);
	const jbyte* nameStr = (*pEnv)->GetStringUTFChars(pEnv, name, NULL);
	__android_log_print(ANDROID_LOG_INFO, "AccessInstanceMethodDemo", "name: %s", nameStr);
	(*pEnv)->ReleaseStringUTFChars(pEnv, name, nameStr);

	//call the set method
	jstring newNameStr = (*pEnv)->NewStringUTF(pEnv, "B");
	(*pEnv)->CallVoidMethod(pEnv, pDummySub, sSetNameMID, newNameStr);
	//call get method again
	name = (*pEnv)->CallObjectMethod(pEnv, pDummySub, sGetNameMID);
	nameStr = (*pEnv)->GetStringUTFChars(pEnv, name, NULL);
	__android_log_print(ANDROID_LOG_INFO, "AccessInstanceMethodDemo", "name after set to B: %s", nameStr);
	(*pEnv)->ReleaseStringUTFChars(pEnv, name, nameStr);

	//call the set method A
	jvalue args[1];
	args[0].l = (*pEnv)->NewStringUTF(pEnv, "C");
	(*pEnv)->CallVoidMethodA(pEnv, pDummySub, sSetNameMID, args);
	//call get method again
	name = (*pEnv)->CallObjectMethod(pEnv, pDummySub, sGetNameMID);
	nameStr = (*pEnv)->GetStringUTFChars(pEnv, name, NULL);
	__android_log_print(ANDROID_LOG_INFO, "AccessInstanceMethodDemo", "name after set to C: %s", nameStr);
	(*pEnv)->ReleaseStringUTFChars(pEnv, name, nameStr);

	//call the set method V
	newNameStr = (*pEnv)->NewStringUTF(pEnv, "D");
	callVoidMethodVUtil(pEnv, pDummySub, sSetNameMID, newNameStr);
	//call get method again
	name = (*pEnv)->CallObjectMethod(pEnv, pDummySub, sGetNameMID);
	nameStr = (*pEnv)->GetStringUTFChars(pEnv, name, NULL);
	__android_log_print(ANDROID_LOG_INFO, "AccessInstanceMethodDemo", "name after set to C: %s", nameStr);
	(*pEnv)->ReleaseStringUTFChars(pEnv, name, nameStr);

	jmethodID sGetValueMID, sSetValueMID;
	jclass dummyCls = (*pEnv)->GetSuperclass(pEnv, dummySubCls);
	sGetValueMID = (*pEnv)->GetMethodID(pEnv, dummyCls, "getValue", "()I");
	sSetValueMID = (*pEnv)->GetMethodID(pEnv, dummyCls, "setValue", "(I)V");
	//call nonvirtual set method
//	jint value = (*pEnv)->CallNonvirtualIntMethod(pEnv, pDummySub, dummySubCls, sGetValueMID);
	jint value = (*pEnv)->CallNonvirtualIntMethod(pEnv, pDummySub, dummyCls, sGetValueMID);
	__android_log_print(ANDROID_LOG_INFO, "AccessInstanceMethodDemo", "value: %d", value);
	//call nonvirtual get method
//	(*pEnv)->CallNonvirtualVoidMethod(pEnv, pDummySub, dummySubCls, sSetValueMID, 123);
	(*pEnv)->CallNonvirtualVoidMethod(pEnv, pDummySub, dummyCls, sSetValueMID, 123);
//	value = (*pEnv)->CallNonvirtualIntMethod(pEnv, pDummySub, dummySubCls, sGetValueMID);
	value = (*pEnv)->CallNonvirtualIntMethod(pEnv, pDummySub, dummyCls, sGetValueMID);
	__android_log_print(ANDROID_LOG_INFO, "AccessInstanceMethodDemo", "value: %d", value);
}

JNIEXPORT jobject JNICALL Java_cookbook_chapter2_CallingMethodsActivity_MethodReflectionDemo(JNIEnv *pEnv, jobject pObj, jobject pMethod, jobject pDummy) {
	//jobject ToReflectedMethod(JNIEnv *env, jclass cls, jmethodID methodID, jboolean isStatic);
	//jmethodID FromReflectedMethod(JNIEnv *env, jobject method);
	jclass dummyCls = (*pEnv)->GetObjectClass(pEnv, pDummy);
	jmethodID mid = (*pEnv)->FromReflectedMethod(pEnv, pMethod);
	jint value = (*pEnv)->CallIntMethod(pEnv, pDummy, mid);
	__android_log_print(ANDROID_LOG_INFO, "MethodReflectionDemo", "value: %d", value);

	jmethodID mid2 = (*pEnv)->GetMethodID(pEnv, dummyCls, "setValue", "(I)V");
	return (*pEnv)->ToReflectedMethod(pEnv, dummyCls, mid2, JNI_TRUE);
}

//GetMethodID performs a symbolic lookup on a given class or interface and returns the method ID of
//an instance method or a constructor.
//Functions of the Call<Type>Method family invoke instance methods.
//Functions of the CallNonvirtual<Type>Method family invoke either instance methods of a
//superclass or constructors.

//GetStaticMethodID performs a symbolic lookup on a given class and returns
//the method ID of a static method.
//Functions of the CallStatic<Type>Method family invoke static methods.


//jobject ToReflectedMethod(JNIEnv *env, jclass cls, jmethodID methodID, jboolean isStatic);
//jmethodID FromReflectedMethod(JNIEnv *env, jobject method);


