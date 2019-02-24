package com.kugou.widget;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.Surface;

import com.kugou.glutils.GLDrawer2D;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PlayerGLSurfaceView extends GLSurfaceView implements AspectRatioViewInterface {

    private double mRequestedAspect = -1.0;
    private Render mRender;

    public PlayerGLSurfaceView(Context context) {
        super(context);
        setEGLContextClientVersion(2);
        setZOrderOnTop(true);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.RGBA_8888);
        mRender = new Render();
        setRenderer(mRender);
        this.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public PlayerGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        setZOrderOnTop(true);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.RGBA_8888);
        mRender = new Render();
        setRenderer(mRender);
        this.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mRequestedAspect > 0) {
            int initialWidth = MeasureSpec.getSize(widthMeasureSpec);
            int initialHeight = MeasureSpec.getSize(heightMeasureSpec);

            final int horizPadding = getPaddingLeft() + getPaddingRight();
            final int vertPadding = getPaddingTop() + getPaddingBottom();
            initialWidth -= horizPadding;
            initialHeight -= vertPadding;

            final double viewAspectRatio = (double)initialWidth / initialHeight;
            final double aspectDiff = mRequestedAspect / viewAspectRatio - 1;

            // stay size if the difference of calculated aspect ratio is small enough from specific value
            if (Math.abs(aspectDiff) > 0.01) {
                if (aspectDiff > 0) {
                    // adjust heght from width
                    initialHeight = (int) (initialWidth / mRequestedAspect);
                } else {
                    // adjust width from height
                    initialWidth = (int) (initialHeight * mRequestedAspect);
                }
                initialWidth += horizPadding;
                initialHeight += vertPadding;
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(initialWidth, MeasureSpec.EXACTLY);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(initialHeight, MeasureSpec.EXACTLY);
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void setAspectRatio(double aspectRatio) {
        if (aspectRatio < 0) {
            throw new IllegalArgumentException();
        }
        if (mRequestedAspect != aspectRatio) {
            mRequestedAspect = aspectRatio;
            requestLayout();
        }
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    public Surface getInputSurface(){
        if (mRender != null)
            return mRender.getInputSurface();
        else
            return null;
    }

    private class Render implements GLSurfaceView.Renderer {
        private int mExternalTexId = -1;
        private GLDrawer2D mOutputVideoFrame;
        private SurfaceTexture mInputSurfaceTexture;
        private Surface mInputSurface;
        private Object locker = new Object();
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            mOutputVideoFrame = new GLDrawer2D();
            mExternalTexId = GLDrawer2D.initTex();
            mInputSurfaceTexture = new SurfaceTexture(mExternalTexId);
            mInputSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                @Override
                public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                    requestRender();
                }
            });

            if (mInputSurface != null) {
                mInputSurface.release();
            }

            synchronized (locker) {
                mInputSurface = new Surface(mInputSurfaceTexture);
                locker.notifyAll();
            }
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
            float ratio = (float) width / height;
//		    Matrix.frustumM(mProjectionMatrix, 0, -ratio,ratio, -1, 1, 3, 7);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
//            GLES20.glClearColor(1.0f, 0.0f, 0.0f, 0.5f);
//            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            if(mInputSurfaceTexture != null){
                mInputSurfaceTexture.updateTexImage();

                if (mOutputVideoFrame != null) {
                    mOutputVideoFrame.draw(mExternalTexId, null);
                }
            }

        }

        public Surface getInputSurface(){
            synchronized (locker) {
                while (mInputSurface == null) {
                    try {
                        locker.wait();
                    } catch (InterruptedException e) {

                    }
                }
            }
            return mInputSurface;
        }
    }

}
