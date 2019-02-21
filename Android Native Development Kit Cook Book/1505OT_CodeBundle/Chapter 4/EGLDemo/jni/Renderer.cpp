#include <pthread.h>
#include <android/native_window.h>
#include <EGL/egl.h>
#include <GLES/gl.h>

#include <unistd.h>

#include "mylog.h"
#include "OldRenderMethods.h"
#include "Renderer.h"

Renderer::Renderer()
{
    pthread_mutex_init(&mMutex, NULL);
    pthread_cond_init(&mCondVar, NULL);
    mDisplay = EGL_NO_DISPLAY;
    mSurface = EGL_NO_SURFACE;
    mContext = EGL_NO_CONTEXT;
    mWidth = 0;
    mHeight = 0;
    mAngleX = 0.0f;
    mAngleY = 0.0f;
    mRendererEvent = RTE_NONE;
}

Renderer::~Renderer() {
    pthread_mutex_destroy(&mMutex);
    pthread_cond_destroy(&mCondVar);
}

void Renderer::start() {
    if (pthread_create(&mThreadId, NULL, startRenderThread, this)) {
    	LOGE(1, "failed to start rendering thread");
    }
}

void Renderer::stop() {
    pthread_mutex_lock(&mMutex);
    mRendererEvent = RTE_EXIT;
    pthread_mutex_unlock(&mMutex);
    pthread_cond_signal(&mCondVar);	//signal the render thread
    pthread_join(mThreadId, 0);		//wait for render thread to finish
}

void Renderer::initEGLAndOpenGL1x(ANativeWindow *pWindow) {
	LOGI(2, "Renderer::initEGLAndOpenGL1x");
    pthread_mutex_lock(&mMutex);
    mRendererEvent = RTE_SURFACE_CREATE;
    mWindow = pWindow;
    pthread_mutex_unlock(&mMutex);
    pthread_cond_signal(&mCondVar);	//signal the render thread
}

void Renderer::renderAFrame(float pAngleX, float pAngleY) {
	LOGI(2, "Renderer::renderAFrame: %f:%f", pAngleX, pAngleY);
	pthread_mutex_lock(&mMutex);
	mAngleX = pAngleX; mAngleY = pAngleY;
	mRendererEvent = RTE_DRAW_FRAME;
	pthread_mutex_unlock(&mMutex);
	pthread_cond_signal(&mCondVar);	//signal the render thread
	LOGI(2, "***signal sent");
}

void Renderer::renderThreadRun() {
    bool ifRendering = true;
    while (ifRendering) {
        pthread_mutex_lock(&mMutex);
        LOGI(2, "about to sleep, wait on condition");
        pthread_cond_wait(&mCondVar, &mMutex);	//wait for signal
        //renderer processing event
        LOGI(2, "*****mRendererEvent=%d", mRendererEvent);
        switch (mRendererEvent) {
            case RTE_SURFACE_CREATE:
            	mRendererEvent = RTE_NONE;
            	pthread_mutex_unlock(&mMutex);
            	initDisplay();
            	naInitGL1x();
            	naSurfaceChanged(mWidth, mHeight);
                break;
            case RTE_SURFACE_CHANGE:
            	//since we're sending RTE_SURFACE_CREATE when MySurfaceView surfaceChanged occurs
            	//we don't simulate this event.
            	//there might be better ways to handle this
            	mRendererEvent = RTE_NONE;
            	pthread_mutex_unlock(&mMutex);
            	break;
            case RTE_DRAW_FRAME:
            	mRendererEvent = RTE_NONE;
            	pthread_mutex_unlock(&mMutex);
            	if (EGL_NO_DISPLAY!=mDisplay) {
					naDrawGraphics(mAngleX, mAngleY);
					if (!eglSwapBuffers(mDisplay, mSurface)) {
						LOGE(1, "eglSwapBuffers error %d", eglGetError());
					}
            	}
                break;
            case RTE_EXIT:
            	mRendererEvent = RTE_NONE;
            	pthread_mutex_unlock(&mMutex);
            	ifRendering = false;
            	terminateDisplay();
            	break;
            default:
            	mRendererEvent = RTE_NONE;
            	pthread_mutex_unlock(&mMutex);
                break;
        }
//        mRendererEvent = RTE_NONE;
//        pthread_mutex_unlock(&mMutex);
    }
    LOGI(1, "Render thread finished");
}

bool Renderer::initDisplay() {
	/*
	 * Here specify the attributes of the desired configuration.
	 * Below, we select an EGLConfig with at least 8 bits per color
	 * component compatible with on-screen windows
	 */
	const EGLint attribs[] = {
			EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
			EGL_BLUE_SIZE, 8,
			EGL_GREEN_SIZE, 8,
			EGL_RED_SIZE, 8,
			EGL_NONE
	};
	EGLint width, height, format;
	EGLint numConfigs;
	EGLConfig config;
	EGLSurface surface;
	EGLContext context;

	EGLDisplay display = eglGetDisplay(EGL_DEFAULT_DISPLAY);

	eglInitialize(display, 0, 0);

	/* Here, the application chooses the configuration it desires. In this
	 * sample, we have a very simplified selection process, where we pick
	 * the first EGLConfig that matches our criteria */
	eglChooseConfig(display, attribs, &config, 1, &numConfigs);

	/* EGL_NATIVE_VISUAL_ID is an attribute of the EGLConfig that is
	 * guaranteed to be accepted by ANativeWindow_setBuffersGeometry().
	 * As soon as we picked a EGLConfig, we can safely reconfigure the
	 * ANativeWindow buffers to match, using EGL_NATIVE_VISUAL_ID. */
	eglGetConfigAttrib(display, config, EGL_NATIVE_VISUAL_ID, &format);

	ANativeWindow_setBuffersGeometry(mWindow, 0, 0, format);

	surface = eglCreateWindowSurface(display, config, mWindow, NULL);
	context = eglCreateContext(display, config, NULL, NULL);

	if (eglMakeCurrent(display, surface, surface, context) == EGL_FALSE) {
		LOGE(1, "Unable to eglMakeCurrent");
		return -1;
	}

	eglQuerySurface(display, surface, EGL_WIDTH, &width);
	eglQuerySurface(display, surface, EGL_HEIGHT, &height);

    mDisplay = display;
    mSurface = surface;
    mContext = context;
    mWidth = width;
    mHeight = height;

    return true;
}

void Renderer::terminateDisplay() {
	 if (mDisplay != EGL_NO_DISPLAY) {
		eglMakeCurrent(mDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
		if (mContext != EGL_NO_CONTEXT) {
			eglDestroyContext(mDisplay, mContext);
		}
		if (mSurface != EGL_NO_SURFACE) {
			eglDestroySurface(mDisplay, mSurface);
		}
		eglTerminate(mDisplay);
	}
    
    mDisplay = EGL_NO_DISPLAY;
    mSurface = EGL_NO_SURFACE;
    mContext = EGL_NO_CONTEXT;
}

void *Renderer::startRenderThread(void *arg) {
	Renderer *thisRenderer = (Renderer*)arg;
	thisRenderer->renderThreadRun();
}

