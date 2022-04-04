package com.example.mamn01_hellosensor;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.hardware.SensorManager;

public class AccelerometerActivity extends AppCompatActivity {

    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;

    private TextView xText, yText, zText;
    private ImageView dotImage;
    private float currentX, currentY, currentZ;
    private float[] floatGravity = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);

        xText = (TextView) findViewById(R.id.x_textView);
        yText = (TextView) findViewById(R.id.y_textView);
        zText = (TextView) findViewById(R.id.z_textView);
        dotImage = (ImageView) findViewById(R.id.imageDot);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(sensorEventListenerAccelrometer, sensorAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();

        //Stop listener to save battery
        sensorManager.unregisterListener(sensorEventListenerAccelrometer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorEventListenerAccelrometer, sensorAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    SensorEventListener sensorEventListenerAccelrometer = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            final float alpha = 0.8f;

            floatGravity[0] = alpha * floatGravity[0] + (1-alpha) * sensorEvent.values[0];
            floatGravity[1] = alpha * floatGravity[1] + (1-alpha) * sensorEvent.values[1];
            floatGravity[2] = alpha * floatGravity[2] + (1-alpha) * sensorEvent.values[2];


            xText.setText("X: " + Math.round(floatGravity[0]*10.0)/10.0);
            yText.setText("Y: " + Math.round(floatGravity[1]*10.0)/10.0);
            zText.setText("Z: " + Math.round(floatGravity[2]*10.0)/10.0);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int centerY = (displayMetrics.heightPixels/2) - (dotImage.getHeight()/2);
            int centerX = (displayMetrics.widthPixels/2) - (dotImage.getWidth()/2);

            dotImage.setX((float)(centerX - 25.0 * floatGravity[0]));
            dotImage.setY((float)(centerY + 25.0 * floatGravity[1]));

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };

}