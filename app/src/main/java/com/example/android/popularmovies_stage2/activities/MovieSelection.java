/*
The following code is the property and sole work of Mike Palarz, a student at Udacity
 */

package com.example.android.popularmovies_stage2.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmovies_stage2.Movie;
import com.example.android.popularmovies_stage2.MovieAdapter;
import com.example.android.popularmovies_stage2.MovieFetcher;
import com.example.android.popularmovies_stage2.R;
import com.example.android.popularmovies_stage2.data.MovieContract;
import com.example.android.popularmovies_stage2.data.MovieDBHelper;
import com.example.android.popularmovies_stage2.fragments.SettingsFragment;
import com.example.android.popularmovies_stage2.sync.MovieSyncUtils;
import com.facebook.stetho.Stetho;


/*
This class is responsible for displaying a grid of movie posters which the user can sort by either
popularity, top rated, or their own personal favorites. When the user clicks on any of the posters,
they are shown a details screen which contains additional information about the movie.
 */
public class MovieSelection extends AppCompatActivity implements LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = "MovieSelection";  //Tag used for debugging

    //IDs used for cursor loaders in order to uniquely identify them
    private static final int LOADER_ID_POPULARITY = 0;
    private static final int LOADER_ID_TOP_RATED = 1;
    private static final int LOADER_ID_FAVORITES = 2;

    RecyclerView mRecyclerView; //Reference to the RecyclerView
    MovieAdapter mAdapter;  //MovieAdapter that will be set to mRecyclerView

    //Reference to the SQlite database
    private SQLiteDatabase mDatabase;

    TextView mErrorMessageTextView; //TextView which is shown in case data could not be retrieved


    //A helper array that is used whenever Cursors are involved
    public static final String[] MOVIE_SELECTION_PROJECTION = {
        MovieContract.MovieTable.COLUMN_MOVIE_ID,
        MovieContract.MovieTable.COLUMN_POSTER_PATH,
        MovieContract.MovieTable.COLUMN_SORTED_BY,
        MovieContract.MovieTable.COLUMN_FAVORITE
    };

    //References to the Strings within MOVIE_SELECTION_PROJECTION
    public static final int INDEX_MOVIE_ID = 0;
    public static final int INDEX_POSTER_PATH = 1;
    public static final int INDEX_SORTED_BY = 2;
    public static final int INDEX_FAVORITE = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_selection);

        Stetho.initializeWithDefaults(this);

        //Obtain references to all of the View member variables
        mErrorMessageTextView = (TextView) findViewById(R.id.tv_error_loading_message);
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_movie_selection);

        //The LayoutManager is set to being a GridLayoutManager, with a span count of 3
        GridLayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 3);
        mRecyclerView.setLayoutManager(layoutManager);

        //This setting is used because all of the elements within the RecyclerView will be the same
        //size; this makes the RecyclerView operate more efficiently
        mRecyclerView.setHasFixedSize(true);

        //Initializing the SQLite database
        MovieDBHelper dbHelper = new MovieDBHelper(this);
        mDatabase = dbHelper.getWritableDatabase();

        //Create and set the adapter accordingly
        mAdapter = new MovieAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        //Setup the page number to be used in case the user scrolls to the bottom of the RecyclerView
        setupPageNumber();

        //Setup SharedPreferences, largely to register the OnSharedPreferenceChangeListener
        setupSharedPreferences();

        //Initialize the DB if necessary
        MovieSyncUtils.initialize(this);

        /*Obtain a reference to the method flag, which indicates which sorting method should be used -
        either popular, top rated, or favorites
         */
        String methodFlagKey = getString(R.string.list_preference_sorting_options_key);
        final int methodFlag = SettingsFragment.getPreferenceValue(this, methodFlagKey);

        //Add an OnScrollListener in order to implement pagination
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView view, int dx, int dy){
                super.onScrolled(view, dx, dy);

                //First we check if the user has scrolled vertically at all
                if(dy > 0){

                    //Then we check if vertically scrolling in the down direction is no longer
                    //possible and load the next set of data based on the current value of the page
                    //number
                    if(!mRecyclerView.canScrollVertically(1)){
                        if (methodFlag != 2){
                            MovieSyncUtils.startSubsequentSync(MovieSelection.this);

                            switch (methodFlag){
                                case 0:
                                    getSupportLoaderManager().restartLoader(LOADER_ID_POPULARITY, null, MovieSelection.this);
                                    break;

                                case 1:
                                    getSupportLoaderManager().restartLoader(LOADER_ID_TOP_RATED, null, MovieSelection.this);
                                    break;

                                case 2:
                                    throw new UnsupportedOperationException("Pagination shouldn't be occuring for favorite movies...");

                                default:
                                    throw new UnsupportedOperationException("Could not recognize value of method flag within onCreate()");
                            }
                        }
                    }
                }
            }
        });

        //Finally, we initialize the loader
        switch (methodFlag){
            case 0:
                getSupportLoaderManager().initLoader(LOADER_ID_POPULARITY, null, this);
                break;

            case 1:
                getSupportLoaderManager().initLoader(LOADER_ID_TOP_RATED, null, this);
                break;

            case 2:
                getSupportLoaderManager().initLoader(LOADER_ID_FAVORITES, null, this);
                break;

            default:
                throw new UnsupportedOperationException("Could not recognize value of method flag within onCreate()");
        }



    }

    @Override
    protected void onResume() {

        String methodFlagKey = getString(R.string.list_preference_sorting_options_key);
        int methodFlag = SettingsFragment.getPreferenceValue(this, methodFlagKey);

        switch (methodFlag){
            case 0:
                getSupportLoaderManager().restartLoader(LOADER_ID_POPULARITY, null, this);
                break;

            case 1:
                getSupportLoaderManager().restartLoader(LOADER_ID_TOP_RATED, null, this);
                break;

            case 2:
                getSupportLoaderManager().restartLoader(LOADER_ID_FAVORITES, null, this);
                break;

            default:
                throw new UnsupportedOperationException("Could not recognize value of method flag within onResume()");
        }

        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Make sure to close the DB to prevent any memory leaks
        mDatabase.close();

        //We also unregister the SharedPreferenceChangeListener
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }



    //Simply inflates the main menu layout
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    //Handles the functionality of the different buttons within the main menu
    @Override
    public boolean onOptionsItemSelected(final MenuItem item){
        switch(item.getItemId()){

            case R.id.settings:
                //If the 'Settings' button is clicked, SettingsActivity is launched
                Intent launchSettingsActivity = new Intent(this, SettingsActivity.class);
                startActivity(launchSettingsActivity);
                return true;

            case R.id.refresh:
                //If the 'Refresh' is clicked, then the appropriate loader is restarted

                String methodFlagKey = getString(R.string.list_preference_sorting_options_key);
                final int methodFlag = SettingsFragment.getPreferenceValue(this, methodFlagKey);

                switch (methodFlag){
                    case 0:
                        getSupportLoaderManager().restartLoader(LOADER_ID_POPULARITY, null, this);
                        break;
                    case 1:
                        getSupportLoaderManager().restartLoader(LOADER_ID_TOP_RATED, null, this);
                        break;
                    case 2:
                        getSupportLoaderManager().restartLoader(LOADER_ID_FAVORITES, null, this);
                        break;
                    default:
                        throw new UnsupportedOperationException("Could not recognize value of method flag within onResume()");
                }

                return true;

            default:
                super.onOptionsItemSelected(item);
                return true;
        }
    }

    /*
    This method is called whenever a new Loader is to be instantiated by the LoaderManager. In
    our case, we are creating a CursorLoader, which is a subclass of Loader. The Loader class
    itself is abstract, so a subclass of it must be created.

    This method is called the first time a Loader is created via initLoader(). It is always when
    restartLoader() is used, regardless if the Loader already exists or not.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection;
        String[] selectionArgs;

        //The appropriate loader is created based on it's ID
        switch (id){
            case LOADER_ID_POPULARITY:
                selection = MOVIE_SELECTION_PROJECTION[INDEX_SORTED_BY] + " = ?";
                selectionArgs = new String[]{Movie.SORTED_BY_POPULARITY};

                return new CursorLoader(this,
                        MovieContract.MovieTable.CONTENT_URI,
                        MOVIE_SELECTION_PROJECTION,
                        selection,
                        selectionArgs,
                        MovieContract.MovieTable._ID);

            case LOADER_ID_TOP_RATED:
                selection = MOVIE_SELECTION_PROJECTION[INDEX_SORTED_BY] + " = ?";
                selectionArgs = new String[]{Movie.SORTED_BY_TOP_RATED};

                return new CursorLoader(this,
                        MovieContract.MovieTable.CONTENT_URI,
                        MOVIE_SELECTION_PROJECTION,
                        selection,
                        selectionArgs,
                        MovieContract.MovieTable._ID);

            case LOADER_ID_FAVORITES:
                selection = MOVIE_SELECTION_PROJECTION[INDEX_FAVORITE] + " = ?";
                selectionArgs = new String [] {"1"};

                return new CursorLoader(this,
                        MovieContract.MovieTable.CONTENT_URI,
                        MOVIE_SELECTION_PROJECTION,
                        selection,
                        selectionArgs,
                        MovieContract.MovieTable._ID);

            default:
                throw new RuntimeException("This type of loader with ID" + id + " was not implemented.");
        }
    }

    /*
    Called whenever a previously created Loader has finished its load.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);

        //If the data is empy, then it's very likely that the user should be shown the error message
        if (data == null){
            mErrorMessageTextView.setVisibility(View.VISIBLE);
        }
        else{
            mErrorMessageTextView.setVisibility(View.INVISIBLE);
        }
    }

    /*
    This method is called whenever a Loader is being reset, which inherently makes its data unavailable.
    Due to the data being unavailable, any references to that data should be removed. In the of this
    application, we don't have a need for this method but it must be implemented in order to
    use the LoaderCallbacks interface.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    /*
    * At first had a need for this method, but no longer needed in final implementation. It is
    * kept for the sake of completeness
    */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /*
    This is a helper method which pulls all of the values from SharedPreferences that correspond to
    a view within the UI and adjusts those views accordingly. As of now, the only setting is for
    the sorting options, but this method would be expanded as more settings are included.
     */
    private void setupSharedPreferences(){
        //Get a reference to the SharedPreferences DB
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        //Obtain the current value of the sorting options setting, which is stored as a String.
        String preferenceMethodFlagString = preferences.getString(getString(R.string.list_preference_sorting_options_key),
               getString(R.string.list_preference_sorting_options_default_value));

        /*
        However, the value of the sorting options ListPreference corresponds to the ID of the
        current loader that is being used. Therefore, it needs to be set accordingly by being parsed
        into an int.
        */

        /*
        This used to be passed to setMethodFlag() from the MovieFetcher class. However, the app
        was later on redesigned so that the method flag is always pulled from the SharedPreferences
        database. Therefore, this line is kept for the sake of completeness, even though
        preferenceMethodFlag is never used.
        */
        int preferenceMethodFlag = Integer.parseInt(preferenceMethodFlagString);

        //Finally, we register this class as our OnSharedPreferenceChangeListener
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    /*
    This method listens to any changes that may have occurred within the Settings screen (and the
    SharedPreferences database as well) and makes adjustments to the UI that correspond to those
    changes. Currently, the only setting that can be adjusted is the sorting options, but this
    would be expanded as more settings would be added.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //First we check if key matches the key of sorting options
        if (key.equals(getString(R.string.list_preference_sorting_options_key))){

            //Then we obtain the value of the ListPreference and parse it into an int
            String methodFlagString = sharedPreferences
                    .getString(key, getString(R.string.list_preference_sorting_options_default_value));
            int methodFlag = Integer.parseInt(methodFlagString);

            //Finally, we relaunch the CursorLoader
            switch (methodFlag){
                case 0:
                    getSupportLoaderManager().restartLoader(LOADER_ID_POPULARITY, null, this);
                    break;

                case 1:
                    getSupportLoaderManager().restartLoader(LOADER_ID_TOP_RATED, null, this);
                    break;

                case 2:
                    getSupportLoaderManager().restartLoader(LOADER_ID_FAVORITES, null, this);
                    break;

                default:
                    throw new UnsupportedOperationException("Could not recognize value of method flag within onSharedPreferenceChanged()");
            }

        }

    }

    /*
    * The entire purpose of this method is to ensure that the value of the page number correctly
    * reflects the number of records within the database. We know that the database is a persistent
    * set of data and it will remain within the phone's internal storage long after onDestroy() is
    * called. However, the page number value, which is stored within the MovieFetcher class, is
    * not the same case. This value may potentially be lost due to the Android lifecycle. To ensure
    * that the correct value of page number is always used, we set its value by referring to the
    * number of rows within the database.
     */
    public void setupPageNumber(){
        String selection;
        String[] selectionArgs;
        Cursor cursor;
        int totalNumberOfRows;

        String methodFlagKey = getString(R.string.list_preference_sorting_options_key);
        int methodFlag = SettingsFragment.getPreferenceValue(this, methodFlagKey);

        /*
        * There may be a different number of records for top-rated and popular movies. Therefore,
        * we set the values of those page numbers depending on the value of the method flag.
         */
        switch (methodFlag){
            case 0:
                selection = MOVIE_SELECTION_PROJECTION[INDEX_SORTED_BY] + " = ?";
                selectionArgs = new String[] {Movie.SORTED_BY_POPULARITY};

                cursor = mDatabase.query(MovieContract.MovieTable.TABLE_NAME,
                        MOVIE_SELECTION_PROJECTION,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        MovieContract.MovieTable._ID);

                totalNumberOfRows = cursor.getCount();

                /*
                * We also know that each page returns 20 movies. Therefore, if we divide the total
                * number of movies by 20, then we should obtain the correct value for the page
                * number. The page number is then set accordingly.
                 */
                MovieFetcher.setPopularPageNumber(totalNumberOfRows/20);
                cursor.close();

                break;

            case 1:
                selection = MOVIE_SELECTION_PROJECTION[INDEX_SORTED_BY] + " = ?";
                selectionArgs = new String[] {Movie.SORTED_BY_TOP_RATED};

                cursor = mDatabase.query(MovieContract.MovieTable.TABLE_NAME,
                        MOVIE_SELECTION_PROJECTION,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        MovieContract.MovieTable._ID);

                totalNumberOfRows = cursor.getCount();

                MovieFetcher.setTopRatedPageNumber(totalNumberOfRows/20);
                cursor.close();

                break;

            /*
            If we're currently looking at favorite movies, then page numbers don't apply. Therefore,
            we simply return from this method.
             */
            case 2:
                return;


            default:
                throw new UnsupportedOperationException("Value for method flag not possible");
        }

    }
}
