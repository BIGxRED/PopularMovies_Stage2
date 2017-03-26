/*
The following code is the property and sole work of Mike Palarz, a student at Udacity
 */

package com.example.android.popularmovies_stage2.sync;

import android.app.IntentService;
import android.content.Intent;

/*
An implementation of an IntentService. Whenever new data is obtained from TMDB, I've decided to
perform this action using a Service. This is because obtaining this data does not have a direct
impact to the UI (although it will eventually within MovieSelection and MovieDetails).
 */
public class MovieIntentService extends IntentService {

    public static final String TAG = "MovieIntentService";  //Tag used for debugging

    //This constructor is necessary in order to implement an IntentService
    public MovieIntentService(){
        super("MovieIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        //Instead of creating two different IntentServices, I've decided to make efficient use
        //of the one that I already have and simply switch what it does through an integer that
        //I pass into the intent. Depending on the value of this integer, we either initialize
        //the DB or obtain additional movie data for pagination.
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
