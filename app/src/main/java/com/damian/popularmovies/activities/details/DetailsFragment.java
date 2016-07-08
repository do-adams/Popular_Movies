/*
 * Copyright (C) 2016 Damián Adams
 */

package com.damian.popularmovies.activities.details;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.damian.popularmovies.R;
import com.damian.popularmovies.activities.MainActivity;
import com.damian.popularmovies.activities.MainFragment;
import com.damian.popularmovies.data.Movie;
import com.damian.popularmovies.data.Review;
import com.damian.popularmovies.database.CupboardSQLiteOpenHelper;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * Fragment used to display relevant Movie information to the user.
 */

public class DetailsFragment extends Fragment {

    private static final String TAG = DetailsFragment.class.getSimpleName();
    private static final String YOUTUBE_BASE_URL = "https://www.youtube.com/watch?v=";

    private SQLiteDatabase mDb;
    private Activity mActivityContext;

    private LinearLayout mDetailsFragmentContainer;
    private TextView mTitle;
    private ImageView mPoster;
    private TextView mReleaseDate;
    private TextView mRatings;
    private Button mFavoriteButton;
    private TextView mOverview;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.details_fragment, container, false);

        mActivityContext = getActivity();
        CupboardSQLiteOpenHelper dbHelper = new CupboardSQLiteOpenHelper(mActivityContext);
        mDb = dbHelper.getWritableDatabase();

        mTitle = (TextView) rootView.findViewById(R.id.details_title);
        mPoster = (ImageView) rootView.findViewById(R.id.details_movie_poster);
        mReleaseDate = (TextView) rootView.findViewById(R.id.details_release_date);
        mRatings = (TextView) rootView.findViewById(R.id.details_ratings);
        mFavoriteButton = (Button) rootView.findViewById(R.id.details_mark_favorite_button);
        mOverview = (TextView) rootView.findViewById(R.id.details_overview);

        Movie movie = null;

        //if DetailFragment has been called from an Intent with movie data
        if (mActivityContext.getIntent().hasExtra(Movie.MOVIE_KEY)) {
            Intent intent = mActivityContext.getIntent();
            if (intent != null && intent.hasExtra(Movie.MOVIE_KEY))
                movie = intent.getParcelableExtra(Movie.MOVIE_KEY);
        }
        //if DetailFragment has been called from a Fragment Manager (tablet layout)
        else if (MainActivity.isTwoPane()) {
            Bundle bundle = getArguments();
            if (bundle != null)
                movie = bundle.getParcelable(Movie.MOVIE_KEY);
        }

        if (movie != null)
            rootView = createFragmentView(rootView, movie);

        return rootView;
    }

    /**
     * Sets the fields for the detail layout, and makes the calls for fetching the data for these fields.
     * Also creates the trailer and review views if available.
     * To be used in onCreateView method on the rootView.
     */
    private View createFragmentView(View rootView, final Movie movie) {
        mDetailsFragmentContainer = (LinearLayout) rootView.findViewById(R.id.details_fragment_container);
        mTitle.setText(movie.getTitle());
        Picasso.with(mActivityContext).load(movie.getMoviePoster()).into(mPoster);
        mReleaseDate.setText(movie.getReleaseYear());
        mRatings.setText(movie.getTotalRating());
        mOverview.setText(movie.getOverview());

        //checks if the movie is stored using its unique ID
        Movie checkIfStored = cupboard().withDatabase(mDb).get(Movie.class, Long.parseLong(movie.getId()));

        if (checkIfStored != null) { //if the movie item has been saved (favored) by the user
            mFavoriteButton.setVisibility(View.INVISIBLE);
        } else {
            mFavoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cupboard().withDatabase(mDb).put(movie);
                    Toast.makeText(mActivityContext, "Added to favorites!", Toast.LENGTH_SHORT).show();
                    mFavoriteButton.setVisibility(View.INVISIBLE);
                }
            });
        }

        //In the case that the current movie is from the db, we save ourselves an API call
        if (movie.getTrailerUrls() != null && movie.getReviews() != null) {
            Log.d(TAG, "Getting movie details from the database");
            setTrailerViews(movie);
            setReviewViews(movie);
        } else {
            Log.d(TAG, "Getting movie details from the web");
            getOnlineMovieContent(movie);
        }

        return rootView;
    }

    @Override
    public void onDestroyView() {
        if (mDb != null && mDb.isOpen()) //close the database
            mDb.close();
        super.onDestroyView();
    }

    /**
     * Makes the API call to get the movie trailers and reviews. Then stores this data in the movie object
     * and dynamically inserts views into our layout based on the available trailer and review results.
     */
    private void getOnlineMovieContent(final Movie movie) {
        OkHttpClient client = new OkHttpClient();

        Uri moviesUrl = new Uri.Builder()
                .scheme("https")
                .authority("api.themoviedb.org")
                .appendPath("3")
                .appendPath("movie")
                .appendPath(movie.getId())
                .appendQueryParameter("api_key", MainFragment.MOVIE_DB_API_KEY)
                .appendQueryParameter("append_to_response", "videos,reviews")
                .build();

        Log.d(TAG, moviesUrl.toString());

        Request request = new Request.Builder()
                .url(moviesUrl.toString())
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

                    movie.setTrailerUrls(getTrailerData(jsonData));
                    Log.d(TAG, Arrays.toString(movie.getTrailerUrls()));

                    movie.setReviews(getReviewData(jsonData));
                    for (int i = 0; i < movie.getReviews().length; i++) {
                        Log.d(TAG, movie.getReviews()[i].getAuthor() + "\n" + movie.getReviews()[i].getContent());
                    }

                    mActivityContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setTrailerViews(movie);
                            setReviewViews(movie);
                        }
                    });

                } else {
                    Log.e(TAG, "Error on themovieDB server response call");
                }
            }
        });
    }

    /**
     * Parses the API response into an array of String objects containing the Trailer URls
     * that can be used to store inside a Movie object.
     */
    private String[] getTrailerData(String jsonData) {
        String[] trailerUrls = null;
        try {
            JSONObject movieData = new JSONObject(jsonData);
            JSONArray videosData = movieData.getJSONObject("videos")
                    .getJSONArray("results");

            trailerUrls = new String[videosData.length()];

            for (int i = 0; i < videosData.length(); i++) {
                String key = videosData.getJSONObject(i)
                        .getString("key");
                String url = YOUTUBE_BASE_URL + key;
                trailerUrls[i] = url;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return trailerUrls;
    }

    /**
     * Parses the API response into an array of Review objects that can be stored in a Movie object.
     */
    private Review[] getReviewData(String jsonData) {
        Review[] reviews = null;
        try {
            JSONObject movieData = new JSONObject(jsonData);
            JSONArray reviewsData = movieData.getJSONObject("reviews")
                    .getJSONArray("results");

            reviews = new Review[reviewsData.length()];
            for (int i = 0; i < reviewsData.length(); i++) {
                JSONObject userReview = reviewsData.getJSONObject(i);
                reviews[i] = new Review(userReview.getString("author"), userReview.getString("content"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    /**
     * Uses a Movie object to extract the Trailer URLs and dynamically insert
     * special formatted views into the parent layout.
     */
    private void setTrailerViews(final Movie movie) {
        if (movie.getTrailerUrls() != null) {
            int trailerViewPosition = getViewPositionInLayout(R.id.details_trailers_view);

            for (int i = movie.getTrailerUrls().length - 1; i >= 0; i--) {
                LinearLayout trailerItem = (LinearLayout) LayoutInflater.from(mActivityContext).inflate(R.layout.trailer_item_layout, null);
                TextView trailerTextView = (TextView) trailerItem.findViewById(R.id.trailer_text_view);
                trailerTextView.setText("Trailer " + (i + 1));

                final int position = i;
                trailerItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(movie.getTrailerUrls()[position])));
                    }
                });

                mDetailsFragmentContainer.addView(trailerItem, trailerViewPosition);
                Log.d(TAG, "Added trailer view #" + (i + 1));
            }
        } else {
            Log.d(TAG, "Movie trailers URLs = null");
        }
    }

    /**
     * Gets the position of a given child inside the main LinearLayout of the DetailsFragment.
     * If the child is not found it returns the child count of the layout.
     */
    private int getViewPositionInLayout(int viewId) {
        int viewPosition = mDetailsFragmentContainer.getChildCount();

        for (int i = 0; i < mDetailsFragmentContainer.getChildCount(); i++) {
            int id = mDetailsFragmentContainer.getChildAt(i).getId();

            if (id == viewId) {
                viewPosition = i + 1;
            }
        }
        return viewPosition;
    }

    /**
     * Uses a Movie object to extract the Reviews information and dynamically insert special
     * formatted views into the parent layout.
     */
    private void setReviewViews(Movie movie) {
        if (movie.getReviews() != null) {
            int reviewViewPosition = getViewPositionInLayout(R.id.details_review_view);

            for (int i = movie.getReviews().length - 1; i >= 0; i--) {
                LinearLayout reviewItem = (LinearLayout) LayoutInflater.from(mActivityContext).inflate(R.layout.movie_review_item_layout, null);
                TextView author = (TextView) reviewItem.findViewById(R.id.movie_review_author_text_view);
                author.setText("Author: " + movie.getReviews()[i].getAuthor());
                TextView content = (TextView) reviewItem.findViewById(R.id.movie_review_content_text_view);
                content.setText(movie.getReviews()[i].getContent());

                mDetailsFragmentContainer.addView(reviewItem, reviewViewPosition);
                Log.d(TAG, "Added review view #" + (i + 1));
            }
        } else {
            Log.d(TAG, "Movie reviews = null");
        }
    }
}
