#include <jni.h>
#include <unistd.h>
#include <pthread.h>

#include "mylog.h"

void jni_start_threads();
void *run_by_thread1(void *arg);
void *run_by_thread2(void *arg);

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
	jclass cls = env->FindClass("cookbook/chapter6/nativethreadscondvar/MainActivity");
	// Register methods with env->RegisterNatives.
	env->RegisterNatives(cls, nm, 1);
	return JNI_VERSION_1_6;
}

pthread_mutex_t mux;
pthread_cond_t cond;
void jni_start_threads() {
	pthread_t th1, th2;
	int threadNum1 = 1, threadNum2 = 2;
	int ret;
	pthread_mutex_init(&mux, NULL);
	pthread_cond_init(&cond, NULL);
	ret = pthread_create(&th1, NULL, run_by_thread1, (void*)&threadNum1);
	if(ret) {
		LOGE(1, "cannot create the thread %d thread: %d", threadNum1, ret);
	}
	LOGI(1, "thread 1 started");
	ret = pthread_create(&th2, NULL, run_by_thread2, (void*)&threadNum2);
	if(ret) {
		LOGE(1, "cannot create the thread %d: %d", threadNum2, ret);
	}
	LOGI(1, "thread 2 started");
	ret = pthread_join(th1, NULL);
	LOGI(1, "thread 1 end %d", ret);
	ret = pthread_join(th2, NULL);
	LOGI(1, "thread 2 end %d", ret);
	pthread_mutex_destroy(&mux);
	pthread_cond_destroy(&cond);
}

int cnt = 0;
int THR = 10, THR2 = 5;
void *run_by_thread1(void *arg) {
	int* threadNum = (int*)arg;
	pthread_mutex_lock(&mux);
	while (cnt != THR2) {
		LOGI(1, "thread %d: about to wait", *threadNum);
		pthread_cond_wait(&cond, &mux);
	}
	++cnt;
	LOGI(1, "thread %d: cnt = %d", *threadNum, cnt);
	pthread_mutex_unlock(&mux);
}

void *run_by_thread2(void *arg) {
	int* threadNum = (int*)arg;
	while (cnt < THR) {
		pthread_mutex_lock(&mux);
		if (cnt == THR2) {
			pthread_cond_signal(&cond);
		} else {
			++cnt;
			LOGI(1, "thread %d: cnt = %d", *threadNum, cnt);
		}
		pthread_mutex_unlock(&mux);
		sleep(1);
	}
}
