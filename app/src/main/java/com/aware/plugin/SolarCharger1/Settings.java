package com.aware.plugin.SolarCharger1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.aware.Aware;

public class Settings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String STATUS_PLUGIN_SOLARCHARGER1 = "status_plugin_SolarCharger1";

    //mode_plugin_SolarCharger1 "Mode 1 = Solar, 2 = PC USB, 3 = AC"

    public static final String MODE_PLUGIN_SOLARCHARGER1 = "mode_plugin_SolarCharger1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

    }

    //destroy
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //We are done, ask AWARE to apply new settings
        sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncSettings();
    }

    @Override
    protected void onPause() {
        super.onPause();
        syncSettings();
    }

    private void syncSettings() {
        //Make sure to load the latest values

        //public static final String STATUS_PLUGIN_SOLARCHARGER1 = "status_plugin_SolarCharger1";

        //mode_plugin_SolarCharger1

        //public static final String MODE_PLUGIN_SOLARCHARGER1 = "mode_plugin_SolarCharger1";


        CheckBoxPreference active = (CheckBoxPreference) findPreference(STATUS_PLUGIN_SOLARCHARGER1);
        if( Aware.getSetting(getApplicationContext(), STATUS_PLUGIN_SOLARCHARGER1).length() == 0 ) {
            Aware.setSetting(getApplicationContext(), STATUS_PLUGIN_SOLARCHARGER1, true);
        }

        active.setChecked(Aware.getSetting(getApplicationContext(), STATUS_PLUGIN_SOLARCHARGER1).equals("true"));

        EditTextPreference mode = (EditTextPreference) findPreference(MODE_PLUGIN_SOLARCHARGER1);

        if( Aware.getSetting(getApplicationContext(), MODE_PLUGIN_SOLARCHARGER1).length() == 0 ) {
            Aware.setSetting(getApplicationContext(), MODE_PLUGIN_SOLARCHARGER1, 1);
        }
        mode.setSummary("Mode " + Aware.getSetting(getApplicationContext(), MODE_PLUGIN_SOLARCHARGER1));


    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if( preference.getKey().equals(MODE_PLUGIN_SOLARCHARGER1)) {
            Aware.setSetting(getApplicationContext(), key, sharedPreferences.getString(key, "1"));
        }

        if( preference.getKey().equals(STATUS_PLUGIN_SOLARCHARGER1) ) {
            boolean is_active = sharedPreferences.getBoolean(key, false);
            Aware.setSetting(getApplicationContext(), key, is_active);
            if( is_active ) {
                Aware.startPlugin(getApplicationContext(), getPackageName());
            } else {
                Aware.stopPlugin(getApplicationContext(), getPackageName());
            }
        }
        //Update UI
        syncSettings();

    }
}
