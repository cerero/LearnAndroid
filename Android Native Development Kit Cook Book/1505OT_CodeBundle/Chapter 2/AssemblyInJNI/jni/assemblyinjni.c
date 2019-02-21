#include <jni.h>
#include <android/log.h>

#include "assemblyinjni.h"

JNIEXPORT jint JNICALL Java_cookbook_chapter2_AssemblyInJNIActivity_InlineAssemblyAddDemo(JNIEnv *pEnv, jobject pObj, jint pa, jint pb) {
	int sum;
	__asm__("add %[c], %[a], %[b]"
			: [c] "=r" (sum)
			: [a] "r" (pa), [b] "r" (pb));
	return sum;
}


