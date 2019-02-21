package cookbook.chapter6.nativethreadsdata;

import android.os.Bundle;
import android.app.Activity;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        jni_start_threads(3);
    }

    private static native void jni_start_threads(int pNumOfThreads);
    static {
        System.loadLibrary("NativeThreadsData");
    }
}
