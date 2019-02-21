package cookbook.chapter10.framegrabber;

import java.io.File;
import java.io.FileOutputStream;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SimpleVideoSurfaceView extends SurfaceView implements Runnable, SurfaceHolder.Callback  {
	private static final String TAG = "SimpleVideoView";
	private Bitmap mBitmap;
	private final Paint prFramePaint = new Paint();
	private int mDrawTop, mDrawLeft;
	private int mDisplayWidth, mDisplayHeight;	
	private Thread thread;
	private SurfaceHolder surfaceHolder;
	private int mStatus = 0;	//0: not started, 1: playing, 2: paused
	private int mVideoDuration = 0;
	private int mVideoCurrentFrame = 0;
	private int mVideoFR = 0;
	private String mVideoFileName = "";
	
	public void init() {
		mDrawTop = 0;
		mDrawLeft = 0;
		mStatus = 0;
		mVideoDuration = 0;
		mVideoCurrentFrame = 0;
		mVideoFR = 0;
	}
	
	public SimpleVideoSurfaceView(Context context) {
		super(context);
		surfaceHolder = this.getHolder();
		surfaceHolder.addCallback(this);
		init();
	}
	
	public SimpleVideoSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		surfaceHolder = this.getHolder();
		surfaceHolder.addCallback(this);
		init();
	}
	
	public void setVideoFile(String pPath) {
		naInit(pPath);
		mVideoFileName = pPath.substring(pPath.lastIndexOf("/"), pPath.lastIndexOf("."));
		mVideoDuration = naGetDuration();
		mVideoFR = naGetFrameRate();
		calVideoScaling();
		mBitmap = Bitmap.createBitmap(mDisplayWidth, mDisplayHeight, Bitmap.Config.ARGB_8888);
		naPrepareDisplay(mBitmap, mDisplayWidth, mDisplayHeight);
	}
	
	private void calVideoScaling() {
		//decide the scaling and drawing position
		int[] res = naGetVideoRes();	//width, height
		int viewWidth = this.getWidth();
		int viewHeight = this.getHeight();
		float scaleF = 1.0f;
		if (((float)res[0])/viewWidth > ((float)res[1])/viewHeight) {
			//fit width
			scaleF = ((float)res[0])/viewWidth;
			mDrawLeft = 0;
			mDrawTop = (viewHeight - (int)(res[1]/scaleF))/2;
		} else {
			//fit height
			scaleF = ((float)res[1])/viewHeight;
			mDrawLeft = (viewWidth - (int)(res[0]/scaleF))/2;
			mDrawTop = 0;
		}
		mDisplayWidth = (int)(res[0]/scaleF)/16*16;
		mDisplayHeight = (int)(res[1]/scaleF)/16*16;
		Log.i(TAG, mDrawLeft + ":" + mDrawTop + ":" + scaleF + ":" + mDisplayWidth + ":" + mDisplayHeight);
	}
	
	public void startPlay() {
		thread = new Thread(this);
		thread.start();
		mStatus = 1;
	}
	
	public int getPlayStatus() {
		return mStatus;
	}
	
	public void pausePlay() {
		mStatus = 2;
	}
	
	public void resumePlay() {
		mStatus = 1;
	}
	
	public void stopPlay() {
		if (1 == mStatus) {
			Log.i(TAG, "stop playback");
			naQuit();
			mStatus = 0;
		}
	}
	
	//get current video pos, in [0, 1000]
	//current frame / total No. of frames * 1000 = > [0, 1000]
	public int getCurrentPos() {
		return mVideoCurrentFrame*1000/(mVideoFR*mVideoDuration);
	}

	@Override
	public void run() {
		while (true) {
			while (2 == mStatus) {
				//pause
				SystemClock.sleep(100);
			}
			mVideoCurrentFrame = naGetVideoFrame();
			if (0 < mVideoCurrentFrame) {
				//success, redraw
				if(surfaceHolder.getSurface().isValid()){
				     Canvas canvas = surfaceHolder.lockCanvas();
				     if (null != mBitmap) {
				    	 canvas.drawBitmap(mBitmap, mDrawLeft, mDrawTop, prFramePaint);
				     }
				     surfaceHolder.unlockCanvasAndPost(canvas);
				}
			} else {
				//failure, probably end of video, break
				naFinish(mBitmap);
				mStatus = 0;
				break;
			}
		}
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

	private static native int naInit(String pVideoFileName);
	
	private static native int[] naGetVideoRes();
	private static native int naGetDuration();
	private static native int naGetFrameRate();
	private static native int naFinish(Bitmap bitmap);
	
	private static native int naGetVideoFrame();
	private static native int naPrepareDisplay(Bitmap bitmap, int width, int height);
	private static native void naQuit();
	
    static {
        System.loadLibrary("framegrabber");
    }
}
