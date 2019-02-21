#include <jni.h>
#include <unistd.h>
#include <limits.h>
#include <pthread.h>
#include <semaphore.h>

#include "mylog.h"

void *run_by_consumer_thread(void *arg);
void *run_by_producer_thread(void *arg);
void jni_start_threads(JNIEnv *pEnv, jobject pObj, int pNumOfConsumer, int pNumOfProducer, int numOfSlots);

jint JNI_OnLoad(JavaVM* pVm, void* reserved)
{
	JNIEnv* env;
	if (pVm->GetEnv((void **)&env, JNI_VERSION_1_6) != JNI_OK) {
		 return -1;
	}
	JNINativeMethod nm[1];
	nm[0].name = "jni_start_threads";
	nm[0].signature = "(III)V";
	nm[0].fnPtr = (void*)jni_start_threads;
	jclass cls = env->FindClass("cookbook/chapter6/nativethreadssemaphore/MainActivity");
	// Register methods with env->RegisterNatives.
	env->RegisterNatives(cls, nm, 1);
	return JNI_VERSION_1_6;
}

int numOfItems;
sem_t emptySem, fullSem;
pthread_mutex_t mux;
void jni_start_threads(JNIEnv *pEnv, jobject pObj, int pNumOfConsumer, int pNumOfProducer, int numOfSlots) {
	pthread_t *ths;
	int i, ret;
	int *thNum;
	pthread_mutex_init(&mux, NULL);
	sem_init(&emptySem, 0, numOfSlots);
	sem_init(&fullSem, 0, 0);
	ths = (pthread_t*)malloc(sizeof(pthread_t)*(pNumOfConsumer+pNumOfProducer));
	thNum = (int*)malloc(sizeof(int)*(pNumOfConsumer+pNumOfProducer));
	for (i = 0; i < pNumOfConsumer + pNumOfProducer; ++i) {
		thNum[i] = i;
		if (i < pNumOfConsumer) {
			ret = pthread_create(&ths[i], NULL, run_by_consumer_thread, (void*)&(thNum[i]));
		} else {
			ret = pthread_create(&ths[i], NULL, run_by_producer_thread, (void*)&(thNum[i]));
		}
		if(ret) {
			LOGI(1, "cannot create the thread %d thread: %d\n", thNum[i], ret);
			continue;
		}
		if (i < pNumOfConsumer) {
			LOGI(1, "consumer thread %d started\n", thNum[i]);
		} else {
			LOGI(1, "producer thread %d started\n", thNum[i]);
		}
	}
	for (i = 0; i < pNumOfConsumer+pNumOfProducer; ++i) {
		ret = pthread_join(ths[i], NULL);
		if (ret != 0) {
			LOGI(1, "join failed\n");
		}
		//LOGI(1, "thread %d join returned\n", thNum[i]);
	}
	sem_destroy(&emptySem);
	sem_destroy(&fullSem);
	pthread_mutex_destroy(&mux);
	free(thNum);
	free(ths);
}

void *run_by_consumer_thread(void *arg) {
	int* threadNum = (int*)arg;
	int i;
	for (i = 0; i < 4; ++i) {
		//int emValue, fuValue, pItemNo;
		sem_wait(&fullSem);
		pthread_mutex_lock(&mux);
		//pItemNo = numOfItems;
		--numOfItems;
		//sem_getvalue(&fullSem, &fuValue);
		//sem_getvalue(&emptySem, &emValue);
		//LOGI(1, "consumed %d empty: %d occupied: %d\n", pItemNo, emValue, fuValue);
		LOGI(1, "consumer thread %d consumed %d \n", *threadNum, (numOfItems+1));
		pthread_mutex_unlock(&mux);
		sem_post(&emptySem);
	}
	LOGI(1, "consumer thread %d return\n", *threadNum);
	return NULL;
}

void *run_by_producer_thread(void *arg) {
	int* threadNum = (int*)arg;
	int i;
	for (i = 0; i < 4; ++i) {
		//int emValue, fuValue, pItemNo;
		sem_wait(&emptySem);
		pthread_mutex_lock(&mux);
		++numOfItems;
		//sem_getvalue(&emptySem, &emValue);
		//sem_getvalue(&fullSem, &fuValue);
		//LOGI(1, "produced %d empty: %d occupied: %d\n", pItemNo, emValue, fuValue);
		LOGI(1, "producer thread %d produced %d \n", *threadNum, numOfItems);
		pthread_mutex_unlock(&mux);
		sem_post(&fullSem);
	}
	LOGI(1, "producer thread %d return\n", *threadNum);
	return NULL;
}
