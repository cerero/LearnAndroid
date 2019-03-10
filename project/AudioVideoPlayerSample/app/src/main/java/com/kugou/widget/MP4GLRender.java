package com.kugou.widget;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.Surface;

import com.kugou.glutils.GLDrawer2D;
import com.kugou.media.IVideoConsumer;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MP4GLRender implements GLSurfaceView.Renderer, IVideoConsumer {
    private String TAG = "MP4GLRender";
    private boolean mSupportHWDecode;

    private GLDrawer2D mOutputVideoFrame;

    //用于接收外部硬解来源的texture id
    private int mExternalTexId = -1;
    private SurfaceTexture mInputSurfaceTexture;
    private Surface mInputSurface;

    //用于软解的y部分的texture id
    private int mYTexId = -1;
    //用于软解的uv部分的texture id
    private int mUTexId = -1;
    private int mVTexId = -1;
    //y平面的宽高
    private int mYWidth;
    private int mYHeight;
    //u或n平面的宽高
    private int mUVWidth;
    private int mUVHeight;
    //yuv的分辨率
    private int mResolution;
    //u / v 各自的分辨率
    private int mUorVResolution;

    private ByteBuffer mYBuffer;
    private ByteBuffer mUBuffer;
    private ByteBuffer mVBuffer;

    private Object locker = new Object();
    private GLSurfaceView mSurfacdeView;

    private Boolean hasSurfaceCreate = false;
    private Boolean hasChoseMode = false;
    public MP4GLRender(GLSurfaceView surfaceView) {
        this.mSurfacdeView = surfaceView;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        hasSurfaceCreate = true;
        if (hasChoseMode) {
            initRenderStuff();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
//		    Matrix.frustumM(mProjectionMatrix, 0, -ratio,ratio, -1, 1, 3, test_7);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        if (mSupportHWDecode) { //硬解渲染
            if(mInputSurfaceTexture != null){
                mInputSurfaceTexture.updateTexImage();

                if (mOutputVideoFrame != null) {
                    mOutputVideoFrame.drawExternalTex(mExternalTexId, null);
                }
            }
        } else { //软解渲染
            synchronized (locker) {
                if (mOutputVideoFrame != null && mYBuffer != null && mUBuffer != null && mVBuffer != null) {
                    mOutputVideoFrame.drawYUVTex(mYTexId, mUTexId, mVTexId, mYBuffer,  mUBuffer, mVBuffer, mYWidth, mYHeight, mUVWidth, mUVHeight, null);
                }
                //draw完后，通知video解码线程继续执行
                locker.notifyAll();
            }
        }
    }

    @Override
    public void choseRenderMode(int mode) {
        mSupportHWDecode = mode == 1 ? true : false;
        hasChoseMode = true;
        if (hasSurfaceCreate) {
            initRenderStuff();
        }
    }

    private void initRenderStuff() {
        mOutputVideoFrame = new GLDrawer2D(mSupportHWDecode);

        if (mSupportHWDecode) { //初始化硬解的外部纹理
            mExternalTexId = GLDrawer2D.initExternalOESTex();
            if (mInputSurfaceTexture != null) {
                mInputSurfaceTexture.release();
            }
            mInputSurfaceTexture = new SurfaceTexture(mExternalTexId);
            mInputSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                @Override
                public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                    mSurfacdeView.requestRender();
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
            mYTexId = GLDrawer2D.initTex(GLES20.GL_TEXTURE0);
            mUTexId = GLDrawer2D.initTex(GLES20.GL_TEXTURE1);
            mVTexId = GLDrawer2D.initTex(GLES20.GL_TEXTURE2);
        }
    }

    @Override
    public void onYUVData(ByteBuffer yuvData, int frameWidth, int frameHeight) {
//        Log.d(TAG, "onYUVData width=" + frameWidth + ", height=" + frameHeight);
        //软解时用于接收解码后的yuv数据，该方法在解码线程中执行
        synchronized (locker) {
            if (mYBuffer == null) {
                mYWidth = frameWidth;
                mYHeight = frameHeight;

                mUVWidth = frameWidth / 2;
                mUVHeight = frameHeight / 2;

                mResolution = mYWidth * mYHeight;
                mUorVResolution = mResolution >> 2;

                mYBuffer = ByteBuffer.allocate(mResolution);
                mYBuffer.order(yuvData.order());

                mUBuffer = ByteBuffer.allocate(mUorVResolution);
                mUBuffer.order(yuvData.order());

                mVBuffer = ByteBuffer.allocate(mUorVResolution);
                mVBuffer.order(yuvData.order());
            }

            mYBuffer.put(yuvData.array(), 0, mResolution).flip();
            mUBuffer.put(yuvData.array(), mResolution, mUorVResolution).flip();
            mVBuffer.put(yuvData.array(), mResolution + mUorVResolution, mUorVResolution).flip();

            mSurfacdeView.requestRender();

            try {
                locker.wait();//等待渲染线程把 yuvdata渲染好
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public Surface generateHardWareOutputSurface() {
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
}
