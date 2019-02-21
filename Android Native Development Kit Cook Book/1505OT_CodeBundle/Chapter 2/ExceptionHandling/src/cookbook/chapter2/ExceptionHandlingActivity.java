package cookbook.chapter2;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class ExceptionHandlingActivity extends Activity {
	private TextView tv;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exception_handling);
        tv = (TextView) findViewById(R.id.display_res);
//        callExceptionDemo();
        callFatalErrorDemo();
    }
    
    private void callExceptionDemo() {
    	try {
    		ExceptionDemo();
    	} catch (RuntimeException e) {
    		String msg = e.getMessage();
    		tv.setText(msg);
    	}
    }
    
    private void callFatalErrorDemo() {
    	FatalErrorDemo();
    	tv.setText("after calling FatalErrorDemo");
    }
    
    private native void ExceptionDemo();
    private native void FatalErrorDemo();
    
    static {
        System.loadLibrary("ExceptionHandling");
    }
}
