package com.example.testffmpeg.renderer;

import android.app.Activity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.testffmpeg.decoder.VideoDecoder;

public class VideoRender implements SurfaceHolder.Callback{
    private VideoDecoder mVideoDecoder;
    private Activity mParent;
    private SurfaceView mSurfaceView;
    private String mFilePath = null;
    public VideoRender(Activity parent, String filePath) {

        this.mParent = parent;
        this.mFilePath = filePath;
        this.mSurfaceView = new SurfaceView(parent);
        this.mSurfaceView.getHolder().addCallback(this);
        this.mParent.setContentView(this.mSurfaceView);

        mVideoDecoder = new VideoDecoder();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mVideoDecoder != null) {
            if (mVideoDecoder.initWithFilePath(holder.getSurface(), mFilePath)) {
                mVideoDecoder.start();

            } else {
                mVideoDecoder = null;
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mVideoDecoder != null) {
            mVideoDecoder.close();
        }
    }
}
