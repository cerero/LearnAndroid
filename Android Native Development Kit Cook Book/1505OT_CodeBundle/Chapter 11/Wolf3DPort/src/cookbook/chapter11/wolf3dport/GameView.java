package cookbook.chapter11.wolf3dport;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements SurfaceHolder.Callback  {
	private static final String TAG = "GameView";
	private SurfaceHolder surfaceHolder;
	private Bitmap mBitmap;
	private final Paint prFramePaint = new Paint();
	
	public GameView(Context context) {
		super(context);
		surfaceHolder = this.getHolder();
		surfaceHolder.addCallback(this);
	}
	
	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		surfaceHolder = this.getHolder();
		surfaceHolder.addCallback(this);
	}
	
	public void init(int vWidth, int vHeight) {
		mBitmap = Bitmap.createBitmap(vWidth, vHeight, Bitmap.Config.ARGB_8888);
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int width, int height) {
		Log.i(TAG, "surfaceChanged " + width + ":" + height + ":" + arg1);
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		Log.i(TAG, "surfaceCreated");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		Log.i(TAG, "surfaceDestroyed");
	}
	
	/**
	 * called by native code to update the game view
	 */
	public void updateGameBitmap() {
		if(null != surfaceHolder && surfaceHolder.getSurface().isValid()){
		     Canvas canvas = surfaceHolder.lockCanvas();
		     if (null != mBitmap) {
		    	 canvas.drawBitmap(mBitmap, 0, 0, prFramePaint);
		     }
		     surfaceHolder.unlockCanvasAndPost(canvas);
		} 
	}
}
