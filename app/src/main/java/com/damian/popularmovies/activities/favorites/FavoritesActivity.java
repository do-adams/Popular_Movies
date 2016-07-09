/*
 * Copyright (C) 2016 Damián Adams
 */

package com.damian.popularmovies.activities.favorites;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.damian.popularmovies.R;
import com.damian.popularmovies.activities.details.DetailsActivity;
import com.damian.popularmovies.data.Movie;
import com.damian.popularmovies.database.CupboardSQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import nl.qbusict.cupboard.QueryResultIterable;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * Activity responsible for displaying the user's favorite movies
 */

public class FavoritesActivity extends AppCompatActivity {

    private static final String TAG = FavoritesActivity.class.getSimpleName();

    private SQLiteDatabase mDb;

    private Button mResetButton;
    private ListView mListView;
    private ArrayAdapter<Movie> mArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        //Open the database
        CupboardSQLiteOpenHelper dbHelper = new CupboardSQLiteOpenHelper(FavoritesActivity.this);
        mDb = dbHelper.getReadableDatabase();

        //TODO: This query should be moved off of the main thread
        Cursor cursor = cupboard().withDatabase(mDb).query(Movie.class).getCursor();

        if (cursor != null) {
            List<Movie> movieTitles = new ArrayList<>();
            QueryResultIterable<Movie> itr = cupboard().withCursor(cursor).iterate(Movie.class);
            for (Movie movie : itr) {
                movieTitles.add(movie);
            }
            itr.close();

            mListView = (ListView) findViewById(R.id.favorites_list_view);
            mArrayAdapter = new ArrayAdapter<Movie>(this,
                    android.R.layout.simple_list_item_1, android.R.id.text1,
                    movieTitles);

            mListView.setAdapter(mArrayAdapter);
            mListView.setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Movie movie = (Movie) parent.getItemAtPosition(position);

                    Intent intent = new Intent(FavoritesActivity.this, DetailsActivity.class)
                            .putExtra(Movie.MOVIE_KEY, movie);
                    startActivity(intent);
                }
            });
        }

        mResetButton = (Button) findViewById(R.id.favorites_reset_button);
        mResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Deletes all movie entries
                cupboard().withDatabase(mDb).delete(Movie.class, null);
                mArrayAdapter.clear();
                mArrayAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (mDb != null && mDb.isOpen())
            mDb.close();
        super.onDestroy();
    }
}
