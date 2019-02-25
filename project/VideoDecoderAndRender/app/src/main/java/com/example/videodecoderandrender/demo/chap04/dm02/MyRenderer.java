package com.example.videodecoderandrender.demo.chap04.dm02;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.example.videodecoderandrender.R;

public class MyRenderer implements GLSurfaceView.Renderer{
    public float mAngleX;
    public float mAngleY;
    private Context mContext;
    public MyRenderer(Context pContext) {
    	super();
    	mContext = pContext;
    }
	//Called once to set up the view's OpenGL ES environment.
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		String vetexShaderStr = LoadShaderStr(mContext, R.raw.vshader);
		String fragmentShaderStr = LoadShaderStr(mContext, R.raw.fshader);
		naInitGL20(vetexShaderStr, fragmentShaderStr);
	}
	//Called for each redraw of the view.
	@Override
	public void onDrawFrame(GL10 gl) {
		naDrawGraphics(mAngleX, mAngleY);
	}
	//Called if the geometry of the view changes, for example when the device's 
	//screen orientation changes.
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		naSurfaceChanged(width, height);
	}
	
	private String LoadShaderStr(Context context, int resId) {
		StringBuffer strBuf = new StringBuffer();
		try {
			InputStream inputStream = context.getResources().openRawResource(resId);
			// setup Bufferedreader
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
			String read = in.readLine();
			while (read != null) {
				strBuf.append(read + "\n");
				read = in.readLine();
			}
			strBuf.deleteCharAt(strBuf.length() - 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strBuf.toString();
	}
	
    private static native void naInitGL20(String vertexShaderStr, String fragmentShaderStr);
    private static native void naDrawGraphics(float angleX, float angleY);
    private static native void naSurfaceChanged(int width, int height);
}
