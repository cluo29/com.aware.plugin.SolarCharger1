package com.aware.plugin.charging_monitor;

import android.content.Intent;
import android.util.Log;
import android.media.MediaPlayer;
import android.content.ContentValues;

import android.database.Cursor;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.utils.Aware_Plugin;
import com.aware.plugin.charging_monitor.Provider.Charging_Monitor_Data;
import com.aware.providers.Battery_Provider.Battery_Charges;
import com.aware.providers.Battery_Provider.Battery_Data;
import com.aware.providers.Battery_Provider.Battery_Discharges;

public class Plugin extends Aware_Plugin {

    public static final String ACTION_AWARE_PLUGIN_CHARGING_MONITOR = "ACTION_AWARE_PLUGIN_CHARGING_MONITOR";

    public static final String EXTRA_DATA = "data";

    private static int charger_type = 0; //1=solar, 2= pc ,3=ac

    private static int percentage_start = -1; // shall be 0 - 100

    private static int percentage_end = -1;

    private static long time_start = -1;

    private static long time_end = -1;

    private static double speed = 0;

    private static long time_discharge = -1;

    private static double speed_discharge = 0;

    private static int percentage_start_discharge = -1; // shall be 0 - 100

    private static int percentage_end_discharge = 101;

    public static ContextProducer context_producer;

    private static MediaPlayer mp;

    private static boolean music_started = false;

    private static boolean music_end = false;


    private static ContentValues data;

