package com.cajama.malaria;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;


/**
 * Created by Jasper on 11/13/13.
 */
public class SettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 11)
            getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
        else
            addPreferencesFromResource(R.xml.preferences);
    }
}
