package cookbook.chapter2;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class NativeMethodRegisterActivity extends Activity {
	private TextView tv;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native_method_register);
        tv = (TextView) findViewById(R.id.display_res);
        callNativeMethods();
    }
    
    private void callNativeMethods() {
    	int a = 10, b = 100;
        int c = NativeAddition(a, b);
        tv.setText(a + "+" + b + "=" + c);
        c = NativeMultiplication(a, b);
        tv.append("\n" + a + "x" + b + "=" + c);
    }
    
    private native int NativeAddition(int a, int b);
    private native int NativeMultiplication(int a, int b);
    static {
    	//use either of the two methods below
//        System.loadLibrary("NativeRegister");
        System.load("/data/data/cookbook.chapter2/lib/libNativeRegister.so");
    }
}
