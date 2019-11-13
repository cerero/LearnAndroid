package com.example.lightweightanimationengine;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;

//import com.android.templateApp.ch0203.templateApp;
import com.android.templateApp.ch0304.templateApp;

public class MainActivity extends Activity implements SensorEventListener {

    private templateApp tApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tApp = new templateApp(this);
    }

    @Override protected void onResume()
    {
        super.onResume();
        tApp.onResume();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        tApp.onSensorChanged(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        tApp.onAccuracyChanged(sensor, i);
    }
}
