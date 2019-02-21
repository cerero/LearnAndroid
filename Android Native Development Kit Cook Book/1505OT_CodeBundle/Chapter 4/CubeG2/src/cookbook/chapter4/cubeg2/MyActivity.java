package cookbook.chapter4.cubeg2;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class MyActivity extends Activity {
	private GLSurfaceView mGLView;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mGLView = new MySurfaceView(this);
        setContentView(mGLView);
	}
}
