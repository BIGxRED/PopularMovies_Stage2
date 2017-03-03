package com.example.android.popularmovies_stage2.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.example.android.popularmovies_stage2.MovieFetcher;
import com.example.android.popularmovies_stage2.data.MovieContract;

public class MovieTask {

    public static final String TAG = "MovieTask";

    synchronized public static void syncMovies(Context context){

        MovieFetcher.setMethodFlag(0);
        ContentValues[] popularMovies = MovieFetcher.fetchMovies();
        MovieFetcher.setMethodFlag(1);
        ContentValues[] topRatedMovies = MovieFetcher.fetchMovies();

        ContentResolver resolver = context.getContentResolver();

            if (popularMovies != null && popularMovies.length > 0 && topRatedMovies != null && topRatedMovies.length > 0){
                int popularMoviesCount = resolver.bulkInsert(MovieContract.MovieTable.CONTENT_URI, popularMovies);
                int topRatedMoviesCount = resolver.bulkInsert(MovieContract.MovieTable.CONTENT_URI, topRatedMovies);
                Log.i(TAG, "Number of popular movies inserted: " + popularMoviesCount);
                Log.i(TAG, "Number of top rated movies inserted: " + topRatedMoviesCount);
            }

    }
}
