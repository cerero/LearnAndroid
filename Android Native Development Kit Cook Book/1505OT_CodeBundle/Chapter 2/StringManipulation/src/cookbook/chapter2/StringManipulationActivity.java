package cookbook.chapter2;

import java.nio.charset.Charset;

import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;

public class StringManipulationActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_string_manipulation);
        TextView tv = (TextView) findViewById(R.id.display_res);
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("Android default character encoding: ").append(Charset.defaultCharset().name())
        	.append(System.getProperty("line.separator"))	
        	.append(passStringReturnString("hello native code"));
        tv.setText(strBuilder.toString());
    }

    private native String passStringReturnString(String p);
    
    static {
        System.loadLibrary("StringManipulation");
    }
}
