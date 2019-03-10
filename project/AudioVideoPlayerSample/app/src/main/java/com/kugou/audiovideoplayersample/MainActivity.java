package com.kugou.audiovideoplayersample;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.kugou.media.GiftMp4Player;
import com.kugou.media.IGiftMp4Player;
import com.kugou.uiperformance.core.UIPerformance;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;

public class MainActivity extends Activity {

    static {
		System.loadLibrary("fdk-aac");
		System.loadLibrary("mp3lame");
		System.loadLibrary("x264");
		System.loadLibrary("rtmp");
		System.loadLibrary("ffmpeg");

        System.loadLibrary("AVNative-lib");
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		final File dir = getFilesDir();
		dir.mkdirs();
		final File path = new File(dir, "gift_480.mp4");

		try {
			prepareSampleMovie(path);
		} catch (IOException e){}

		String localRes = path.toString();//Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "gift_750.mp4";
		Log.i("MainActivity", "localRes=" + localRes);
		GiftMp4Player mp4Player = GiftMp4Player.getInstance();
		mp4Player.startGift(this, localRes, 1, new IGiftMp4Player.EventCallBack() {
			@Override
			public void onActionFeedBack() {

			}

			@Override
			public void onErrorOccur(int errorId) {

			}

			@Override
			public void onStatusChange() {

			}
		});

//		setContentView(R.layout.activity_main);
//		if (savedInstanceState == null) {
//			getSupportFragmentManager().beginTransaction()
//					.add(R.id.container, new PlayerFragment()).commit();
//		}

//		UIPerformance.getInstance(this).start();
	}

	public native void stringFromJNI();

	private final void prepareSampleMovie(File path) throws IOException {
		if (!path.exists()) {
			final BufferedInputStream in = new BufferedInputStream(getResources().openRawResource(R.raw.gift_480));
			final BufferedOutputStream out = new BufferedOutputStream(openFileOutput(path.getName(), Context.MODE_PRIVATE));
			byte[] buf = new byte[8192];
			int size = in.read(buf);
			while (size > 0) {
				out.write(buf, 0, size);
				size = in.read(buf);
			}
			in.close();
			out.flush();
			out.close();
		}
	}

}
