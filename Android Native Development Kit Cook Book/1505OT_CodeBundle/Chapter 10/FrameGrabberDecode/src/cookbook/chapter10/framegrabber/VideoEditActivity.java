package cookbook.chapter10.framegrabber;

import java.io.FileOutputStream;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.VideoView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class VideoEditActivity extends SherlockActivity {
	private static final String TAG = "VideoEditActivity";
	public static int THEME = R.style.Theme_Sherlock;
	private Context mContext;
	public static final String INTENT_PATH_DATA = "IPD";
	private String mPath = "/sdcard/amc/madrid10.mp4";
	private SimpleVideoSurfaceView mVideoView;
	private SeekBar mProgress;
	private ImageButton btn1;
	private long lastSeekTime;	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		setTheme(VideoEditActivity.THEME);
		super.onCreate(savedInstanceState);
		mContext = this.getApplicationContext();
		
		setContentView(R.layout.activity_video_edit);
		
		Intent intent = this.getIntent();
		mPath = intent.getStringExtra(INTENT_PATH_DATA);
		
		mProgress = (SeekBar) findViewById(R.id.video_edit_seekBar);
		mProgress.setMax(1000);
		mProgress.setProgress(0);
		
		mVideoView = (SimpleVideoSurfaceView) findViewById(R.id.videoView);
		
		btn1 = (ImageButton) findViewById(R.id.imageButton1);
		btn1.setImageResource(android.R.drawable.ic_media_play);
		btn1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int status = mVideoView.getPlayStatus();
				if (0 == status) {
					//not started, start play
					mVideoView.setVideoFile(mPath);
					mVideoView.startPlay();
					lastSeekTime = 0;
					((ImageButton)v).setImageResource(android.R.drawable.ic_media_pause);
					mHandler.removeMessages(UPDATE_PLAYBACK_PROG);
					mHandler.sendEmptyMessage(UPDATE_PLAYBACK_PROG);
				} else if (1 == status) {
					//playing, pause it
					mVideoView.pausePlay();
					((ImageButton)v).setImageResource(android.R.drawable.ic_media_play);
				} else {
					//paused, resume
					mVideoView.resumePlay();
					((ImageButton)v).setImageResource(android.R.drawable.ic_media_pause);
				}
			}
		});
	}
	
	private static final int UPDATE_PLAYBACK_PROG = 1;
	private Handler mHandler = new Handler() {
		@Override
        public void handleMessage(Message msg) {
			switch (msg.what) {
			case UPDATE_PLAYBACK_PROG:
				if (null != mVideoView) {
					if (null == mProgress) {
						Log.e(TAG, "mProgress is null");
					}
					int prog = mVideoView.getCurrentPos();
					mProgress.setProgress(prog);
					if (0 == prog && 0 == mVideoView.getPlayStatus()) {
						//the video playback is finished
						btn1.setImageResource(android.R.drawable.ic_media_play);
					}
                    msg = obtainMessage(UPDATE_PLAYBACK_PROG);
                    sendMessageDelayed(msg, 100);
				}
				break;
			}
		}
	};

	@Override
	protected void onStop() {
		super.onStop();
		mVideoView.stopPlay();
		mHandler.removeMessages(UPDATE_PLAYBACK_PROG);
	}
}
