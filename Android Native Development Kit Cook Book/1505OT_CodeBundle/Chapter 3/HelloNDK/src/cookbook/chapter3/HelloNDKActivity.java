package cookbook.chapter3;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class HelloNDKActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setTextSize(30);
        tv.setText(naGetHelloNDKStr());
        this.setContentView(tv);
    }
    public native String naGetHelloNDKStr();
    static {
        System.loadLibrary("hello");
    }
}
