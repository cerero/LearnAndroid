package cookbook.chapter11.wolf3dport;

import java.io.File;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
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
	protected void onDestroy() {
		super.onDestroy();
		System.exit(0);	//to kill the bg thread
	}
	
	private native int naMain(String[] argv, GameView pObj);
	
	static {
        System.loadLibrary("wolf3dport");
    }
}
