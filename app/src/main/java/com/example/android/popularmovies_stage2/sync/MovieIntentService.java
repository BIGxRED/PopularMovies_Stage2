package com.example.android.popularmovies_stage2.sync;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.example.android.popularmovies_stage2.MovieSelection;

public class MovieIntentService extends IntentService {

    public static final String TAG = "MovieIntentService";

    public MovieIntentService(){
        super("MovieIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
//        int methodFlag = intent.getIntExtra(MovieSelection.EXTRA_METHOD_FLAG,0);
//        Log.i(TAG, "Value of methodFlag within MovieIntentService: " + methodFlag);

        MovieTask.syncMovies(this);
    }
}
