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
        int pageNumber = 1;
        ContentValues[] popularMovies;
        ContentValues[] topRatedMovies;
        ContentResolver resolver = context.getContentResolver();

        while(pageNumber < 6){
            MovieFetcher.setMethodFlag(0);
            popularMovies = MovieFetcher.fetchMovies(pageNumber);
            MovieFetcher.setMethodFlag(1);
            topRatedMovies = MovieFetcher.fetchMovies(pageNumber);


            if (popularMovies != null && popularMovies.length > 0 && topRatedMovies != null && topRatedMovies.length > 0){

                int popularMoviesCount = resolver.bulkInsert(MovieContract.MovieTable.CONTENT_URI,popularMovies);
                int topRatedMoviesCount = resolver.bulkInsert(MovieContract.MovieTable.CONTENT_URI,topRatedMovies);
                Log.i(TAG, "Number of popular movies inserted: " + popularMoviesCount);
                Log.i(TAG, "Number of top rated movies inserted: " + topRatedMoviesCount);
//                Log.i(TAG, "Current value of pageNumber: " + pageNumber);
            }
            pageNumber++;
        }
    }
}
