package cookbook.chapter2;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;

public class CallingMethodsActivity extends Activity {
	private TextView tv;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accessing_methods);
        tv = (TextView) findViewById(R.id.display_res);
//        callAccessStaticMethodDemo();
//        callAccessInstanceMethodDemo();
        callMethodReflectionDemo();
    }

    private void callAccessInstanceMethodDemo() {
    	DummySub dummySub = new DummySub(1, "A");
    	AccessInstanceMethodDemo(dummySub);
    	StringBuffer strBuf = new StringBuffer();
    	strBuf.append("dummySub: ").append(dummySub.getName()).append("\n");
    	tv.setText(strBuf.toString());
    }
    
    private void callAccessStaticMethodDemo() {
    	Dummy dummy = new Dummy(1);
    	dummy.setValue2(100);
    	AccessStaticMethodDemo(dummy);
    	StringBuffer strBuf = new StringBuffer();
    	strBuf.append("dummy: ").append(dummy.getValue2()).append("\n");
    	tv.setText(strBuf.toString());
    }
    
    private void callMethodReflectionDemo() {
    	StringBuffer strBuf = new StringBuffer();
    	Dummy dummy = new Dummy(10);
    	strBuf.append("before native call: ").append(dummy.getValue()).append("\n");
    	try {
			Method m = dummy.getClass().getMethod("getValue", null);
			Method m2 = MethodReflectionDemo(m, dummy);
			Object args[] = new Object[1];
			args[0] = new Integer(1234);
			m2.invoke(dummy, args);
			strBuf.append("after invoke method returned from native method: ").append(dummy.getValue()).append("\n");
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
    	tv.setText(strBuf.toString());
    }
    
    static {
        System.loadLibrary("CallMethods");
    }
    
    private native void AccessStaticMethodDemo(Dummy p);
    private native void AccessInstanceMethodDemo(DummySub p);
    private native Method MethodReflectionDemo(Method p1, Dummy p2);
}
