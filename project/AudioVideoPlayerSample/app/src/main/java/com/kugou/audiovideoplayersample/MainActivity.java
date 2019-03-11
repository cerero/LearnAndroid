package com.kugou.audiovideoplayersample;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.kugou.media.GiftMp4Player;
import com.kugou.media.IMP4Player;

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

        setContentView(R.layout.test_main);

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

    @Override
    protected void onResume() {
        super.onResume();

        ViewGroup parentViewGroup = findViewById(R.id.mylayout);

        final File dir = getFilesDir();
        dir.mkdirs();
        final File path = new File(dir, "gift_480.mp4");

        try {
            prepareSampleMovie(path);
        } catch (IOException e){}

        String localRes = path.toString();
        Log.i("MainActivity", "localRes=" + localRes);

        GiftMp4Player mp4Player = new GiftMp4Player(parentViewGroup, new IMP4Player.EventCallBack() {
            @Override
            public void onErrorOccur(int errorId, String desc) {
                Log.e("MainActivity", "onErrorOccur errorId:" + errorId + ", desc:" + desc);
            }

            @Override
            public void onStatusChange(int status) {
                Log.i("MainActivity", "onStatusChange:" + status);
            }
        });
//
//        mp4Player.start(localRes, 1);

        Button btn_start = (Button)findViewById(R.id.button_start);
		btn_start.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {//点击开始
                mp4Player.start(localRes, 1);
			}
		});

		Button btn_stop = (Button)findViewById(R.id.button_stop);
        btn_stop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {//点击停止

			}
		});
    }
}
