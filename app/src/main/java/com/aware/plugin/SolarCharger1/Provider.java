package com.aware.plugin.SolarCharger1;

/**
 * Created by Comet on 06/05/15.
 */
import java.util.HashMap;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.util.Log;

import com.aware.Aware;
import com.aware.utils.DatabaseHelper;

public class Provider extends ContentProvider {
    public static final int DATABASE_VERSION = 5;
    /**
     * Provider authority: com.aware.plugin.SolarCharger1.provider.SolarCharger1
     */
    public static String AUTHORITY = "com.aware.plugin.SolarCharger1.provider.SolarCharger1";
    private static final int SOLARCHARGER1 = 1;
    private static final int SOLARCHARGER1_ID = 2;
    public static final String DATABASE_NAME = Environment.getExternalStorageDirectory() + "/AWARE/SolarCharger1.db";

    public static final String[] DATABASE_TABLES = {
            "plugin_SolarCharger1"
    };
    public static final class SolarCharger1_Data implements BaseColumns {
        private SolarCharger1_Data(){};

        public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/plugin_SolarCharger1");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware.plugin.SolarCharger1";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware.plugin.SolarCharger1";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String CHARGER_TYPE = "charger_type";   //1=solar, 2= pc ,3=ac
        public static final String PERCENTAGE_START = "percentage_start";    // power percentages
        public static final String PERCENTAGE_END = "percentage_end";    //
        public static final String TIME_START = "time_start";    //timestamp
        public static final String TIME_END = "time_end";   //
        public static final String SPEED = "double_speed";   // speed for this charge
        public static final String TIME_DISCHARGE = "time_discharge";   //
        public static final String SPEED_DISCHARGE = "double_speed_discharge";   // speed for discharge
        public static final String PERCENTAGE_START_DISCHARGE  = "percentage_start_discharge";    // power percentages
        public static final String PERCENTAGE_END_DISCHARGE  = "percentage_end_discharge";    //
    }
    public static final String[] TABLES_FIELDS = {
            SolarCharger1_Data._ID + " integer primary key autoincrement," +
                    SolarCharger1_Data.TIMESTAMP + " real default 0," +
                    SolarCharger1_Data.DEVICE_ID + " text default ''," +
                    SolarCharger1_Data.CHARGER_TYPE + " integer default 0," +
                    SolarCharger1_Data.PERCENTAGE_START + " integer default 0," +
                    SolarCharger1_Data.PERCENTAGE_END + " integer default 0," +
                    SolarCharger1_Data.TIME_START + " integer default 0," +
                    SolarCharger1_Data.TIME_END + " integer default 0," +
                    SolarCharger1_Data.SPEED + " real default 0," +
                    SolarCharger1_Data.TIME_DISCHARGE + " integer default 0," +
                    SolarCharger1_Data.SPEED_DISCHARGE + " real default 0," +
                    SolarCharger1_Data.PERCENTAGE_START_DISCHARGE + " integer default 0," +
                    SolarCharger1_Data.PERCENTAGE_END_DISCHARGE + " integer default 0," +
                    "UNIQUE("+ SolarCharger1_Data.TIMESTAMP+","+ SolarCharger1_Data.DEVICE_ID+")"
    };
    private static UriMatcher URIMatcher;
    private static HashMap<String, String> databaseMap;
    private static DatabaseHelper databaseHelper;
    private static SQLiteDatabase database;

    @Override
    public boolean onCreate() {
        AUTHORITY = getContext().getPackageName() + ".provider.SolarCharger1";

        URIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        URIMatcher.addURI(AUTHORITY, DATABASE_TABLES[0], SOLARCHARGER1);
        URIMatcher.addURI(AUTHORITY, DATABASE_TABLES[0] + "/#", SOLARCHARGER1_ID);
        databaseMap = new HashMap<String, String>();
        databaseMap.put(SolarCharger1_Data._ID, SolarCharger1_Data._ID);
        databaseMap.put(SolarCharger1_Data.TIMESTAMP, SolarCharger1_Data.TIMESTAMP);
        databaseMap.put(SolarCharger1_Data.DEVICE_ID, SolarCharger1_Data.DEVICE_ID);
        databaseMap.put(SolarCharger1_Data.CHARGER_TYPE, SolarCharger1_Data.CHARGER_TYPE);
        databaseMap.put(SolarCharger1_Data.PERCENTAGE_START, SolarCharger1_Data.PERCENTAGE_START);
        databaseMap.put(SolarCharger1_Data.PERCENTAGE_END, SolarCharger1_Data.PERCENTAGE_END);
        databaseMap.put(SolarCharger1_Data.TIME_START, SolarCharger1_Data.TIME_START);
        databaseMap.put(SolarCharger1_Data.TIME_END, SolarCharger1_Data.TIME_END);

        databaseMap.put(SolarCharger1_Data.SPEED, SolarCharger1_Data.SPEED);
        databaseMap.put(SolarCharger1_Data.TIME_DISCHARGE, SolarCharger1_Data.TIME_DISCHARGE);
        databaseMap.put(SolarCharger1_Data.SPEED_DISCHARGE, SolarCharger1_Data.SPEED_DISCHARGE);
        databaseMap.put(SolarCharger1_Data.PERCENTAGE_START_DISCHARGE, SolarCharger1_Data.PERCENTAGE_START_DISCHARGE);
        databaseMap.put(SolarCharger1_Data.PERCENTAGE_END_DISCHARGE, SolarCharger1_Data.PERCENTAGE_END_DISCHARGE);
        return true;
    }

    private boolean initializeDB() {

        if (databaseHelper == null) {

            databaseHelper = new DatabaseHelper( getContext(), DATABASE_NAME, null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS );

        }
        if( databaseHelper != null && ( database == null || ! database.isOpen() )) {
            database = databaseHelper.getWritableDatabase();

        }
        return( database != null && databaseHelper != null);
    }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return 0;
        }

        int count = 0;
        switch (URIMatcher.match(uri)) {
            case SOLARCHARGER1:
                count = database.delete(DATABASE_TABLES[0], selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (URIMatcher.match(uri)) {
            case SOLARCHARGER1:
                return SolarCharger1_Data.CONTENT_TYPE;
            case SOLARCHARGER1_ID:
                return SolarCharger1_Data.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }
    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        if (!initializeDB()) {
            Log.w(AUTHORITY, "Database unavailable...");
            return null;
        }

        ContentValues values = (initialValues != null) ? new ContentValues(
                initialValues) : new ContentValues();

        switch (URIMatcher.match(uri)) {
            case SOLARCHARGER1:
                long weather_id = database.insert(DATABASE_TABLES[0], SolarCharger1_Data.DEVICE_ID, values);

                if (weather_id > 0) {
                    Uri new_uri = ContentUris.withAppendedId(
                            SolarCharger1_Data.CONTENT_URI,
                            weather_id);
                    getContext().getContentResolver().notifyChange(new_uri,
                            null);
                    return new_uri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return null;
        }

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (URIMatcher.match(uri)) {
            case SOLARCHARGER1:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(databaseMap);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        try {
            Cursor c = qb.query(database, projection, selection, selectionArgs,
                    null, null, sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        } catch (IllegalStateException e) {
            if (Aware.DEBUG)
                Log.e(Aware.TAG, e.getMessage());
            return null;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return 0;
        }

        int count = 0;
        switch (URIMatcher.match(uri)) {
            case SOLARCHARGER1:
                count = database.update(DATABASE_TABLES[0], values, selection,
                        selectionArgs);
                break;
            default:

                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}