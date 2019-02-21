package cookbook.chapter8.portingshared;

import android.os.Bundle;
import android.widget.ImageView;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        naCreateABmp(400, 400, 24);
    }
    
    private static native void naCreateABmp(int width, int height, int depth);
    
    static {
    	System.loadLibrary("bmp");
        System.loadLibrary("PortingShared");
    }
}
