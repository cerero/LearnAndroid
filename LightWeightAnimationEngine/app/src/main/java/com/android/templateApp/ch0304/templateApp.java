package com.android.templateApp.ch0304;

import android.app.Activity;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import com.android.templateApp.ch0304.GL2View;

public class templateApp implements SensorEventListener
{  
	com.android.templateApp.ch0304.GL2View mView;
	
	SensorManager mSensorManager;

    public templateApp(Activity activity) {
        System.loadLibrary( "openal" );
        System.loadLibrary( "gfx" );
        System.loadLibrary( "templateApp" );

        mView = new GL2View( activity.getApplication() );
        activity.setContentView(mView);

        // Start the accelerometer
        /*
        mSensorManager = ( SensorManager ) getSystemService( SENSOR_SERVICE );

        // Refresh 24 times per second.
        mSensorManager.registerListener( this,
			       						 mSensorManager.getDefaultSensor( SensorManager.SENSOR_ACCELEROMETER ),
			       						 41000 );
		*/
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
