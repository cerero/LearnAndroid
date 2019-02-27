package com.example.videodecoderandrender.demo.chap04.dm04;


import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class DrawTextureG1 extends Activity {
	private GLSurfaceView mGLView;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mGLView = new MySurfaceView(this);
        setContentView(mGLView);
	}
}
