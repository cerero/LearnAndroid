#include <jni.h>

jstring getString(JNIEnv* env) {
	return env->NewStringUTF("Hello NDK");
}

extern "C" {
	JNIEXPORT jstring JNICALL Java_cookbook_chapter3_HelloNDKMultipleABIActivity_getString(JNIEnv* env, jobject o){
		return getString(env);
	}
}
