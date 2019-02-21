#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>

#include "mylog.h"

void *run_by_thread(void *arg);
void jni_start_threads(JNIEnv *pEnv, jobject pObj, int pNumOfThreads);
void free_muxCnt(void *pMuxCntData);
void thread_step_2(int thNum);
void thread_step_1();

jint JNI_OnLoad(JavaVM* pVm, void* reserved)
{
	JNIEnv* env;
	if (pVm->GetEnv((void **)&env, JNI_VERSION_1_6) != JNI_OK) {
		 return -1;
	}
	JNINativeMethod nm[1];
	nm[0].name = "jni_start_threads";
	nm[0].signature = "(I)V";
	nm[0].fnPtr = (void*)jni_start_threads;
	jclass cls = env->FindClass("cookbook/chapter6/nativethreadsdata/MainActivity");
	// Register methods with env->RegisterNatives.
	env->RegisterNatives(cls, nm, 1);
	return JNI_VERSION_1_6;
}

pthread_mutex_t mux;
pthread_key_t muxCntKey;
void jni_start_threads(JNIEnv *pEnv, jobject pObj, int pNumOfThreads) {
	pthread_t *ths;
	int i, ret;
	int *thNum;
	ths = (pthread_t*)malloc(sizeof(pthread_t)*pNumOfThreads);
	thNum = (int*)malloc(sizeof(int)*pNumOfThreads);
	pthread_mutex_init(&mux, NULL);
	pthread_key_create(&muxCntKey, free_muxCnt);
	for (i = 0; i < pNumOfThreads; ++i) {
		thNum[i] = i;
		ret = pthread_create(&ths[i], NULL, run_by_thread, (void*)&(thNum[i]));
		if(ret) {
			LOGI(1, "cannot create the thread %d thread: %d\n", thNum[i], ret);
			continue;
		}
		LOGI(1, "thread %d started\n", thNum[i]);
	}
	for (i = 0; i < pNumOfThreads; ++i) {
		ret = pthread_join(ths[i], NULL);
		if (ret != 0) {
			LOGI(1, "join failed\n");
		}
		//LOGI(1, "thread %d join returned\n", thNum[i]);
	}
	pthread_key_delete(muxCntKey);
	pthread_mutex_destroy(&mux);
	free(thNum);
	free(ths);
}

void free_muxCnt(void *pMuxCntData) {
	free((int*)pMuxCntData);
}

void thread_step_1() {
	struct timeval st, cu;
	long stt, cut;
	int *muxCntData = (int*)pthread_getspecific(muxCntKey);
	gettimeofday(&st, NULL);
	stt = st.tv_sec*1000 + st.tv_usec/1000;
	//LOGI(1, "st time: %ld\n", stt);
	do {
        pthread_mutex_lock(&mux);
		(*muxCntData)++;
        pthread_mutex_unlock(&mux);
		gettimeofday(&st, NULL);
		cut = st.tv_sec*1000 + st.tv_usec/1000;
		//LOGI(1, "cur time: %d\n", cut);
    } while (cut - stt < 10000);
}

void thread_step_2(int thNum) {
	int *muxCntData = (int*)pthread_getspecific(muxCntKey);
	LOGI(1, "thread %d: mux usage count: %d\n", thNum, *muxCntData);
}

void *run_by_thread(void *arg) {
	int* threadNum = (int*)arg;
	int *muxCntData = (int*)malloc(sizeof(int));
	*muxCntData = 0;
	pthread_setspecific(muxCntKey, (void*)muxCntData);
	thread_step_1();
	thread_step_2(*threadNum);
	LOGI(1, "%d return\n", *threadNum);
	return NULL;
}
