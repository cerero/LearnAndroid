package cookbook.chapter5.nativeassets;


import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.util.Log;

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
		naLoadTexture(mContext.getAssets());
		naInitGL1x();
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
	
	private static native void naLoadTexture(AssetManager pAssetManager);
    private static native void naInitGL1x();
    private static native void naDrawGraphics(float angleX, float angleY);
    private static native void naSurfaceChanged(int width, int height);
    
    static {
        System.loadLibrary("DiceG1NativeAssets");
    }
}
