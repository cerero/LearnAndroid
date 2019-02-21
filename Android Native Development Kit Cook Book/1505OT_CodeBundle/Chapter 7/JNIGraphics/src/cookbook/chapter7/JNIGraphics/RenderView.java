package cookbook.chapter7.JNIGraphics;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

public class RenderView extends View{
	private static final String TAG = "RenderView";
	private Bitmap mBitmap;
	private final Paint prFramePaint = new Paint(Paint.FILTER_BITMAP_FLAG);
	
	private static native void naDemoJniGraphics(Bitmap _bitmap);
	
	public RenderView(Context _context, Bitmap pBitmap) {
		super(_context);
		mBitmap = pBitmap;
		naDemoJniGraphics(mBitmap);
		/*render the bitmap*/
		invalidate();
	}
	
	@Override 
	protected void onDraw(Canvas _canvas) {
		if (mBitmap != null) {
			Log.i("drawbitmap", "---RENDER ST");
			_canvas.drawBitmap(mBitmap, 0, 0, prFramePaint);
			Log.i("drawbitmap", "---RENDER ED");
		}
	}
	
	@Override 
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}
	
	//load the native library
    static {
    	System.loadLibrary("JNIGraphics");
    }
}
