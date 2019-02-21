package cookbook.chapter3;

import android.os.Bundle;
import android.widget.TextView;
import android.app.Activity;

public class HelloNDKGDBActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setTextSize(30);
        tv.setText("10 x 20 = " + multiply(10, 20));
        this.setContentView(tv);
    }
    public native int multiply(int a, int b);
    static {
        System.loadLibrary("HelloNDKGDB");
    }
}
