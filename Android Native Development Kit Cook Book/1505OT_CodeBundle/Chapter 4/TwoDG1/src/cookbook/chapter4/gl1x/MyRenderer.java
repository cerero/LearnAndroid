package cookbook.chapter4.gl1x;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;

public class MyRenderer implements GLSurfaceView.Renderer{
	//Called once to set up the view's OpenGL ES environment.
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		naInitGL1x();
	}
	//Called for each redraw of the view.
	@Override
	public void onDrawFrame(GL10 gl) {
		naDrawGraphics();
	}
	//Called if the geometry of the view changes, for example when the device's 
	//screen orientation changes.
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		naSurfaceChanged(width, height);
	}
	
    private static native void naInitGL1x();
    private static native void naDrawGraphics();
    private static native void naSurfaceChanged(int width, int height);
    
    static {
        System.loadLibrary("TwoDG1");
    }
}
