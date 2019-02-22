package com.example.videodecoderandrender.demo.chap04.dm01;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class MySurfaceView extends GLSurfaceView {
	private MyRenderer mRenderer;
	public MySurfaceView(Context context) {
		super(context);
		mRenderer = new MyRenderer();
		this.setRenderer(mRenderer);
		//control whether continuously drawing or on-demand
		this.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}
}
