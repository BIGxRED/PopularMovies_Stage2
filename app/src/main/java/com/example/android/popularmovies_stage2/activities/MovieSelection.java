/*
The following code is the property and sole work of Mike Palarz, a student at Udacity
 */

package com.example.android.popularmovies_stage2.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.database.DatabaseUtilsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmovies_stage2.Movie;
import com.example.android.popularmovies_stage2.MovieAdapter;
import com.example.android.popularmovies_stage2.MovieFetcher;
import com.example.android.popularmovies_stage2.R;
import com.example.android.popularmovies_stage2.data.MovieContract;
import com.example.android.popularmovies_stage2.data.MovieDBHelper;
import com.example.android.popularmovies_stage2.sync.MovieSyncUtils;

public class MovieSelection extends AppCompatActivity implements LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = "MovieSelection";  //Tag used for debugging

    //Strings which are used to uniquely identify the data that will eventually be passed onto the
    //MovieDetails class
    public static final String EXTRA_PARCEL = "com.example.android.popularmovies_stage2.parcel";
    public static final String EXTRA_ID = "com.example.android.popularmovies_stage2.id";

    public static final String EXTRA_METHOD_FLAG = "com.example.android.popularmovies_stage2.method_flag";
    public static final String EXTRA_PAGE_NUMBER = "com.example.android.popularmovies_stage2.page_number";

    private static final int LOADER_ID_POPULARITY = 0;
    private static final int LOADER_ID_TOP_RATED = 1;

    RecyclerView mRecyclerView; //Reference to the RecyclerView
    MovieAdapter mAdapter;  //MovieAdapter that will be set to mRecyclerView

    private SQLiteDatabase mDatabase;

    ProgressBar mProgressBar;   //ProgressBar which is used to show that data is being loaded
    TextView mErrorMessageTextView; //TextView which is shown in case data could not be retrieved

    //TODO: Should this variable be a part of this class? It made sense previously. However, since
    //you're now using services to load the movie data, it may be better to move this to another
    //class that would be more suitable.

//    public int mPageNumber;    //Used to move onto the next page once the user scrolls to the bottom of
                        // the RecyclerView

