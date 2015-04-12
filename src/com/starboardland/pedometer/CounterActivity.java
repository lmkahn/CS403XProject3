package com.starboardland.pedometer;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.*;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

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
    private float stepsBeforeApp = 0;
    boolean activityRunning;
    Date startTime;
    long start_time;
    SQLiteDatabase database;

    private float[] minuteSteps = {0,0,0,0,0,0,0,0};
    private TextView[] textViews = new TextView[9]; //{null, null, null, null, null, null, null, null, null};
    private float stepsInApp = 0;
    private float stepsInMinute = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        count1 = (TextView) findViewById(R.id.count1);
        textViews[0] = count1;
        count2 = (TextView) findViewById(R.id.count2);
        textViews[1] = count2;
        count3 = (TextView) findViewById(R.id.count3);
        textViews[2] = count3;
        count4 = (TextView) findViewById(R.id.count4);
        textViews[3] = count4;
        count5 = (TextView) findViewById(R.id.count5);
        textViews[4] = count5;
        count6 = (TextView) findViewById(R.id.count6);
        textViews[5] = count6;
        count7 = (TextView) findViewById(R.id.count7);
        textViews[6] = count7;
        count8 = (TextView) findViewById(R.id.count8);
        textViews[7] = count8;
        countTotal = (TextView) findViewById(R.id.count_total);
        textViews[8] = countTotal;
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        start_time = System.nanoTime();
        StepCounterOpenHelper db = new StepCounterOpenHelper(this);
        database = db.getWritableDatabase();
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

    private float calculateStepsSoFar(TextView view, int minNumber, float eventval) {
        if(view.getText().equals("0")){
            if(currentTotalSteps != 0) {
                System.out.println("Before: " + currentTotalSteps);
                currentTotalSteps -= stepsBeforeApp;
                System.out.println("After: " + currentTotalSteps);
            }
        }
        String minute = (String)view.getText();
        float minuteSteps;
        int eventvalsteps = (int)eventval;
        String queryString = "SELECT stepCounts.Count FROM stepCounts WHERE stepCounts.Minute" + " = " + Integer.toString(minNumber);
        Cursor c = database.rawQuery(queryString, null);
        int id = c.getColumnIndex("Count");
        ArrayList<String> strings = new ArrayList<String>();
        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            strings.add(c.getString(id));
        }
        String[] array = strings.toArray(new String[0]);
        String stepsLastMinute = array[0];//c.getString(id);
        c.close();
        currentTotalSteps += Float.parseFloat(stepsLastMinute);
        minuteSteps = eventvalsteps - currentTotalSteps;
        System.out.println("Eventvalsteps: " + eventvalsteps);
        System.out.println("Beginning Steps: " + stepsBeforeApp);
        System.out.println("Current Total Steps: " + currentTotalSteps);
        System.out.println("MinuteSteps: " + minuteSteps);
        return minuteSteps;

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (activityRunning) {
            if(count1.getText().equals("0")){
                stepsBeforeApp = event.values[0];
            }

            long currentTime = System.nanoTime();
            long elapsedTimeInSeconds = (currentTime - start_time) / 1000000000;
            int seconds = (int) elapsedTimeInSeconds;
            int minute = 0;

            //Minute 1
            if (seconds < 60){
                minute = 0;
            }

            //Minute 2
            else if(seconds >= 60 && seconds < 120) {
                minute = 1;
            }

            //Minute 3
            else if(seconds >= 120 && seconds < 180) {
                minute = 2;
            }

            //Minute 4
            else if(seconds >= 180 && seconds < 240) {
                minute = 3;
            }

            //Minute 5
            else if(seconds >= 240 && seconds < 300) {
                minute = 4;
            }

            //Minute 6
            else if(seconds >= 300 && seconds < 360) {
                minute = 5;
            }

            //Minute 7
            else if(seconds >= 360 && seconds < 420) {
                minute = 6;
            }

            //Minute 8
            else if(seconds >= 420 && seconds < 480) {
                minute = 7;
            }

            //Time to total
            else {
                minute = 8;
            }

            if(minute != 8) {
                if (minuteSteps[minute] == 0) {
                    stepsInApp += stepsInMinute;
                    stepsInMinute = 0;
                }

                stepsInMinute = event.values[0] - stepsInApp - stepsBeforeApp;
                minuteSteps[minute] = stepsInMinute;
                textViews[minute].setText(String.valueOf(stepsInMinute));

                //Store in DB
                ContentValues dbValues = new ContentValues();
                dbValues.put("Count", String.valueOf(stepsInMinute));
                String where = "Minute" + " = ?";
                String[] whereArgs = {String.valueOf(minute)};
                database.update("stepCounts", dbValues, where, whereArgs);
            }


            else {
                //get total
                float totalNumber = 0;
                for (Float f : minuteSteps) {
                    totalNumber += f;
                }
                System.out.println("I THINK THIS THE TOTAL: " + totalNumber);
                textViews[minute].setText(Float.toString(totalNumber));
            }


        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
