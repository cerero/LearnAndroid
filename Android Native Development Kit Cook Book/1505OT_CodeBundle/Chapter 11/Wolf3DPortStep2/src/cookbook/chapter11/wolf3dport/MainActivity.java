package cookbook.chapter11.wolf3dport;

import java.io.File;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.app.Activity;

public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
	private GameView mView;
	private static final String DATA_FOLDER = "/data/data" + File.separator ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		mView = (GameView) findViewById(R.id.gameView);
		//the game controller buttons
		Button btnForward = (Button) findViewById(R.id.buttonForward);
		btnForward.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				if (action == MotionEvent.ACTION_DOWN) {
					Log.i(TAG, "forward pressed");
					keyPress(ScanCodes.sc_UpArrow);
				} else if (action == MotionEvent.ACTION_UP) {
					Log.i(TAG, "forward released");
					keyRelease(ScanCodes.sc_UpArrow);
				}
				return false;
			}
		});
		Button btnBack = (Button) findViewById(R.id.buttonBack);
		btnBack.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				if (action == MotionEvent.ACTION_DOWN) {
					Log.i(TAG, "back pressed");
					keyPress(ScanCodes.sc_DownArrow);
				} else if (action == MotionEvent.ACTION_UP) {
					Log.i(TAG, "back released");
					keyRelease(ScanCodes.sc_DownArrow);
				}
				return false;
			}
		});
		Button btnLeft = (Button) findViewById(R.id.buttonLeft);
		btnLeft.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				if (action == MotionEvent.ACTION_DOWN) {
					Log.i(TAG, "left pressed");
					keyPress(ScanCodes.sc_LeftArrow);
				} else if (action == MotionEvent.ACTION_UP) {
					Log.i(TAG, "left released");
					keyRelease(ScanCodes.sc_LeftArrow);
				}
				return false;
			}
		});
		Button btnRight = (Button) findViewById(R.id.buttonRight);
		btnRight.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				if (action == MotionEvent.ACTION_DOWN) {
					Log.i(TAG, "right pressed");
					keyPress(ScanCodes.sc_RightArrow);
				} else if (action == MotionEvent.ACTION_UP) {
					Log.i(TAG, "right released");
					keyRelease(ScanCodes.sc_RightArrow);
				}
				return false;
			}
		});
		Button btnFire = (Button) findViewById(R.id.buttonFire);
		btnFire.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				if (action == MotionEvent.ACTION_DOWN) {
					Log.i(TAG, "fire pressed");
					keyPress(ScanCodes.sc_Control);
				} else if (action == MotionEvent.ACTION_UP) {
					Log.i(TAG, "fire released");
					keyRelease(ScanCodes.sc_Control);
				}
				return false;
			}
		});
		Button btnOpen = (Button) findViewById(R.id.buttonOpen);
		btnOpen.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				if (action == MotionEvent.ACTION_DOWN) {
					Log.i(TAG, "open pressed");
					keyPress(ScanCodes.sc_Space);
				} else if (action == MotionEvent.ACTION_UP) {
					Log.i(TAG, "open released");
					keyRelease(ScanCodes.sc_Space);
				}
				return false;
			}
		});
		//the data files 
		String gameDataDirStr = DATA_FOLDER
				+ this.getPackageName() + File.separator + "gamedata" + File.separator;
		File gameDataDir = new File(gameDataDirStr);
		if (!gameDataDir.exists()) {
			if (!gameDataDir.mkdirs()) {
				Log.e(TAG, "unable to create dir " + gameDataDirStr);
			}
		}
		GameUtils.installGameDataFiles(this.getApplicationContext(), gameDataDir);
		//the view scale
		Display display = getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();
		width = width/16*16;
		height = height/16*16;
		mView.init(width, height);
		argv = new String[] {"wolf3d", "wolfsw", "datadir", gameDataDirStr, 
				"width", String.valueOf(width), "height", String.valueOf(height)};
		thread.start();
	}
	
	private String argv[];
	
	Thread thread = new Thread() {
	    @Override
	    public void run() {
	    	naMain(argv, mView);
	    }
	};
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	menu.add(0, 0, 0, "Yes");
    	menu.add(0, 1, 1, "No");
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	super.onOptionsItemSelected(item);
    	switch (item.getItemId()) {
		case 0:
			keyPress(ScanCodes.sc_Y);
			SystemClock.sleep(10);
			keyRelease(ScanCodes.sc_Y);
			return true;
		case 1:
			keyPress(ScanCodes.sc_N);
			SystemClock.sleep(10);
			keyRelease(ScanCodes.sc_N);
			return true;
    	}
    	return false;
    }
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_MENU) 
    		return false;
    	Log.i(TAG, "KEY CODE: " + keyCode);
    	if (KeyEvent.KEYCODE_BACK == keyCode) {
    		//back key pressed
    		keyPress(ScanCodes.sc_Escape);
    	}
    	return false;
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_MENU) 
    		return false;
    	Log.i(TAG, "KEY CODE: " + keyCode);
    	if (KeyEvent.KEYCODE_BACK == keyCode) {
    		//back key pressed
    		keyRelease(ScanCodes.sc_Escape);
    	}
    	return false;
    }
    
    @Override
	protected void onDestroy() {
		super.onDestroy();
		System.exit(0);	//to kill the bg thread
	}
	
	private native int naMain(String[] argv, GameView pObj);
	public static native int keyPress(int key);
	public static native int keyRelease(int key);
	
	static {
        System.loadLibrary("wolf3dport");
    }
}
