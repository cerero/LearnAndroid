#include <jni.h>
#include <unistd.h>
#include <pthread.h>
#include <sys/time.h>
#include <sys/resource.h>

#include "mylog.h"

#include "NativeThreadsSchedule.h"

void *run_by_thread2(void *arg);
void jni_thread_set_priority() {
//	LOGI(1, "jni_thread_set_priority");
	int ret;
	pthread_t thFive[5];
	int threadNums[5];
	int i;
	for (i = 0; i < 5; ++i) {
		threadNums[i] = i;
		ret = pthread_create(&thFive[i], NULL, run_by_thread2, (void*)&(threadNums[i]));
		if(ret) {
			LOGE(1, "cannot create the thread %d: %d", i, ret);
		}
		LOGI(1, "thread %d started", i);
	}
	for (i = 0; i < 5; ++i) {
		ret = pthread_join(thFive[i], NULL);
		LOGI(1, "join returned for thread %d", i);
	}
}

void *run_by_thread2(void *arg) {
	int cnt = 18000000, i;
	int* threadNum = (int*)arg;
	switch (*threadNum) {
	case 0:
		setpriority(PRIO_PROCESS, 0, 21);
		break;
	case 1:
		setpriority(PRIO_PROCESS, 0, 10);
		break;
	case 2:
		setpriority(PRIO_PROCESS, 0, 0);
		break;
	case 3:
		setpriority(PRIO_PROCESS, 0, -10);
		break;
	case 4:
		setpriority(PRIO_PROCESS, 0, -21);
		break;
	default:
		break;
	}

	for (i = 1; i < cnt; ++i) {
		if (0 == i%6000000) {
			int prio = getpriority(PRIO_PROCESS, 0);
			LOGI(1, "thread %d (prio = %d): %d", *threadNum, prio, i);
		}
	}
	int prio = getpriority(PRIO_PROCESS, 0);
	LOGI(1, "thread %d (prio = %d): %d return", *threadNum, prio, i);
}




