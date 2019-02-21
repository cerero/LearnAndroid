#include <jni.h>
#include <bmpfile.h>
#include <stdio.h>

#include "mylog.h"

void naCreateABmp(JNIEnv* env, jclass clazz, jint width, jint height, jint depth);

jint JNI_OnLoad(JavaVM* pVm, void* reserved) {
	JNIEnv* env;
	if ((*pVm)->GetEnv(pVm, (void **)&env, JNI_VERSION_1_6) != JNI_OK) {
		 return -1;
	}
	JNINativeMethod nm[1];
	nm[0].name = "naCreateABmp";
	nm[0].signature = "(III)V";
	nm[0].fnPtr = (void*)naCreateABmp;
	jclass cls = (*env)->FindClass(env, "cookbook/chapter8/portingstatic/MainActivity");
	// Register methods with env->RegisterNatives.
	(*env)->RegisterNatives(env, cls, nm, 1);
	return JNI_VERSION_1_6;
}

void naCreateABmp(JNIEnv* env, jclass clazz, jint width, jint height, jint depth) {
	bmpfile_t *bmp;
	int i, j;
	rgb_pixel_t pixel = {128, 64, 0, 0};

	if (NULL == (bmp = bmp_create(width, height, depth))) {
		LOGE(1, "Invalid depth value: %d. (valid values include 1, 4, 8, 16, 24, 32)\n", depth);
		return;
	}

	for (i = 10, j = 10; j < height; ++i, ++j) {
		bmp_set_pixel(bmp, i, j, pixel);
		pixel.red++;
		pixel.green++;
		pixel.blue++;
		bmp_set_pixel(bmp, i + 1, j, pixel);
		bmp_set_pixel(bmp, i, j + 1, pixel);
	}

	bmp_save(bmp, "/sdcard/test_static.bmp");
	bmp_destroy(bmp);
}
