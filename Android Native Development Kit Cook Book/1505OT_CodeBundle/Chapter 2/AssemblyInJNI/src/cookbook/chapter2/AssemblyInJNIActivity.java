package cookbook.chapter2;

import android.os.Bundle;
import android.widget.TextView;
import android.app.Activity;

public class AssemblyInJNIActivity extends Activity {
	private TextView tv;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assembly_in_jni);
        tv = (TextView) findViewById(R.id.display_res);
        
//        callInlineAssemblyAddDemo();
        callAssemblyMultiplyDemo();
    }
    
    private void callInlineAssemblyAddDemo() {
    	int a = 5, b = 10;
    	int rv = InlineAssemblyAddDemo(a, b);
    	tv.setText(a + "+" + b + "=" + rv);
    }
    
    private void callAssemblyMultiplyDemo() {
    	int a = 5, b = 10;
    	int rv = AssemblyMultiplyDemo(a, b);
    	tv.setText(a + "x" + b + "=" + rv);
    }

    private native int AssemblyMultiplyDemo(int a, int b);
    private native int InlineAssemblyAddDemo(int a, int b);
    
    static {
        System.loadLibrary("AssemblyInJNI");
    }
    
    
}
