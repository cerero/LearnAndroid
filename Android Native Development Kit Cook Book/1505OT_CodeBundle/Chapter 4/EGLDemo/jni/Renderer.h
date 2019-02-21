#ifndef RENDERER_H
#define RENDERER_H

#include <pthread.h>
#include <EGL/egl.h>
#include <GLES/gl.h>


class Renderer {

public:
    Renderer();
    virtual ~Renderer();
    void start();
    void stop();
    void initEGLAndOpenGL1x(ANativeWindow* window);
    void renderAFrame(float x, float y);
    
private:
    enum RenderThreadEvent {
    	RTE_NONE,				//do nothing
    	RTE_SURFACE_CREATE,		//simulate onSurfaceCreated event
        RTE_SURFACE_CHANGE,		//simulate onSurfaceChanged event
        RTE_DRAW_FRAME,			//simulate onDrawFrame event
        RTE_EXIT
    };
    enum RenderThreadEvent mRendererEvent;

    int mWidth;
    int mHeight;

    float mAngleX, mAngleY;

    pthread_t mThreadId;
    pthread_mutex_t mMutex;
    pthread_cond_t mCondVar;
    
    ANativeWindow* mWindow;

    EGLDisplay mDisplay;
    EGLSurface mSurface;
    EGLContext mContext;
    
    void renderThreadRun();
    bool initDisplay();
    void terminateDisplay();
    static void *startRenderThread(void *arg);	//helper function to use with pthread_create
};

#endif
