package com.example.mamn01_hellosensor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class CompassActivity extends AppCompatActivity {

    //Device sensor manager
    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Sensor sensorMagneticField;
    private Vibrator vibrator;
    private MediaPlayer mp;

    // Define the compass picture that will be used
    private ImageView compassNeedle;
    private TextView degreeDisplay;
    private ImageButton muteButton;

    // Record the angle turned of the compass picture;
    private float currentDegrees = 0f;
    private boolean rumble = true, muted = false;
    private String direction = "";

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
        muteButton = (ImageButton) findViewById(R.id.imageButton);

        // Initialize android device sensor capabilities
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sensorManager.registerListener(sensorEventListener, sensorAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(sensorEventListener, sensorMagneticField, SensorManager.SENSOR_DELAY_GAME);
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
        sensorManager.registerListener(sensorEventListener, sensorAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(sensorEventListener, sensorMagneticField, SensorManager.SENSOR_DELAY_GAME);
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
            currentDegrees = Math.round((float) ((Math.toDegrees(floatOrientation[0]) + 360.0) % 360.0));
                compassNeedle.setRotation(-currentDegrees);
                updateDirection();
                degreeDisplay.setText(direction + " " + (int) currentDegrees + " °");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if ((currentDegrees < 2 || currentDegrees > 358) && !rumble ) {
                    vibrator.vibrate(VibrationEffect.createOneShot(25, 255));
                    rumble = true;
                } else if ((currentDegrees % 30 < 2 || currentDegrees % 30 > 28) && !rumble) {
                    vibrator.vibrate(VibrationEffect.createOneShot(10, 175));
                    rumble = true;
                } else if (currentDegrees % 30 > 3 && currentDegrees% 30 < 27 && rumble) {
                    rumble = false;
                };
                }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };


    private void updateDirection() {
        float degrees = currentDegrees;
        
        if (degrees >= 350 || degrees <= 10) {
            if (!direction.equals("N")) {
                direction =  "N";
                playSound(R.raw.north);
            }
        } else if (degrees < 350 && degrees > 280) {
            if (!direction.equals("NW")) {
                direction =  "NW";
                playSound(R.raw.northwest);
            }
        } else if (degrees <= 280 && degrees > 260) {
            if (!direction.equals("W")) {
                direction =  "W";
                playSound(R.raw.west);
            }
        } else if (degrees <= 260 && degrees > 190) {
            if (!direction.equals("SW")) {
                direction =  "SW";
                playSound(R.raw.southwest);
            }
        } else  if (degrees <= 190 && degrees > 170) {
            if (!direction.equals("S")) {
                direction =  "S";
                playSound(R.raw.south);
            }
        } else if (degrees <=170 && degrees > 100) {
            if (!direction.equals("SE")) {
                direction =  "SE";
                playSound(R.raw.southeast);
            }
        } else if (degrees <= 100 && degrees > 80) {
            if (!direction.equals("E")) {
                direction =  "E";
                playSound(R.raw.east);
            }
        } else {
            if (!direction.equals("NE")) {
                direction =  "NE";
                playSound(R.raw.northeast);
            }
        }
    }

    private void playSound(int id) {
            if (mp != null) {
                mp.stop();
                mp.release();
            }
            mp = MediaPlayer.create(this, id);
            if (mp != null)
                if (!muted) {
                    mp.start();
                }
    }

    private float[] lowpass( float[] input, float[] output) {
        if (output == null) return input;
        final float alpha = 0.95f;

        for(int i = 0; i<input.length; i++) {
            output[i] = alpha * output[i] + (1.0f-alpha) * input[i];
        }
        return output;
    }

    public void toggleMute(View view) {
        muted = !muted;
        if(muted) {
            muteButton.setImageResource(R.drawable.mute);
        } else muteButton.setImageResource(R.drawable.unmute);
    }
}