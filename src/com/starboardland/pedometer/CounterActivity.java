package com.starboardland.pedometer;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.*;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.common.api.GoogleApiClient;


public class CounterActivity extends FragmentActivity implements SensorEventListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

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

    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = CounterActivity.class.getSimpleName();
    private LocationRequest mLocationRequest;
    private GoogleMap mMap;

    long starttime = 0;
    //this  posts a message to the main thread from our timertask
    //and updates the textfield
    final Handler h = new Handler(new Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            long millis = System.currentTimeMillis() - starttime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds     = seconds % 60;

            //text.setText(String.format("%d:%02d", minutes, seconds));
            return false;
        }
    });
    //runs without timer be reposting self
    Handler h2 = new Handler();
    Runnable run = new Runnable() {

        @Override
        public void run() {
            long millis = System.currentTimeMillis() - starttime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds     = seconds % 60;

            //text3.setText(String.format("%d:%02d", minutes, seconds));

            h2.postDelayed(this, 500);
        }
    };

    //tells handler to send a message
    class firstTask extends TimerTask {

        @Override
        public void run() {
            h.sendEmptyMessage(0);
        }
    };

    //tells activity to run on ui thread
    class secondTask extends TimerTask {

        @Override
        public void run() {
            CounterActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    //start_time
                    long currentTime = System.nanoTime();
                    long elapsedTimeInSeconds = (currentTime - start_time) / 1000000000;
                    int seconds = (int) elapsedTimeInSeconds;
//                    long millis = System.currentTimeMillis() - starttime;
//                    int seconds = (int) (millis / 1000);
//                    int minutes = seconds / 60;
//                    seconds     = seconds % 60;
                    System.out.println("Seconds: " + seconds);
                    if(seconds % 60 == 0 && seconds != 0 && seconds <= 480) {
                        int minute = seconds/ 60;
                        Toast.makeText(getApplicationContext(), "You took " +  minuteSteps[minute-1] + " steps in segment " + minute + ".", Toast.LENGTH_SHORT).show();
                        if(seconds == 480) {
                            //get total
                            float totalNumber = 0;
                            for (Float f : minuteSteps) {
                                totalNumber += f;
                            }
                            textViews[8].setText(Float.toString(totalNumber));
                        }
                    }
                    //text2.setText(String.format("%d:%02d", minutes, seconds));
                }
            });
        }
    };


    Timer timer = new Timer();
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

        timer.schedule(new secondTask(),  0,1000);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        start_time = System.nanoTime();
        StepCounterOpenHelper db = new StepCounterOpenHelper(this);
        database = db.getWritableDatabase();

        setUpMapIfNeeded();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds
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
        mGoogleApiClient.connect();

    }

    @Override
    protected void onPause() {
        super.onPause();
        activityRunning = false;
        // if you unregister the last listener, the hardware will stop detecting step events
//        sensorManager.unregisterListener(this);
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
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
                textViews[minute].setText(Float.toString(totalNumber));
            }


        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected.");
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else {
            handleNewLocation(location);
        };
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
        //CameraUpdateFactory.zoomTo(15);
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }
}
