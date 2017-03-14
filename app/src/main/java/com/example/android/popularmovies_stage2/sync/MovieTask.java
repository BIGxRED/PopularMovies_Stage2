package com.example.android.popularmovies_stage2.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.example.android.popularmovies_stage2.MovieFetcher;
import com.example.android.popularmovies_stage2.R;
import com.example.android.popularmovies_stage2.data.MovieContract;
import com.example.android.popularmovies_stage2.fragments.SettingsFragment;

public class MovieTask {

    public static final String TAG = "MovieTask";

    synchronized public static void syncInitialMovies(Context context){

        ContentValues[] newMovies = MovieFetcher.fetchMovies();

        ContentResolver resolver = context.getContentResolver();

        if (newMovies != null && newMovies.length > 0){
            int newMoviesCount = resolver.bulkInsert(MovieContract.MovieTable.CONTENT_URI, newMovies);
            Log.i(TAG, "Number of new movies inserted: " + newMoviesCount);
        }

    }

    synchronized public static void syncAdditionalMovies(Context context){
        ContentValues[] newMovies = MovieFetcher.fetchMovies(context);

        Log.i(TAG, "Current value of sorting option: " + SettingsFragment.getPreferenceValue(context,
                context.getString(R.string.list_preference_sorting_options_key)));

        ContentResolver resolver = context.getContentResolver();

        if (newMovies != null && newMovies.length > 0){
            int newMoviesCount = resolver.bulkInsert(MovieContract.MovieTable.CONTENT_URI, newMovies);
            Log.i(TAG, "Number of new movies inserted: " + newMoviesCount);
        }
    }
}
