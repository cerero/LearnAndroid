#include <jni.h>
#include <unistd.h>
#include <pthread.h>
#include <sys/time.h>
#include <sys/resource.h>

#include "mylog.h"

#include "NativeThreadsSchedule.h"

JavaVM *cachedJvm;

void *run_by_thread3(void *arg);

void jni_process_setThreadPriority() {
//	LOGI(1, "jni_thread_set_priority");
	int ret;
	pthread_t thFive[5];
	int threadNums[5];
	int i;
	for (i = 0; i < 5; ++i) {
		threadNums[i] = i;
		ret = pthread_create(&thFive[i], NULL, run_by_thread3, (void*)&(threadNums[i]));
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

void *run_by_thread3(void *arg) {
	int cnt = 18000000, i;
	int* threadNum = (int*)arg;
	JNIEnv *env;
	jmethodID setThreadPriorityMID;
	cachedJvm->AttachCurrentThread(&env, NULL);
	jclass procClass = env->FindClass("android/os/Process");
	setThreadPriorityMID = env->GetStaticMethodID(procClass, "setThreadPriority", "(I)V");
//	LOGI(1, "%d", *threadNum);
	switch (*threadNum) {
	case 0:
		env->CallStaticVoidMethod(procClass, setThreadPriorityMID, 21);
		break;
	case 1:
		env->CallStaticVoidMethod(procClass, setThreadPriorityMID, 10);
		break;
	case 2:
		env->CallStaticVoidMethod(procClass, setThreadPriorityMID, 0);
		break;
	case 3:
		env->CallStaticVoidMethod(procClass, setThreadPriorityMID, -10);
		break;
	case 4:
		env->CallStaticVoidMethod(procClass, setThreadPriorityMID, -21);
		break;
	default:
		break;
	}

	//we can also use getThreadPriority(int tid) through JNI interface
	for (i = 1; i < cnt; ++i) {
		if (0 == i%6000000) {
			int prio = getpriority(PRIO_PROCESS, 0);
			LOGI(1, "thread %d (prio = %d): %d", *threadNum, prio, i);
		}
	}
	int prio = getpriority(PRIO_PROCESS, 0);
	LOGI(1, "thread %d (prio = %d): %d return", *threadNum, prio, i);
	cachedJvm->DetachCurrentThread();
}
