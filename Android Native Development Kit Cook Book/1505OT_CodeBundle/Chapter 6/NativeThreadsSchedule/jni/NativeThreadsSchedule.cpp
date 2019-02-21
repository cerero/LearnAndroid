#include <jni.h>
#include <unistd.h>
#include <pthread.h>

#include "mylog.h"

#include "NativeThreadsSchedule.h"

extern JavaVM *cachedJvm;

jint JNI_OnLoad(JavaVM* pVm, void* reserved)
{
	JNIEnv* env;
	if (pVm->GetEnv((void **)&env, JNI_VERSION_1_6) != JNI_OK) {
		 return -1;
	}
	JNINativeMethod nm[5];
	cachedJvm = pVm;
	nm[0].name = "jni_thread_scope";
	nm[0].signature = "()V";
	nm[0].fnPtr = (void*)jni_thread_scope;
	nm[1].name = "jni_thread_get_priority_ranges";
	nm[1].signature = "()V";
	nm[1].fnPtr = (void*)jni_thread_get_priority_ranges;
	nm[2].name = "jni_thread_fifo";
	nm[2].signature = "()V";
	nm[2].fnPtr = (void*)jni_thread_fifo;
	nm[3].name = "jni_thread_set_priority";
	nm[3].signature = "()V";
	nm[3].fnPtr = (void*)jni_thread_set_priority;
	nm[4].name = "jni_process_setThreadPriority";
	nm[4].signature = "()V";
	nm[4].fnPtr = (void*)jni_process_setThreadPriority;
	jclass cls = env->FindClass("cookbook/chapter6/nativethreadsschedule/MainActivity");
	// Register methods with env->RegisterNatives.
	env->RegisterNatives(cls, nm, 5);
	return JNI_VERSION_1_6;
}

void *run_by_thread(void *arg);
void jni_thread_scope() {
	pthread_attr_t attr;
	int ret;
	pid_t fpId = fork();
	if (0 == fpId) {
		//the child process, set scope to process
		pthread_attr_init(&attr);
		int ret = pthread_attr_setscope(&attr, PTHREAD_SCOPE_PROCESS);
		LOGI(1, "process %d set scope %d %d", getpid(), ret, pthread_attr_getscope(&attr));
		pthread_t thFive[5];
		int threadNums[5];
		int i;
		for (i = 0; i < 5; ++i) {
			threadNums[i] = i;
			ret = pthread_create(&thFive[i], &attr, run_by_thread, (void*)&(threadNums[i]));
			if(ret) {
				LOGE(1, "cannot create the thread %d: %d", i, ret);
			}
			LOGI(1, "process %d thread %d started", getpid(), i);
		}
		for (i = 0; i < 5; ++i) {
			ret = pthread_join(thFive[i], NULL);
//			LOGI(1, "join returned for process %d thread %d", getpid(), i);
		}
	} else {
		//the parent process, set scope to system
		pthread_attr_init(&attr);
		int ret = pthread_attr_setscope(&attr, PTHREAD_SCOPE_SYSTEM);
		LOGI(1, "process %d set scope %d %d", getpid(), ret, pthread_attr_getscope(&attr));
		pthread_t th1;
		int threadNum1 = 0;
		ret = pthread_create(&th1, &attr, run_by_thread, (void*)&threadNum1);
		if(ret) {
			LOGE(1, "cannot create the thread %d thread: %d", threadNum1, ret);
		}
		LOGI(1, "process %d thread 0 started", getpid());
		ret = pthread_join(th1, NULL);
//		LOGI(1, "join returned for process %d thread 0", getpid());
	}
	//code executed by both processes
	pthread_attr_destroy(&attr);
}

