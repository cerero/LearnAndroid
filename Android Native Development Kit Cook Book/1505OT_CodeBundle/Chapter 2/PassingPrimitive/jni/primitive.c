#include <jni.h>
#include <android/log.h>

#include "primitive.h"

JNIEXPORT jboolean JNICALL Java_cookbook_chapter2_PassingPrimitiveActivity_passBooleanReturnBoolean(JNIEnv *pEnv, jobject pObj, jboolean pBooleanP){
	__android_log_print(ANDROID_LOG_INFO, "native", "%d in %d bytes", pBooleanP, sizeof(jboolean));
	return (!pBooleanP);
}
JNIEXPORT jbyte JNICALL Java_cookbook_chapter2_PassingPrimitiveActivity_passByteReturnByte(JNIEnv *pEnv, jobject pObj, jbyte pByteP){
	__android_log_print(ANDROID_LOG_INFO, "native", "%d in %d bytes", pByteP, sizeof(jbyte));
	return pByteP + 1;
}
JNIEXPORT jchar JNICALL Java_cookbook_chapter2_PassingPrimitiveActivity_passCharReturnChar(JNIEnv *pEnv, jobject pObj, jchar pCharP) {
	__android_log_print(ANDROID_LOG_INFO, "native", "%c in %d bytes", pCharP, sizeof(jchar));
	pCharP = '*';
	return pCharP;
}
JNIEXPORT jshort JNICALL Java_cookbook_chapter2_PassingPrimitiveActivity_passShortReturnShort(JNIEnv *pEnv, jobject pObj, jshort pShortP){
	__android_log_print(ANDROID_LOG_INFO, "native", "%d in %d bytes", pShortP, sizeof(jshort));
	return pShortP - 1;
}
JNIEXPORT jint JNICALL Java_cookbook_chapter2_PassingPrimitiveActivity_passIntReturnInt(JNIEnv *pEnv, jobject pObj, jint pIntP) {
	__android_log_print(ANDROID_LOG_INFO, "native", "%d in %d bytes", pIntP, sizeof(jint));
	return pIntP + 1;
}
JNIEXPORT jlong JNICALL Java_cookbook_chapter2_PassingPrimitiveActivity_passLongReturnLong(JNIEnv *pEnv, jobject pObj, jlong pLongP) {
	__android_log_print(ANDROID_LOG_INFO, "native", "%llu in %d bytes", pLongP, sizeof(jlong));
	return pLongP + 1;
}
JNIEXPORT jfloat JNICALL Java_cookbook_chapter2_PassingPrimitiveActivity_passFloatReturnFloat(JNIEnv *pEnv, jobject pObj, jfloat pFloatP) {
	__android_log_print(ANDROID_LOG_INFO, "native", "%f in %d bytes", pFloatP, sizeof(jfloat));
	return pFloatP + 0.5;
}
JNIEXPORT jdouble JNICALL Java_cookbook_chapter2_PassingPrimitiveActivity_passDoubleReturnDouble(JNIEnv *pEnv, jobject pObj, jdouble pDoubleP) {
	__android_log_print(ANDROID_LOG_INFO, "native", "%f in %d bytes", pDoubleP, sizeof(jdouble));
	return pDoubleP + 0.5;
}
