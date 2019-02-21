package cookbook.chapter4.egl;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {
	private float mPreviousX;
    private float mPreviousY;
    private float mAngleX;
    private float mAngleY;
    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
	public MySurfaceView(Context context) {
		super(context);
		this.getHolder().addCallback(this);
	}
	
	public MySurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.getHolder().addCallback(this);
    }
	
	public void startRenderer() {
		naNewRenderer();
		naStartRenderer();
	}
	
	public void stopRenderer() {
		naStopRenderer();
	}
	
	public void destroyRender() {
		naDestroyRenderer();
	}
	
	public boolean onTouchEvent(final MotionEvent event) {
		float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
        case MotionEvent.ACTION_MOVE:
            float dx = x - mPreviousX;
            float dy = y - mPreviousY;
            mAngleX += dx * TOUCH_SCALE_FACTOR;
            mAngleY += dy * TOUCH_SCALE_FACTOR;
            naRequestRenderer(mAngleX, mAngleY);
        }
        mPreviousX = x;
        mPreviousY = y;
        return true;
    }

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		//to get the correct format and dimen
		naSurfaceChanged(holder.getSurface());
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		naSurfaceDestroyed();
	}
	
	private static native void naNewRenderer();
	private static native void naStartRenderer();
	private static native void naStopRenderer();
	private static native void naDestroyRenderer();
	
    private static native void naSurfaceChanged(Surface surface);
    private static native void naSurfaceDestroyed();
    
    private static native void naRequestRenderer(float pAngleX, float pAngleY);
    
    static {
        System.loadLibrary("EGLDemo");
    }
}
