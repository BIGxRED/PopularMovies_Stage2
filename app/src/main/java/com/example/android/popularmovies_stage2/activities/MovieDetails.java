/*
The following code is the property and sole work of Mike Palarz, a student at Udacity
 */

package com.example.android.popularmovies_stage2.activities;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmovies_stage2.Movie;
import com.example.android.popularmovies_stage2.R;
import com.example.android.popularmovies_stage2.data.MovieContract;
import com.example.android.popularmovies_stage2.data.MovieDBHelper;
import com.example.android.popularmovies_stage2.fragments.MovieTrailersDialogFragment;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/*
MovieDetails shows additional information about a movie, such as an overview of the plot, when the
movie was released, and the average vote count as well as the total number of votes. This activity
is started whenever a user clicks on a movie poster within MovieSelection. A ViewPager has been
implemented within this class to allow the user to swipe left or right at the movie details screen
to move through the different movies.
 */

public class MovieDetails extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //Tag that is used for debugging
    private static final String TAG = "MovieDetails";

    //Member variables, majority of which are references to different views within the movie
    //details layout
    ImageView mMoviePoster;
    TextView mMovieTitle;
    TextView mMovieOverview;
    TextView mMovieReleaseDate;
    TextView mMovieVoteResults;
    CheckBox mFavorite;
    TextView mMovieRuntime;
    ImageView mPlayButton;
    TextView mReview1;

    ArrayList<Movie> mMoviesList;   //Reference to movies list provided by MovieSelection
    ViewPager mViewPager;

    public static final String[] MOVIE_DETAIL_PROJECTION = {
            MovieContract.MovieTable.COLUMN_TITLE,
            MovieContract.MovieTable.COLUMN_MOVIE_ID,
            MovieContract.MovieTable.COLUMN_OVERVIEW,
            MovieContract.MovieTable.COLUMN_RELEASE_DATE,
            MovieContract.MovieTable.COLUMN_VOTE_COUNT,
            MovieContract.MovieTable.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieTable.COLUMN_POSTER_PATH,
            MovieContract.MovieTable.COLUMN_RUNTIME,
            MovieContract.MovieTable.COLUMN_FAVORITE,
            MovieContract.MovieTable.COLUMN_FIRST_REVIEW_LINK,
            MovieContract.MovieTable.COLUMN_FIRST_REVIEW_CONTENT,
            MovieContract.MovieTable.COLUMN_SECOND_REVIEW_LINK,
            MovieContract.MovieTable.COLUMN_SECOND_REVIEW_CONTENT
    };

    public static final int INDEX_TITLE = 0;
    public static final int INDEX_MOVIE_ID = 1;
    public static final int INDEX_OVERVIEW = 2;
    public static final int INDEX_RELEASE_DATE = 3;
    public static final int INDEX_VOTE_COUNT = 4;
    public static final int INDEX_VOTE_AVERAGE = 5;
    public static final int INDEX_POSTER_PATH = 6;
    public static final int INDEX_RUNTIME = 7;
    public static final int INDEX_FAVORITE = 8;
    public static final int INDEX_FIRST_REVIEW_LINK = 9;
    public static final int INDEX_FIRST_REVIEW_CONTENT = 10;
    public static final int INDEX_SECOND_REVIEW_LINK = 11;
    public static final int INDEX_SECOND_REVIEW_CONTENT = 12;

    private static final int DETAIL_LOADER_ID = 0;

    public static final String EXTRA_URI_FOR_DIALOG =
            "com.example.android.popularmovies_stage2.activities.moviedetails.uri";

    private Uri mMovieUri;
    private int mMovieID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        mMoviePoster = (ImageView) findViewById(R.id.iv_movie_poster);
        mMovieTitle = (TextView) findViewById(R.id.tv_movie_title);
        mMovieOverview = (TextView) findViewById(R.id.tv_movie_overview);
        mMovieReleaseDate = (TextView) findViewById(R.id.tv_movie_release_date);
        mMovieVoteResults = (TextView) findViewById(R.id.tv_movie_vote_results);
        mFavorite = (CheckBox) findViewById(R.id.cb_favorite_star);
        mMovieRuntime = (TextView) findViewById(R.id.tv_movie_runtime);
        mReview1 = (TextView) findViewById(R.id.tv_review_1);
        mPlayButton = (ImageView) findViewById(R.id.iv_play_button);
        mPlayButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                MovieTrailersDialogFragment dialogFragment = new MovieTrailersDialogFragment();
                Bundle dialogArguments = new Bundle();
                dialogArguments.putString(EXTRA_URI_FOR_DIALOG, mMovieUri.toString());
                dialogFragment.setArguments(dialogArguments);
                dialogFragment.show(getSupportFragmentManager(), null);
            }
        });

        //First, we obtain a reference to the intent received from the MovieSelection class
        Intent receivedIntent = getIntent();
        mMovieUri = receivedIntent.getData();
        if (mMovieUri == null){
            throw new NullPointerException("Uri for the selected movie cannot be null");
        }

        mMovieID = Integer.parseInt(mMovieUri.getLastPathSegment());

        getSupportLoaderManager().initLoader(DETAIL_LOADER_ID, null, this);


    }

    //This class is needed because we are implementing the ViewPager widget for the movie details
    //activity. The PagerAdapter works in a similar way as the Adapter for the RecyclerView such
    //that it holds data for multiple views at the same time and also preloads adjacent views.
    public class MoviePagerAdapter extends PagerAdapter{
        private Context mContext;

        public MoviePagerAdapter (Context context){
            mContext = context;
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int position){

            //Inflating the layouts for the movie details activity
            LayoutInflater inflater = LayoutInflater.from(mContext);
            ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.activity_movie_details,
                    collection, false);

            //Obtaining references to all of the views within the XML layout
            mMoviePoster = (ImageView) layout.findViewById(R.id.iv_movie_poster);
            mMovieTitle = (TextView) layout.findViewById(R.id.tv_movie_title);
            mMovieOverview = (TextView) layout.findViewById(R.id.tv_movie_overview);
            mMovieReleaseDate = (TextView) layout.findViewById(R.id.tv_movie_release_date);
            mMovieVoteResults = (TextView) layout.findViewById(R.id.tv_movie_vote_results);

            Movie receivedMovie = null;

            //If the ArrayList has been populated earlier on within onCreate(), then we actually
            //have movie data to work with and can populate the movie details
            if(mMoviesList.size() != 0 && mMoviesList != null) {
                //Obtaining the Movie object with the ViewPager's current position
                receivedMovie = mMoviesList.get(position);

                //Reset the movie poster
                Picasso.with(getApplicationContext())
                        .load("https://image.tmdb.org/t/p/w185/" + receivedMovie.getPosterPath())
                        .into(mMoviePoster);

                //Set the movie title and overview accordingly
                mMovieTitle.setText(receivedMovie.getTitle());
                mMovieOverview.setText(receivedMovie.getOverview());

                //We have the total number of votes every movie received. However, the vote counts
                //is an integer w/ no comma separators (3456, not 3,456). To make this value more
                //presentable, we first format the integer using NumberFormat so that comma
                //separators are included; it is returned to us as a String
                String formattedVoteCount = NumberFormat.getIntegerInstance()
                        .format(receivedMovie.getVoteCount());

                //We then set the TextView accordingly, with the vote average and vote count both
                //contained within the same view
                mMovieVoteResults.setText(Float.toString(receivedMovie.getVoteAverage()) + "/10"
                        + " (" + formattedVoteCount + ")");

                //Similar case as with the vote count value, the release date data obtained from
                //TheMovieDB API isn't in a format that is seen often (2016-10-24 for example).
                //Therefore, SimpleDateFormat is used to reformat this data and then display it
                //within the TextView set aside for the release date
                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate = null;
                try{
                    Date movieReleaseDate = dateFormatter.parse(receivedMovie.getReleaseDate());
                    formattedDate = new SimpleDateFormat("MMMM dd, yyyy").format(movieReleaseDate);
                }
                catch(ParseException pe){
                    //In case the date cannot be parsed correctly, we should handle this
                    //exception somehow
                    pe.printStackTrace();
                    Log.e(TAG, "Location where the error occurred: " + pe.getErrorOffset());
                }
                mMovieReleaseDate.setText("Release date: " + formattedDate);
            }

            //Now that the view has been setup, it is added to the collection of views which are
            //maintained by the pager adapter
            collection.addView(layout);
            return layout;
        }

        //This method removes a particular view from the collection of views maintained by the
        //pager adapter
        @Override
        public void destroyItem(ViewGroup collection, int position, Object view){
            collection.removeView((View) view);
        }

        //Simply returns the number of views that the pager adapter needs to keep record of
        @Override
        public int getCount(){
            return mMoviesList.size();
        }

        //Checks whether a particular Object (in this case, the movie details layout) belongs to a
        //given view. The Object is the same Object that is returned by instantiateItem()
        @Override
        public boolean isViewFromObject(View view, Object object){
            return view == object;
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case DETAIL_LOADER_ID:
                return new CursorLoader(this,
                        mMovieUri,
                        MOVIE_DETAIL_PROJECTION,
                        null,
                        null,
                        null);

            default:
                throw new RuntimeException("This type of loader with ID" + id + " was not implemented.");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
          /*
          There is a chance that once onLoadFinished() is called, the cursor may not contain any
          data (incorrect Uri was passed, the movie ID did not match, etc.). Therefore, we first
          must check if the cursor contains anything. We call moveToFirst() on the cursor, which
          moves the cursor to the first row. However, it will also return a boolean. The value of the
          boolean is true if the cursor successfully moved to the first row. We therefore take
          advantage of this boolean value to see if we indeed have any data to work with.
         */
        boolean cursorHasData = false;
        if (data != null && data.moveToFirst()) {
            cursorHasData = true;
        }

        if (!cursorHasData) {
            return;
        }

        String title = data.getString(INDEX_TITLE);
        String overview = data.getString(INDEX_OVERVIEW);
        String releaseDate = data.getString(INDEX_RELEASE_DATE);
        int voteCount = data.getInt(INDEX_VOTE_COUNT);
        float voteAverage = data.getFloat(INDEX_VOTE_AVERAGE);
        String posterPath = data.getString(INDEX_POSTER_PATH);
        boolean favorite = data.getInt(INDEX_FAVORITE) > 0;
        int runtime = data.getInt(INDEX_RUNTIME);
        String firstReviewLink = data.getString(INDEX_FIRST_REVIEW_LINK);
        String firstReviewContent = data.getString(INDEX_FIRST_REVIEW_CONTENT);

        //TODO; Make sure to check if these are null at some point and hide the views if necessary
        String secondReviewLink = data.getString(INDEX_SECOND_REVIEW_LINK);
        String secondReviewContent = data.getString(INDEX_SECOND_REVIEW_CONTENT);

        Picasso.with(getApplicationContext())
                .load("https://image.tmdb.org/t/p/w185/" + posterPath)
                .into(mMoviePoster);

        mMovieTitle.setText(title);
        mMovieOverview.setText(overview);

        String formattedVoteCount = NumberFormat.getIntegerInstance()
                .format(voteCount);
        mMovieVoteResults.setText(Float.toString(voteAverage) + "/10"
                + " (" + formattedVoteCount + ")");

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = null;
        try{
            Date date = dateFormatter.parse(releaseDate);
            formattedDate = new SimpleDateFormat("MMMM dd, yyyy").format(date);
        }
        catch(ParseException pe){
            //In case the date cannot be parsed correctly, we should handle this
            //exception somehow
            pe.printStackTrace();
            Log.e(TAG, "Location where the error occurred: " + pe.getErrorOffset());
        }
        mMovieReleaseDate.setText(formattedDate);
        mFavorite.setChecked(favorite);

        mMovieRuntime.setText(runtime + "m");
        mReview1.setText(firstReviewContent);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Nothing to be done within this method yet
    }

    public void onFavoriteClicked(View view){
        boolean checked = ((CheckBox) view).isChecked();
        ContentValues updatedValues = new ContentValues();

        if (checked){
            updatedValues.put(MOVIE_DETAIL_PROJECTION[INDEX_FAVORITE], Boolean.TRUE);
        }
        else {
            updatedValues.put(MOVIE_DETAIL_PROJECTION[INDEX_FAVORITE], Boolean.FALSE);
        }

        getContentResolver().update(mMovieUri, updatedValues, null, null);

        //TODO: Since this is only used for debugging, make sure to remove this once you're
        //comfortable that this works
        Cursor cursor = getContentResolver().query(MovieContract.MovieTable.CONTENT_URI,
                MOVIE_DETAIL_PROJECTION,
                MOVIE_DETAIL_PROJECTION[INDEX_MOVIE_ID] + " = ?",
                new String[] {Integer.toString(mMovieID)},
                null);

        cursor.moveToFirst();
        boolean favoriteValue = cursor.getInt(INDEX_FAVORITE) > 0;
        Log.i(TAG, "Value of favorite for movie " + cursor.getString(INDEX_TITLE) + ": " + favoriteValue);
        cursor.close();
    }

}
