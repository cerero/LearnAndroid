package cookbook.chapter7.JNIGraphics;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

public class MainActivity extends Activity {
	private RenderView prRenderView;
	private Context mContext;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getApplicationContext();
        //force 32-bit display for better display quality
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
    	lp.copyFrom(getWindow().getAttributes());
    	lp.format = PixelFormat.RGBA_8888;
        getWindow().setBackgroundDrawable(new ColorDrawable(0xff000000));
        getWindow().setAttributes(lp);
        //render the bitmap
        Bitmap bitmap = LoadBitmap(R.drawable.test, mContext);
        prRenderView = new RenderView(this, bitmap);
        setContentView(prRenderView);
    }
    
    private Bitmap LoadBitmap(int resId, Context context) {
		//Get the texture from the Android resource directory
		InputStream is = context.getResources().openRawResource(resId);
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//		options.inPreferredConfig = Bitmap.Config.RGB_565;
//		options.inPreferredConfig = Bitmap.Config.ARGB_4444;
		bitmap = BitmapFactory.decodeStream(is, null, options);
		Log.i("bitmap info", bitmap.getWidth() + ":" + bitmap.getHeight() + ":" + bitmap.getConfig().toString());
		try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		is = null;
		return bitmap;
	}
}