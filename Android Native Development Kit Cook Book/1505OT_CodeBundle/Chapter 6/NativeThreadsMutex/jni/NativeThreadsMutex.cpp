#include <jni.h>
#include <unistd.h>
#include <pthread.h>

#include "mylog.h"

void jni_start_threads();
void *run_by_thread1(void *arg);
void *run_by_thread2(void *arg);

void jni_start_threads_dead();
void *run_by_thread1dead(void *arg);
void *run_by_thread2dead(void *arg);

jint JNI_OnLoad(JavaVM* pVm, void* reserved)
{
	JNIEnv* env;
	if (pVm->GetEnv((void **)&env, JNI_VERSION_1_6) != JNI_OK) {
		 return -1;
	}
	JNINativeMethod nm[2];
	nm[0].name = "jni_start_threads";
	nm[0].signature = "()V";
	nm[0].fnPtr = (void*)jni_start_threads;
	nm[1].name = "jni_start_threads_dead";
	nm[1].signature = "()V";
	nm[1].fnPtr = (void*)jni_start_threads_dead;
	jclass cls = env->FindClass("cookbook/chapter6/nativethreadsmutex/MainActivity");
	// Register methods with env->RegisterNatives.
	env->RegisterNatives(cls, nm, 2);
	return JNI_VERSION_1_6;
}

//pthread_mutex_t mux1 = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t mux1;
pthread_mutex_t mux2;
void jni_start_threads() {
	pthread_t th1, th2;
	int threadNum1 = 1, threadNum2 = 2;
	int ret;
	pthread_mutex_init(&mux1, NULL);
	pthread_mutex_init(&mux2, NULL);
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
	pthread_mutex_destroy(&mux1);
	pthread_mutex_destroy(&mux2);
}

int cnt = 0;
int THR = 10;
void *run_by_thread1(void *arg) {
	int* threadNum = (int*)arg;
	while (cnt < THR) {
		pthread_mutex_lock(&mux1);
		while ( pthread_mutex_trylock(&mux2) ) {
		   pthread_mutex_unlock(&mux1);  //avoid deadlock
		   usleep(50000);				 //if failed to get mux2, release mux1 first
		   pthread_mutex_lock(&mux1);
		}
		++cnt;
		LOGI(1, "thread %d: cnt = %d", *threadNum, cnt);
		pthread_mutex_unlock(&mux1);
		pthread_mutex_unlock(&mux2);
		sleep(1);
	}
}

void *run_by_thread2(void *arg) {
	int* threadNum = (int*)arg;
	while (cnt < THR) {
		pthread_mutex_lock(&mux2);
		while ( pthread_mutex_trylock(&mux1) ) {
		   pthread_mutex_unlock(&mux2);  //avoid deadlock
		   usleep(50000);				 //if failed to get mux2, release mux1 first
		   pthread_mutex_lock(&mux2);
		}
		++cnt;
		LOGI(1, "thread %d: cnt = %d", *threadNum, cnt);
		pthread_mutex_unlock(&mux2);
		pthread_mutex_unlock(&mux1);
		sleep(1);
	}
}

void jni_start_threads_dead() {
	pthread_t th1, th2;
	int threadNum1 = 1, threadNum2 = 2;
	int ret;
	pthread_mutex_init(&mux1, NULL);
	pthread_mutex_init(&mux2, NULL);
	ret = pthread_create(&th1, NULL, run_by_thread1dead, (void*)&threadNum1);
	if(ret) {
		LOGE(1, "cannot create the thread %d thread: %d", threadNum1, ret);
	}
	LOGI(1, "thread 1 started");
	ret = pthread_create(&th2, NULL, run_by_thread2dead, (void*)&threadNum2);
	if(ret) {
		LOGE(1, "cannot create the thread %d: %d", threadNum2, ret);
	}
	LOGI(1, "thread 2 started");
	ret = pthread_join(th1, NULL);
	LOGI(1, "thread 1 end %d", ret);
	ret = pthread_join(th2, NULL);
	LOGI(1, "thread 2 end %d", ret);
	pthread_mutex_destroy(&mux1);
	pthread_mutex_destroy(&mux2);
}

void *run_by_thread1dead(void *arg) {
	int* threadNum = (int*)arg;
	while (cnt < THR) {
		pthread_mutex_lock(&mux1);
		usleep(50);
		pthread_mutex_lock(&mux2);
		++cnt;
		LOGI(1, "thread %d: cnt = %d", *threadNum, cnt);
		pthread_mutex_unlock(&mux2);
		pthread_mutex_unlock(&mux1);
		sleep(1);
	}
}

void *run_by_thread2dead(void *arg) {
	int* threadNum = (int*)arg;
	while (cnt < THR) {
		pthread_mutex_lock(&mux2);
		usleep(50);
		pthread_mutex_lock(&mux1);
		++cnt;
		LOGI(1, "thread %d: cnt = %d", *threadNum, cnt);
		pthread_mutex_unlock(&mux1);
		pthread_mutex_unlock(&mux2);
		sleep(1);
	}
}
