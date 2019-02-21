#include <jni.h>
#include <android/log.h>
#include <stdio.h>

#include "exceptiontest.h"

JNIEXPORT void JNICALL Java_cookbook_chapter2_ExceptionHandlingActivity_ExceptionDemo(JNIEnv *pEnv, jobject pObj) {
//	jboolean ExceptionCheck(JNIEnv *env);
//	jthrowable ExceptionOccurred(JNIEnv *env);
	//if no exception pending
	jboolean ifExceptionPending =(*pEnv)->ExceptionCheck(pEnv);
	__android_log_print(ANDROID_LOG_INFO, "ExceptionCheckDemo", "ExceptionCheck: %d", ifExceptionPending);
	jthrowable exception = (*pEnv)->ExceptionOccurred(pEnv);
	__android_log_print(ANDROID_LOG_INFO, "ExceptionCheckDemo",
			"ExceptionOccurred returned NULL? : %d", (*pEnv)->IsSameObject(pEnv, exception, NULL));

	//search for a class which are not available, which will cause an exception
	(*pEnv)->FindClass(pEnv, "java/lang/XXYY");
	//use ExceptionCheck to check
	ifExceptionPending =(*pEnv)->ExceptionCheck(pEnv);
	__android_log_print(ANDROID_LOG_INFO, "ExceptionCheckDemo", "ExceptionCheck after finding non-existing class: %d", ifExceptionPending);
	(*pEnv)->ExceptionClear(pEnv);	//clear the exception, so we can proceed
	ifExceptionPending =(*pEnv)->ExceptionCheck(pEnv);
	__android_log_print(ANDROID_LOG_INFO, "ExceptionCheckDemo", "ExceptionCheck after clear: %d", ifExceptionPending);

	//throw a java.lang.NullPointerException using ThrowNew
	jclass cls = (*pEnv)->FindClass(pEnv, "java/lang/NullPointerException");
	jint st = (*pEnv)->ThrowNew(pEnv, cls, "throw null pointer exception");
	if (st == 0) {
		__android_log_print(ANDROID_LOG_INFO, "ExceptionCheckDemo", "null pointer exception thrown using ThrowNew");
		(*pEnv)->DeleteLocalRef(pEnv, cls);
	}
	//use ExceptionOccurred to check
	jthrowable exObj = (*pEnv)->ExceptionOccurred(pEnv);
	if (exObj == NULL) {
		__android_log_print(ANDROID_LOG_INFO, "ExceptionCheckDemo", "no exception");
	} else {
		__android_log_print(ANDROID_LOG_INFO, "ExceptionCheckDemo", "there's pending exception, call ExceptionDescribe");
		(*pEnv)->ExceptionDescribe(pEnv);	//this does not clear the exception
		ifExceptionPending =(*pEnv)->ExceptionCheck(pEnv);
		__android_log_print(ANDROID_LOG_INFO, "ExceptionCheckDemo", "ExceptionCheck after ExceptionDescribe: %d", ifExceptionPending);
		(*pEnv)->ExceptionClear(pEnv);		//clear the exception, so we can proceed
		ifExceptionPending =(*pEnv)->ExceptionCheck(pEnv);
		__android_log_print(ANDROID_LOG_INFO, "ExceptionCheckDemo", "ExceptionCheck after clear: %d", ifExceptionPending);
		(*pEnv)->DeleteLocalRef(pEnv, exObj);
	}

	//throw a java.lang.RuntimeException using Throw
	cls = (*pEnv)->FindClass(pEnv, "java/lang/RuntimeException");
	jmethodID exConstructor = (*pEnv)->GetMethodID(pEnv, cls, "<init>","(Ljava/lang/String;)V");
	//passing UTF-8 string directly to exConstructor won't work because it expects jstring
	//jthrowable rtExObj = (*pEnv)->NewObject(pEnv, reCls, exConstructor, "throw runtime exception");
	jstring msg = (*pEnv)->NewStringUTF(pEnv, "throw runtime exception");
	exObj = (*pEnv)->NewObject(pEnv, cls, exConstructor, msg);
	(*pEnv)->DeleteLocalRef(pEnv, msg);
	if (exObj == NULL) {
		__android_log_print(ANDROID_LOG_INFO, "ExceptionCheckDemo", "create RuntimeException failed");
	} else {
		jint st = (*pEnv)->Throw(pEnv, exObj);
		if (st == 0) {
			__android_log_print(ANDROID_LOG_INFO, "ExceptionCheckDemo", "exception thrown using Throw");
			(*pEnv)->DeleteLocalRef(pEnv, cls);
			(*pEnv)->DeleteLocalRef(pEnv, exObj);
		}
		//do not clear the exception, let the caller handle it
	}
}

JNIEXPORT void JNICALL Java_cookbook_chapter2_ExceptionHandlingActivity_FatalErrorDemo(JNIEnv *pEnv, jobject pObj) {
	(*pEnv)->FatalError(pEnv, "fatal error");
	__android_log_print(ANDROID_LOG_INFO, "FatalErrorDemo", "after calling FatalError");
}

//jboolean ExceptionCheck(JNIEnv *env);
//Determines if an exception has been thrown. The exception stays thrown until either the
//native code calls ExceptionClear, or the caller of the native method handles the exception.
//Returns the JNI_TRUE if there is a pending exception, or
//JNI_FALSE if there is no pending exception.

//void ExceptionClear(JNIEnv *env);
//Clears any pending exception that is currently being thrown in the current thread.
//If no exception is currently being thrown, this function has no effect.
//This function has no effect on exceptions pending on other threads.

//void ExceptionDescribe(JNIEnv *env);
//Prints the pending exception and a backtrace of the stack to the system error-reporting
//channel System.out.err. This is a convenience routine provided for debugging.

//jthrowable ExceptionOccurred(JNIEnv *env);
//Determines if an exception is pending in the current thread. The exception stays pending until either
//the native code calls ExceptionClear, or the caller of the native method handles the exception.

//void FatalError(JNIEnv *env, const char *msg);
//Raises a fatal error and does not expect the virtual machine implementation to recover.
//Prints the message in a system debugging channel, such as stderr, and terminates
//the virtual machine instance. This function does not return.

//jint Throw(JNIEnv *env, jthrowable obj);
//Causes a java.lang.Throwable object to be thrown. A thrown exception will be pending in the current thread,
//but does not immediately disrupt native code execution.
//Returns zero on success; otherwise, returns a negative value
//if the specified exception cannot be thrown.

//jint ThrowNew(JNIEnv *env, jclass clazz, const char *message);
//Constructs an exception object from the specified class with the message specified by message
//and causes that exception to be thrown.


