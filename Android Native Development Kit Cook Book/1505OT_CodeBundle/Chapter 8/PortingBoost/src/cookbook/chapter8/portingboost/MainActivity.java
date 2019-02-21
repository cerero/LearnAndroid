package cookbook.chapter8.portingboost;

import android.os.Bundle;
import android.app.Activity;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        naExtractSubject("Subject: this is a boost test\nHello, boost");
    }
    
    
    private native static void naExtractSubject(String pInputStr);
    //load the native library
    static {
    	System.loadLibrary("PortingBoost");
    }
}
