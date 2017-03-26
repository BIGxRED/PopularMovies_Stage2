/*
The following code is the property and sole work of Mike Palarz, a student at Udacity
 */

package com.example.android.popularmovies_stage2.sync;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.example.android.popularmovies_stage2.data.MovieContract;

/*
This is a helper class which aids us in synchronizing with TMDB data. It provides several helper
methods to obtain the data from TMDB.
 */
public class MovieSyncUtils {

    //A String which is passed into the IntentService which indicates which method from this
    //class will be used
    public static final String EXTRA_INTENT_SERVICE_SWITCH =
            "com.example.android.popularmovies_stage2.intent_service_switch";

    //A boolean which checks if the DB has been initialized. This variable is vital to ensuring that
    //the DB is only initialized upon startup.
    private static boolean sInitialized;

    /*
    A helper method which initializes the SQLite DB. If the DB has already been initialized, then
    this method will not attempt to reinitialize it.
     */
    synchronized public static void initialize(@NonNull final Context context) {
        //If the database has already been created, then we simply break away from this method
        if (sInitialized){
            return;
        }

        sInitialized = true;

        //Otherwise, we fire an ASyncTask...
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {

                //...Check to see if our DB contains any data...
                Cursor databaseContents = context.getContentResolver().query(
                        MovieContract.MovieTable.CONTENT_URI,
                        new String[] {MovieContract.MovieTable._ID},
                        null,
                        null,
                        MovieContract.MovieTable._ID);

                //...And if the DB is empty, then we initialize it with data.
                if (databaseContents == null || databaseContents.getCount() == 0){
                    startImmediateSync(context);
                }
                databaseContents.close();
                return null;
            }

        }.execute();
    }

    //This method is responsible for the initialization of the DB upon startup.
    public static void startImmediateSync(@NonNull Context context){
        Intent syncMoviesIntent = new Intent(context, MovieIntentService.class);
        syncMoviesIntent.putExtra(EXTRA_INTENT_SERVICE_SWITCH, 0);

        context.startService(syncMoviesIntent);
    }

    //This method is responsible for additional data being appended to the DB for pagination.
    public static void startSubsequentSync(@NonNull Context context){
        Intent syncMoviesIntent = new Intent(context, MovieIntentService.class);
        syncMoviesIntent.putExtra(EXTRA_INTENT_SERVICE_SWITCH, 1);

        context.startService(syncMoviesIntent);

    }
}
