package com.aware.plugin.charging_monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.database.Cursor;

import com.aware.plugin.charging_monitor.Provider.Charging_Monitor_Data;

import com.aware.providers.Battery_Provider;
import com.aware.ui.Stream_UI;
import com.aware.utils.IContextCard;

public class ContextCard implements IContextCard {

    //Set how often your card needs to refresh if the stream is visible (in milliseconds)
    private int refresh_interval = 1 * 1000; //1s

    private Handler uiRefresher = new Handler(Looper.getMainLooper());
    private Runnable uiChanger = new Runnable() {
        @Override
        public void run() {

            boolean is_charging = false;
            Cursor currentCharging = sContext.getContentResolver().query(Battery_Provider.Battery_Data.CONTENT_URI, new String[]{Battery_Provider.Battery_Data.STATUS}, null, null, Battery_Provider.Battery_Data.TIMESTAMP + " DESC LIMIT 1");
            if( currentCharging != null && currentCharging.moveToFirst() ) {
                is_charging = (currentCharging.getInt(currentCharging.getColumnIndex(Battery_Provider.Battery_Data.STATUS))!=3);
            }
            if( currentCharging != null && ! currentCharging.isClosed() ) currentCharging.close();

            Cursor latest = sContext.getContentResolver().query(Provider.Charging_Monitor_Data.CONTENT_URI, null, null, null, Charging_Monitor_Data.TIMESTAMP + " DESC LIMIT 1");
            if (latest != null && latest.moveToFirst()) {
                if( card != null ) {
                    counter_txt.setVisibility(View.GONE);
                    counter_txt2.setVisibility(View.VISIBLE);
                    counter_txt3.setVisibility(View.VISIBLE);
                    counter_txt4.setVisibility(View.VISIBLE);
                    counter_txt5.setVisibility(View.VISIBLE);
                    if( is_charging ) {
                        counter_txt2.setTextColor(Color.parseColor("#ff00a33c"));
                        counter_txt3.setTextColor(Color.parseColor("#ff00a33c"));
                        counter_txt4.setTextColor(Color.parseColor("#ff00a33c"));
                        counter_txt5.setTextColor(Color.parseColor("#ff00a33c"));

                        counter_txt2.setText("Charging");
                        counter_txt3.setText(latest.getInt(latest.getColumnIndex(Provider.Charging_Monitor_Data.PERCENTAGE_START)) + "->");
                        counter_txt4.setText(""+latest.getInt(latest.getColumnIndex(Provider.Charging_Monitor_Data.PERCENTAGE_END)));
                        counter_txt5.setText(String.format("%.3f", latest.getDouble(latest.getColumnIndex(Provider.Charging_Monitor_Data.SPEED))) + " %/min");
                    } else {
                        counter_txt2.setTextColor(Color.RED);
                        counter_txt3.setTextColor(Color.RED);
                        counter_txt4.setTextColor(Color.RED);
                        counter_txt5.setTextColor(Color.RED);
                        counter_txt2.setText("Discharging");
                        counter_txt3.setText(latest.getInt(latest.getColumnIndex(Provider.Charging_Monitor_Data.PERCENTAGE_START_DISCHARGE))+ "->");
                        counter_txt4.setText(""+latest.getInt(latest.getColumnIndex(Charging_Monitor_Data.PERCENTAGE_END_DISCHARGE)));
                        counter_txt5.setText(String.format("%.3f", latest.getDouble(latest.getColumnIndex(Provider.Charging_Monitor_Data.SPEED_DISCHARGE))) + " %/min");
                    }
                }
            } else {
                counter_txt.setVisibility(View.VISIBLE);
                counter_txt.setText("Please connect to charger to begin.");

                counter_txt2.setVisibility(View.GONE);
                counter_txt3.setVisibility(View.GONE);
                counter_txt4.setVisibility(View.GONE);
                counter_txt5.setVisibility(View.GONE);

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
    private TextView counter_txt2; // charging label
    private TextView counter_txt3; //start
    private TextView counter_txt4; //end
    private TextView counter_txt5; //rate

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
        counter_txt = (TextView) card.findViewById(R.id.data_available);
        counter_txt2 = (TextView) card.findViewById(R.id.charging_state);
        counter_txt3 = (TextView) card.findViewById(R.id.start);
        counter_txt4 = (TextView) card.findViewById(R.id.end);
        counter_txt5 = (TextView) card.findViewById(R.id.rate);
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
