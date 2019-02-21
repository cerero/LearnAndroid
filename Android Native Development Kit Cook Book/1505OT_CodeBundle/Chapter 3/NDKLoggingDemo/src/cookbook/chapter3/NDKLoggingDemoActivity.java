package cookbook.chapter3;

import android.os.Bundle;
import android.app.Activity;

public class NDKLoggingDemoActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoggingDemo();
    }
    public native void LoggingDemo();
    static {
        System.loadLibrary("NDKLoggingDemo");
    }
}
