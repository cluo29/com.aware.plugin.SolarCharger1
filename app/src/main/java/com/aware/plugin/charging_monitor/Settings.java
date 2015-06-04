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
    public static final String SIZE_OF_PANEL = "size_of_panel";
    public static final String LUX_INSIDE_BOX = "lux_inside_box";
    public static final String TYPE_OF_LIGHT= "type_of_light";
    public static final String TYPE_OF_SOLAR_CELL= "type_of_solar_cell";
    public static final String MODE_PLUGIN_CHARGING_MONITOR = "mode_plugin_charging_monitor";
    public static final String SOLAR_CURRENT = "solar_current";

    private static CheckBoxPreference status;
    private static EditTextPreference size;
    private static EditTextPreference lux;
    private static EditTextPreference light;
    private static EditTextPreference cell;
    private static EditTextPreference mode;
    private static EditTextPreference current;

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

        size = (EditTextPreference) findPreference(SIZE_OF_PANEL);
        if( Aware.getSetting(getApplicationContext(), SIZE_OF_PANEL).length() == 0 ) {
            Aware.setSetting(getApplicationContext(), SIZE_OF_PANEL, "1");
        }
        size.setSummary("Size: " + Aware.getSetting(getApplicationContext(), SIZE_OF_PANEL));

        lux = (EditTextPreference) findPreference(LUX_INSIDE_BOX);
        if( Aware.getSetting(getApplicationContext(), LUX_INSIDE_BOX).length() == 0 ) {
            Aware.setSetting(getApplicationContext(), LUX_INSIDE_BOX, "1");
        }
        lux.setSummary("Lux: " + Aware.getSetting(getApplicationContext(), LUX_INSIDE_BOX));

        light = (EditTextPreference) findPreference(TYPE_OF_LIGHT);
        if( Aware.getSetting(getApplicationContext(), TYPE_OF_LIGHT).length() == 0 ) {
            Aware.setSetting(getApplicationContext(), TYPE_OF_LIGHT, "1");
        }
        light.setSummary("Type of light: " + Aware.getSetting(getApplicationContext(), TYPE_OF_LIGHT));

        cell = (EditTextPreference) findPreference(TYPE_OF_SOLAR_CELL);
        if( Aware.getSetting(getApplicationContext(), TYPE_OF_SOLAR_CELL).length() == 0 ) {
            Aware.setSetting(getApplicationContext(), TYPE_OF_SOLAR_CELL, "1");
        }
        cell.setSummary("Type of cell: " + Aware.getSetting(getApplicationContext(), TYPE_OF_SOLAR_CELL));

        mode = (EditTextPreference) findPreference(MODE_PLUGIN_CHARGING_MONITOR);
        if( Aware.getSetting(getApplicationContext(), MODE_PLUGIN_CHARGING_MONITOR).length() == 0 ) {
            Aware.setSetting(getApplicationContext(), MODE_PLUGIN_CHARGING_MONITOR, "1");
        }
        mode.setSummary("Mode: " + Aware.getSetting(getApplicationContext(), MODE_PLUGIN_CHARGING_MONITOR));

        current = (EditTextPreference) findPreference(SOLAR_CURRENT);
        if( Aware.getSetting(getApplicationContext(), SOLAR_CURRENT).length() == 0 ) {
            Aware.setSetting(getApplicationContext(), SOLAR_CURRENT, "1");
        }
        current.setSummary("Current: " + Aware.getSetting(getApplicationContext(), SOLAR_CURRENT));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if( preference.getKey().equals(MODE_PLUGIN_CHARGING_MONITOR)) {
            Aware.setSetting(getApplicationContext(), key, sharedPreferences.getString(key, "1"));
            mode.setSummary("Mode: " + sharedPreferences.getString(key,"1"));
        }
        if( preference.getKey().equals(SIZE_OF_PANEL)) {
            Aware.setSetting(getApplicationContext(), key, sharedPreferences.getString(key, "1"));
            size.setSummary("Size: " + sharedPreferences.getString(key, "1"));
        }
        if( preference.getKey().equals(LUX_INSIDE_BOX)) {
            lux.setSummary("Lux: " + sharedPreferences.getString(key, "1"));
            Aware.setSetting(getApplicationContext(), key, sharedPreferences.getString(key, "1"));
        }
        if( preference.getKey().equals(TYPE_OF_LIGHT)) {
            light.setSummary("Type of light: " + sharedPreferences.getString(key, "1"));
            Aware.setSetting(getApplicationContext(), key, sharedPreferences.getString(key, "1"));
        }
        if( preference.getKey().equals(TYPE_OF_SOLAR_CELL)) {
            cell.setSummary("Type of cell: " + sharedPreferences.getString(key, "1"));
            Aware.setSetting(getApplicationContext(), key, sharedPreferences.getString(key, "1"));
        }
        if( preference.getKey().equals(SOLAR_CURRENT)) {
            current.setSummary("Current: " + sharedPreferences.getString(key, "1"));
            Aware.setSetting(getApplicationContext(), key, sharedPreferences.getString(key, "1"));
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
