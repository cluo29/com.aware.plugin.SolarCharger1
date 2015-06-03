package com.aware.plugin.charging_monitor;

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

    public static final String STATUS_PLUGIN_CHARGING_MONITOR = "status_plugin_charging_monitor";

    /**
     * 1 = Solar
     * 2 = USB
     * 3 = AC
     */
    public static final String MODE_PLUGIN_CHARGING_MONITOR = "mode_plugin_charging_monitor";

    private static CheckBoxPreference status;
    private static EditTextPreference mode;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        status = (CheckBoxPreference) findPreference(STATUS_PLUGIN_CHARGING_MONITOR);
        if( Aware.getSetting(getApplicationContext(), STATUS_PLUGIN_CHARGING_MONITOR).length() == 0 ) {
            Aware.setSetting(getApplicationContext(), STATUS_PLUGIN_CHARGING_MONITOR, true);
        }
        status.setChecked(Aware.getSetting(getApplicationContext(), STATUS_PLUGIN_CHARGING_MONITOR).equals("true"));

        mode = (EditTextPreference) findPreference(MODE_PLUGIN_CHARGING_MONITOR);
        if( Aware.getSetting(getApplicationContext(), MODE_PLUGIN_CHARGING_MONITOR).length() == 0 ) {
            Aware.setSetting(getApplicationContext(), MODE_PLUGIN_CHARGING_MONITOR, 1);
        }
        mode.setSummary("Mode " + Aware.getSetting(getApplicationContext(), MODE_PLUGIN_CHARGING_MONITOR));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if( preference.getKey().equals(MODE_PLUGIN_CHARGING_MONITOR)) {
            Aware.setSetting(getApplicationContext(), key, sharedPreferences.getString(key, "1"));
            String mode_setting = Aware.getSetting(getApplicationContext(), MODE_PLUGIN_CHARGING_MONITOR);
            String mode_summary = "";
            if( mode_setting.equals("1") ) {
                mode_summary = "Solar";
            }else if (mode_setting.equals("2") ) {
                mode_summary = "USB";
            }else if (mode_setting.equals("3") ) {
                mode_summary = "AC";
            }
            mode.setSummary("Mode " + mode_summary);
        }

        if( preference.getKey().equals(STATUS_PLUGIN_CHARGING_MONITOR) ) {
            boolean is_active = sharedPreferences.getBoolean(key, false);
            Aware.setSetting(getApplicationContext(), key, is_active);
            if( is_active ) {
                Aware.startPlugin(getApplicationContext(), getPackageName());
            } else {
                Aware.stopPlugin(getApplicationContext(), getPackageName());
            }
            status.setChecked(is_active);
        }
    }
}
