package com.example.android.popularmovies_stage2.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import com.example.android.popularmovies_stage2.MovieFetcher;
import com.example.android.popularmovies_stage2.data.MovieContract;

public class MovieTask {

    synchronized public static void syncMovies(Context context, int methodFlag, int pageNumber){
        ContentValues[] movieData = MovieFetcher.fetchMovies(methodFlag,pageNumber);
        if (movieData != null && movieData.length > 0){
            ContentResolver resolver = context.getContentResolver();
            resolver.bulkInsert(MovieContract.MovieTable.CONTENT_URI,movieData);
        }
    }
}
