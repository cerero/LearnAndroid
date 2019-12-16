package com.kugou.widget;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.Surface;
import android.view.ViewGroup;

import com.kugou.glutils.GLDrawer2D;
import com.kugou.media.IErrorReceiver;
import com.kugou.media.IMP4Player;
import com.kugou.media.IVideoConsumer;
import com.kugou.util.LogWrapper;
import com.kugou.util.MatrixUtils;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MP4GLRender implements GLSurfaceView.Renderer, IVideoConsumer {
    private String TAG = "MP4GLRender";
    //1 - 硬解  2 - 软解
    private int mRenderMode = -1;

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
    private int mCropImgWidth;
    private int mCropImgHeight;
    private float mWidthScale = 1.0f;
    private float mHeightScale = 1.0f;
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

    private IErrorReceiver mErrorReceiver;

    public MP4GLRender(GLSurfaceView surfaceView, IErrorReceiver errorReceiver) {
        this.mSurfacdeView = surfaceView;
        this.mErrorReceiver = errorReceiver;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        if (hasSurfaceCreate) { //重新创建的opengl环境，需恢复资源
            LogWrapper.LOGD(TAG, "onSurfaceReCreated");
        } else { //首次创建的opengl环境
            LogWrapper.LOGD(TAG, "onSurfaceCreated");
            hasSurfaceCreate = true;
        }

        GLES20.glDisable(GLES20.GL_DITHER);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        if (hasChoseMode) {
            initRenderStuff();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        LogWrapper.LOGD(TAG, "onSurfaceChanged width:" + width + ",height:" + height);
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
        clearScreen();

//        if (triggerRelease) {
//            doRelease();
//            return;
//        }

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

        if (isSurfaceChanged || isTextureChanged) {//surface尺寸变更或纹理大小改变，重置ratio
            if (mRenderMode == 1) {
                if (mHardDecodeFrame != null) {
                    mHardDecodeFrame.active(mExternalTexId, mYTexId, mUTexId, mVTexId);
                    mHardDecodeFrame.onViewPortChange(mCropImgWidth, mCropImgHeight, mViewWidth, mViewHeight);
                }
            } else {
                if (mSoftDecodeFrame != null){
                    mSoftDecodeFrame.active(mExternalTexId, mYTexId, mUTexId, mVTexId);
                    mSoftDecodeFrame.onViewPortChange(mImgWidth, mImgHeight, mViewWidth, mViewHeight);
                }
            }
            isSurfaceChanged &= true;
            isTextureChanged &= true;
        }

        if (mRenderMode == 1) { //硬解渲染
            if(mInputSurfaceTexture != null){
                if (isVisible && mHardDecodeFrame != null) {
                    float[] textureMat = MatrixUtils.getOriginalMatrix();
                    MatrixUtils.scale(textureMat, mWidthScale, mHeightScale);
                    mHardDecodeFrame.drawExternalTex(mExternalTexId, textureMat);
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
        LogWrapper.LOGD(TAG, "choseRenderMode mode:" + mode);
        mRenderMode = mode;
        hasChoseMode = true;
        isTextureChanged = true;
        if (mSurfacdeView != null)
            mSurfacdeView.requestRender();
    }

    private void initRenderStuff() {
        LogWrapper.LOGD(TAG, "initRenderStuff");
        //创建硬解用的
        int[] compileRet = {-1, -1};
        int shaderProgram = 0;
        Boolean hasError = false;
        shaderProgram = GLDrawer2D.loadShader(GLDrawer2D.vss, GLDrawer2D.fss, compileRet);
        if (compileRet[0] != 0) {
            LogWrapper.LOGE(TAG, "硬解 vertext shader编译失败");
            hasError = true;
            mErrorReceiver.onError(IMP4Player.EventCallBack.ERROR_SHADER_FAIL, "硬解 vertext shader编译失败");
        } else if (compileRet[1] != 0) {
            LogWrapper.LOGE(TAG, "硬解 fragment shader编译失败");
            hasError = true;
            mErrorReceiver.onError(IMP4Player.EventCallBack.ERROR_SHADER_FAIL, "硬解 fragment shader编译失败");
        }
        mHardDecodeFrame = new GLDrawer2D(true, shaderProgram);

        //创建软解用的
        shaderProgram = GLDrawer2D.loadShader(GLDrawer2D.vss, GLDrawer2D.yuvFSS, compileRet);
        if (compileRet[0] != 0) {
            LogWrapper.LOGE(TAG, "软解 vertext shader编译失败");
            hasError = true;
            mErrorReceiver.onError(IMP4Player.EventCallBack.ERROR_SHADER_FAIL, "软解 vertext shader编译失败");
        } else if (compileRet[1] != 0) {
            LogWrapper.LOGE(TAG, "软解 fragment shader编译失败");
            hasError = true;
            mErrorReceiver.onError(IMP4Player.EventCallBack.ERROR_SHADER_FAIL, "软解 fragment shader编译失败");
        }

        if (hasError) {//shader错误，不继续执行
            hasInit = false;
            hasChoseMode = false;
            return;
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
//    public void onYUVData(ByteBuffer yuvData, int frameWidth, int frameHeight) {
    public void onYUVData(ByteBuffer yData, ByteBuffer uData, ByteBuffer vData, int frameWidth, int frameHeight) {
//        LogWrapper.LOGD(TAG, "onYUVData width=" + frameWidth + ", height=" + frameHeight);
        //软解时用于接收解码后的yuv数据，该方法在解码线程中执行
        synchronized (locker) {
            if (mYBuffer == null) {
                mYWidth = frameWidth;
                mYHeight = frameHeight;

                mUVWidth = frameWidth / 2;
                mUVHeight = frameHeight / 2;

                mResolution = mYWidth * mYHeight;
                mUorVResolution = mResolution >> 2;
            }
            mYBuffer = yData;
            mUBuffer = uData;
            mVBuffer = vData;
            mSurfacdeView.requestRender();

//            try {
//                locker.wait();//等待渲染线程把 yuvdata渲染好
//            } catch (InterruptedException e) {
//            }
        }
    }

    @Override
    public void onTextureInfo(int imgWidth, int imgHeight) {
        LogWrapper.LOGD(TAG, "onTextureInfo imgWidth:" + imgWidth + ",imgHeight:" + imgHeight);
        if (mImgWidth != imgWidth || mImgHeight != imgHeight) {
            mCropImgWidth = mImgWidth = imgWidth;
            mCropImgHeight = mImgHeight = imgHeight;
            mWidthScale = 1.0f;
            mHeightScale = 1.0f;
            synchronized (locker) {
                isTextureChanged = true;
                mYBuffer = null;
                mUBuffer = null;
                mVBuffer = null;
            }
        }
    }

    @Override
    public void onTextureActualSizeChange(int cropImgWidth, int cropImgHeight, int alignWidth, int alignHeight) {
        if (cropImgWidth > 0 && cropImgHeight > 0 && (cropImgWidth != mImgWidth || cropImgHeight != mImgHeight)) {
            mImgWidth = alignWidth == 0 ? mImgWidth : alignWidth;
            mImgHeight = alignHeight == 0 ? mImgHeight : alignHeight;
            mCropImgWidth = cropImgWidth;
            mCropImgHeight = cropImgHeight;
            isTextureChanged = true;
            mWidthScale = (float) mCropImgWidth / mImgWidth;
            mHeightScale = (float) mCropImgHeight / mImgHeight;
            LogWrapper.LOGD(TAG, "onTextureActualSizeChange:  \n\t\tcropImgWidth=" + cropImgWidth + ", cropImgHeight=" + cropImgHeight + ", alignWidth=" + alignWidth + ", alignHeight=" + alignHeight + ",widthScale=" + mWidthScale + ", heightScale=" + mHeightScale);
        }
    }

    @Override
    public void end() {
        LogWrapper.LOGD(TAG, "end()");
        isEOS = true;
        mSurfacdeView.requestRender();
    }

    private void clearScreen() {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }
    @Override
    public void start() {
        LogWrapper.LOGD(TAG, "start()");
        isEOS = false;
    }

    public void setVisible(Boolean isVisible) {
        this.isVisible = isVisible;
    }

    @Override
    public Surface generateHardWareOutputSurface() {
        LogWrapper.LOGD(TAG, "generateHardWareOutputSurface()");
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
            triggerRelease = true;
            doRelease();
//            mSurfacdeView.requestRender();
        }
    }

    private void doRelease() {
        LogWrapper.LOGI(TAG, "doRelease opgnelES res");
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

        ViewGroup parent = (ViewGroup) mSurfacdeView.getParent();
        if (parent!=null) {
            parent.removeView(mSurfacdeView);
        }
        mYBuffer = null;
        mUBuffer = null;
        mVBuffer = null;
        mSurfacdeView = null;
        mErrorReceiver = null;
    }
}
