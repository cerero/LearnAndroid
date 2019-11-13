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

	private boolean use720p = true;
    private GiftMp4Player mp4Player;
    private int ind = 0;
    @Override
    protected void onResume() {
        super.onResume();

        ViewGroup parentViewGroup = findViewById(R.id.mylayout);

        String resOf720p[] = {
                createResFromeRaw("six_720.mp4", R.raw.six_720),
                createResFromeRaw("chucixindong_720.mp4", R.raw.chucixindong_720),
                createResFromeRaw("motianlun_720.mp4", R.raw.motianlun_720),
                createResFromeRaw("yueguangzhichen_720.mp4", R.raw.yueguangzhichen_720),
                createResFromeRaw("aidehuojian_720.mp4", R.raw.aidehuojian_720),
                createResFromeRaw("zhenaiyisheng_720.mp4", R.raw.zhenaiyisheng_720),
                createResFromeRaw("sirenfeiji_720.mp4", R.raw.sirenfeiji_720),
                createResFromeRaw("jinlinvhsen_720.mp4", R.raw.jinlinvhsen_720),
        };

        String resOf480p[] = {
                createResFromeRaw("six_480.mp4", R.raw.six_480),
                createResFromeRaw("chucixindong_480.mp4", R.raw.chucixindong_480),
                createResFromeRaw("motianlun_480.mp4", R.raw.motianlun_480),
                createResFromeRaw("yueguangzhichen_480.mp4", R.raw.yueguangzhichen_480),
                createResFromeRaw("aidehuojian_480.mp4", R.raw.aidehuojian_480),
                createResFromeRaw("zhenaiyisheng_480.mp4", R.raw.zhenaiyisheng_480),
                createResFromeRaw("sirenfeiji_480.mp4", R.raw.sirenfeiji_480),
                createResFromeRaw("jinlinvhsen_480.mp4", R.raw.jinlinvhsen_480),
        };

        if (mp4Player != null)
            mp4Player.onActivityResume();

        Button btn_start = (Button)findViewById(R.id.button_start);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//点击开始
                if (mp4Player == null)
                    mp4Player = new GiftMp4Player(parentViewGroup);

                String[] locaResArr = use720p ? resOf720p : resOf480p;
                if (ind >= locaResArr.length) {
                    ind = 0;
                }

                mp4Player.start(locaResArr[ind], 1, new IMP4Player.EventCallBack() {
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

        Button btn_next = (Button)findViewById(R.id.button_next);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//点击停止
                if (mp4Player != null)
                    mp4Player.stop();

                ind++;
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

        CheckBox chk_rate = (CheckBox) findViewById(R.id.checkBox_rate);
        chk_rate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                use720p = isChecked;
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
