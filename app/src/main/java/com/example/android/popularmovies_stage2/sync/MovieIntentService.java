package com.example.android.popularmovies_stage2.sync;

import android.app.IntentService;
import android.content.Intent;

import com.example.android.popularmovies_stage2.MovieSelection;

public class MovieIntentService extends IntentService {
    public MovieIntentService(){
        super("MovieIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int methodFlag = intent.getIntExtra(MovieSelection.EXTRA_METHOD_FLAG,0);
        int pageNumber = intent.getIntExtra(MovieSelection.EXTRA_PAGE_NUMBER, 1);
        MovieTask.syncMovies(this, methodFlag, pageNumber);
    }
}
