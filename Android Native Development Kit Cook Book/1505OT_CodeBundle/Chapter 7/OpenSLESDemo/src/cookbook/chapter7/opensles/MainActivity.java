package cookbook.chapter7.opensles;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.app.Activity;


public class MainActivity extends Activity {
	private static final String TAG = "OpenSLESDemo";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        naCreateEngine();
        Button recordBtn = (Button) findViewById(R.id.button1);
        recordBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean rv = naCreateAudioRecorder();
				if (!rv) {
					Log.e(TAG, "cannot create native audio recorder");
					return;
				}
				naStartRecording();
			}
		});
        
        Button playBtn = (Button) findViewById(R.id.button2);
        playBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				naCreateBufferQueueAudioPlayer();
				naStartPlaying();
			}
		});
    }

    @Override
    protected void onDestroy()
    {
    	naShutdown();
        super.onDestroy();
    }
    
    private static native void naCreateEngine();
    private static native void naShutdown();
    private static native boolean naCreateAudioRecorder();
    private static native void naStartRecording();
    private static native void naCreateBufferQueueAudioPlayer();
    private static native void naStartPlaying();
    
    static {
        System.loadLibrary("OpenSLESDemo");
    }
}
