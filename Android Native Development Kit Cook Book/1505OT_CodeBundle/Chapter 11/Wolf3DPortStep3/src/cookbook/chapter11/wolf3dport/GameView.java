package cookbook.chapter11.wolf3dport;

import java.io.File;
import java.io.FileOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Environment;
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
	
	public String saveCurrentFrame(int idx) {
		String filePath = null;
		try {
			File extDir = Environment.getExternalStorageDirectory();
			filePath = extDir.getAbsolutePath() + "/" + idx + ".png";
			FileOutputStream out = new FileOutputStream(filePath);
			mBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
		} catch (Exception e) {
		    e.printStackTrace();
		    filePath = null;
		}
		return filePath;
	}
	
	/**
	 * called by native code to update the game view
	 */
//	int dumpCount = 0;
	public void updateGameBitmap() {
//		Log.i(TAG, "updateGameBitmap called " + dumpCount);
//		dumpCount++;
//		if (dumpCount > 500 && dumpCount < 505) {
//			saveCurrentFrame(dumpCount);
//		}
		if(null != surfaceHolder && surfaceHolder.getSurface().isValid()){
		     Canvas canvas = surfaceHolder.lockCanvas();
		     if (null != mBitmap) {
//		    	 Log.i(TAG, "draw bitmap");
		    	 canvas.drawBitmap(mBitmap, 0, 0, prFramePaint);
		     }
		     surfaceHolder.unlockCanvasAndPost(canvas);
		} 
	}
}
