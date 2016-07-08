/*
 * Copyright (C) 2016 Damián Adams
 */

package com.damian.popularmovies.activities.details;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.damian.popularmovies.R;

/**
 * Activity used to launch a fragment for displaying Movie data to the user.
 */

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.details_container, new DetailsFragment())
                    .commit();
        }
    }
}
