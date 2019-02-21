package cookbook.chapter3;

import android.os.Bundle;
import android.os.SystemClock;
import android.app.Activity;
import android.widget.TextView;

public class HelloNDKEclipseActivity extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setTextSize(30);
        tv.setText(getString());
        this.setContentView(tv);
    }
    public native String getString();
    static {
        System.loadLibrary("HelloNDKEclipse");
    }
}
