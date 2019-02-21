package cookbook.chapter7.zlibdemo;

import android.os.Bundle;
import android.app.Activity;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        naCompressAndDecompress();
        naGzFileDemo();
//        setContentView(R.layout.activity_main);
    }
    
    private static native void naCompressAndDecompress();
    private static native void naGzFileDemo();
    
	//load the native library
    static {
    	System.loadLibrary("ZlibDemo");
    }
}
