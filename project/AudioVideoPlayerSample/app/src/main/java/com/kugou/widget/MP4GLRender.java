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
    //1 - 硬解  2 - 软解
    private int mRenderMode;

    private GLDrawer2D mHardDecodeFrame;
    private GLDrawer2D mSoftDecodeFrame;

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

    private int mViewWidth;
    private int mViewHeight;
    private int mImgWidth;
    private int mImgHeight;

    private ByteBuffer mYBuffer;
    private ByteBuffer mUBuffer;
    private ByteBuffer mVBuffer;

    private float mViewRatio;

    private Object locker = new Object();
    private GLSurfaceView mSurfacdeView;

    private Boolean hasSurfaceCreate = false;
    private Boolean hasChoseMode = false;
    private Boolean hasInit = false;
    private Boolean isEOS = false;
    private Boolean isVisible = true;
    private Boolean triggerRelease = false;

    private Boolean isSurfaceChanged = false;
    private Boolean isTextureChanged = false;

    public MP4GLRender(GLSurfaceView surfaceView) {
        this.mSurfacdeView = surfaceView;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        if (hasSurfaceCreate) { //重新创建的opengl环境，需恢复资源
            Log.d(TAG, "onSurfaceReCreated");
        } else { //首次创建的opengl环境
            Log.d(TAG, "onSurfaceCreated");
            hasSurfaceCreate = true;
        }

        if (hasChoseMode) {
            initRenderStuff();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "onSurfaceChanged width:" + width + ",height:" + height);
        GLES20.glViewport(0, 0, width, height);
        if (mViewWidth != width || mViewHeight != height) {
            mViewWidth = width;
            mViewHeight = height;
            synchronized (locker) {
                isSurfaceChanged = true;
            }
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
//        if (triggerRelease) {
//            doRelease();
//            triggerRelease = false;
//            return;
//        }

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        if (!hasInit && hasChoseMode && hasSurfaceCreate) {
            initRenderStuff();
        }

        if (!hasInit) {
            return;
        }

        if (mRenderMode == 1) {
            if (mInputSurfaceTexture != null) {//每次draw调用，都消费一下硬解surface的图像数据
                mInputSurfaceTexture.updateTexImage();
            }
        }

        if (isEOS) {
            return;
        }

        if (isSurfaceChanged || isTextureChanged) {
            if (mHardDecodeFrame != null) mHardDecodeFrame.onViewPortChange(mImgWidth, mImgHeight, mViewWidth, mViewHeight);
            if (mSoftDecodeFrame != null) mSoftDecodeFrame.onViewPortChange(mImgWidth, mImgHeight, mViewWidth, mViewHeight);
            isSurfaceChanged &= true;
            isTextureChanged &= true;
        }

        if (mRenderMode == 1) { //硬解渲染
            if(mInputSurfaceTexture != null){
                if (isVisible && mHardDecodeFrame != null) {
                    mHardDecodeFrame.drawExternalTex(mExternalTexId, null);
                }
            }
        } else { //软解渲染
            synchronized (locker) {
                if (isVisible && mSoftDecodeFrame != null && mYBuffer != null && mUBuffer != null && mVBuffer != null) {
                    mSoftDecodeFrame.drawYUVTex(mYTexId, mUTexId, mVTexId, mYBuffer, mUBuffer, mVBuffer, mYWidth, mYHeight, mUVWidth, mUVHeight, null);
                }
                //draw完后，通知video解码线程继续执行
                locker.notifyAll();
            }
        }
    }

    @Override
    public void choseRenderMode(int mode) {
        Log.d(TAG, "choseRenderMode mode:" + mode);
        mRenderMode = mode;
        hasChoseMode = true;
        if (mSurfacdeView != null)
            mSurfacdeView.requestRender();
    }

    private void initRenderStuff() {
        Log.d(TAG, "initRenderStuff");
        //创建硬解用的
        int[] compileRet = {-1, -1};
        int shaderProgram = 0;

        shaderProgram = GLDrawer2D.loadShader(GLDrawer2D.vss, GLDrawer2D.fss, compileRet);
        if (compileRet[0] != 0) {
            Log.e(TAG, "硬解 vertext shader编译失败");
        } else if (compileRet[1] != 0) {
            Log.e(TAG, "硬解 fragment shader编译失败");
        }
        mHardDecodeFrame = new GLDrawer2D(true, shaderProgram);

        //创建软解用的
        shaderProgram = GLDrawer2D.loadShader(GLDrawer2D.vss, GLDrawer2D.yuvFSS, compileRet);
        if (compileRet[0] != 0) {
            Log.e(TAG, "软解 vertext shader编译失败");
        } else if (compileRet[1] != 0) {
            Log.e(TAG, "软解 fragment shader编译失败");
        }
        mSoftDecodeFrame = new GLDrawer2D(false, shaderProgram);

        mYTexId = GLDrawer2D.initTex(GLES20.GL_TEXTURE0);
        mUTexId = GLDrawer2D.initTex(GLES20.GL_TEXTURE1);
        mVTexId = GLDrawer2D.initTex(GLES20.GL_TEXTURE2);
        mExternalTexId = GLDrawer2D.initExternalOESTex();

        if (mInputSurfaceTexture != null) {
            mInputSurfaceTexture.release();
        }
        mInputSurfaceTexture = new SurfaceTexture(mExternalTexId);
        mInputSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                if (mSurfacdeView != null)
                    mSurfacdeView.requestRender();
            }
        });
        if (mInputSurface != null) {
            mInputSurface.release();
        }
        synchronized (locker) {
            //实例创建后,notify到调用方
            mInputSurface = new Surface(mInputSurfaceTexture);
            locker.notifyAll();
        }

        hasInit = true;
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

                mYBuffer = ByteBuffer.allocateDirect(mResolution);
                mYBuffer.order(yuvData.order());

                mUBuffer = ByteBuffer.allocateDirect(mUorVResolution);
                mUBuffer.order(yuvData.order());

                mVBuffer = ByteBuffer.allocateDirect(mUorVResolution);
                mVBuffer.order(yuvData.order());
            }

            mYBuffer.put(yuvData.array(), 0, mResolution).flip();
            mUBuffer.put(yuvData.array(), mResolution, mUorVResolution).flip();
            mVBuffer.put(yuvData.array(), mResolution + mUorVResolution, mUorVResolution).flip();

            mSurfacdeView.requestRender();

