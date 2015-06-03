package com.aware.plugin.charging_monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.database.Cursor;
import com.aware.plugin.charging_monitor.Provider.Charging_Monitor_Data;

import com.aware.ui.Stream_UI;
import com.aware.utils.IContextCard;

public class ContextCard implements IContextCard {

    //Set how often your card needs to refresh if the stream is visible (in milliseconds)
    private int refresh_interval = 30 * 1000; //30s

    private Handler uiRefresher = new Handler(Looper.getMainLooper());
    private Runnable uiChanger = new Runnable() {
        @Override
        public void run() {

            Cursor latest = sContext.getContentResolver().query(Provider.Charging_Monitor_Data.CONTENT_URI, null, null, null, Charging_Monitor_Data.TIMESTAMP + " DESC LIMIT 1");
            if (latest != null && latest.moveToFirst()) {
                if( card != null ) {
                    counter_txt2.setText("Charging Monitor");
                    counter_txt3.setText("Charging % start: "+latest.getInt(latest.getColumnIndex(Provider.Charging_Monitor_Data.PERCENTAGE_START)));
                    counter_txt4.setText("Charging % end: "+latest.getInt(latest.getColumnIndex(Provider.Charging_Monitor_Data.PERCENTAGE_END)));
                    counter_txt5.setText("Charging rate in %/minute: " + String.format("%.3f", latest.getDouble(latest.getColumnIndex(Provider.Charging_Monitor_Data.SPEED))));
                    counter_txt6.setText("Discharging % start: "+latest.getInt(latest.getColumnIndex(Provider.Charging_Monitor_Data.PERCENTAGE_START_DISCHARGE)));
                    counter_txt7.setText("Discharging % end: "+latest.getInt(latest.getColumnIndex(Charging_Monitor_Data.PERCENTAGE_END_DISCHARGE)));
                    counter_txt8.setText("Discharging rate in %/minute: " + String.format("%.3f", latest.getDouble(latest.getColumnIndex(Provider.Charging_Monitor_Data.SPEED_DISCHARGE))));
                }
            } else {
                counter_txt.setText("no data");
            }
            if (latest != null && !latest.isClosed()) latest.close();

            uiRefresher.postDelayed(uiChanger, refresh_interval);
        }
    };

    //Empty constructor used to instantiate this card
    public ContextCard(){}

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

    @Override
    public View getContextCard(Context context) {
        sContext = context;

        //Tell Android that you'll monitor the stream statuses
        IntentFilter filter = new IntentFilter();
        filter.addAction(Stream_UI.ACTION_AWARE_STREAM_OPEN);
        filter.addAction(Stream_UI.ACTION_AWARE_STREAM_CLOSED);
        context.registerReceiver(streamObs, filter);

        //Load card information to memory
        LayoutInflater sInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        card = sInflater.inflate(R.layout.card, new RelativeLayout(context));

        //Initialize UI elements from the card
        counter_txt = (TextView) card.findViewById(R.id.textView);
        counter_txt2 = (TextView) card.findViewById(R.id.textView2);
        counter_txt3 = (TextView) card.findViewById(R.id.textView3);
        counter_txt4 = (TextView) card.findViewById(R.id.textView4);
        counter_txt5 = (TextView) card.findViewById(R.id.textView5);
        counter_txt6 = (TextView) card.findViewById(R.id.textView6);
        counter_txt7 = (TextView) card.findViewById(R.id.textView7);
        counter_txt8 = (TextView) card.findViewById(R.id.textView8);

        //Begin refresh cycle
        uiRefresher.post(uiChanger);

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
                uiRefresher.post(uiChanger);
            }
            if( intent.getAction().equals(Stream_UI.ACTION_AWARE_STREAM_CLOSED) ) {
                //stop refreshing when user leaves the stream
                uiRefresher.removeCallbacks(uiChanger);
                uiRefresher.removeCallbacksAndMessages(null);
            }
        }
    }
}
