package com.example.videodecoderandrender.demo.chap04.dm03;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.app.Activity;
import android.view.Window;
import android.view.WindowManager;

public class EGLDemoActivity extends Activity {
	private MySurfaceView myView;
	private static final String TAG = "EGLDemoActivity";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        myView = new MySurfaceView(this);
        this.setContentView(myView);
//        this.setContentView(R.layout.activity_egldemo);
//        myView = (MySurfaceView)findViewById(R.id.surfaceview);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: startRenderer");
        myView.startRenderer();
    }
    
    
    public boolean onCreateOptionsMenu(Menu menu)
    {
     menu.add(1, 1, 0, "Item1");
     menu.add(1, 2, 1, "Item2");
     menu.add(1, 3, 2, "Item3");
     menu.add(1, 4, 3, "Item4");
     menu.add(1, 5, 4, "Item5");

     menu.add(2, 6, 0, "Item6");
     menu.add(2, 7, 1, "Item7");
     menu.add(2, 8, 2, "Item8");
     menu.add(2, 9, 3, "Item9");
     menu.add(2, 10, 4, "Item10");

     return true;
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onResume: stopRenderer");
        myView.stopRenderer();
    }

    @Override
    protected void onStop() {
    	super.onStop();
    	Log.i(TAG, "onResume: destroyRender");
    	myView.destroyRender();
    }
}
