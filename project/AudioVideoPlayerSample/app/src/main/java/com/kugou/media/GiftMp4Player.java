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

    /**播放器内部状态**/
    private int mInnerStatus = EventCallBack.STATE_NONE;
    /**外部调用方状态**/
    private int mOuterStatus = EventCallBack.STATE_NONE;
    private Object mLock = new Object();
    private EventCallBack mCallBack;
    private ViewGroup mParent;
//    private Activity mParent;
    private String mLocalMp4ResPath;
    private MP4GLSurfaceView mGLSurfaceView;
    private MP4GLRender mGLRender;
    private MediaContentProducer mContentProducer;

    public GiftMp4Player(ViewGroup parent, EventCallBack callBack){
//    public GiftMp4Player(Activity parent, EventCallBack callBack){
        this.mParent = parent;
        this.mCallBack = callBack;
        initGLSurfaceView();
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
            public void onPrepared(Boolean canHardWareDecode) {
                Log.d(Tag, "onPrepared canHardWareDecode:" + canHardWareDecode);
//                Activity activity = (Activity)mParent.getContext();
//                if ((activity != null) && !activity.isFinishing()) {
//                    activity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(activity, "开始播放", Toast.LENGTH_LONG).show();
//                        }
//                    });
//                }
                mContentProducer.play();
            }

            @Override
            public void onFinished() {
                synchronized (mLock) {
                    mInnerStatus = EventCallBack.STATE_FINISHED;
                }
//                Activity activity = (Activity)mParent.getContext();
//                if ((activity != null) && !activity.isFinishing()) {
//                    activity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(activity, "结束播放", Toast.LENGTH_LONG).show();
//                        }
//                    });
//                }
            }

            @Override
            public boolean onFrameAvailable(long presentationTimeUs) {
                return false;
            }
        });
    }

    private void destroyContentProducer() {
        if (mContentProducer != null) {
            mContentProducer.release();
            mContentProducer = null;
        }
    }

    @Override
    public void start(String localMp4ResPath, int loops) {
        final File fd = new File(localMp4ResPath);
        if (!fd.exists()) {
            mCallBack.onErrorOccur(EventCallBack.ERROR_RES_NOT_EXIT, localMp4ResPath + " not exit");
        } else {
            this.mLocalMp4ResPath = localMp4ResPath;
            initContentProducer();
            mContentProducer.prepare(localMp4ResPath);
        }
    }

    @Override
    public Boolean addLoops(int loops) {
        return true;
    }

    @Override
    public void pause() {
        if (mContentProducer != null) {
            mContentProducer.pause();
        }
    }

    @Override
    public void resume() {
        if (mContentProducer != null) {
            mContentProducer.resume();
        }
    }

    @Override
    public void stop() {
        if (mContentProducer != null) {
            mContentProducer.release();
            mContentProducer = null;
        }
    }

    @Override
    public void confirmStatus(int status) {

    }

    @Override
    public void setVisible(Boolean val) {

    }

    @Override
    public void onActivityStop() {

    }

    @Override
    public void onActivityResume() {

    }
}
