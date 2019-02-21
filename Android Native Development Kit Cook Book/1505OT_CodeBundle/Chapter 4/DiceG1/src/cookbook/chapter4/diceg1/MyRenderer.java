package cookbook.chapter4.diceg1;


import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
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
		//call native methods to load the textures
		LoadTexture(R.drawable.dice41, mContext, 0);
		LoadTexture(R.drawable.dice42, mContext, 1);
		LoadTexture(R.drawable.dice43, mContext, 2);
		LoadTexture(R.drawable.dice44, mContext, 3);
		LoadTexture(R.drawable.dice45, mContext, 4);
		LoadTexture(R.drawable.dice46, mContext, 5);
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
	
	private void LoadTexture(int resId, Context context, int texIdx) {
		//Get the texture from the Android resource directory
		InputStream is = context.getResources().openRawResource(resId);
		Bitmap bitmap = null;
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//			options.inPreferredConfig = Bitmap.Config.RGB_565;
			bitmap = BitmapFactory.decodeStream(is, null, options);
			Log.i("bitmap info", bitmap.getWidth() + ":" + bitmap.getHeight() + ":" + bitmap.getConfig().toString());
			naLoadTexture(bitmap, bitmap.getWidth(), bitmap.getHeight(), texIdx);
		} finally {
			try {
				is.close();
				is = null;
			} catch (IOException e) {
			}
		}
		if (null != bitmap) {
			bitmap.recycle();
		}
	}
	
	private static native void naLoadTexture(Bitmap pBitmap, int pWidth, int pHeight, int pId);
    private static native void naInitGL1x();
    private static native void naDrawGraphics(float angleX, float angleY);
    private static native void naSurfaceChanged(int width, int height);
    
    static {
        System.loadLibrary("DiceG1");
    }
}
