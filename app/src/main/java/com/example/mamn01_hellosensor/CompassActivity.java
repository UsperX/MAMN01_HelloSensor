package com.example.mamn01_hellosensor;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class CompassActivity extends AppCompatActivity {

    //Device sensor manager
    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Sensor sensorMagneticField;

    // Define the compass picture that will be used
    private ImageView compassNeedle;
    private TextView degreeDisplay;

    // Record the angle turned of the compass picture;
    private float currentDegrees = 0f;

    private float[] floatGravity = new float[3];
    private float[] floatGeoMagnetic = new float[3];

    private float[] floatOrientation = new float[3];
    private float[] floatRotationMatrix = new float[9];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        compassNeedle = (ImageView) findViewById(R.id.imageView);

        degreeDisplay = (TextView) findViewById(R.id.textView);

        // Initialize android device sensor capabilities
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sensorManager.registerListener(sensorEventListener, sensorAccelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(sensorEventListener, sensorMagneticField, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Stop listener to save battery
        sensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorEventListener, sensorAccelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(sensorEventListener, sensorMagneticField, SensorManager.SENSOR_DELAY_UI);
    }

    SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event == null) {
                return;
            }
            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                floatGravity = lowpass(event.values, floatGravity);
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                floatGeoMagnetic = lowpass(event.values, floatGeoMagnetic);
            }

            SensorManager.getRotationMatrix(floatRotationMatrix, null, floatGravity, floatGeoMagnetic);
            SensorManager.getOrientation(floatRotationMatrix, floatOrientation);

            float previousDegrees = currentDegrees;
            currentDegrees = (float) ((Math.toDegrees(floatOrientation[0]) + 360.0) % 360.0);
            if (Math.abs(currentDegrees-previousDegrees) > 0.5) {
                compassNeedle.setRotation(-Math.round(currentDegrees));
                degreeDisplay.setText(getDirection(currentDegrees) + " " + (int) currentDegrees + " °");
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };


    private String getDirection(float degrees) {
        if (degrees >= 350 || degrees <= 10)
            return "N";
        else if (degrees < 350 && degrees > 280)
            return "NW";
        else if (degrees <= 280 && degrees > 260)
            return "W";
        else if (degrees <= 260 && degrees > 190)
            return "SW";
        else  if (degrees <= 190 && degrees > 170)
            return "S";
        else if (degrees <=170 && degrees > 100)
            return "SE";
        else if (degrees <= 100 && degrees > 80)
            return "E";
        else return "NE";
    }

    private float[] lowpass( float[] input, float[] output) {
        if (output == null) return input;
        final float alpha = 0.8f;

        for(int i = 0; i<input.length; i++) {
            output[i] = alpha * output[i] + (1-alpha) * input[i];
        }
        return output;
    }
}