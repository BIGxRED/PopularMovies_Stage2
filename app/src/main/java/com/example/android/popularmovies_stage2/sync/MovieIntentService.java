package com.example.android.popularmovies_stage2.sync;

import android.app.IntentService;
import android.content.Intent;

public class MovieIntentService extends IntentService {

    public static final String TAG = "MovieIntentService";

    public MovieIntentService(){
        super("MovieIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int whichTask = intent.getIntExtra(MovieSyncUtils.EXTRA_INTENT_SERVICE_SWITCH, 0);

        switch (whichTask){
            case 0:
                MovieTask.syncInitialMovies(this);
                break;

            case 1:
                MovieTask.syncAdditionalMovies(this);
                break;

            default:
                MovieTask.syncInitialMovies(this);
                break;
        }
    }
}
