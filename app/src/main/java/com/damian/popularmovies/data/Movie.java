/*
 * Copyright (C) 2016 Damián Adams
 */

package com.damian.popularmovies.data;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Provides an abstraction for movie data.
 */

public class Movie implements Parcelable {

    public static final String IMAGE_WIDTH = "w342";
    //Key used for Parcelable storage in Intent bundle
    public static final String MOVIE_KEY = "Movie";

    private String mTitle;
    private String mMoviePoster;
    private String mOverview;
    private String mUserRating;
    private String mReleaseDate;
    private String mId;

    private Long _id; //field used for Cupboard, mirrors mId

    /**
     * Trailer Urls are set using a String array but stored as a json String.
     */
    private String mTrailerUrls;

    /**
     * Reviews are set using an array of Review objects but stored as a json String.
     */
    private String mReviews;

    /**
     * Empty constructor used only for querying the database with an ORM.
     * Do NOT use to instantiate Movie objects.
     */
    public Movie() {

    }

    /**
     * Constructs a Movie object when provided with a parsed
     * JSON String that represents a single JSONObject entry
     * from the "results" JSONArray provided by a themovieDB API
     * popular/highest rated movies request.
     */
    public Movie(JSONObject movie) throws JSONException {
        mTitle = movie.getString("title");

        String posterPath = movie.getString("poster_path");
        Uri posterUrl = Uri.parse("https://image.tmdb.org/t/p/")
                .buildUpon()
                .appendPath(IMAGE_WIDTH)
                .appendEncodedPath(posterPath)
                .build();
        mMoviePoster = posterUrl.toString();

        mOverview = movie.getString("overview");
        mUserRating = movie.getString("vote_average");
        mReleaseDate = movie.getString("release_date");

        mId = movie.getString("id");
        _id = Long.parseLong(mId); //each movie has a particular id in the database
    }

    /**
     * Returns formatted user rating in a scale of 1-10.
     */
    public String getTotalRating() {
        return mUserRating + "/10";
    }

    /**
     * Returns the year of the movie release
     */
    public String getReleaseYear() {
        return mReleaseDate.substring(0, 4);
    }

    /**
     * Returns the title, for easy use with ArrayAdapters
     */
    @Override
    public String toString() {
        return mTitle;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getMoviePoster() {
        return mMoviePoster;
    }

    public void setMoviePoster(String moviePoster) {
        mMoviePoster = moviePoster;
    }

    public String getOverview() {
        return mOverview;
    }

    public void setOverview(String overview) {
        mOverview = overview;
    }

    public String getUserRating() {
        return mUserRating;
    }

    public void setUserRating(String userRating) {
        mUserRating = userRating;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        mReleaseDate = releaseDate;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    /**
     * Returns a String array of ready-to-use Trailer URLs, de-serialized from their json form.
     */
    public String[] getTrailerUrls() {
        return new Gson().fromJson(mTrailerUrls, String[].class);
    }

    /**
     * Takes a String array of Trailer URLs, and serializes it into json data for db storage.
     */
    public void setTrailerUrls(String[] trailerUrls) {
        mTrailerUrls = new Gson().toJson(trailerUrls);
    }

    /**
     * Returns a Review array of review objects, de-serialized from their json form.
     */
    public Review[] getReviews() {
        return new Gson().fromJson(mReviews, Review[].class);
    }

    /**
     * Takes an array of Review items, and serializes it into json data for db storage.
     */
    public void setReviews(Review[] reviews) {
        mReviews = new Gson().toJson(reviews);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeString(mMoviePoster);
        dest.writeString(mOverview);
        dest.writeString(mUserRating);
        dest.writeString(mReleaseDate);
        dest.writeString(mId);
        dest.writeLong(_id);
        dest.writeString(mTrailerUrls);
        dest.writeString(mReviews);
    }

    private Movie(Parcel source) {
        mTitle = source.readString();
        mMoviePoster = source.readString();
        mOverview = source.readString();
        mUserRating = source.readString();
        mReleaseDate = source.readString();
        mId = source.readString();
        _id = source.readLong();
        mTrailerUrls = source.readString();
        mReviews = source.readString();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
