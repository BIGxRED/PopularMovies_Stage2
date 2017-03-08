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

    //TODO: Need to update this so that both popular and top-rated movies are obtained
    synchronized public static void syncMovies(Context context){

//        MovieFetcher.setMethodFlag(0);
//        ContentValues[] popularMovies = MovieFetcher.fetchMovies(context);
//        MovieFetcher.setMethodFlag(1);
        ContentValues[] newMovies = MovieFetcher.fetchMovies(context);

        Log.i(TAG, "Current value of sorting option: " + SettingsFragment.getPreferenceValue(context,
                context.getString(R.string.list_preference_sorting_options_key)));

        ContentResolver resolver = context.getContentResolver();

//            if (popularMovies != null && popularMovies.length > 0 && topRatedMovies != null && topRatedMovies.length > 0){
//                int popularMoviesCount = resolver.bulkInsert(MovieContract.MovieTable.CONTENT_URI, popularMovies);
//                int topRatedMoviesCount = resolver.bulkInsert(MovieContract.MovieTable.CONTENT_URI, topRatedMovies);
//                Log.i(TAG, "Number of popular movies inserted: " + popularMoviesCount);
//                Log.i(TAG, "Number of top rated movies inserted: " + topRatedMoviesCount);
//            }

        if (newMovies != null && newMovies.length > 0){
            int newMoviesCount = resolver.bulkInsert(MovieContract.MovieTable.CONTENT_URI, newMovies);
            Log.i(TAG, "Number of new movies inserted: " + newMoviesCount);
        }

    }
}
