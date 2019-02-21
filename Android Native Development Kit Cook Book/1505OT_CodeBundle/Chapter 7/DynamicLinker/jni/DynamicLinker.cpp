#include <jni.h>
#include <android/log.h>

#include <stdio.h>
#include <dlfcn.h>

#include "mylog.h"

void naDLDemo(JNIEnv* pEnv, jclass clazz);

jint JNI_OnLoad(JavaVM* pVm, void* reserved)
{
	JNIEnv* env;
	if (pVm->GetEnv((void **)&env, JNI_VERSION_1_6) != JNI_OK) {
		 return -1;
	}
	JNINativeMethod nm[1];
	nm[0].name = "naDLDemo";
	nm[0].signature = "()V";
	nm[0].fnPtr = (void*)naDLDemo;
	jclass cls = env->FindClass("cookbook/chapter7/dynamiclinker/MainActivity");
	// Register methods with env->RegisterNatives.
	env->RegisterNatives(cls, nm, 1);
	return JNI_VERSION_1_6;
}

void naDLDemo(JNIEnv* pEnv, jclass clazz) {
	void *handle;
	double (*sqrt)(double);
	const char *error;
	handle = dlopen("libm.so", RTLD_LAZY);
	if (!handle) {
		LOGI(1, "%s\n", dlerror());
		return;
	}
	dlerror();    /* Clear any existing error */
	*(void **) (&sqrt) = dlsym(handle, "sqrt");
	if ((error = dlerror()) != NULL)  {
		LOGI(1, "%s\n", error);
		return;
	}
	LOGI(1, "%f\n", (*sqrt)(2.0));
	dlclose(handle);
}


