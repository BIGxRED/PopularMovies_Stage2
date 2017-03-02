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

        MovieTask.syncMovies(this);
    }
}
