package cookbook.chapter6.nativethreadscondvar;

import android.os.Bundle;
import android.app.Activity;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        jni_start_threads();
    }

    private static native void jni_start_threads();
    
    static {
        System.loadLibrary("NativeThreadsCondVar");
    }
}
