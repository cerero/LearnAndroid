package com.android.templateApp;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class templateApp implements SensorEventListener
{  
	GL2View mView;
	
	public templateApp(Activity activity) {
        System.loadLibrary( "openal" );
        System.loadLibrary( "gfx" );
        System.loadLibrary( "templateApp" );

        mView = new GL2View( activity.getApplication() );
        activity.setContentView(mView);
    }

    public void onResume()
    {
        mView.onResume();
    }    

    
	public static native void Accelerometer( float x, float y, float z );
	
    public void onSensorChanged( SensorEvent event )
	{
		float x = event.values[ SensorManager.DATA_X ],
			  y = event.values[ SensorManager.DATA_Y ],
			  z = event.values[ SensorManager.DATA_Z ];

		Accelerometer( x, y, z );
	}
 
    public void onAccuracyChanged( Sensor sensor, int arg1 ) {}
}
