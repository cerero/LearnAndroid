package com.kugou.media;

import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;

import com.kugou.util.LogWrapper;
import com.kugou.widget.MP4GLRender;
import com.kugou.widget.MP4GLSurfaceView;

import java.io.File;

public class GiftMp4Player implements IMP4Player {
    private static final String TAG = "GiftMp4Player";
    /**播放器内部状态**/
    private int mInnerStatus = EventCallBack.STATE_NONE;
    /**外部调用方状态**/
    private int mOuterStatus = EventCallBack.STATE_NONE;
    private final Object mLock = new Object();
    private EventCallBack mCallBack;
    private ViewGroup mParent;
    private Handler handler = new Handler(Looper.getMainLooper());
    private String mLocalMp4ResPath;
    private MP4GLSurfaceView mGLSurfaceView;
    private MP4GLRender mGLRender;
    private MediaContentProducer mContentProducer;
    private int mLoops;

    public GiftMp4Player(ViewGroup parent){
        LogWrapper.LOG_LEVEL = LogWrapper.MY_LOG_LEVEL_VERBOSE;
        this.mParent = parent;
        initGLSurfaceView();
        initContentProducer();
    }

    private void initGLSurfaceView() {
        mGLSurfaceView = new MP4GLSurfaceView(mParent.getContext());
        mGLRender = new MP4GLRender(mGLSurfaceView, new IErrorReceiver() {
            @Override
            public void onError(int code, String desc) {
                onErrorOccur(code, desc);
            }
        });
        mGLSurfaceView.setRenderer(mGLRender);
        mParent.addView(mGLSurfaceView);

    }

    private void initContentProducer() {
        mContentProducer = new MediaContentProducer(mGLRender, null, new IFrameCallback() {
            @Override
            public void onFinishing() {
                synchronized (mLock) {
                    mLoops--;
                    LogWrapper.LOGD(TAG, "onFinishing mLoops=" + mLoops);
                    if (mLoops < 1) {
                        mInnerStatus = EventCallBack.STATE_FINISHING;
                        notifyExternalStatus();
                    } else {
                        if (mContentProducer != null) {
                            mContentProducer.play();
                        }
                    }
                }
            }

            @Override
            public void onPrepared(Boolean canHardWareDecode) {
                LogWrapper.LOGE(TAG, "onPrepared canHardWareDecode:" + canHardWareDecode);
                if (mContentProducer != null) {
                    mContentProducer.play();
                }
            }

            @Override
            public void onFinished() {
                synchronized (mLock) {
                    LogWrapper.LOGD(TAG, "onFinished");
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
                    LogWrapper.LOGD(TAG, "onStart");
                    mInnerStatus = EventCallBack.STATE_START;
                    notifyExternalStatus();
                }
            }

        }, new IErrorReceiver() {
            @Override
            public void onError(int code, String desc) {
                onErrorOccur(code, desc);
            }
        });
    }

    private void onErrorOccur(final int errorId, final String desc) {
        if (mCallBack != null) {
            final IMP4Player.EventCallBack tmpCallBack = mCallBack;
            mCallBack = null;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    tmpCallBack.onErrorOccur(errorId, desc);
                }
            });
        }
    }

    private void notifyExternalStatus() {
        if (mContentProducer == null || mCallBack == null)
            return;

        if (mInnerStatus == EventCallBack.STATE_FINISHING && mOuterStatus == EventCallBack.STATE_FINISHING) {
            //外部已经是finishing状态了，直接直接切换到 finished状态
            mContentProducer.stop();
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    synchronized (mLock) {
                        if (mCallBack != null && mInnerStatus > mOuterStatus) {
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
        synchronized (mLock) {
            if (mInnerStatus != EventCallBack.STATE_FINISHED && mInnerStatus != EventCallBack.STATE_NONE) {
                LogWrapper.LOGI(TAG, "start in wrong state, current state:" + mInnerStatus);
                return;
            }
        }

        if (mContentProducer == null)
            return;

        mCallBack = callBack;
        final File fd = new File(localMp4ResPath);
        mOuterStatus = EventCallBack.STATE_NONE;
        mInnerStatus = EventCallBack.STATE_NONE;
        if (!fd.exists() || !fd.canRead()) {
            mCallBack.onErrorOccur(EventCallBack.ERROR_RES_NOT_EXIT, localMp4ResPath + " not exit");
        } else {
            synchronized (mLock) {
                this.mLocalMp4ResPath = localMp4ResPath;
                mLoops = loops;
                LogWrapper.LOGD(TAG, "start mLoops: " + mLoops);
                mContentProducer.prepare(localMp4ResPath);
            }
        }
    }

    @Override
    public void addLoops(int loops) {
        if (mContentProducer == null)
            return;

        synchronized (mLock) {
            if (mInnerStatus == EventCallBack.STATE_START || mInnerStatus == EventCallBack.STATE_FINISHING) {
                mLoops += loops;
//                LogWrapper.LOGD(TAG, "addLoops: mLoops=" + mLoops);
                if (mInnerStatus == EventCallBack.STATE_FINISHING) {//由finishing切换到start
                    mContentProducer.play();
                }
            }
        }
    }

    @Override
    public void stop() {
        if (mContentProducer == null)
            return;

        synchronized (mLock) {
//            mLoops = 1;
//            mContentProducer.finishing();
            if (mInnerStatus == EventCallBack.STATE_START) {
                mLoops = 1;
                mContentProducer.finishing();
            }
//            else {
//                LogWrapper.LOGI(TAG, "stop in wrong statestop in wrong state, current state:" + mInnerStatus);
//            }
        }
    }

    @Override
    public void confirmStatus(int status) {
        if (mContentProducer == null)
            return;

        synchronized (mLock) {
            mOuterStatus = status;
            if (mOuterStatus == EventCallBack.STATE_FINISHING) {
                if (mLoops < 1) { //播放次数为0的情况下，才能执行切换到finished，防止漏掉连接礼物
                    LogWrapper.LOGI(TAG, "confirmStatus goto finished mLoops:" + mLoops);
                    mContentProducer.stop();
                }
            } else if (mOuterStatus == EventCallBack.STATE_FINISHED) {
                mCallBack = null;
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
        synchronized (mLock) {
            if (mGLSurfaceView != null) {
                mParent.removeView(mGLSurfaceView);
                mGLSurfaceView = null;
            }
            destroyContentProducer();
            mCallBack = null;
            mParent = null;
        }
    }
}
