package com.kugou.audiovideoplayersample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.kugou.uiperformance.core.UIPerformance;

public class MainActivity extends AppCompatActivity {

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

//		RelativeLayout layout = new RelativeLayout(this);
//		setContentView(layout);
//		layout
//		ImageView imgView = new ImageView(this);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlayerFragment()).commit();
		}

//		UIPerformance.getInstance(this).start();
	}

	public native void stringFromJNI();

}
