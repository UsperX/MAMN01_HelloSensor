package com.example.mamn01_hellosensor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.hardware.SensorManager;

public class AccelerometerActivity extends AppCompatActivity {

    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Vibrator vibrator;

    private TextView xText, yText, zText, debuggerText;
    private ImageView dotImage, circleImage;
    private float[] floatGravity = new float[3];
    private int centerWidth;
    private int centerHeight;
    private int circleCenterW;
    private int circleCenterH;
    private boolean rumble = false;
    private DisplayMetrics displayMetrics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);

        xText = (TextView) findViewById(R.id.x_textView);
        yText = (TextView) findViewById(R.id.y_textView);
        zText = (TextView) findViewById(R.id.z_textView);
        debuggerText = (TextView) findViewById(R.id.debbugerText);
        dotImage = (ImageView) findViewById(R.id.imageDot);
        circleImage = (ImageView) findViewById(R.id.outerCircleImage);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        sensorManager.registerListener(sensorEventListenerAccelrometer, sensorAccelerometer, SensorManager.SENSOR_DELAY_GAME);

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
        sensorManager.registerListener(sensorEventListenerAccelrometer, sensorAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    SensorEventListener sensorEventListenerAccelrometer = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            final float alpha = 0.8f;

            floatGravity[0] = alpha * floatGravity[0] + (1-alpha) * sensorEvent.values[0];
            floatGravity[1] = alpha * floatGravity[1] + (1-alpha) * sensorEvent.values[1];
            floatGravity[2] = alpha * floatGravity[2] + (1-alpha) * sensorEvent.values[2];

            setCordText();
            moveDot();
            rumble();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };

    public void moveDot() {

        ViewGroup.LayoutParams circleParams = (ViewGroup.LayoutParams) circleImage.getLayoutParams();
        circleParams.width = (int) (0.8 * displayMetrics.widthPixels);
        circleParams.height = (int) (0.8 * displayMetrics.widthPixels);
        circleImage.setLayoutParams(circleParams);
        centerWidth = (displayMetrics.widthPixels/2) - (dotImage.getWidth()/2);
        centerHeight = (displayMetrics.heightPixels/2)  - (dotImage.getHeight()/2);
        circleCenterW = (displayMetrics.widthPixels/2) - (circleImage.getWidth()/2);
        circleCenterH= (displayMetrics.heightPixels/2) - (circleImage.getHeight()/2);
        circleImage.setX(circleCenterW);
        circleImage.setY(circleCenterH);

        double factor = (circleImage.getWidth()-(dotImage.getWidth()/2))/10;

        double C_sq = Math.pow(floatGravity[0],2) + Math.pow(floatGravity[1],2);
        circleImage.setX(circleCenterW);
        circleImage.setY(circleCenterH);
        if(C_sq > 25) {
            double v = floatGravity[1]/Math.sqrt(C_sq);
            double adjH = 5 * v;
            double adjW = Math.sqrt(25 - (adjH * adjH));
            if (floatGravity[0] < 0) adjW = -adjW;
            dotImage.setX((float) (centerWidth - factor * adjW));
            dotImage.setY((float) (centerHeight + factor * adjH));
        } else {
            dotImage.setX((float) (centerWidth - factor * floatGravity[0]));
            dotImage.setY((float) (centerHeight + factor * floatGravity[1]));
        }
    }

    public void setCordText() {
        xText.setText("X: " + Math.round(floatGravity[0]*10.0)/10.0);
        yText.setText("Y: " + Math.round(floatGravity[1]*10.0)/10.0);
        zText.setText("Z: " + Math.round(floatGravity[2]*10.0)/10.0);
    }

    public void rumble() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if ((Math.pow(floatGravity[0], 2) + Math.pow(floatGravity[1], 2))  > 25) {
                if (!rumble) {
                    vibrator.vibrate(VibrationEffect.createOneShot(10, 255));
                    rumble = true;
                }
            } else {
                rumble = false;
            }
        }
    }

    public void calculateColor() {

    }
}

