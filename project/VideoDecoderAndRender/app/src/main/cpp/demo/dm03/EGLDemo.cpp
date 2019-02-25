#include <jni.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>

#include "dm03/Renderer.h"
#include "dm02/mylog.h"
#include "dm03/EGLDemo.h"
void naSurfaceChanged_03(JNIEnv* env, jclass clazz, jobject pSurface);
void naSurfaceDestroyed_03(JNIEnv* env, jclass clazz);
void naNewRenderer_03(JNIEnv* env, jclass clazz);
void naStartRenderer_03(JNIEnv* env, jclass clazz);
void naRequestRenderer_03(JNIEnv* env, jclass clazz, float pAngleX, float pAngleY);
void naStopRenderer_03(JNIEnv* env, jclass clazz);
void naDestroyRenderer_03(JNIEnv* env, jclass clazz);

void EGLDemo::OnLoad(JNIEnv* env, void* reserved) {
	JNINativeMethod nm[7];
	nm[0].name = "naSurfaceChanged";
	nm[0].signature = "(Landroid/view/Surface;)V";
	nm[0].fnPtr = (void*)naSurfaceChanged_03;
	nm[1].name = "naSurfaceDestroyed";
	nm[1].signature = "()V";
	nm[1].fnPtr = (void*)naSurfaceDestroyed_03;
	nm[2].name = "naNewRenderer";
	nm[2].signature = "()V";
	nm[2].fnPtr = (void*)naNewRenderer_03;
	nm[3].name = "naStartRenderer";
	nm[3].signature = "()V";
	nm[3].fnPtr = (void*)naStartRenderer_03;
	nm[4].name = "naRequestRenderer";
	nm[4].signature = "(FF)V";
	nm[4].fnPtr = (void*)naRequestRenderer_03;
	nm[5].name = "naStopRenderer";
	nm[5].signature = "()V";
	nm[5].fnPtr = (void*)naStopRenderer_03;
	nm[6].name = "naDestroyRenderer";
	nm[6].signature = "()V";
	nm[6].fnPtr = (void*)naDestroyRenderer_03;
	jclass cls = env->FindClass("com/example/videodecoderandrender/demo/chap04/dm03/MySurfaceView");
	env->RegisterNatives(cls, nm, 7);
}

ANativeWindow *gWindow;
Renderer *gRenderer;

void naSurfaceChanged_03(JNIEnv* env, jclass clazz, jobject pSurface) {
	gWindow = ANativeWindow_fromSurface(env, pSurface);
	LOGI(2, "naSurfaceCreated");
	gRenderer->initEGLAndOpenGL1x(gWindow);
}

void naSurfaceDestroyed_03(JNIEnv* env, jclass clazz) {
	LOGI(2, "naSurfaceDestroyed");
	ANativeWindow_release(gWindow);
}

void naNewRenderer_03(JNIEnv* env, jclass clazz) {
	LOGI(2, "naNewRenderer");
	gRenderer = new Renderer();
}

void naStartRenderer_03(JNIEnv* env, jclass clazz) {
	LOGI(2, "naStartRenderer");
	gRenderer->start();
}

void naRequestRenderer_03(JNIEnv* env, jclass clazz, float pAngleX, float pAngleY) {
	LOGI(2, "naRequestRender");
	gRenderer->renderAFrame(pAngleX, pAngleY);
}

void naStopRenderer_03(JNIEnv* env, jclass clazz) {
	LOGI(2, "naStopRenderer");
	gRenderer->stop();
}

void naDestroyRenderer_03(JNIEnv* env, jclass clazz) {
	LOGI(2, "naDestroyRenderer");
	ANativeWindow_release(gWindow);
	delete gRenderer;
	gRenderer = NULL;
}




