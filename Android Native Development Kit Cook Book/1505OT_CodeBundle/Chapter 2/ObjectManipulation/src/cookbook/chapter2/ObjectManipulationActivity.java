package cookbook.chapter2;

import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;

public class ObjectManipulationActivity extends Activity {
	private TextView tv;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_manipulation);
        tv = (TextView) findViewById(R.id.display_res);
        //call different demo methods, one at a time
//        callAllocNewDemo();
//        callGetObjectDemo();
        callIsInstanceDemo();
    }
    
    private void callAllocNewDemo() {
    	Contact con1 = AllocObjectDemo();
        Contact con2 = NewObjectDemo("B", 20);
        Contact con3 = NewObjectADemo("C", 30);
        Contact con4 = NewObjectVDemo("D", 40);
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(con1.getContactStr()).append("\n")
        	.append(con2.getContactStr()).append("\n")
        	.append(con3.getContactStr()).append("\n")
        	.append(con4.getContactStr()).append("\n");
        
        tv.setText(strBuilder.toString());
    }
    
    private void callGetObjectDemo() {
    	Contact con5 = GetObjectClassDemo(new Contact("dummy", 100));
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(con5.getContactStr()).append("\n");
        tv.setText(strBuilder.toString());
    }
    
    private void callIsInstanceDemo() {
    	IsInstanceOfDemo();
    }
    
    static {
        System.loadLibrary("ObjectManipulation");
    }

    private native Contact GetObjectClassDemo(Contact p);
    private native void IsInstanceOfDemo();
    private native Contact AllocObjectDemo();
    private native Contact NewObjectDemo(String name, int age);
    private native Contact NewObjectADemo(String name, int age);
    private native Contact NewObjectVDemo(String name, int age);
}
