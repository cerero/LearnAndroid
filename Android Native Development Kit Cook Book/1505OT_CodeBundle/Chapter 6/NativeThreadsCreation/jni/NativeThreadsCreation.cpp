#include <jni.h>
#include <unistd.h>
#include <pthread.h>

#include "mylog.h"

void jni_start_threads();
void *run_by_thread(void *arg);

jint JNI_OnLoad(JavaVM* pVm, void* reserved)
{
	JNIEnv* env;
	if (pVm->GetEnv((void **)&env, JNI_VERSION_1_6) != JNI_OK) {
		 return -1;
	}
	JNINativeMethod nm[1];
	nm[0].name = "jni_start_threads";
	nm[0].signature = "()V";
	nm[0].fnPtr = (void*)jni_start_threads;
	jclass cls = env->FindClass("cookbook/chapter6/nativethreadscreation/MainActivity");
	// Register methods with env->RegisterNatives.
	env->RegisterNatives(cls, nm, 1);
	return JNI_VERSION_1_6;
}

//int pthread_create(pthread_t *thread, const pthread_attr_t *attr,
//    void *(*start_routine)(void*), void *arg);
void jni_start_threads() {
	pthread_t th1, th2;
	int threadNum1 = 1, threadNum2 = 2;
	int ret;
	ret = pthread_create(&th1, NULL, run_by_thread, (void*)&threadNum1);
	if(ret) {
		LOGE(1, "cannot create the thread %d thread: %d", threadNum1, ret);
	}
	LOGI(1, "thread 1 started");
	ret = pthread_create(&th2, NULL, run_by_thread, (void*)&threadNum2);
	if(ret) {
		LOGE(1, "cannot create the thread %d: %d", threadNum2, ret);
	}
	void *status;
	LOGI(1, "thread 2 started");
	ret = pthread_join(th1, &status);
	int* st = (int*)status;
	LOGI(1, "thread 1 end %d %d", ret, *st);
	ret = pthread_join(th2, &status);
	st = (int*)status;
	LOGI(1, "thread 2 end %d %d", ret, *st);
}

int retStatus;
void *run_by_thread(void *arg) {
	int cnt = 3, i;
	int* threadNum = (int*)arg;
	for (i = 0; i < cnt; ++i) {
		sleep(1);
		LOGI(1, "thread %d: %d", *threadNum, i);
	}
	if (1 == *threadNum) {
		retStatus = 100;
		return (void*)&retStatus;
	} else if (2 == *threadNum) {
		retStatus = 200;
		pthread_exit((void*)&retStatus);
	}
}
