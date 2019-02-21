#include <jni.h>
#include <unistd.h>
#include <pthread.h>
#include <math.h>

#include "mylog.h"

void *run_by_read_thread(void *arg);
void *run_by_write_thread(void *arg);
void jni_start_threads(JNIEnv *pEnv, jobject pObj, int pNumOfReader, int pNumOfWriter);

jint JNI_OnLoad(JavaVM* pVm, void* reserved)
{
	JNIEnv* env;
	if (pVm->GetEnv((void **)&env, JNI_VERSION_1_6) != JNI_OK) {
		 return -1;
	}
	JNINativeMethod nm[1];
	nm[0].name = "jni_start_threads";
	nm[0].signature = "(II)V";
	nm[0].fnPtr = (void*)jni_start_threads;
	jclass cls = env->FindClass("cookbook/chapter6/nativethreadsrwlock/MainActivity");
	// Register methods with env->RegisterNatives.
	env->RegisterNatives(cls, nm, 1);
	return JNI_VERSION_1_6;
}

pthread_rwlock_t rwlock;
int sharedCnt;
int numOfWriter;
void jni_start_threads(JNIEnv *pEnv, jobject pObj, int pNumOfReader, int pNumOfWriter) {
	pthread_t *ths;
	int i, ret;
	int *thNum;
	LOGI(1, "readers: %d, writers: %d", pNumOfReader, pNumOfWriter);
	numOfWriter = pNumOfWriter;
	ths = (pthread_t*)malloc(sizeof(pthread_t)*(pNumOfReader+pNumOfWriter));
	thNum = (int*)malloc(sizeof(int)*(pNumOfReader+pNumOfWriter));
	pthread_rwlock_init(&rwlock, NULL);
	for (i = 0; i < pNumOfReader + pNumOfWriter; ++i) {
		thNum[i] = i;
		if (i < pNumOfReader) {
			ret = pthread_create(&ths[i], NULL, run_by_read_thread, (void*)&(thNum[i]));
		} else {
			ret = pthread_create(&ths[i], NULL, run_by_write_thread, (void*)&(thNum[i]));
		}
		if(ret) {
			LOGI(1,"cannot create the thread %d thread: %d\n", thNum[i], ret);
			continue;
		}
		if (i < pNumOfReader) {
			LOGI(1, "reader thread %d started\n", thNum[i]);
		} else {
			LOGI(1, "writer thread %d started\n", thNum[i]);
		}
	}
	for (i = 0; i < pNumOfReader+pNumOfWriter; ++i) {
		ret = pthread_join(ths[i], NULL);
		if (ret != 0) {
			LOGI(1, "join failed\n");
		}
		//LOGI(1, "thread %d join returned\n", thNum[i]);
	}
	pthread_rwlock_destroy(&rwlock);
	free(thNum);
	free(ths);
}

void *run_by_read_thread(void *arg) {
	int* threadNum = (int*)arg;
	int ifRun = 1;
	int accessTimes = 0;
	int ifPrint = 1;
	while (ifRun) {
		if (!pthread_rwlock_rdlock(&rwlock)) {
			//LOGI(1, "reader thread %d: %d\n", *threadNum, sharedCnt);
			if (100000*numOfWriter == sharedCnt) {
				ifRun = 0;
			}
			if (0 <= sharedCnt && ifPrint) {
				LOGI(1, "reader thread %d sharedCnt value before processing %d\n", *threadNum, sharedCnt);
				int j, k;//some dummy processing
				for (j = 0; j < 100000; ++j) {
					k = j*2;
					k = sqrt(k);
				}
				ifPrint = 0;
				LOGI(1, "reader thread %d sharedCnt value after processing %d %d\n", *threadNum, sharedCnt, k);
			}
			if ((++accessTimes) == INT_MAX/5) {
				accessTimes = 0;
				LOGI(1, "reader thread %d still running: %d\n", *threadNum, sharedCnt);
			}
			pthread_rwlock_unlock(&rwlock);
		}
	}
	LOGI(1, "reader thread %d return %d\n", *threadNum, sharedCnt);
	return NULL;
}

void *run_by_write_thread(void *arg) {
	int cnt = 100000, i, j, k;
	int* threadNum = (int*)arg;
	for (i = 0; i < cnt; ++i) {
		if (!pthread_rwlock_wrlock(&rwlock)) {
			int lastShCnt = sharedCnt;
			for (j = 0; j < 10; ++j) {	//some dummy processing
				k = j*2;
				k = sqrt(k);
			}
			sharedCnt = lastShCnt + 1;
			//LOGI(1, "writer thread %d after update: %d\n", *threadNum, sharedCnt);
			pthread_rwlock_unlock(&rwlock);
		}
	}
	//print value of k to make sure the calculation based on k is not stripped away
	LOGI(1, "writer thread %d return %d %d\n", *threadNum, sharedCnt, k);
	return NULL;
}
