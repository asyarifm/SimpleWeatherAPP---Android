package com.example.mimos.simpleweatherapp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
 
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
 
public class DatabaseHandler extends SQLiteOpenHelper {
 
    //All Static variables
    //Database Version
    private static final int DATABASE_VERSION = 1;
 
    //Database Name
    private static final String DATABASE_NAME = "placeList";
 
    //Table name
    private static final String TABLE = "latlon";
 
    //Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LON = "lon";
 
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    //Creating Table
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_LAT + " TEXT,"
                + KEY_LON + " TEXT" + ")";
        db.execSQL(CREATE_TABLE);
    }
 
    //Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
 
        //Create tables again
        onCreate(db);
    }
 
    /**
     *All CRUD(Create, Read, Update, Delete) Operations
     */

    //Adding a Place
    public void addPlace(String lat, String lon) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_LAT, lat); // latitude
        values.put(KEY_LON, lon); // longitude
 
        // Inserting Row
        db.insert(TABLE, null, values);

        db.close(); // Closing database connection
    }
 
    //Getting record (id, latitude, longitude) of a place
    public String[] getPlace(int id) {
        String[] place = new String[3];
        SQLiteDatabase db = this.getReadableDatabase();
 
        Cursor cursor = db.query(TABLE, new String[] { KEY_ID,
                KEY_LAT, KEY_LON }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        place[0] = cursor.getString(0);
        place[1] = cursor.getString(1);
        place[2] = cursor.getString(2);

        return place;
    }
 
    //Getting record for All Places
    public ArrayList<List> getAllPlaces() {
        ArrayList<List> placeList = new ArrayList<List>();
        //Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE;
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        //looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                placeList.add(Arrays.asList(cursor.getString(0),cursor.getString(1),cursor.getString(2))); //id, lat, lon
            } while (cursor.moveToNext());
        }
 
        //return place list
        return placeList;
    }
 
    //Updating a single place record
    public int updatePlace(int id, String newLat, String newLon) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_LAT, newLat);
        values.put(KEY_LON, newLon);
 
        //updating row
        return db.update(TABLE, values, KEY_ID + " = ?",
                new String[] { String.valueOf(id) });
    }
 
    //Deleting single place
    public void deleteCity(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE, KEY_ID + " = ?",
                new String[] { String.valueOf(id) });
        db.close();
    }
 
    //Getting places Count
    public int getPlacesCount() {
        String countQuery = "SELECT  * FROM " + TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
 
        //return count
        return cursor.getCount();
    }
}