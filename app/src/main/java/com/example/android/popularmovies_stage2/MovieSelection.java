/*
The following code is the property and sole work of Mike Palarz, a student at Udacity
 */

package com.example.android.popularmovies_stage2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/*
The purpose of this class is to allow the user to select a movie from a grid array of movie posters.
A menu is also configured in order to allow different sorting options, such as sorting by
popularity and top rated movies. Pagination (infinite scrolling) is implemented, such that
when the user scrolls to the bottom of the current collection of movies, another API query is
initiated and additional movies are appended to the current view.
 */

public class MovieSelection extends AppCompatActivity implements LoaderCallbacks<List<Movie>>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = "MovieSelection";  //Tag used for debugging

    //Strings which are used to uniquely identify the data that will eventually be passed onto the
    //MovieDetails class
    public static final String EXTRA_PARCEL = "com.example.android.popularmovies_stage2.parcel";
    public static final String EXTRA_ID = "com.example.android.popularmovies_stage2.id";

    public static final String EXTRA_METHOD_FLAG = "com.example.android.popularmovies_stage2.method_flag";
    public static final String EXTRA_PAGE_NUMBER = "com.example.android.popularmovies_stage2.page_number";

    private static final int MOVIE_LOADER_ID = 0;

    RecyclerView mRecyclerView; //Reference to the RecyclerView
    MovieAdapter mAdapter;  //MovieAdapter that will be set to mRecyclerView
    ArrayList<Movie> mMoviesList = new ArrayList<>();   //ArrayList which will store all of the Movies
    ProgressBar mProgressBar;   //ProgressBar which is used to show that data is being loaded
    TextView mErrorMessageTextView; //TextView which is shown in case data could not be retrieved
    int mPageNumber;    //Used to move onto the next page once the user scrolls to the bottom of
                        // the RecyclerView
    int mMethodFlag;    //Used to choose the correct method for sorting the movies through the
                        // PopUp menu


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

//        mPageNumber = 1;    //First page always has a value of 1

        /*
        This is deprecated due to implementing SharedPreferences. mMethodFlag is now stored within
        the SharedPreferences database and must be obtained from their accordingly. The value of
        mMethodFlag is obtained via the setupSharedPreferences() method.
        */

