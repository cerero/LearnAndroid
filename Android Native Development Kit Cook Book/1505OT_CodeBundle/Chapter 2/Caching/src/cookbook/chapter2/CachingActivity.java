package cookbook.chapter2;

import android.os.Bundle;
import android.widget.TextView;
import android.app.Activity;

public class CachingActivity extends Activity {
	private TextView tv;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caching);
        tv = (TextView) findViewById(R.id.display_res);
//        callCachingFieldMethodIDDemo1();
//        callCachingFieldMethodIDDemo2();
        callCachingReferencesDemo();
    }
    
    private void callCachingFieldMethodIDDemo1() {
    	value = 100;
        CachingFieldMethodIDDemo1();
        value = 200;
        CachingFieldMethodIDDemo1();
        value = 300;
        CachingFieldMethodIDDemo1();
    }
    
    private void callCachingFieldMethodIDDemo2() {
    	value = 100;
        CachingFieldMethodIDDemo2();
        value = 200;
        CachingFieldMethodIDDemo2();
        value = 300;
        CachingFieldMethodIDDemo2();
    }
    
    private void callCachingReferencesDemo() {
    	CachingReferencesDemo(false);
    	CachingReferencesDemo(true);
    	CachingReferencesDemo(true);
    }
    
    private int value;
    public int getValue() {
    	return value;
    }
    public void setValue(int pValue) {
    	value = pValue;
    }
    
    private native void CachingFieldMethodIDDemo1();
    private native void CachingFieldMethodIDDemo2();
    private native void CachingReferencesDemo(boolean p);
    private native static void InitIDs();
    
    static {
        System.loadLibrary("Caching");
//        InitIDs();
    }
}