void jni_thread_get_priority_ranges() {
	int minP, maxP;
	minP = sched_get_priority_min(SCHED_OTHER);
	maxP = sched_get_priority_max(SCHED_OTHER);
	LOGI(1, "SCHED_OTHER: %d, min pri: %d, max pri: %d", SCHED_OTHER, minP, maxP);
	minP = sched_get_priority_min(SCHED_FIFO);
	maxP = sched_get_priority_max(SCHED_FIFO);
	LOGI(1, "SCHED_FIFO: %d, min pri: %d, max pri: %d", SCHED_FIFO, minP, maxP);
	minP = sched_get_priority_min(SCHED_RR);
	maxP = sched_get_priority_max(SCHED_RR);
	LOGI(1, "SCHED_RR: %d, min pri: %d, max pri: %d", SCHED_RR, minP, maxP);
}

void jni_thread_fifo() {
	pthread_attr_t attr;
	int ret;
	pid_t fpId = fork();
	struct sched_param prio;
	if (0 == fpId) {
		//the child process
		pthread_attr_init(&attr);
		pthread_t thFive[5];
		int threadNums[5];
		int i;
		for (i = 0; i < 5; ++i) {
			if (i == 4) {
				prio.sched_priority = 10;
			} else {
				prio.sched_priority = 1;
			}
			ret = pthread_attr_setschedpolicy(&attr, SCHED_FIFO);
			if (ret) {
				LOGE(1, "process %d set policy %d", getpid(), ret);
			}
			ret = pthread_attr_setschedparam(&attr, &prio);
			if (ret) {
				LOGE(1, "process %d set priority %d", getpid(), ret);
			}
			threadNums[i] = i;
			ret = pthread_create(&thFive[i], &attr, run_by_thread, (void*)&(threadNums[i]));
			if(ret) {
				LOGE(1, "cannot create the thread %d: %d", i, ret);
			}
			pthread_attr_t lattr;
			struct sched_param lprio;
			int lpolicy;
			pthread_getattr_np(thFive[i], &lattr);
			pthread_attr_getschedpolicy(&lattr, &lpolicy);
			pthread_attr_getschedparam(&lattr, &lprio);
			pthread_attr_destroy(&lattr);
			LOGI(1, "process %d thread %d started, policy: %d, prio: %d", getpid(), i, lpolicy, lprio.sched_priority);
		}
		for (i = 0; i < 5; ++i) {
			ret = pthread_join(thFive[i], NULL);
//			LOGI(1, "join returned for process %d thread %d", getpid(), i);
		}
	} else {
		//the parent process
		pthread_attr_init(&attr);
		prio.sched_priority = 10;
		ret = pthread_attr_setschedpolicy(&attr, SCHED_FIFO);
		if (ret) {
			LOGE(1, "process %d set policy %d", getpid(), ret);
		}
		ret = pthread_attr_setschedparam(&attr, &prio);
		if (ret) {
			LOGE(1, "process %d set priority %d", getpid(), ret);
		}
		pthread_t th1;
		int threadNum1 = 0;
		ret = pthread_create(&th1, &attr, run_by_thread, (void*)&threadNum1);
		if(ret) {
			LOGE(1, "cannot create the thread %d thread: %d", threadNum1, ret);
		}
		pthread_attr_t lattr;
		struct sched_param lprio;
		int lpolicy;
		pthread_getattr_np(th1, &lattr);
		pthread_attr_getschedpolicy(&lattr, &lpolicy);
		pthread_attr_getschedparam(&lattr, &lprio);
		pthread_attr_destroy(&lattr);
		LOGI(1, "process %d thread 0 started, policy: %d, prio: %d", getpid(), lpolicy, lprio.sched_priority);
		ret = pthread_join(th1, NULL);
//		LOGI(1, "join returned for process %d thread 0", getpid());
	}
	//code executed by both processes
	pthread_attr_destroy(&attr);
}

void *run_by_thread(void *arg) {
	int cnt = 18000000, i;
	int* threadNum = (int*)arg;
	for (i = 1; i < cnt; ++i) {
		if (0 == i%6000000) {
			LOGI(1, "process %d thread %d: %d", getpid(), *threadNum, i);
		}
	}
	LOGI(1, "process %d thread %d return", getpid(), *threadNum);
}
