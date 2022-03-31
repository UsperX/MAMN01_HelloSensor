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

        sensorManager.registerListener(sensorEventListenerAccelrometer, sensorAccelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(sensorEventListenerMagneticField, sensorMagneticField, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();

        //Stop listener to save battery
        sensorManager.unregisterListener(sensorEventListenerAccelrometer);
        sensorManager.unregisterListener(sensorEventListenerMagneticField);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorEventListenerAccelrometer, sensorAccelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(sensorEventListenerMagneticField, sensorMagneticField, SensorManager.SENSOR_DELAY_UI);
    }

    SensorEventListener sensorEventListenerAccelrometer = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            floatGravity = lowpass(sensorEvent.values, floatGravity);

            SensorManager.getRotationMatrix(floatRotationMatrix, null, floatGravity, floatGeoMagnetic);
            SensorManager.getOrientation(floatRotationMatrix, floatOrientation);

            float lastDegrees = currentDegrees;
            currentDegrees = (float) (((floatOrientation[0]*180/Math.PI) + 360) % 360);
            if(Math.abs(lastDegrees-currentDegrees) > 0.5) {
                compassNeedle.setRotation(-currentDegrees);
                degreeDisplay.setText(getDirection(currentDegrees) + " " + (int) currentDegrees + " °");
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };

    SensorEventListener sensorEventListenerMagneticField = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            floatGeoMagnetic = lowpass(sensorEvent.values, floatGeoMagnetic);

            SensorManager.getRotationMatrix(floatRotationMatrix, null, floatGravity, floatGeoMagnetic);
            SensorManager.getOrientation(floatRotationMatrix, floatOrientation);

            float lastDegrees = currentDegrees;
            currentDegrees = (float) (((floatOrientation[0]*180/Math.PI) + 360) % 360);
            if(Math.abs(lastDegrees-currentDegrees) > 0.5) {
                compassNeedle.setRotation(-currentDegrees);
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

        for(int i = 0; i<input.length; i++) {
            output[i] = output[i] + 0.15f * (input[i]-output[i]);
        }
        return output;
    }
}