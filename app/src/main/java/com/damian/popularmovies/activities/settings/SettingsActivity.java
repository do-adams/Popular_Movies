/*
 * Copyright (C) 2016 Damián Adams
 */

package com.damian.popularmovies.activities.settings;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.damian.popularmovies.R;

/**
 * Activity for displaying the settings to the user.
 */

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (savedInstanceState == null)
            getFragmentManager().beginTransaction()
                    .add(R.id.settings_activity_container, new SettingsFragment()).commit();
    }
}
