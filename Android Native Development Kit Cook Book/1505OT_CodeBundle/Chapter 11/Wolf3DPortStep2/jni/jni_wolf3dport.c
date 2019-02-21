#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include <jni.h>
#include <android/bitmap.h>

#include "mylog.h"
#include "wl_def.h"

static JavaVM *cachedJvm;
jobject cachedGameViewObj;
jmethodID updateGameBitmapMID;
void* gBitmap;

extern int wolf_main(int argc, char **argv);

jint naMain(JNIEnv *pEnv, jobject pObj, jobjectArray pArgv, jobject pGameViewObj) {
	(*pEnv)->GetJavaVM(pEnv, &cachedJvm);
	jclass jGameViewCls = (*pEnv)->GetObjectClass(pEnv, pGameViewObj);
	cachedGameViewObj = (*pEnv)->NewGlobalRef(pEnv, pGameViewObj);

	jfieldID bitmapFID;
	bitmapFID = (*pEnv)->GetFieldID(pEnv, jGameViewCls, "mBitmap", "Landroid/graphics/Bitmap;");
	jobject pBitmap = (*pEnv)->GetObjectField(pEnv, cachedGameViewObj, bitmapFID);

	AndroidBitmapInfo linfo;
	int lret;
	//1. retrieve information about the bitmap
	if ((lret = AndroidBitmap_getInfo(pEnv, pBitmap, &linfo)) < 0) {
		LOGE(1, "AndroidBitmap_getinfo failed! error = %d", lret);
		return -1;
	}
	if (linfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
		LOGE(1, "bitmap format is not rgba_8888!");
		return- 1;
	}
	//2. lock the pixel buffer and retrieve a pointer to it
	if ((lret = AndroidBitmap_lockPixels(pEnv, pBitmap, &gBitmap)) < 0) {
		LOGE(1, "AndroidBitmap_lockpixels() failed! error = %d", lret);
		return -1;
	}
	//get the arguments
	jsize argc =  (*pEnv)->GetArrayLength(pEnv, pArgv);
	char** argv = (char**) malloc(sizeof(char*)*argc);
	int i;
	for (i = 0; i < argc; i++) {
	    jstring anArgv = (jstring)(*pEnv)->GetObjectArrayElement(pEnv, pArgv, i);
	    const char *anArgvChars  = (*pEnv)->GetStringUTFChars(pEnv, anArgv, 0);
	    argv[i] = malloc(strlen(anArgvChars) + 1);
	    strcpy(argv[i], anArgvChars);
	    (*pEnv)->ReleaseStringUTFChars(pEnv, anArgv, anArgvChars);
	    LOGI(1, "%d %s", i, argv[i]);
	}
	LOGI(1, "No. of arguments: %d", argc);

	updateGameBitmapMID = (*pEnv)->GetMethodID(pEnv, jGameViewCls, "updateGameBitmap", "()V");
	if (NULL == updateGameBitmapMID) {
		LOGE(1, "error finding method jupdateGameBitmapMID");
		return -1;
	}
	//start the game
	LOGI(1, "start WolfMain");
	WolfMain(argc, argv);
	AndroidBitmap_unlockPixels(pEnv, pBitmap);
	(*pEnv)->DeleteGlobalRef(pEnv, cachedGameViewObj);
	cachedGameViewObj = NULL;
	return 0;
}

//call the updateGameBitmap method through JNI
void jni_updateGameBitmap() {
	JNIEnv *pEnv;
	(*cachedJvm)->AttachCurrentThread(cachedJvm, &pEnv, NULL);
	(*pEnv)->CallVoidMethod(pEnv, cachedGameViewObj, updateGameBitmapMID);
}

extern void keyboard_handler(int code, int press);
jint keyPress(JNIEnv * pEnv, jobject pObj, jint scanCode) {
	keyboard_handler(scanCode, 1);
	return scanCode;
}

jint keyRelease(JNIEnv * env, jobject pObj, jint scanCode) {
	keyboard_handler(scanCode, 0);
	return scanCode;
}

jint JNI_OnLoad(JavaVM* pVm, void* reserved) {
	JNIEnv* env;
	if ((*pVm)->GetEnv(pVm, (void **)&env, JNI_VERSION_1_6) != JNI_OK) {
		 return -1;
	}
	JNINativeMethod nm[3];
	nm[0].name = "naMain";
	nm[0].signature = "([Ljava/lang/String;Lcookbook/chapter11/wolf3dport/GameView;)I";
	nm[0].fnPtr = (void*)naMain;
	nm[1].name = "keyPress";
	nm[1].signature = "(I)I";
	nm[1].fnPtr = (void*)keyPress;
	nm[2].name = "keyRelease";
	nm[2].signature = "(I)I";
	nm[2].fnPtr = (void*)keyRelease;
	jclass cls = (*env)->FindClass(env, "cookbook/chapter11/wolf3dport/MainActivity");
	(*env)->RegisterNatives(env, cls, nm, 3);
	return JNI_VERSION_1_6;
}

