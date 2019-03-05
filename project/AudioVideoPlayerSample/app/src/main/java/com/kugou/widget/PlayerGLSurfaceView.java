package com.kugou.widget;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.Surface;

import com.kugou.glutils.GLDrawer2D;
import com.kugou.media.IYUVDataReceiver;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PlayerGLSurfaceView extends GLSurfaceView implements AspectRatioViewInterface {

    private double mRequestedAspect = -1.0;
    private Render mRender;

    public PlayerGLSurfaceView(Context context) {
        super(context);
        configSurface();
    }

    public PlayerGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        configSurface();
    }

    private void configSurface() {
        setEGLContextClientVersion(2);

        setZOrderOnTop(true);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.RGBA_8888);

        mRender = new Render(false);
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

    /**用于硬解，获取输入用surface，传给MediaCodec**/
    public Surface getInputSurface(){
        if (mRender != null)
            return mRender.getInputSurface();
        else
            return null;
    }

    public IYUVDataReceiver getYUVReceiver() {
        return mRender != null ? mRender : null;
    }

    private class Render implements GLSurfaceView.Renderer, IYUVDataReceiver {
        private int mExternalTexId = -1;
        private GLDrawer2D mOutputVideoFrame;
        private SurfaceTexture mInputSurfaceTexture;
        private ByteBuffer mYUVData;
        private Surface mInputSurface;
        private boolean mSupportHWDecode;
        private Object locker = new Object();

        public Render(boolean supportHWDecode) {
            super();
            this.mSupportHWDecode = supportHWDecode;
        }
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            mOutputVideoFrame = new GLDrawer2D();

            if (mSupportHWDecode) { //初始化硬解的外部纹理
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
            } else {

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
            if (mSupportHWDecode) { //硬解渲染
                if(mInputSurfaceTexture != null){
                    mInputSurfaceTexture.updateTexImage();

                    if (mOutputVideoFrame != null) {
                        mOutputVideoFrame.draw(mExternalTexId, null);
                    }
                }
            } else { //软解渲染

            }

        }

        public Surface getInputSurface(){
            if (mSupportHWDecode) {
                synchronized (locker) {
                    while (mInputSurface == null) {
                        try {
                            locker.wait();
                        } catch (InterruptedException e) {

                        }
                    }
                }
                return mInputSurface;
            } else {
                return null;
            }
        }

        @Override
        public void onYUVData(ByteBuffer yuvData, int frameWidth, int frameHeight, int outputSize) {
            //软解时用于接收解码后的yuv数据，该方法在解码子线程中执行
            synchronized (locker) {
                if (mYUVData == null) {
                    mYUVData = ByteBuffer.allocate(outputSize);
                }
                mYUVData.rewind();
                mYUVData.put(yuvData);
                requestRender();
            }
        }
    }

}
