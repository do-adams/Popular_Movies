/*
 * Copyright (C) 2016 Damián Adams
 */

package com.damian.popularmovies.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.damian.popularmovies.R;
import com.damian.popularmovies.activities.details.DetailsFragment;
import com.damian.popularmovies.data.Movie;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static boolean mIsTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.details_container) != null) { //if on a two-pane layout
            mIsTwoPane = true;
            if (savedInstanceState == null) {
                getFragmentManager().beginTransaction()
                        .add(R.id.main_frame_container, new MainFragment())
                        .commit();
            }
        } else { //on regular phone layout
            mIsTwoPane = false;
            if (savedInstanceState == null) {
                getFragmentManager().beginTransaction()
                        .add(R.id.main_frame_container, new MainFragment())
                        .commit();
            }
        }
    }

    public static boolean isTwoPane() {
        return mIsTwoPane;
    }

    /**
     * Launches the details fragment, when on a two pane layout, with the relevant
     * movie object and proper fragment manager handling.
     */
    public void launchDetailsFragmentForTwoPane(Movie movie) {
        if (mIsTwoPane) {
            DetailsFragment detailsFragment = new DetailsFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(Movie.MOVIE_KEY, movie);
            detailsFragment.setArguments(bundle);

            //launches the DetailsFragment
            getFragmentManager().beginTransaction().replace(R.id.details_container, detailsFragment).commit();
        } else {
            Log.d(TAG, "Error: invalid call to launchDetailsFragmentForTwoPane");
        }
    }
}
