#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>

#include "mylog.h"

void naDemoJniGraphics(JNIEnv* env, jclass clazz, jobject pBitmap);

jint JNI_OnLoad(JavaVM* pVm, void* reserved)
{
	JNIEnv* env;
	if (pVm->GetEnv((void **)&env, JNI_VERSION_1_6) != JNI_OK) {
		 return -1;
	}
	JNINativeMethod nm[4];
	nm[0].name = "naDemoJniGraphics";
	nm[0].signature = "(Landroid/graphics/Bitmap;)V";
	nm[0].fnPtr = (void*)naDemoJniGraphics;
	jclass cls = env->FindClass("cookbook/chapter7/JNIGraphics/RenderView");
	// Register methods with env->RegisterNatives.
	env->RegisterNatives(cls, nm, 1);
	return JNI_VERSION_1_6;
}

void naDemoJniGraphics(JNIEnv* pEnv, jclass clazz, jobject pBitmap) {
	int lRet, i, j;
	AndroidBitmapInfo lInfo;
	void* lBitmap;
	 //1. retrieve information about the bitmap
	if ((lRet = AndroidBitmap_getInfo(pEnv, pBitmap, &lInfo)) < 0) {
		LOGE(1, "AndroidBitmap_getInfo failed! error = %d", lRet);
		return;
	}
	if (lInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
		LOGE(1, "Bitmap format is not RGBA_8888!");
		return;
	}
	LOGI(1, "height: %d, width: %d, stride: %d, format: %d", lInfo.height, lInfo.width, lInfo.stride, lInfo.format);
	//2. lock the pixel buffer and retrieve a pointer to it
	if ((lRet = AndroidBitmap_lockPixels(pEnv, pBitmap, &lBitmap)) < 0) {
		LOGE(1, "AndroidBitmap_lockPixels() failed! error = %d", lRet);
	}
	//3. manipulate the pixel buffer
	unsigned char *pixelBuf = (unsigned char*)lBitmap;
	for (i = 0; i < lInfo.height; ++i) {
		for (j = 0; j < lInfo.width; ++j) {
			unsigned char *pixelP = pixelBuf + i*lInfo.stride + j*4;
			*pixelP = (unsigned char)0x00;				//remove R component
//			*(pixelP+1) = (unsigned char)0x00;			//remove G component
//			*(pixelP+2) = (unsigned char)0x00;			//remove B component
//			*(pixelP+3) = (unsigned char)0x00;			//the A component
//			LOGI(1, "%d:%d:%d:%d", *pixelP, *(pixelP+1), *(pixelP+2), *(pixelP+3));
		}
	}
	//4. unlock the bitmap
	AndroidBitmap_unlockPixels(pEnv, pBitmap);
}