//    public int mMethodFlag;    //Used to choose the correct method for sorting the movies through the
//                        // PopUp menu


    //TODO: For this activity, it doesn't make much sense to include ALL of the movie attributes
    //since all we really care about is the movie poster. Adjust this later on once you have the
    //posters displaying again
    public static final String[] MOVIE_DEFAULT_PROJECTION = {
        MovieContract.MovieTable.COLUMN_TITLE,
        MovieContract.MovieTable.COLUMN_MOVIE_ID,
        MovieContract.MovieTable.COLUMN_OVERVIEW,
        MovieContract.MovieTable.COLUMN_RELEASE_DATE,
        MovieContract.MovieTable.COLUMN_VOTE_COUNT,
        MovieContract.MovieTable.COLUMN_VOTE_AVERAGE,
        MovieContract.MovieTable.COLUMN_POSTER_PATH,
        MovieContract.MovieTable.COLUMN_BACKDROP_PATH,
        MovieContract.MovieTable.COLUMN_SORTED_BY
    };

    public static final int INDEX_TITLE = 0;
    public static final int INDEX_MOVIE_ID = 1;
    public static final int INDEX_OVERVIEW = 2;
    public static final int INDEX_RELEASE_DATE = 3;
    public static final int INDEX_VOTE_COUNT = 4;
    public static final int INDEX_VOTE_AVERAGE = 5;
    public static final int INDEX_POSTER_PATH = 6;
    public static final int INDEX_BACKDROP_PATH = 7;
    public static final int INDEX_SORTED_BY = 8;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_selection);

        //Obtain references to all of the View member variables
        mProgressBar = (ProgressBar) findViewById(R.id.pb_loading_data);
        mErrorMessageTextView = (TextView) findViewById(R.id.tv_error_loading_message);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_movie_selection);

        //The LayoutManager is set to being a GridLayoutManager, with a span count of 3
        GridLayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 3);
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setHasFixedSize(true);

        //Initializing the SQLite database
        MovieDBHelper dbHelper = new MovieDBHelper(this);
        mDatabase = dbHelper.getWritableDatabase();


        //Create and set the adapter accordingly
        mAdapter = new MovieAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        setupSharedPreferences();
        setupPageNumber();

        MovieSyncUtils.initialize(this);

        Log.i(TAG, "Value of the method flag: " + MovieFetcher.getMethodFlag());

        switch (MovieFetcher.getMethodFlag()){
            case 0:
                getSupportLoaderManager().initLoader(LOADER_ID_POPULARITY,null,this);
                break;
            case 1:
                getSupportLoaderManager().initLoader(LOADER_ID_TOP_RATED,null,this);
                break;
            default:
                throw new UnsupportedOperationException("Could not recognize value of method flag within onResume()");
        }



    }

    @Override
    protected void onResume() {

        switch (MovieFetcher.getMethodFlag()){
            case 0:
                getSupportLoaderManager().restartLoader(LOADER_ID_POPULARITY,null,this);
                break;
            case 1:
                getSupportLoaderManager().restartLoader(LOADER_ID_TOP_RATED,null,this);
                break;
            default:
                throw new UnsupportedOperationException("Could not recognize value of method flag within onResume()");
        }

        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabase.close();
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
                Intent launchSettingsActivity = new Intent(this, SettingsActivity.class);
                startActivity(launchSettingsActivity);
                return true;

            //Simply fetches the movie data again, starting from the first page
            case R.id.refresh:

                switch (MovieFetcher.getMethodFlag()){
                    case 0:
                        getSupportLoaderManager().restartLoader(LOADER_ID_POPULARITY,null,this);
                        break;
                    case 1:
                        getSupportLoaderManager().restartLoader(LOADER_ID_TOP_RATED,null,this);
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

        switch (id){
            case LOADER_ID_POPULARITY:
                selection = MOVIE_DEFAULT_PROJECTION[INDEX_SORTED_BY] + " = ?";
                selectionArgs = new String[]{Movie.SORTED_BY_POPULARITY};

                return new CursorLoader(this,
                        MovieContract.MovieTable.CONTENT_URI,
                        MOVIE_DEFAULT_PROJECTION,
                        selection,
                        selectionArgs,
                        MovieContract.MovieTable._ID);

            case LOADER_ID_TOP_RATED:
                selection = MOVIE_DEFAULT_PROJECTION[INDEX_SORTED_BY] + " = ?";
                selectionArgs = new String[]{Movie.SORTED_BY_TOP_RATED};

                return new CursorLoader(this,
                        MovieContract.MovieTable.CONTENT_URI,
                        MOVIE_DEFAULT_PROJECTION,
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

        Log.i(TAG, "Number of items within Cursor in onLoadFinished(): " + data.getCount());
    }

    /*
    This method is called whenever a Loader is being reset, which inherently makes it data unavailable.
    Due to the data being unavailable, any references to that data should be removed. In the of this
    application, we don't have a need for this method but it must be implemented in order to
    use the LoaderCallbacks interface.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

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

        //TODO: See if there's a cleaner way of doing this. Maybe get a reference to an individual
        //preference (in this case, the sorting options preference) and then use findIndexOfValue().
        //Also consider this: https://developer.android.com/training/basics/network-ops/xml.html

        //Obtain the current value of the sorting options setting, which is stored as a String.
        String preferenceMethodFlagString = preferences.getString(getString(R.string.list_preference_sorting_options_key),
               getString(R.string.list_preference_sorting_options_default_value));

        //However, the value of the sorting options ListPreference corresponds to the method flag
        //of the MovieFetcher class. Therefore, it needs to be set accordingly by being parsed into
        //an int.
        int preferenceMethodFlag = Integer.parseInt(preferenceMethodFlagString);
        MovieFetcher.setMethodFlag(preferenceMethodFlag);

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
            MovieFetcher.setMethodFlag(Integer.parseInt(methodFlagString));

            //Finally, we relaunch the CursorLoader
            switch (MovieFetcher.getMethodFlag()){
                case 0:
                    getSupportLoaderManager().restartLoader(LOADER_ID_POPULARITY,null,this);
                    break;
                case 1:
                    getSupportLoaderManager().restartLoader(LOADER_ID_TOP_RATED,null,this);
                    break;
                default:
                    throw new UnsupportedOperationException("Could not recognize value of method flag within onResume()");
            }

        }

        //TODO: Maybe one additional setting would be to allow the user to clear out all of their
        //current favorited movies? Just a thought.
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

        String selection = MOVIE_DEFAULT_PROJECTION[INDEX_SORTED_BY] + " = ?";
        String[] selectionArgs;

        /*
        * There may be a different number of records for top-rated and popular movies. Therefore,
        * we set the values of those page numbers depending on the value of the method flag.
         */
        switch (MovieFetcher.getMethodFlag()){
            case 0:
                selectionArgs = new String[] {Movie.SORTED_BY_POPULARITY};
                break;

            case 1:
                selectionArgs = new String[] {Movie.SORTED_BY_TOP_RATED};
                break;

            default:
                throw new UnsupportedOperationException("Value for method flag not possible");
        }

        Cursor cursor = mDatabase.query(MovieContract.MovieTable.TABLE_NAME,
                MOVIE_DEFAULT_PROJECTION,
                selection,
                selectionArgs,
                null,
                null,
                MovieContract.MovieTable._ID);

        int totalNumberOfRows = cursor.getCount();
                        /*
        * We also know that each page returns 20 movies. Therefore, if we divide the total
        * number of movies by 20, then we should obtain the correct value for the page
        * number. The page number is then set accordingly.
         */
        MovieFetcher.setPopularPageNumber(totalNumberOfRows/20);
        cursor.close();
    }
}
