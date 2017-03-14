package com.example.android.popularmovies_stage2.sync;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.example.android.popularmovies_stage2.data.MovieContract;

public class MovieSyncUtils {

    public static final String EXTRA_INTENT_SERVICE_SWITCH =
            "com.example.android.popularmovies_stage2.intent_service_switch";

    private static boolean sInitialized;

    //TODO: Look into what 'synchronized' does and means
    synchronized public static void initialize(@NonNull final Context context) {
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
                    startImmediateSync(context);
                }
                databaseContents.close();
                return null;
            }

        }.execute();
    }

    public static void startImmediateSync(@NonNull Context context){
        Intent syncMoviesIntent = new Intent(context, MovieIntentService.class);
        syncMoviesIntent.putExtra(EXTRA_INTENT_SERVICE_SWITCH, 0);

        context.startService(syncMoviesIntent);
    }

    public static void startSubsequentSync(@NonNull Context context){
        Intent syncMoviesIntent = new Intent(context, MovieIntentService.class);
        syncMoviesIntent.putExtra(EXTRA_INTENT_SERVICE_SWITCH, 1);

        context.startService(syncMoviesIntent);

    }
}
