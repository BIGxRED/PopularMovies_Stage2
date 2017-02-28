package com.example.android.popularmovies_stage2.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.example.android.popularmovies_stage2.MovieFetcher;
import com.example.android.popularmovies_stage2.data.MovieContract;

public class MovieTask {

    public static final String TAG = "MovieTask";

    synchronized public static void syncMovies(Context context, int methodFlag){
        int pageNumber = 1;
        ContentValues[] movieData;

        while(pageNumber < 6){
            movieData = MovieFetcher.fetchMovies(methodFlag,pageNumber);

            if (movieData != null && movieData.length > 0){
                ContentResolver resolver = context.getContentResolver();
                int insertedMoviesCount = resolver.bulkInsert(MovieContract.MovieTable.CONTENT_URI,movieData);
                Log.i(TAG, "Number of movies inserted: " + insertedMoviesCount);
                Log.i(TAG, "Current value of pageNumber: " + pageNumber);
            }
            pageNumber++;
        }
    }
}
