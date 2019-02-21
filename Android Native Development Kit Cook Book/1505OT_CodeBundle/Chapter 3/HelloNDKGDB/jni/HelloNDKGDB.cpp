#include <jni.h>
#include <unistd.h>

int multiply(int i, int j) {
	int x = i * j;
	return x;
}

extern "C" {
	JNIEXPORT jint JNICALL Java_cookbook_chapter3_HelloNDKGDBActivity_multiply(JNIEnv* env, jobject o, jint pi, jint pj){
		int i = 1, j = 0;
		while (i) {
			j=(++j)/100;
		}
		return multiply(pi, pj);
	}
}
