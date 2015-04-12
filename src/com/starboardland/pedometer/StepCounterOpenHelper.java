package com.starboardland.pedometer;

import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.content.Context;
import android.database.Cursor;

/**
 * Created by Andy on 4/11/15.
 */
public class StepCounterOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String COUNTER_TABLE_NAME = "stepCounts";
    private static final String COUNTER_TABLE_CREATE =
            "CREATE TABLE " + COUNTER_TABLE_NAME + " (" +
                    "Minute" + " TEXT, " +
                    "Count" + " TEXT);";

    StepCounterOpenHelper(Context context) {
        super(context, COUNTER_TABLE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(COUNTER_TABLE_CREATE);
        //initialize minute labels here probably
        db.execSQL("INSERT INTO stepCounts(Minute,Count) VALUES (1,0)");
        db.execSQL("INSERT INTO stepCounts(Minute,Count) VALUES (2,0)");
        db.execSQL("INSERT INTO stepCounts(Minute,Count) VALUES (3,0)");
        db.execSQL("INSERT INTO stepCounts(Minute,Count) VALUES (4,0)");
        db.execSQL("INSERT INTO stepCounts(Minute,Count) VALUES (5,0)");
        db.execSQL("INSERT INTO stepCounts(Minute,Count) VALUES (6,0)");
        db.execSQL("INSERT INTO stepCounts(Minute,Count) VALUES (7,0)");
        db.execSQL("INSERT INTO stepCounts(Minute,Count) VALUES (8,0)");
        db.execSQL("INSERT INTO stepCounts(Minute,Count) VALUES (total,0)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int OldVersion, int NewVersion){
        return;
    }

    public Cursor query(String query, SQLiteDatabase db) {
        String select = null;
        if(query.length() > 1) {
            select = query;
        }

        return db.rawQuery(query, null);
    }
}