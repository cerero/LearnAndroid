#include <jni.h>
#include <android/log.h>
#include <stdio.h>
#include <string.h>

#include "stringtest.h"

JNIEXPORT jstring JNICALL Java_cookbook_chapter2_StringManipulationActivity_passStringReturnString(JNIEnv *pEnv, jobject pObj, jstring pStringP){
	//wrong: need to convert jstring to C-string
	__android_log_print(ANDROID_LOG_INFO, "native", "print jstring: %s", pStringP);
	//convert jstring to UTF-8 string
	const jbyte *str;
	jboolean *isCopy;
	str = (*pEnv)->GetStringUTFChars(pEnv, pStringP, isCopy);
	if (str == NULL) {
		__android_log_print(ANDROID_LOG_INFO, "native", "cannot convert jstring to utf-8 string");
	}
	__android_log_print(ANDROID_LOG_INFO, "native", "print UTF-8 string: %s, %d", str, isCopy);
	//get the jstring length in number of bytes
	jsize length = (*pEnv)->GetStringUTFLength(pEnv, pStringP);
	__android_log_print(ANDROID_LOG_INFO, "native", "UTF-8 string length (number of bytes): %d == %d", length, strlen(str));
	//check if they're null-terminated
	__android_log_print(ANDROID_LOG_INFO, "native", "UTF-8 string ends with: %d %d", str[length], str[length+1]);
	//informs Java VM that native code doesn't need to access the UTF-8 string any more
	(*pEnv)->ReleaseStringUTFChars(pEnv, pStringP, str);
	//create a jstring from UTF string
	char nativeStr[100];
	//GetStringUTFRegion(JNIEnv *env, jstring str, jsize start, jsize len, char *buf);
	(*pEnv)->GetStringUTFRegion(pEnv, pStringP, 0, length, nativeStr);
	__android_log_print(ANDROID_LOG_INFO, "native", "jstring converted to UTF-8 string and copied to native buffer: %s", nativeStr);
	//create a jstring from native UTF-8 string
	const char* newStr = "hello 安卓";
	jstring ret = (*pEnv)->NewStringUTF(pEnv, newStr);
	jsize newStrLen = (*pEnv)->GetStringUTFLength(pEnv, ret);
	__android_log_print(ANDROID_LOG_INFO, "native", "UTF-8 string with Chinese characters: %s, string length (number of bytes) %d=%d", newStr, newStrLen, strlen(newStr));
	return ret;
}