    public Thread music_thread = new Thread(){
        public void run() {

            mp = MediaPlayer.create(getApplicationContext(), R.raw.music);
            try {
                mp.setLooping(false);
                mp.start();
                music_end = false;
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                       public void onCompletion(MediaPlayer mp) {
                           mp.release();
                           music_end = true;
                       }
                   }
                );
                mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        return false;
                    }
                });
            } catch (IllegalArgumentException e) {
            } catch (IllegalStateException e) {
            }
        }
    };

    public Thread solar_thread = new Thread(){
        public void run(){
            while(Aware.getSetting(getApplicationContext(), Settings.STATUS_PLUGIN_CHARGING_MONITOR).equals("true")){

                charger_type = Integer.parseInt(Aware.getSetting(getApplicationContext(), Settings.MODE_PLUGIN_CHARGING_MONITOR));
                Cursor PL = getApplicationContext().getContentResolver().query(Battery_Data.CONTENT_URI, null, null, null, Battery_Data.TIMESTAMP + " DESC LIMIT 1");
                if (PL != null && PL.moveToFirst()) {
                    int currentLevel = PL.getInt(PL.getColumnIndex(Battery_Data.LEVEL));
                    if(currentLevel==100)
                    //charging ends with 100
                    {
                        //play music!
                        if(!music_started) {
                            music_thread.start();
                            music_started = true;
                        }
                        else if(music_end)
                        {
                            music_thread.run();
                        }
                    }
                }
                if (PL != null && !PL.isClosed()) {
                    PL.close();
                }

                //record when it starts charging
                Cursor BC = getApplicationContext().getContentResolver().query(Battery_Charges.CONTENT_URI, null, null, null, Battery_Charges.TIMESTAMP + " DESC LIMIT 1");
                if (BC != null && BC.moveToFirst()) {
                    time_start = BC.getLong(BC.getColumnIndex(Battery_Charges.TIMESTAMP));
                    percentage_start = BC.getInt(BC.getColumnIndex(Battery_Charges.BATTERY_START));
                    long time_now = System.currentTimeMillis();
                    long time_spent = time_now - time_start;  //mseconds here
                    if(time_spent<20*1000.0) {
                        percentage_end = percentage_start;
                    }
                }
                if (BC != null && !BC.isClosed()) {
                    BC.close();
                }

                Cursor BL = getApplicationContext().getContentResolver().query(Battery_Data.CONTENT_URI, null, null, null, Battery_Data.TIMESTAMP + " DESC LIMIT 1");
                if (BL != null && BL.moveToFirst()) {
                    int currentLevel = BL.getInt(BL.getColumnIndex(Battery_Data.LEVEL));
                    if(currentLevel>percentage_end)
                    //battery level grows
                    {
                        int gained_percentage = currentLevel - percentage_start;
                        long time_now = System.currentTimeMillis();
                        long time_spent = time_now - time_start;  //mseconds here
                        double time_minutes = time_spent / 60000.0;  ////round to minutes
                        speed = gained_percentage / time_minutes;
                        percentage_end = currentLevel;
                        if( DEBUG ) {
                            Log.d(TAG, "Gained_percentage = " + gained_percentage);
                            Log.d(TAG, "time_start = " + time_start);
                            Log.d(TAG, "time_now = " + time_now);
                            Log.d(TAG, "time_spent = " + time_spent);
                            Log.d(TAG, "time_minutes = " + time_minutes);
                            Log.d(TAG, "speed = " + speed);
                        }
                    }
                }
                if (BL != null && !BL.isClosed()) {
                    BL.close();
                }

                Cursor BD = getApplicationContext().getContentResolver().query(Battery_Discharges.CONTENT_URI, null, null, null, Battery_Discharges.TIMESTAMP + " DESC LIMIT 1");
                if (BD != null && BD.moveToFirst()) {
                    time_discharge = BD.getLong(BD.getColumnIndex(Battery_Discharges.TIMESTAMP));
                    percentage_start_discharge = BD.getInt(BD.getColumnIndex(Battery_Discharges.BATTERY_START));

                    long time_now = System.currentTimeMillis();
                    long time_spent = time_now - time_discharge;  //mseconds here
                    if(time_spent<20*1000.0) {
                        percentage_end_discharge = percentage_start_discharge;
                    }
                }
                if (BD != null && !BD.isClosed()) {
                    BD.close();
                }

                Cursor BLD = getApplicationContext().getContentResolver().query(Battery_Data.CONTENT_URI, null, null, null, Battery_Data.TIMESTAMP + " DESC LIMIT 1");
                if (BLD != null && BLD.moveToFirst()) {
                    int currentLevel = BLD.getInt(BLD.getColumnIndex(Battery_Data.LEVEL));
                    if(currentLevel < percentage_end_discharge)
                    //battery level decreases
                    {
                        int lost_percentage = percentage_start_discharge - currentLevel;
                        long time_now = System.currentTimeMillis();
                        long time_spent = time_now - time_discharge;  //mseconds here
                        double time_mintues = time_spent / 60000.0;  ////round to minutes
                        speed_discharge = lost_percentage / time_mintues;
                        percentage_end_discharge = currentLevel;
                    }
                }
                if (BLD != null && !BLD.isClosed()) {
                    BLD.close();
                }

                time_end = System.currentTimeMillis();

                data = new ContentValues();
                data.put(Provider.Charging_Monitor_Data.TIMESTAMP, System.currentTimeMillis());
                data.put(Provider.Charging_Monitor_Data.DEVICE_ID, Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID));
                data.put(Provider.Charging_Monitor_Data.CHARGER_TYPE, charger_type);
                data.put(Provider.Charging_Monitor_Data.PERCENTAGE_START, percentage_start);
                data.put(Provider.Charging_Monitor_Data.PERCENTAGE_END, percentage_end);
                data.put(Provider.Charging_Monitor_Data.TIME_START, time_start);
                data.put(Provider.Charging_Monitor_Data.SPEED, speed);
                data.put(Charging_Monitor_Data.PERCENTAGE_START_DISCHARGE, percentage_start_discharge);
                data.put(Charging_Monitor_Data.PERCENTAGE_END_DISCHARGE, percentage_end_discharge);
                data.put(Provider.Charging_Monitor_Data.TIME_DISCHARGE, time_discharge);
                data.put(Provider.Charging_Monitor_Data.SPEED_DISCHARGE, speed_discharge);
                data.put(Provider.Charging_Monitor_Data.TIME_END, time_end);

                getContentResolver().insert(Provider.Charging_Monitor_Data.CONTENT_URI, data);

                //Share context
                context_producer.onContext();

                try {
                    Thread.sleep(6000);
                    //detect once every 6 secs
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();

        TAG = "AWARE::" + getResources().getString(R.string.app_name);
        DEBUG = Aware.getSetting(this, Aware_Preferences.DEBUG_FLAG).equals("true");

        CONTEXT_PRODUCER = new ContextProducer() {
            @Override
            public void onContext() {
                Intent context_solar_charger = new Intent();
                context_solar_charger.setAction(ACTION_AWARE_PLUGIN_CHARGING_MONITOR);
                context_solar_charger.putExtra(EXTRA_DATA, data);
                sendBroadcast(context_solar_charger);

                if( DEBUG ) {
                    Log.d(TAG,"Inserted: " + data.toString());
                }
            }
        };
        context_producer = CONTEXT_PRODUCER;
        solar_thread.start();

        if( Aware.getSetting(getApplicationContext(), Settings.STATUS_PLUGIN_CHARGING_MONITOR).length() == 0 ) {
            Aware.setSetting(getApplicationContext(), Settings.STATUS_PLUGIN_CHARGING_MONITOR, true);
        }
        if( Aware.getSetting(getApplicationContext(), Settings.MODE_PLUGIN_CHARGING_MONITOR).length() == 0 ) {
            Aware.setSetting(getApplicationContext(), Settings.MODE_PLUGIN_CHARGING_MONITOR, 1);
        }

        Aware.setSetting(this, Aware_Preferences.STATUS_BATTERY, true);
        Aware.startPlugin(this, getPackageName());

        sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DEBUG = Aware.getSetting(this, Aware_Preferences.DEBUG_FLAG).equals("true");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Aware.setSetting(this, Aware_Preferences.STATUS_BATTERY, false);
        Aware.stopPlugin(this, getPackageName());
    }
}
