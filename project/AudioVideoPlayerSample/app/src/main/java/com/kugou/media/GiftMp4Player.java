package com.kugou.media;

import android.app.Activity;
import android.widget.Toast;

import com.kugou.widget.MP4GLRender;
import com.kugou.widget.MP4GLSurfaceView;

import java.io.File;
import java.lang.ref.WeakReference;

public class GiftMp4Player implements IGiftMp4Player {
    private static GiftMp4Player instance;
    public static GiftMp4Player getInstance() {
        if (instance == null) {
            instance = new GiftMp4Player();
        }
        return instance;
    }

    private GiftMp4Player(){

    }

    private EventCallBack callBack;
    private WeakReference<Activity> mCurrentActivity;
    private String mLocalMp4ResPath;
    private MP4GLSurfaceView mGLSurfaceView;
    private MP4GLRender mGLRender;
    private MediaContentProducer mMediaProducer;

    private void initGLSurfaceView(Activity activity) {
        mCurrentActivity = new WeakReference<Activity>(activity);

        mGLSurfaceView = new MP4GLSurfaceView(activity);
        mGLRender = new MP4GLRender(mGLSurfaceView);
        mGLSurfaceView.setRenderer(mGLRender);
        activity.setContentView(mGLSurfaceView);
    }

    private void initMediaPlayer() {
        mMediaProducer = new MediaContentProducer(mGLRender, null, new IFrameCallback() {
            @Override
            public void onPrepared(Boolean canHardWareDecode) {
                Activity activity = mCurrentActivity.get();
                if ((activity != null) && !activity.isFinishing()) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, "开始播放", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                mMediaProducer.play();
            }

            @Override
            public void onFinished() {
                Activity activity = mCurrentActivity.get();
                if ((activity != null) && !activity.isFinishing()) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, "结束播放", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public boolean onFrameAvailable(long presentationTimeUs) {
                return false;
            }
        });
    }

    @Override
    public void startGift(Activity activity, String localMp4ResPath, int loops, EventCallBack callBack) {
        this.callBack = callBack;
        final File fd = new File(localMp4ResPath);
        if (!fd.exists()) {
            callBack.onErrorOccur(EventCallBack.ERROR_RES_NOT_EXIT);
        } else {
            this.mLocalMp4ResPath = localMp4ResPath;
            initGLSurfaceView(activity);
            initMediaPlayer();

            mMediaProducer.prepare(localMp4ResPath);
        }
    }

    @Override
    public void addLoops(int loops) {

    }

    @Override
    public void pauseGift() {
        if (mMediaProducer != null) {
            mMediaProducer.pause();
        }
    }

    @Override
    public void resumeGift() {
        if (mMediaProducer != null) {
            mMediaProducer.resume();
        }
    }

    @Override
    public void stopGift() {
        if (mMediaProducer != null) {
            mMediaProducer.release();
            mMediaProducer = null;
        }
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
