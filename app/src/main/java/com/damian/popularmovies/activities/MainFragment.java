/*
 * Copyright (C) 2016 Damián Adams
 */

package com.damian.popularmovies.activities;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.damian.popularmovies.BuildConfig;
import com.damian.popularmovies.R;
import com.damian.popularmovies.activities.favorites.FavoritesActivity;
import com.damian.popularmovies.activities.settings.SettingsActivity;
import com.damian.popularmovies.adapters.MovieAdapter;
import com.damian.popularmovies.data.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Fragment used to display the main movie content to the user.
 * Consists of a grid of movie posters.
 */

public class MainFragment extends Fragment {
    private static final String TAG = MainFragment.class.getSimpleName();
    public static final String MOVIE_DB_API_KEY = BuildConfig.MOVIE_DB_API_KEY;
    private static final String SCROLL_POSITION = "Scroll Position";

    private Activity mActivityContext;

    private RecyclerView mRecyclerView;
    private Movie[] mMovies;
    private Bundle mSavedInstanceState;

    //Flag for re-obtaining movie data (used for the SettingsActivity)
    private boolean refreshMovieData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View viewRoot = inflater.inflate(R.layout.main_fragment, container, false);
        mActivityContext = getActivity();

        mSavedInstanceState = savedInstanceState;

        mRecyclerView = (RecyclerView) viewRoot.findViewById(R.id.recyclerView);
        setUpMovieGrid();

        return viewRoot;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (refreshMovieData) {
            refreshMovieData = false;
            setUpMovieGrid();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mActivityContext.getMenuInflater().inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_settings) {
            refreshMovieData = true;
            startActivity(new Intent(mActivityContext, SettingsActivity.class));
            return true;
        } else if (id == R.id.menu_favorites) {
            startActivity(new Intent(mActivityContext, FavoritesActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Makes a threaded network call using themovieDB API,
     * parses the response (most popular or highest rated movies),
     * and "sets up" a RecyclerView Grid with the appropriate adapter to display
     * the Movie data (movie posters) to the user.
     */
    private void setUpMovieGrid() {
        if (isNetworkAvailable()) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(getMovieDBQueryUrl())
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Error connecting to themovieDB API server", e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String jsonData = response.body().string();
                    response.body().close();
                    Log.v(TAG, jsonData);

                    if (response.isSuccessful()) {
                        Log.d(TAG, "Successful HTTP Response");
                        mMovies = getMovies(jsonData);

                        if (mMovies != null) {
                            mActivityContext.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mRecyclerView.setAdapter(new MovieAdapter(mActivityContext, mMovies));
                                    GridLayoutManager gridLayoutManager = new GridLayoutManager(mActivityContext, 2);
                                    mRecyclerView.setLayoutManager(gridLayoutManager);
                                    mRecyclerView.setHasFixedSize(true);

                                    //On orientation change we retrieve the scroll position
                                    if (mSavedInstanceState != null) {
                                        Parcelable savedRecyclerLayoutState = mSavedInstanceState.getParcelable(SCROLL_POSITION);
                                        mRecyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
                                    }
                                }
                            });
                        }
                    } else {
                        Log.e(TAG, "Error on themovieDB server response call");
                    }
                }
            });
        } else {
            Toast.makeText(mActivityContext, "Please connect your device to the internet", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Checks to ensure the user can connect to the network,
     * as per Google's Android Developer guidelines. Returns true
     * if the network is "good to go!"
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) mActivityContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        //This method requires permission ACCESS_NETWORK_STATE
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            //Checks if a network is present and connected
            isAvailable = true;
        }
        return isAvailable;
    }

    /**
     * Returns a String representation of an URL with the relevant API GET request
     * for either the most popular or highest rated movies (varies by user settings).
     */
    private String getMovieDBQueryUrl() {
        String sortByPopularity = getString(R.string.sorting_value_popular);
        String sortByRating = getString(R.string.sorting_value_highest_rated);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mActivityContext);
        String sortBy = preferences.getString(getString(R.string.sorting_preference_key),
                sortByPopularity);

        //Sort by popularity by default
        Uri moviesUrl = new Uri.Builder()
                .scheme("https")
                .authority("api.themoviedb.org")
                .appendPath("3")
                .appendPath("movie")
                .appendPath("popular")
                .appendQueryParameter("api_key", MOVIE_DB_API_KEY)
                .build();

        if (sortBy.equals(sortByRating)) {
            moviesUrl = Uri.parse("https://api.themoviedb.org/3/movie")
                    .buildUpon()
                    .appendPath("top_rated")
                    .appendQueryParameter("api_key", MOVIE_DB_API_KEY)
                    .build();
        }

        Log.d(TAG, moviesUrl.toString());
        return moviesUrl.toString();
    }

    /**
     * Parses a String containing the full themovieDB API JSON response into
     * an array of Movie objects.
     */
    private Movie[] getMovies(String jsonData) {
        Movie[] movies = null;
        try {
            JSONObject moviesQuery = new JSONObject(jsonData);
            JSONArray moviesArray = moviesQuery.getJSONArray("results");

            movies = new Movie[moviesArray.length()];

            for (int i = 0; i < moviesArray.length(); i++) {
                movies[i] = new Movie(moviesArray.getJSONObject(i));
                Log.v(TAG, movies[i] + "\n");
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error while parsing JSON Data", e);
        }
        return movies;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //Save the recycler view's scroll position
        outState.putParcelable(SCROLL_POSITION, mRecyclerView.getLayoutManager().onSaveInstanceState());
        super.onSaveInstanceState(outState);
    }
}
