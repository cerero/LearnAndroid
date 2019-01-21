package com.example.testffmpeg;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

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
        String testPath = getApplicationContext().getExternalCacheDir().getParentFile().getAbsolutePath();
        tv.setText(testPath);

        testListDir(testPath);
        decode("testInput", "testOutput");
    }

    public static native void init();
    public native String stringFromJNI();

    public native void testListDir(String path);

    public native void decode(String input, String output);
}
