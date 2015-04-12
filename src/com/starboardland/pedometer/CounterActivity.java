package com.starboardland.pedometer;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.*;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

public class CounterActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private TextView count1;
    private TextView count2;
    private TextView count3;
    private TextView count4;
    private TextView count5;
    private TextView count6;
    private TextView count7;
    private TextView count8;
    private TextView countTotal;
    private float currentTotalSteps = 0;
    private float beginningSteps = 0;
    private float[] minuteSteps = {0,0,0,0,0,0,0,0};
    boolean activityRunning;
    Date startTime;
    long start_time;
    SQLiteDatabase database;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        count1 = (TextView) findViewById(R.id.count1);
        count2 = (TextView) findViewById(R.id.count2);
        count3 = (TextView) findViewById(R.id.count3);
        count4 = (TextView) findViewById(R.id.count4);
        count5 = (TextView) findViewById(R.id.count5);
        count6 = (TextView) findViewById(R.id.count6);
        count7 = (TextView) findViewById(R.id.count7);
        count8 = (TextView) findViewById(R.id.count8);
        countTotal = (TextView) findViewById(R.id.count_total);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        start_time = System.nanoTime();
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityRunning = true;
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null) {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(this, "Count sensor not available!", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        activityRunning = false;
        // if you unregister the last listener, the hardware will stop detecting step events
//        sensorManager.unregisterListener(this); 
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (activityRunning) {
            if(count1.getText().equals("0")){
                beginningSteps = event.values[0];
            }
            long currentTime = System.nanoTime();
            long elapsedTimeInSeconds = (currentTime - start_time) / 1000000000;
            if(elapsedTimeInSeconds < 60){
                float steps = event.values[0] - beginningSteps;
                currentTotalSteps = steps;
                minuteSteps[0] = steps;
                count1.setText(String.valueOf(steps));
            }

            else if (elapsedTimeInSeconds >= 60 && elapsedTimeInSeconds < 120) {
                float steps = event.values[0] - minuteSteps[0];
                currentTotalSteps = steps;
                minuteSteps[1] = steps;
                count2.setText(String.valueOf(event.values[0]));
            }

            else if (elapsedTimeInSeconds >= 120 && elapsedTimeInSeconds < 180) {
                float steps = event.values[0] - minuteSteps[1];
                currentTotalSteps = steps;
                minuteSteps[2] = steps - minuteSteps[1];
                count3.setText(String.valueOf(event.values[0]));
            }

            else if (elapsedTimeInSeconds >= 180 && elapsedTimeInSeconds < 240) {
                float steps = event.values[0] - minuteSteps[2];
                currentTotalSteps = steps;
                count4.setText(String.valueOf(event.values[0]));
            }

            else if (elapsedTimeInSeconds >= 240 && elapsedTimeInSeconds < 300) {
                float steps = event.values[0] - minuteSteps[3];
                currentTotalSteps = steps;
                count5.setText(String.valueOf(event.values[0]));
            }

            else if (elapsedTimeInSeconds >= 300 && elapsedTimeInSeconds < 360) {
                float steps = event.values[0] - minuteSteps[4];
                currentTotalSteps = steps;
                count6.setText(String.valueOf(event.values[0]));
            }

            else if (elapsedTimeInSeconds >= 360 && elapsedTimeInSeconds < 420) {
                float steps = event.values[0] - minuteSteps[5];
                currentTotalSteps = steps;
                count7.setText(String.valueOf(event.values[0]));
            }

            else if (elapsedTimeInSeconds >= 420 && elapsedTimeInSeconds < 480) {
                float steps = event.values[0] - beginningSteps;
                currentTotalSteps = steps;
                count8.setText(String.valueOf(event.values[0]));
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
