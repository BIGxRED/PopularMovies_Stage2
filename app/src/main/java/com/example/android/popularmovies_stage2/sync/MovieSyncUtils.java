package com.example.android.popularmovies_stage2.sync;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.example.android.popularmovies_stage2.MovieSelection;
import com.example.android.popularmovies_stage2.data.MovieContract;

public class MovieSyncUtils {

    private static boolean sInitialized;

    //TODO: Look into what 'synchronized' does and means
    synchronized public static void initialize(@NonNull final Context context, final Intent syncMoviesIntent) {
        if (sInitialized){
            return;
        }

        sInitialized = true;

        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {

                Cursor databaseContents = context.getContentResolver().query(
                        MovieContract.MovieTable.CONTENT_URI,
                        new String[] {MovieContract.MovieTable._ID},
                        null,
                        null,
                        MovieContract.MovieTable._ID);

                if (databaseContents == null || databaseContents.getCount() == 0){
                    startImmediateSync(context, syncMoviesIntent);
                }
                databaseContents.close();
                return null;
            }

        }.execute();
    }

    public static void startImmediateSync(@NonNull Context context, Intent syncMoviesIntent){
        context.startService(syncMoviesIntent);
    }
}
