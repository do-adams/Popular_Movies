/*
 * Copyright (C) 2016 Damián Adams
 */

package com.damian.popularmovies.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.damian.popularmovies.activities.details.DetailsActivity;
import com.damian.popularmovies.activities.MainActivity;
import com.damian.popularmovies.R;
import com.damian.popularmovies.data.Movie;
import com.squareup.picasso.Picasso;

/**
 * Adapter for setting Movie data and behavior into a RecyclerView.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private Context mContext;
    private Movie[] mMovies;

    public MovieAdapter(Context context, Movie[] movies) {
        mContext = context;
        mMovies = movies;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_posters, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, final int position) {
        holder.bindMoviePoster(mMovies[position]);

        holder.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) mContext;

                if (mainActivity.isTwoPane()) {
                    mainActivity.launchDetailsFragmentForTwoPane(mMovies[position]);
                } else {
                    Intent intent = new Intent(mContext, DetailsActivity.class);
                    intent.putExtra(Movie.MOVIE_KEY, mMovies[position]);
                    mContext.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMovies.length;
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder {
        private ImageView mImageView;

        public MovieViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.movie_poster_imageView);
        }

        public void bindMoviePoster(Movie movie) {
            Picasso.with(mContext).load(movie.getMoviePoster()).into(mImageView);
        }

        /**
         * Returns the View assigned to its corresponding ViewHolder.
         */
        public View getView() {
            return itemView;
        }
    }
}
