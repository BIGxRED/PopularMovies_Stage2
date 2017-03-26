/*
The following code is the property and sole work of Mike Palarz, a student at Udacity
 */

package com.example.android.popularmovies_stage2.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.example.android.popularmovies_stage2.MovieFetcher;
import com.example.android.popularmovies_stage2.R;
import com.example.android.popularmovies_stage2.data.MovieContract;
import com.example.android.popularmovies_stage2.fragments.SettingsFragment;

/*
This is a helper class which is responsible for making use of the MovieFetcher methods to
insert the data obtained from TMDB and inserting the data into the SQLite DB.
 */
public class MovieTask {

    public static final String TAG = "MovieTask";   //Tag used for debugging

    //This method is used for the initial startup of the app...
    synchronized public static void syncInitialMovies(Context context){

        //...Where we use the empty form of fetchMovies()
        ContentValues[] newMovies = MovieFetcher.fetchMovies();

        ContentResolver resolver = context.getContentResolver();

        //If we were successful in obtaining the movie data...
        if (newMovies != null && newMovies.length > 0){
            //...The data is inserted into the database
            resolver.bulkInsert(MovieContract.MovieTable.CONTENT_URI, newMovies);
        }

    }

    //This method is responsible for obtaining additional data for pagination...
    synchronized public static void syncAdditionalMovies(Context context){
        //...Where we use the non-empty form of fetchMovies()
        ContentValues[] newMovies = MovieFetcher.fetchMovies(context);

        ContentResolver resolver = context.getContentResolver();

        if (newMovies != null && newMovies.length > 0){
            resolver.bulkInsert(MovieContract.MovieTable.CONTENT_URI, newMovies);
        }
    }
}
