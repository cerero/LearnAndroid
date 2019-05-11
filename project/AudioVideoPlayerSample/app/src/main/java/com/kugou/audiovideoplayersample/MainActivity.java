package com.kugou.audiovideoplayersample;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.kugou.media.GiftMp4Player;
import com.kugou.media.IMP4Player;
import com.kugou.util.LogWrapper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.test_main);
	}

	private final void prepareSampleMovie(File path, int rawId) throws IOException {
		if (!path.exists()) {
			final BufferedInputStream in = new BufferedInputStream(getResources().openRawResource(rawId));
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

    private GiftMp4Player mp4Player;
    private int ind = 0;
    @Override
    protected void onResume() {
        super.onResume();

        ViewGroup parentViewGroup = findViewById(R.id.mylayout);

//        String localRes1 = createResFromeRaw("data_chucixindong_640.mp4", R.raw.data_chucixindong_640);
//        String localRes2 = createResFromeRaw("data_jinlinvshen_740.mp4", R.raw.data_jinlinvshen_740);
//        String localRes3 = createResFromeRaw("data_sirenfeiji_740.mp4", R.raw.data_sirenfeiji_740);
//        String localRes4 = createResFromeRaw("data_xinfumotianlun_740.mp4", R.raw.data_xinfumotianlun_740);

        String localRes1 = createResFromeRaw("data1_720.mp4", R.raw.data1_720);
        String localRes2 = createResFromeRaw("data2_720.mp4", R.raw.data2_720);
        String localRes3 = createResFromeRaw("data3_720.mp4", R.raw.data3_720);
        String localRes4 = createResFromeRaw("gift_720.mp4", R.raw.gift_720);

        if (mp4Player != null)
            mp4Player.onActivityResume();

        Button btn_start = (Button)findViewById(R.id.button_start);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//点击开始
                if (mp4Player == null)
                    mp4Player = new GiftMp4Player(parentViewGroup);

                String localRes;
                if (ind % 4 == 0) {
                    localRes = localRes1;
                } else if (ind % 4 == 1) {
                    localRes = localRes2;
                } else if (ind % 4 == 2) {
                    localRes = localRes3;
                } else {
                    localRes = localRes4;
                }
                ind ++;
                mp4Player.start(localRes, 1, new IMP4Player.EventCallBack() {
                    @Override
                    public void onErrorOccur(int errorId, String desc) {
                        LogWrapper.LOGE("MainActivity", "onErrorOccur errorId:" + errorId + ", desc:" + desc);
                    }

                    @Override
                    public void onStatusChange(int status) {
                        LogWrapper.LOGI("MainActivity", "onStatusChange:" + status + "  " + Thread.currentThread().getName());

                        if (mp4Player != null) {
//                            if (status == IMP4Player.EventCallBack.STATE_FINISHING) {
//                                mp4Player.addLoops(1);
//                            }
                            mp4Player.confirmStatus(status);
                        }
                    }
                });
            }
        });

        Button btn_stop = (Button)findViewById(R.id.button_stop);
        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//点击停止
                if (mp4Player != null)
                    mp4Player.stop();
            }
        });

        Button button_loop = (Button)findViewById(R.id.button_loop);
        button_loop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//追加循环次数
                if (mp4Player != null)
                    mp4Player.addLoops(999);
            }
        });

        CheckBox chk_visible = (CheckBox)findViewById(R.id.checkBox_visible);
        chk_visible.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mp4Player != null)
                    mp4Player.setVisible(isChecked);
            }
        });

        Button button_release = (Button)findViewById(R.id.button_release);
        button_release.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//追加循环次数
                if (mp4Player != null) {
                    mp4Player.release();
                    mp4Player = null;
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mp4Player != null) {
            mp4Player.onActivityStop();
        }

//        if (mp4Player != null) {
//            mp4Player.release();
//            mp4Player = null;
//        }

    }

    private String createResFromeRaw(String resName, int rawId) {
        final File dir = getFilesDir();
        dir.mkdirs();
        final File path = new File(dir, resName);

        try {
            prepareSampleMovie(path, rawId);
        } catch (IOException e){}

        String localRes = path.toString();
        return localRes;
    }
}
