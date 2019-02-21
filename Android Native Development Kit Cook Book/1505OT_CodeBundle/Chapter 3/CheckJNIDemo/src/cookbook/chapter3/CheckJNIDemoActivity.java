package cookbook.chapter3;

import android.os.Bundle;
import android.app.Activity;

public class CheckJNIDemoActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_jnidemo);
        CheckJNIDemo();
    }
    public native int[] CheckJNIDemo();
    static {
        System.loadLibrary("CheckJNIDemo");
    }
}
