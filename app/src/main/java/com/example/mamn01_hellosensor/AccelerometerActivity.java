package com.example.mamn01_hellosensor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

public class AccelerometerActivity extends AppCompatActivity {

    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Vibrator vibrator;

    private TextView xText, yText, zText;
    private ImageView dotImage, circleImage, circleBorderImage;
    private ConstraintLayout constraintLayout;
    private float[] floatGravity = new float[3];
    private int centerWidth, centerHeight;
    private double factor;
    private boolean rumble = false, paused = false;
    private DisplayMetrics displayMetrics;
    private MediaPlayer mp;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);

        xText = (TextView) findViewById(R.id.x_textView);
        yText = (TextView) findViewById(R.id.y_textView);
        zText = (TextView) findViewById(R.id.z_textView);
        dotImage = (ImageView) findViewById(R.id.imageDot);
        circleImage = (ImageView) findViewById(R.id.outerCircleImage);
        circleBorderImage = (ImageView) findViewById(R.id.circleBorder);
        constraintLayout = (ConstraintLayout) findViewById(R.id.r1);


        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        mp = MediaPlayer.create(getApplicationContext(), R.raw.blue);
        mp.setLooping(true);
        mp.setVolume(0,0);

        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        sensorManager.registerListener(sensorEventListenerAccelrometer, sensorAccelerometer, SensorManager.SENSOR_DELAY_GAME);

    }

    @Override
    protected void onPause() {
        super.onPause();

        //Stop listener to save battery
        sensorManager.unregisterListener(sensorEventListenerAccelrometer);
        if(mp.isPlaying()) {
            mp.pause();
            paused = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorEventListenerAccelrometer, sensorAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        if (paused) {
            mp.start();
            paused = false;
        }
    }

    SensorEventListener sensorEventListenerAccelrometer = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            final float alpha = 0.8f;

            floatGravity[0] = alpha * floatGravity[0] + (1-alpha) * sensorEvent.values[0];
            floatGravity[1] = alpha * floatGravity[1] + (1-alpha) * sensorEvent.values[1];
            floatGravity[2] = alpha * floatGravity[2] + (1-alpha) * sensorEvent.values[2];

            setElements();
            setCordText();
            moveDot();
            rumble();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };

    public void moveDot() {

        double C_sq = Math.pow(floatGravity[0],2) + Math.pow(floatGravity[1],2);
        if(C_sq > 25) {
            double v = floatGravity[1]/Math.sqrt(C_sq);
            double adjH = 5 * v;
            double adjW = Math.sqrt(25 - (adjH * adjH));
            if (floatGravity[0] < 0) adjW = -adjW;
            dotImage.setX((float) (centerWidth - factor * adjW));
            dotImage.setY((float) (centerHeight + factor * adjH));
            setColor((float) adjW, (float) adjH);
        } else {
            dotImage.setX((float) (centerWidth - factor * floatGravity[0]));
            dotImage.setY((float) (centerHeight + factor * floatGravity[1]));
            setColor(floatGravity[0], floatGravity[1]);
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
        //Value = 1
        //Saturation == distans från origo
        // Hue = vinkel från origo
    public void setColor(float x, float y) {
        double value = 1;
        double hue;
        double saturation = Math.sqrt(Math.pow(x/5,2) + Math.pow(y/5,2));
        if(x <= 0 && y <= 0) {
          hue = Math.toDegrees(Math.atan(x/y));
        } else if(x < 0 && y > 0) {
            hue = 90 + Math.toDegrees(Math.atan(y/(-x)));
        } else if(x > 0 && y > 0) {
            hue = 180 + Math.toDegrees(Math.atan((x)/(y)));
        } else {
            hue = 270 + Math.toDegrees(Math.atan((-y)/x));
        }

        if (saturation == 0) {
            constraintLayout.setBackgroundColor(Color.rgb(255,255,255));
        }
        double hh = hue/60;
        int i = (int) hh;
        double ff = hh - i;
        double p = value * (1.0 - saturation);
        double q = value * (1.0 - (saturation*ff));
        double t = value * (1.0 - (saturation * (1.0 - ff)));
        int red = 0;
        int green = 0;
        int blue = 0;

        switch (i) {
            case 0:
                red = (int)(value * 255);
                green = (int)(t * 255);
                blue = (int)(p * 255);
                constraintLayout.setBackgroundColor(Color.rgb(red,green,blue));
                break;
            case 1:
                red = (int)(q * 255);
                green = (int)(value * 255);
                blue = (int)(p * 255);
                constraintLayout.setBackgroundColor(Color.rgb(red, green, blue));
                break;
            case 2:
                red = (int)(p * 255);
                green = (int)(value * 255);
                blue = (int)(t * 255);
                constraintLayout.setBackgroundColor(Color.rgb(red, green, blue));
                break;
            case 3:
                red = (int)(p * 255);
                green = (int)(q * 255);
                blue = (int)(value * 255);
                constraintLayout.setBackgroundColor(Color.rgb(red, green, blue));
                break;
            case 4:
                red = (int)(t * 255);
                green = (int)(p * 255);
                blue = (int)(value * 255);
                constraintLayout.setBackgroundColor(Color.rgb(red, green, blue));
                break;
            case 5:
                red = (int)(value * 255);
                green = (int)(p * 255);
                blue = (int)(q * 255);
                constraintLayout.setBackgroundColor(Color.rgb(red, green, blue));
                break;
        }
        float volume = (blue - red - green)/255.0f;
        if (volume > 0) {
            if (!mp.isPlaying()) {
                mp.start();
            }
            mp.setVolume(volume, volume);
        } else {
            if (mp.isPlaying()) {
                mp.stop();
                try {
                    mp.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setElements() {
        factor = (circleImage.getWidth()-(dotImage.getWidth()/2))/10;

        ViewGroup.LayoutParams circleParams = (ViewGroup.LayoutParams) circleImage.getLayoutParams();
        circleParams.width = (int) (0.8 * displayMetrics.widthPixels);
        circleParams.height = (int) (0.8 * displayMetrics.widthPixels);
        circleImage.setLayoutParams(circleParams);

        ViewGroup.LayoutParams circleBorderParams = (ViewGroup.LayoutParams) circleBorderImage.getLayoutParams();
        circleBorderParams.width = (int) (1.3 * circleImage.getWidth());
        circleBorderParams.height = (int) (1.3 * circleImage.getHeight());
        circleBorderImage.setLayoutParams(circleBorderParams);

        centerWidth = (displayMetrics.widthPixels/2) - (dotImage.getWidth()/2);
        centerHeight = (displayMetrics.heightPixels/2) - (dotImage.getHeight()/2);

       int circleCenterW = (displayMetrics.widthPixels/2) - (circleImage.getWidth()/2);
       int circleCenterH= (displayMetrics.heightPixels/2) - (circleImage.getHeight()/2);
       int circleBorderW = (displayMetrics.widthPixels/2) - (circleBorderImage.getWidth()/2);
       int circleBorderH = (displayMetrics.heightPixels/2) - (circleBorderImage.getHeight()/2);
        circleImage.setX(circleCenterW);
        circleImage.setY(circleCenterH);
        circleBorderImage.setX(circleBorderW);
        circleBorderImage.setY(circleBorderH);
    }
}

