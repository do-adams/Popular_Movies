/*
 * Copyright (C) 2016 Damián Adams
 */

package com.damian.popularmovies.activities.details;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.damian.popularmovies.R;
import com.damian.popularmovies.data.Movie;

/**
 * Activity used to launch a fragment for displaying Movie data to the user.
 */

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            Movie movie = null;

            if (intent != null && intent.hasExtra(Movie.MOVIE_KEY))
                movie = intent.getParcelableExtra(Movie.MOVIE_KEY);

            DetailsFragment detailsFragment = new DetailsFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(Movie.MOVIE_KEY, movie);
            detailsFragment.setArguments(bundle);

            getFragmentManager().beginTransaction()
                    .add(R.id.details_container, detailsFragment)
                    .commit();
        }
    }
}