//        mMethodFlag = 0;    //On startup, the movies are sorted by popularity
        setupSharedPreferences();

        //Create and set the adapter accordingly
        mAdapter = new MovieAdapter(mMoviesList);
        mRecyclerView.setAdapter(mAdapter);

        //Lastly, movie data is obtained through the FetchMoviesTask class
        getSupportLoaderManager().initLoader(MOVIE_LOADER_ID,null,this);

        //Add an OnScrollListener in order to implement pagination
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView view, int dx, int dy){
                super.onScrolled(view, dx, dy);
                //First we check if the user has scrolled vertically at all
                if(dy > 0){
                    //Then we check if vertically scrolling in the down direction is no longer
                    //possible
                    if(!mRecyclerView.canScrollVertically(1)){
                        //A new ASyncTask is launched to poll for additional movies

                        /*It is crucial to use restartLoader() in this case. This forces the data to
                        be reset by calling onCreateLoader() once again, which then causes
                        loadInBackground() to also be called. If initLoader() were to be used in this
                        case (and in any other situation where we want the next page of movie data
                        to be obtained), the next page of data would never be loaded b/c the loader
                        has already been created within onCreate() via initLoader(). If the loader
                        already exists and initLoader() is used instead of restartLoader(), then
                        onCreateLoader() is never used, only onLoadFinished().
                        */

                        getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID,null,MovieSelection.this);
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    //MovieAdapter class, which is an extension of the RecyclerView.Adapter class. This class keeps
    //track of all the views which will be displayed in the RecyclerView.
    private class MovieAdapter extends RecyclerView.Adapter<MovieHolder>{
        private ArrayList<Movie> adapterMovies;

        //Default constructor of the adapter
        public MovieAdapter(ArrayList<Movie> movies){
            adapterMovies = movies;
        }

        //This method simply inflates the layout for each movie poster
        @Override
        public MovieHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            View view = inflater.inflate(R.layout.movie_list_item, viewGroup, false);

            return new MovieHolder(view);
        }

        //This method "binds" the data that is contained within adapterMovies to the different
        //views contained in the MovieHolder. In this case, since only an ImageView is contained
        //within the layout, the poster path is obtained for the corresponding movie and the image
        //is loaded via Picasso.
        @Override
        public void onBindViewHolder(MovieHolder holder, int position){
            Movie currentMovie = adapterMovies.get(position);
            Picasso.with(getApplicationContext())
                    .load("https://image.tmdb.org/t/p/w185/" + currentMovie.getPosterPath())
                    .into(holder.holderImageView);
        }

        //Simply returns the number of movies contained within adapterMovies
        @Override
        public int getItemCount(){
            return adapterMovies.size();
        }

    }

    //This class manages all of the views contained within a list item (individual components within
    //the RecyclerView). It implements View.OnClickListener so that the movie details screen is
    //shown when the user clicks on a movie poster.
    private class MovieHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView holderImageView;  //Reference to the poster ImageView

        //Constructor for the MovieHolder, which essentially sets the reference for holderImageView
        public MovieHolder(View movieView){
            super(movieView);
            holderImageView = (ImageView) movieView.findViewById(R.id.list_item_image_view);
            movieView.setOnClickListener(this);
        }

        //When the user clicks on a movie poster, the movie details screen should be shown.
        @Override
        public void onClick(View view){
            //First we obtain a reference to the movie that was clicked
            Movie clickedMovie = mMoviesList.get(mRecyclerView.getChildAdapterPosition(view));

            //Then an intent is created with a reference to the MovieDetails class
            Intent movieDetailsIntent = new Intent(getApplicationContext(), MovieDetails.class);

            //Extras are then placed into the intent, which include the entire movie ArrayList and
            //the clicked movie's ID
            movieDetailsIntent.putParcelableArrayListExtra(EXTRA_PARCEL, mMoviesList);

            //The ID is also used as an extra because it becomes useful in the movie details page
            //for the ViewPager
            movieDetailsIntent.putExtra(EXTRA_ID, clickedMovie.getID());
            startActivity(movieDetailsIntent);
        }


    }

    /*
    This was a method that was initially used in stage 1 of this project. It is now deprecated for
    stage 2, largely because SharedPreferences were implemented. The general purpsoe of this method
    is to generate a popup menu when the user clicked on a button titled "Sort By". When the user
    clicked on this button, they would be able to choose the different sorting options. However,
    the sorting options are now stored in SharedPreferences.
     */
    private void showPopUp(){
        //The pop-up menu must be anchored to a View object. For this case, the pop-up will be
        //anchored to the "Sort movies by:" menu button
        View menuAnchor = findViewById(R.id.sort_options);

        //A PopupMenu is created using the aforementioned anchor view as a parameter
        PopupMenu popup = new PopupMenu(this, menuAnchor);

        //An on-click listener is set so that the buttons sort the movies as they're supposed to
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.sort_by_popular:
                        mMoviesList.clear();    //mMoviesList is cleared in both cases so that the
                        mPageNumber = 1;        //old movie data does not remain in the RecyclerView
                        mMethodFlag = 0;        //mMethodFlag is set for the correct button
                        getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID,null,MovieSelection.this);    //Finally, a new FetchMoviesTask is
                        return true;                        //launched
                    case R.id.sort_by_top_rated:
                        mMoviesList.clear();
                        mPageNumber = 1;
                        mMethodFlag = 1;
                        getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID,null,MovieSelection.this);
                        return true;
                    default:
                        return true;
                }
            }
        });
        //Finally, the pop-up menu layout is inflated and shown
        popup.inflate(R.menu.sort_movies_options);
        popup.show();
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
            case R.id.sort_options:
                //showPopUp() has been deprecated for stage 2 of this project
                showPopUp();
                return true;

            case R.id.settings:
                Intent launchSettingsActivity = new Intent(this, SettingsActivity.class);
                startActivity(launchSettingsActivity);
                return true;

            //Simply fetches the movie data again, starting from the first page
            case R.id.refresh:
                mPageNumber = 1;
                getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID,null,this);
                return true;
            default:
                super.onOptionsItemSelected(item);
                return true;
        }
    }

    /*
    This method is called whenever a new Loader is to be instantiated by the LoaderManager. In
    our case, we are creating an ASyncTaskLoader, which is a subclass of Loader. The Loader class
    itself is abstract, so a subclass of it must be created.

    This method is called the first time a Loader is created via initLoader(). It is always when
    restartLoader() is used, regardless if the Loader already exists or not.
     */
    @Override
    public Loader<List<Movie>> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<List<Movie>>(this) {

            List<Movie> loaderMovies = null;

            /*
            This method is equivalent to onPreExecute() of a ASyncTask. Any preparations prior to
            the background thread running should be done here. We've also implemented caching via
            loaderMovies.
             */
            @Override
            protected void onStartLoading() {
                if (loaderMovies != null){
                    deliverResult(loaderMovies);
                }
                else{
                    mProgressBar.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            /*
            This method is similar to doInBackground() of a simple ASyncTask. It is where the bulk
            of the work is done, where a background thread is created in order to fetch more movie
            data for us.
             */
            @Override
            public List<Movie> loadInBackground() {
//                Log.i(TAG, "Value of mPageNumber within loadInBackground(): " + mPageNumber);
                return new MovieFetcher().fetchMovies(mMethodFlag, mPageNumber);
            }

            /*
            This method is similar to onPostExecute() of a regular ASyncTask such that it is called
            when there is new data to be sent to the client (the client is whatever entity implements
            the LoaderCallbacks interface, which is the MovieSelection class in this case). The call
            to super() actually handles delivering the data, but additional logic has been added
            to this method.
             */
            @Override
            public void deliverResult(List<Movie> data) {
                loaderMovies = data;
                super.deliverResult(data);
            }
        };
    }

    /*
    Called whenever a previously created Loader has finished its load.
     */
    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> data) {
        mProgressBar.setVisibility(View.INVISIBLE); //ProgressBar is hidden

        //If the ArrayList returned by doInBackground() actually contains data...
        if(data.size() != 0){
            mRecyclerView.setVisibility(View.VISIBLE);  //Ensure that the RecycerView is visible
            mErrorMessageTextView.setVisibility(View.INVISIBLE);    //Hide the error message
            mMoviesList.addAll(data);   //Add all of the movies to mMoviesList
            mAdapter.notifyDataSetChanged();    //Let the adapter know that new data has been
            // obtained
            mPageNumber++;  //Increment mPageNumber so that the next page is obtained the next
            // time a FetchMoviesTask is launched

//            Log.i(TAG, "Value of mPageNumber within onLoadFinished(): " + mPageNumber);
        }

        //Otherwise, hide the RecyclerView and show the error message
        else{
            mRecyclerView.setVisibility(View.INVISIBLE);
            mErrorMessageTextView.setVisibility(View.VISIBLE);
        }
    }

    /*
    This method is called whenever a Loader is being reset, which inherently makes it data unavailable.
    Due to the data being unavailable, any references to that data should be removed. In the of this
    application, we don't have a need for this method but it must be implemented in order to
    use the LoaderCallbacks interface.
     */
    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {
        //Nothing to do in our case
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //TODO: Sort of concerned about using the same constants for this Bundle as what's being
        //used to launch the MovieDetails activity. Keep an eye on this.
        outState.putParcelableArrayList(EXTRA_PARCEL, mMoviesList);
        outState.putInt(EXTRA_METHOD_FLAG, mMethodFlag);
        outState.putInt(EXTRA_PAGE_NUMBER, mPageNumber);
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

        //However, the value of the sorting options ListPreference corresponds to mMethodFlag, so
        //it must be parsed into an int
        int preferenceMethodFlag = Integer.parseInt(preferenceMethodFlagString);
        mMethodFlag = preferenceMethodFlag;

        //We also want to reset the page number so that the data is being pulled from the beginning
        //of the data that is pulled from TheMovieDB API
        mPageNumber = 1;

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
            mMethodFlag = Integer.parseInt(methodFlagString);

            //mPageNumber is also reset so that the beginning of the movie data is being pulled
            //from the API
            mPageNumber = 1;

            //We also want to clear out the old data. There's no need to hold onto it (and hence,
            //display it) if the user has changed their sorting setting
            mMoviesList.clear();

            //Finally, we relaunch the ASyncTaskLoader
            getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID,null,this);
        }

        //TODO: Maybe one additional setting would be to allow the user to clear out all of their
        //current favorited movies? Just a thought.
    }
}
