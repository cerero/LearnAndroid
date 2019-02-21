package cookbook.chapter2;

import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;

public class ClassManipulationActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_manipulation);
        TextView tv = (TextView) findViewById(R.id.display_res);
        StringBuilder strBuilder = new StringBuilder();
        
        tv.setText(strBuilder.toString());
        
//        findClassDemo();
//        findClassDemo2();
//        GetSuperclassDemo();
        IsAssignableFromDemo();
    }
    
    static {
        System.loadLibrary("ClassManipulation");
    }
    
    private native void findClassDemo();
    private native void findClassDemo2();
    private native void GetSuperclassDemo();
    private native void IsAssignableFromDemo();
}
