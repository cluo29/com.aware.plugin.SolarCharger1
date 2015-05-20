package com.aware.plugin.SolarCharger1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.database.Cursor;
import com.aware.plugin.SolarCharger1.Provider.SolarCharger1_Data;

import com.aware.ui.Stream_UI;
import com.aware.utils.IContextCard;

public class ContextCard implements IContextCard {

    //Set how often your card needs to refresh if the stream is visible (in milliseconds)
    private int refresh_interval = 1 * 1000; //1 second = 1000 milliseconds

    //DEMO: we are demo'ing a counter incrementing in real-time
    //private int counter = 0;
    private int time_end = 0;
    private Handler uiRefresher = new Handler(Looper.getMainLooper());
    private Runnable uiChanger = new Runnable() {
        @Override
        public void run() {
            //counter++;
            Cursor latest = sContext.getContentResolver().query(SolarCharger1_Data.CONTENT_URI, null, null, null, SolarCharger1_Data.TIMESTAMP + " DESC LIMIT 1");
            if (latest != null && latest.moveToFirst()) {

                if( card != null ) {
                    //DEMO display the counter value
                    //counter_txt.setText("time start: "+latest.getLong(latest.getColumnIndex(SolarCharger1_Data.TIME_START)));
                    //counter_txt2.setText("time now: "+latest.getLong(latest.getColumnIndex(SolarCharger1_Data.TIME_END)));
                    counter_txt2.setText("systems functional");
                    counter_txt3.setText("charging % start: "+latest.getInt(latest.getColumnIndex(SolarCharger1_Data.PERCENTAGE_START)));
                    counter_txt4.setText("charging % end: "+latest.getInt(latest.getColumnIndex(SolarCharger1_Data.PERCENTAGE_END)));
                    counter_txt5.setText("charging %/minute: " + String.format("%.5f", latest.getDouble(latest.getColumnIndex(SolarCharger1_Data.SPEED))));
                    counter_txt6.setText("discharging % start: "+latest.getInt(latest.getColumnIndex(SolarCharger1_Data.PERCENTAGE_START_DISCHARGE)));
                    counter_txt7.setText("discharging % end: "+latest.getInt(latest.getColumnIndex(SolarCharger1_Data.PERCENTAGE_END_DISCHARGE)));
                    counter_txt8.setText("discharging %/minute: " + String.format("%.5f", latest.getDouble(latest.getColumnIndex(SolarCharger1_Data.SPEED_DISCHARGE))));
                }
            } else {
                counter_txt.setText("no data");
            }
            if (latest != null && !latest.isClosed()) latest.close();
            //Reset timer and schedule the next card refresh
            uiRefresher.postDelayed(uiChanger, refresh_interval);
        }
    };

    //Empty constructor used to instantiate this card
    public ContextCard(){};

    //You may use sContext on uiChanger to do queries to databases, etc.
    private Context sContext;

    //Declare here all the UI elements you'll be accessing
    private View card;
    private TextView counter_txt;
    private TextView counter_txt2;
    private TextView counter_txt3;
    private TextView counter_txt4;
    private TextView counter_txt5;
    private TextView counter_txt6;
    private TextView counter_txt7;
    private TextView counter_txt8;
    //Used to load your context card
    private LayoutInflater sInflater;

    @Override
    public View getContextCard(Context context) {
        sContext = context;

        //Tell Android that you'll monitor the stream statuses
        IntentFilter filter = new IntentFilter();
        filter.addAction(Stream_UI.ACTION_AWARE_STREAM_OPEN);
        filter.addAction(Stream_UI.ACTION_AWARE_STREAM_CLOSED);
        context.registerReceiver(streamObs, filter);

        //Load card information to memory
        sInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        card = sInflater.inflate(R.layout.card, null);

        //Initialize UI elements from the card
        //DEMO only
        counter_txt = (TextView) card.findViewById(R.id.textView);
        counter_txt2 = (TextView) card.findViewById(R.id.textView2);
        counter_txt3 = (TextView) card.findViewById(R.id.textView3);
        counter_txt4 = (TextView) card.findViewById(R.id.textView4);
        counter_txt5 = (TextView) card.findViewById(R.id.textView5);
        counter_txt6 = (TextView) card.findViewById(R.id.textView6);
        counter_txt7 = (TextView) card.findViewById(R.id.textView7);
        counter_txt8 = (TextView) card.findViewById(R.id.textView8);
        //Begin refresh cycle
        uiRefresher.postDelayed(uiChanger, refresh_interval);
        //Return the card to AWARE/apps
        return card;
    }
    //This is a BroadcastReceiver that keeps track of stream status. Used to stop the refresh when user leaves the stream and restart again otherwise
    private StreamObs streamObs = new StreamObs();
    public class StreamObs extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if( intent.getAction().equals(Stream_UI.ACTION_AWARE_STREAM_OPEN) ) {
                //start refreshing when user enters the stream
                uiRefresher.postDelayed(uiChanger, refresh_interval);

                //DEMO only, reset the counter every time the user opens the stream
                //counter = 0;
            }
            if( intent.getAction().equals(Stream_UI.ACTION_AWARE_STREAM_CLOSED) ) {
                //stop refreshing when user leaves the stream
                uiRefresher.removeCallbacks(uiChanger);
                uiRefresher.removeCallbacksAndMessages(null);
            }
        }
    }
}