//            try {
//                locker.wait();//等待渲染线程把 yuvdata渲染好
//            } catch (InterruptedException e) {
//            }
        }
    }

    @Override
    public void onTextureInfo(int imgWidth, int imgHeight) {
        Log.d(TAG, "onTextureInfo imgWidth:" + imgWidth + ",imgHeight:" + imgHeight);
        if (mImgWidth != imgWidth || mImgHeight != imgHeight) {
            mImgWidth = imgWidth;
            mImgHeight = imgHeight;
            synchronized (locker) {
                isTextureChanged = true;
                mYBuffer = null;
                mUBuffer = null;
                mVBuffer = null;
            }
        }
    }

    @Override
    public void end() {
        Log.d(TAG, "end()");
        isEOS = true;
        //执行一次清屏
        mSurfacdeView.requestRender();
    }

    @Override
    public void start() {
        Log.d(TAG, "start()");
        isEOS = false;
    }

    public void setVisible(Boolean isVisible) {
        this.isVisible = isVisible;
    }

    @Override
    public Surface generateHardWareOutputSurface() {
        Log.d(TAG, "generateHardWareOutputSurface()");
        if (mRenderMode == 1) {
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
    public void release() {
        synchronized (locker) {
            Log.i(TAG, "release opgnelES res");
            triggerRelease = true;
            doRelease();
//            mSurfacdeView.requestRender();
        }
    }

    private void doRelease() {
//        Log.i(TAG, "doRelease opgnelES res");
        if (mHardDecodeFrame != null) {
            mHardDecodeFrame.release();
            mHardDecodeFrame = null;
        }

        if (mSoftDecodeFrame != null) {
            mSoftDecodeFrame.release();
            mSoftDecodeFrame = null;
        }

        if (mInputSurfaceTexture != null) {
            mInputSurfaceTexture.release();
            mInputSurfaceTexture = null;
        }

        if (mInputSurface != null) {
            mInputSurface.release();
            mInputSurface = null;
        }

        if (mExternalTexId > -1) {
            GLDrawer2D.deleteTex(mExternalTexId);
        }

        if (mYTexId > -1) {
            GLDrawer2D.deleteTex(mYTexId);
        }

        if (mUTexId > -1) {
            GLDrawer2D.deleteTex(mUTexId);
        }

        if (mVTexId > -1) {
            GLDrawer2D.deleteTex(mVTexId);
        }

        mYBuffer = null;
        mUBuffer = null;
        mVBuffer = null;
        mSurfacdeView = null;
    }
}
