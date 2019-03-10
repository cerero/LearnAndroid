package com.kugou.widget;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;

public class MP4GLSurfaceView extends GLSurfaceView {
    public MP4GLSurfaceView(Context context) {
        super(context);
    }

    @Override
    public void setRenderer(Renderer renderer) {
        setEGLContextClientVersion(2);
        setZOrderOnTop(true);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.RGBA_8888);

        super.setRenderer(renderer);

        this.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}
