package com.example.testffmpeg;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("avutil-56");
        System.loadLibrary("swresample-3");
        System.loadLibrary("avcodec-58");
        System.loadLibrary("avformat-58");
        System.loadLibrary("swscale-5");
        System.loadLibrary("postproc-55");
        System.loadLibrary("avfilter-7");

        System.loadLibrary("native-lib");
        init();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = findViewById(R.id.sample_text);
        StringBuilder strLog = new StringBuilder();

        String mp4_input    =   Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "gift_960.mp4";
        String video_output =   Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "gift_960.yuv";
        String audio_output =   Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "gift_960.pcm";

        strLog.append(mp4_input).append("\n").append(video_output).append("\n").append(audio_output);
//        tv.setText(strLog.toString());

        decode(mp4_input, video_output, audio_output);
//        int ret = add(10, 20);
//        strLog.append("\n");
//        strLog.append(ret);
//        tv.setText(strLog.toString());
    }

    public static native void init();
    public native String stringFromJNI();

    public native void testListDir(String path);

    public native void decode(String input, String decode_video_output, String decode_audio_output);

//    public native int add(int a, int b);
}
