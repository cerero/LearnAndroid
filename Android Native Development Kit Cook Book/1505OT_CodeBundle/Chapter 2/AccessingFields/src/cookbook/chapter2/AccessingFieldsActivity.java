package cookbook.chapter2;

import java.lang.reflect.Field;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class AccessingFieldsActivity extends Activity {
	private TextView tv;
	
	//instance fields
	private int intF;
	private String strF;
	private int[] intArrF;
	private Dummy dummyF;
	
	//static fields
	private static int sintF = 111;
	private static String sstrF = "static string";
	private static int[] sintArrF = {1, 2, 3};
	private static Dummy sdummyF = new Dummy(333);
	
	private Field field;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accessing_fields);
        tv = (TextView) findViewById(R.id.display_res);
        
//        callAccessStaticFieldDemo();
//        callAccessInstanceFieldDemo();
        callFieldReflectionDemo();
    }
    
    private void callAccessStaticFieldDemo() {
    	AccessStaticFieldDemo();
    	StringBuffer strBuf = new StringBuffer();
    	strBuf.append("sintF: ").append(sintF).append("\n")
    		.append("sstrF: ").append(sstrF).append("\n")
    		.append("sintArrF: ");
    	for (int i = 0; i < sintArrF.length; ++i) {
    		strBuf.append(sintArrF[i]).append("\t");
    	}
    	strBuf.append("\n");
    	strBuf.append("sdummyF: ").append(sdummyF.value).append("\n");
    	tv.setText(strBuf.toString());
    }
    
    private void callAccessInstanceFieldDemo() {
    	intF = 222;
    	strF = "instance string";
    	intArrF = new int[]{1, 2, 3, 4, 5};
    	dummyF = new Dummy(666);
    	AccessInstanceFieldDemo();
    	StringBuffer strBuf = new StringBuffer();
    	strBuf.append("intF: ").append(intF).append("\n")
    		.append("strF: ").append(strF).append("\n")
    		.append("intArrF: ");
    	for (int i = 0; i < intArrF.length; ++i) {
    		strBuf.append(intArrF[i]).append("\t");
    	}
    	strBuf.append("\n");
    	strBuf.append("dummyF: ").append(dummyF.value).append("\n");
    	tv.setText(strBuf.toString());
    }
    
    private void callFieldReflectionDemo() {
    	try {
			field = sdummyF.getClass().getField("value");
			Field retField = FieldReflectionDemo(field, sdummyF);
			int value2 = retField.getInt(sdummyF);
			StringBuffer strBuf = new StringBuffer();
			strBuf.append("value2: ").append(value2).append("\n");
			tv.setText(strBuf.toString());
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
    }

    static {
        System.loadLibrary("AccessFields");
    }
    
    private native void AccessStaticFieldDemo();
    private native void AccessInstanceFieldDemo();
    private native Field FieldReflectionDemo(Field p1, Dummy p2);
}
