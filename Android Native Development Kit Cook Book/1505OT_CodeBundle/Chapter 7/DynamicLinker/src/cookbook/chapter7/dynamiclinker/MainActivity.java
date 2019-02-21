package cookbook.chapter7.dynamiclinker;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        naDLDemo();
    }
    
    private static native void naDLDemo();

  //load the native library
    static {
    	System.loadLibrary("DynamicLinker");
    }
}
