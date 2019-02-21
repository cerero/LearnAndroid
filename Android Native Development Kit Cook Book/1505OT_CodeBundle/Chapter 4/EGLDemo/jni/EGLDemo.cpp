#include <jni.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>

#include "Renderer.h"
#include "mylog.h"

void naSurfaceChanged(JNIEnv* env, jclass clazz, jobject pSurface);
void naSurfaceDestroyed(JNIEnv* env, jclass clazz);
void naNewRenderer(JNIEnv* env, jclass clazz);
void naStartRenderer(JNIEnv* env, jclass clazz);
void naRequestRenderer(JNIEnv* env, jclass clazz, float pAngleX, float pAngleY);
void naStopRenderer(JNIEnv* env, jclass clazz);
void naDestroyRenderer(JNIEnv* env, jclass clazz);

jint JNI_OnLoad(JavaVM* pVm, void* reserved)
{
	JNIEnv* env;
	if (pVm->GetEnv((void **)&env, JNI_VERSION_1_6) != JNI_OK) {
		 return -1;
	}
	JNINativeMethod nm[7];
	nm[0].name = "naSurfaceChanged";
	nm[0].signature = "(Landroid/view/Surface;)V";
	nm[0].fnPtr = (void*)naSurfaceChanged;
	nm[1].name = "naSurfaceDestroyed";
	nm[1].signature = "()V";
	nm[1].fnPtr = (void*)naSurfaceDestroyed;
	nm[2].name = "naNewRenderer";
	nm[2].signature = "()V";
	nm[2].fnPtr = (void*)naNewRenderer;
	nm[3].name = "naStartRenderer";
	nm[3].signature = "()V";
	nm[3].fnPtr = (void*)naStartRenderer;
	nm[4].name = "naRequestRenderer";
	nm[4].signature = "(FF)V";
	nm[4].fnPtr = (void*)naRequestRenderer;
	nm[5].name = "naStopRenderer";
	nm[5].signature = "()V";
	nm[5].fnPtr = (void*)naStopRenderer;
	nm[6].name = "naDestroyRenderer";
	nm[6].signature = "()V";
	nm[6].fnPtr = (void*)naDestroyRenderer;
	jclass cls = env->FindClass("cookbook/chapter4/egl/MySurfaceView");
	// Register methods with env->RegisterNatives.
	env->RegisterNatives(cls, nm, 7);
	return JNI_VERSION_1_6;
}

ANativeWindow *gWindow;
Renderer *gRenderer;

void naSurfaceChanged(JNIEnv* env, jclass clazz, jobject pSurface) {
	gWindow = ANativeWindow_fromSurface(env, pSurface);
	LOGI(2, "naSurfaceCreated");
	gRenderer->initEGLAndOpenGL1x(gWindow);
}

void naSurfaceDestroyed(JNIEnv* env, jclass clazz) {
	LOGI(2, "naSurfaceDestroyed");
	ANativeWindow_release(gWindow);
}

void naNewRenderer(JNIEnv* env, jclass clazz) {
	LOGI(2, "naNewRenderer");
	gRenderer = new Renderer();
}

void naStartRenderer(JNIEnv* env, jclass clazz) {
	LOGI(2, "naStartRenderer");
	gRenderer->start();
}

void naRequestRenderer(JNIEnv* env, jclass clazz, float pAngleX, float pAngleY) {
	LOGI(2, "naRequestRender");
	gRenderer->renderAFrame(pAngleX, pAngleY);
}

void naStopRenderer(JNIEnv* env, jclass clazz) {
	LOGI(2, "naStopRenderer");
	gRenderer->stop();
}

void naDestroyRenderer(JNIEnv* env, jclass clazz) {
	LOGI(2, "naDestroyRenderer");
	ANativeWindow_release(gWindow);
	delete gRenderer;
	gRenderer = NULL;
}




