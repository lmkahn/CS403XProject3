package com.starboardland.pedometer;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
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
                currentTotalSteps -= beginningSteps;
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
        System.out.println("Beginning Steps: " + beginningSteps);
        System.out.println("Current Total Steps: " + currentTotalSteps);
        System.out.println("MinuteSteps: " + minuteSteps);
        return minuteSteps;

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
                minuteSteps[0] = steps;
                count1.setText(String.valueOf(steps));
                ContentValues dbValues = new ContentValues();
                dbValues.put("Count", String.valueOf(steps));
                String where = "Minute" + " = ?";
                String[] whereArgs = {"1"};
                database.update("stepCounts", dbValues, where, whereArgs);
                String queryString = "SELECT stepCounts.Count FROM stepCounts WHERE stepCounts.Minute" + " = 1";
                Cursor c = database.rawQuery(queryString, null);
                int id = c.getColumnIndex("Count");
                ArrayList<String> strings = new ArrayList<String>();
                for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    strings.add(c.getString(id));
                }
                String[] array = strings.toArray(new String[0]);
                String result = array[0];//c.getString(id);
                c.close();
            }

            else if (elapsedTimeInSeconds >= 60 && elapsedTimeInSeconds < 120) {
                float steps = calculateStepsSoFar(count2, 1, event.values[0]);
                System.out.println("Steps:" + steps);
                float newSteps = event.values[0] - steps;

                minuteSteps[1] = steps;
                count2.setText(String.valueOf(steps));
                ContentValues dbValues = new ContentValues();
                dbValues.put("Count", String.valueOf(steps));
                String where = "Minute" + " = ?";
                String[] whereArgs = {"2"};
                database.update("stepCounts", dbValues, where, whereArgs);
                String queryString = "SELECT stepCounts.Count FROM stepCounts WHERE stepCounts.Minute" + " = 2";
                Cursor c = database.rawQuery(queryString, null);
                int id = c.getColumnIndex("Count");
                ArrayList<String> strings = new ArrayList<String>();
                for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    strings.add(c.getString(id));
                }
                String[] array = strings.toArray(new String[0]);
                String result = array[0];//c.getString(id);
                c.close();
            }

            else if (elapsedTimeInSeconds >= 120 && elapsedTimeInSeconds < 180) {
                float steps = calculateStepsSoFar(count3, 2, event.values[0]);
                minuteSteps[2] = steps - minuteSteps[1];
                count3.setText(String.valueOf(event.values[0]));
                ContentValues dbValues = new ContentValues();
                dbValues.put("Count", String.valueOf(steps));
                String where = "Minute" + " = ?";
                String[] whereArgs = {"3"};
                database.update("stepCounts", dbValues, where, whereArgs);
                String queryString = "SELECT stepCounts.Count FROM stepCounts WHERE stepCounts.Minute" + " = 3";
                Cursor c = database.rawQuery(queryString, null);
                int id = c.getColumnIndex("Count");
                ArrayList<String> strings = new ArrayList<String>();
                for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    strings.add(c.getString(id));
                }
                String[] array = strings.toArray(new String[0]);
                String result = array[0];//c.getString(id);
                c.close();
            }

            else if (elapsedTimeInSeconds >= 180 && elapsedTimeInSeconds < 240) {
                float steps = calculateStepsSoFar(count4, 3, event.values[0]);
                count4.setText(String.valueOf(event.values[0]));
                ContentValues dbValues = new ContentValues();
                dbValues.put("Count", String.valueOf(steps));
                String where = "Minute" + " = ?";
                String[] whereArgs = {"4"};
                database.update("stepCounts", dbValues, where, whereArgs);
                String queryString = "SELECT stepCounts.Count FROM stepCounts WHERE stepCounts.Minute" + " = 4";
                Cursor c = database.rawQuery(queryString, null);
                int id = c.getColumnIndex("Count");
                ArrayList<String> strings = new ArrayList<String>();
                for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    strings.add(c.getString(id));
                }
                String[] array = strings.toArray(new String[0]);
                String result = array[0];//c.getString(id);
                c.close();
            }

            else if (elapsedTimeInSeconds >= 240 && elapsedTimeInSeconds < 300) {
                float steps = calculateStepsSoFar(count5, 4, event.values[0]);
                count5.setText(String.valueOf(event.values[0]));
                ContentValues dbValues = new ContentValues();
                dbValues.put("Count", String.valueOf(steps));
                String where = "Minute" + " = ?";
                String[] whereArgs = {"5"};
                database.update("stepCounts", dbValues, where, whereArgs);
                String queryString = "SELECT stepCounts.Count FROM stepCounts WHERE stepCounts.Minute" + " = 5";
                Cursor c = database.rawQuery(queryString, null);
                int id = c.getColumnIndex("Count");
                ArrayList<String> strings = new ArrayList<String>();
                for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    strings.add(c.getString(id));
                }
                String[] array = strings.toArray(new String[0]);
                String result = array[0];//c.getString(id);
                c.close();
            }

            else if (elapsedTimeInSeconds >= 300 && elapsedTimeInSeconds < 360) {
                float steps = calculateStepsSoFar(count6, 5, event.values[0]);
                count6.setText(String.valueOf(event.values[0]));
                ContentValues dbValues = new ContentValues();
                dbValues.put("Count", String.valueOf(steps));
                String where = "Minute" + " = ?";
                String[] whereArgs = {"6"};
                database.update("stepCounts", dbValues, where, whereArgs);
                String queryString = "SELECT stepCounts.Count FROM stepCounts WHERE stepCounts.Minute" + " = 6";
                Cursor c = database.rawQuery(queryString, null);
                int id = c.getColumnIndex("Count");
                ArrayList<String> strings = new ArrayList<String>();
                for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    strings.add(c.getString(id));
                }
                String[] array = strings.toArray(new String[0]);
                String result = array[0];//c.getString(id);
                c.close();
            }

            else if (elapsedTimeInSeconds >= 360 && elapsedTimeInSeconds < 420) {
                float steps = calculateStepsSoFar(count7, 6, event.values[0]);
                count7.setText(String.valueOf(event.values[0]));
                ContentValues dbValues = new ContentValues();
                dbValues.put("Count", String.valueOf(steps));
                String where = "Minute" + " = ?";
                String[] whereArgs = {"7"};
                database.update("stepCounts", dbValues, where, whereArgs);
                String queryString = "SELECT stepCounts.Count FROM stepCounts WHERE stepCounts.Minute" + " = 7";
                Cursor c = database.rawQuery(queryString, null);
                int id = c.getColumnIndex("Count");
                ArrayList<String> strings = new ArrayList<String>();
                for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    strings.add(c.getString(id));
                }
                String[] array = strings.toArray(new String[0]);
                String result = array[0];//c.getString(id);
                c.close();
            }

            else if (elapsedTimeInSeconds >= 420 && elapsedTimeInSeconds < 480) {
                float steps = calculateStepsSoFar(count8, 7, event.values[0]);
                currentTotalSteps = steps;
                count8.setText(String.valueOf(event.values[0]));
                ContentValues dbValues = new ContentValues();
                dbValues.put("Count", String.valueOf(steps));
                String where = "Minute" + " = ?";
                String[] whereArgs = {"8"};
                database.update("stepCounts", dbValues, where, whereArgs);
                String queryString = "SELECT stepCounts.Count FROM stepCounts WHERE stepCounts.Minute" + " = 8";
                Cursor c = database.rawQuery(queryString, null);
                int id = c.getColumnIndex("Count");
                ArrayList<String> strings = new ArrayList<String>();
                for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    strings.add(c.getString(id));
                }
                String[] array = strings.toArray(new String[0]);
                String result = array[0];//c.getString(id);
                c.close();
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
