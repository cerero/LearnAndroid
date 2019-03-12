package com.kugou.media;

import android.app.Activity;
import android.nfc.Tag;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kugou.widget.MP4GLRender;
import com.kugou.widget.MP4GLSurfaceView;

import java.io.File;

public class GiftMp4Player implements IMP4Player {
    private static final String TAG = "GiftMp4Player";
    /**播放器内部状态**/
    private int mInnerStatus = EventCallBack.STATE_NONE;
    /**外部调用方状态**/
    private int mOuterStatus = EventCallBack.STATE_NONE;
    private Object mLock = new Object();
    private EventCallBack mCallBack;
    private ViewGroup mParent;
    private Activity mActivity;
    private String mLocalMp4ResPath;
    private MP4GLSurfaceView mGLSurfaceView;
    private MP4GLRender mGLRender;
    private MediaContentProducer mContentProducer;
    private int mLoops = 1;

    public GiftMp4Player(ViewGroup parent){
        this.mParent = parent;
        this.mActivity = (Activity) mParent.getContext();
        initGLSurfaceView();
        initContentProducer();
    }

    private void initGLSurfaceView() {
        mGLSurfaceView = new MP4GLSurfaceView(mParent.getContext());
        mGLRender = new MP4GLRender(mGLSurfaceView);
        mGLSurfaceView.setRenderer(mGLRender);
        mParent.addView(mGLSurfaceView);
    }

    private void initContentProducer() {
        mContentProducer = new MediaContentProducer(mGLRender, null, new IFrameCallback() {
            @Override
            public void onFinishing() {
                synchronized (mLock) {
                    Log.d(TAG, "onFinishing");
                    mLoops--;
                    if (mLoops < 1) {
                        mInnerStatus = EventCallBack.STATE_FINISHING;
                        notifyExternalStatus();
                    } else {
                        mContentProducer.play();
                    }
                }
            }

            @Override
            public void onPrepared(Boolean canHardWareDecode) {
                Log.e(TAG, "onPrepared canHardWareDecode:" + canHardWareDecode);
                mContentProducer.play();
            }

            @Override
            public void onFinished() {
                synchronized (mLock) {
                    Log.d(TAG, "onFinished");
                    mInnerStatus = EventCallBack.STATE_FINISHED;
                    notifyExternalStatus();
                }
            }

            @Override
            public boolean onFrameAvailable(long presentationTimeUs) {
                return false;
            }

            @Override
            public void onStart() {
                synchronized (mLock) {
                    Log.d(TAG, "onStart");
                    mInnerStatus = EventCallBack.STATE_START;
                    notifyExternalStatus();
                }
            }
        });
    }

    private void notifyExternalStatus() {
        if (mInnerStatus == EventCallBack.STATE_FINISHING && mOuterStatus == EventCallBack.STATE_FINISHING) {
            //外部已经是finishing状态了，直接直接切换到 finished状态
            mContentProducer.stop();
        } else {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    synchronized (mLock) {
                        if (mInnerStatus > mOuterStatus) {
                            mCallBack.onStatusChange(mInnerStatus);
                        }
                    }
                }
            });
        }
    }

    private void destroyContentProducer() {
        if (mContentProducer != null) {
            mContentProducer.release();
            mContentProducer = null;
        }
    }

    @Override
    public void start(String localMp4ResPath, int loops, EventCallBack callBack) {
        mCallBack = callBack;
        final File fd = new File(localMp4ResPath);
        mOuterStatus = EventCallBack.STATE_NONE;
        mInnerStatus = EventCallBack.STATE_NONE;
        if (!fd.exists()) {
            mCallBack.onErrorOccur(EventCallBack.ERROR_RES_NOT_EXIT, localMp4ResPath + " not exit");
        } else {
            synchronized (mLock) {
                if (mInnerStatus == EventCallBack.STATE_NONE || mInnerStatus == EventCallBack.STATE_FINISHED) {
                    this.mLocalMp4ResPath = localMp4ResPath;
                    mLoops = loops;
                    mContentProducer.prepare(localMp4ResPath);
                } else {
                    Log.i(TAG, "start in wrong state, current state:" + mInnerStatus);
                }
            }
        }
    }

    @Override
    public Boolean addLoops(int loops) {
        Boolean stateValid = false;
        synchronized (mLock) {
            if (mInnerStatus == EventCallBack.STATE_START || mInnerStatus == EventCallBack.STATE_FINISHING) {
                stateValid = true;
                mLoops += loops;
                if (mInnerStatus == EventCallBack.STATE_FINISHING) {//由finishing切换到start
                    mContentProducer.play();
                }
            } else {
                Log.i(TAG, "addLoops in wrong state, current state:" + mInnerStatus);
            }
        }
        return stateValid;
    }

    @Override
    public void stop() {
        synchronized (mLock) {
            if (mInnerStatus == EventCallBack.STATE_START) {
                mLoops = 0;
                mContentProducer.finishing();
            } else {
                Log.i(TAG, "stop in wrong statestop in wrong state, current state:" + mInnerStatus);
            }
        }
    }

    @Override
    public void confirmStatus(int status) {
        synchronized (mLock) {
            mOuterStatus = status;
            if (mOuterStatus == EventCallBack.STATE_FINISHING) {
                if (mInnerStatus == EventCallBack.STATE_FINISHING) {//等待外部确认 finishing后，才能执行stop
                    if (mLoops < 1) { //播放次数为0的情况下，才能执行切换到finished，防止漏掉连接礼物
                        mContentProducer.stop();
                    }
                }
            }
        }
    }

    @Override
    public void setVisible(Boolean val) {
        mGLRender.setVisible(val);
    }

    @Override
    public void onActivityStop() {

    }

    @Override
    public void onActivityResume() {

    }

    @Override
    public void release() {

    }
}
