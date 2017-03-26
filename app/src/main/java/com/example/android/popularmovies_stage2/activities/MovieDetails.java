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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.popularmovies_stage2.Movie;
import com.example.android.popularmovies_stage2.R;
import com.example.android.popularmovies_stage2.data.MovieContract;
import com.example.android.popularmovies_stage2.fragments.MovieTrailersDialogFragment;
import com.example.android.popularmovies_stage2.fragments.SettingsFragment;
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

    /*
    Views that pertain to the movie's reviews A LinearLayout is used for both of the reviews because
    I wanted the entire review to be a clickable item. Therefore, an OnClickListener has been set
    for each of the reviews within instantiateItem() of the PagerAdapter.
     */
    TextView mReviewsHeading;
    LinearLayout mFirstReview_LL;
    TextView mFirstReview_TV;
    LinearLayout mSecondReview_LL;
    TextView mSecondReview_TV;

    //ViewPager and it's adapter which allows for left/right scrolling of the movies
    ViewPager mViewPager;
    MoviePagerAdapter mPagerAdapter;

    //Array which is used to simplify queries into the DB
    public static final String[] MOVIE_DETAIL_PROJECTION = {
            MovieContract.MovieTable.COLUMN_TITLE,
            MovieContract.MovieTable.COLUMN_MOVIE_ID,
            MovieContract.MovieTable.COLUMN_OVERVIEW,
            MovieContract.MovieTable.COLUMN_RELEASE_DATE,
            MovieContract.MovieTable.COLUMN_VOTE_COUNT,
            MovieContract.MovieTable.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieTable.COLUMN_POSTER_PATH,
            MovieContract.MovieTable.COLUMN_SORTED_BY,
            MovieContract.MovieTable.COLUMN_RUNTIME,
            MovieContract.MovieTable.COLUMN_FAVORITE,
            MovieContract.MovieTable.COLUMN_FIRST_REVIEW_LINK,
            MovieContract.MovieTable.COLUMN_FIRST_REVIEW_CONTENT,
            MovieContract.MovieTable.COLUMN_SECOND_REVIEW_LINK,
            MovieContract.MovieTable.COLUMN_SECOND_REVIEW_CONTENT
    };

    //Integer values that refer to the Strings within MOVIE_DETAIL_PROJECTION
    public static final int INDEX_TITLE = 0;
    public static final int INDEX_MOVIE_ID = 1;
    public static final int INDEX_OVERVIEW = 2;
    public static final int INDEX_RELEASE_DATE = 3;
    public static final int INDEX_VOTE_COUNT = 4;
    public static final int INDEX_VOTE_AVERAGE = 5;
    public static final int INDEX_POSTER_PATH = 6;
    public static final int INDEX_SORTED_BY = 7;
    public static final int INDEX_RUNTIME = 8;
    public static final int INDEX_FAVORITE = 9;
    public static final int INDEX_FIRST_REVIEW_LINK = 10;
    public static final int INDEX_FIRST_REVIEW_CONTENT = 11;
    public static final int INDEX_SECOND_REVIEW_LINK = 12;
    public static final int INDEX_SECOND_REVIEW_CONTENT = 13;

    //ID for the loader which is responsible for obtaining the data for the movie from the DB
    private static final int DETAIL_LOADER_ID = 0;

    //An extra which is used to identify the Uri that is passed into the dialog for the movie trailers;
    //This extra is needed since a Bundle is passed into the intent that laucnhes the
    //MovieTrailersDialogFragment
    public static final String EXTRA_URI_FOR_DIALOG =
            "com.example.android.popularmovies_stage2.activities.moviedetails.uri";

    //Member variables which contain the Uri and movie ID; the movie ID is later used so that
    //the ViewPager scrolls to the appropriate movie that the user selected within MovieSelection
    private Uri mMovieUri;
    private int mMovieID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //We need to ensure that the layout is set for the ViewPager within this method
        setContentView(R.layout.activity_view_pager);

        //We obtain a reference to the intent received from the MovieSelection class
        Intent receivedIntent = getIntent();

        //...and then extra the Uri that was attached to the intent
        mMovieUri = receivedIntent.getData();

        //If the Uri is empty, then throw an exception; not much that we can do in this activity
        //with an empty Uri
        if (mMovieUri == null){
            throw new NullPointerException("Uri for the selected movie cannot be null");
        }

        //Extracting the movie's ID from the Uri
        mMovieID = Integer.parseInt(mMovieUri.getLastPathSegment());

        //We then instatiate the ViewPager and PagerAdapter accordingly
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mPagerAdapter = new MoviePagerAdapter(this, null);
        mViewPager.setAdapter(mPagerAdapter);

        //Finally, we launch the loader to obtain the data that is necessary for the ViewPager/PagerAdapter
        getSupportLoaderManager().initLoader(DETAIL_LOADER_ID, null, this);


    }

    //This class is needed because we are implementing the ViewPager widget for the movie details
    //activity. The PagerAdapter works in a similar way as the Adapter for the RecyclerView such
    //that it holds data for multiple views at the same time and also preloads adjacent views.
    public class MoviePagerAdapter extends PagerAdapter{

        //Member variables for the PagerAdapter
        private Context pagerContext;
        private Cursor pagerCursor;

        public MoviePagerAdapter (Context context, Cursor cursor){
            pagerContext = context;
            pagerCursor = cursor;
        }

        //This method creates the page for the given position argument; the adapter is then
        //responsible for adding the page into the collection
        @Override
        public Object instantiateItem(ViewGroup collection, int position){

            //Inflating the layouts for the movie details activity
            LayoutInflater inflater = LayoutInflater.from(pagerContext);
            ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.activity_movie_details,
                    collection, false);

            //Obtaining references to all of the views within the XML layout
            mMoviePoster = (ImageView) layout.findViewById(R.id.iv_movie_poster);
            mMovieTitle = (TextView) layout.findViewById(R.id.tv_movie_title);
            mMovieOverview = (TextView) layout.findViewById(R.id.tv_movie_overview);
            mMovieReleaseDate = (TextView) layout.findViewById(R.id.tv_movie_release_date);
            mMovieVoteResults = (TextView) layout.findViewById(R.id.tv_movie_vote_results);
            mFavorite = (CheckBox) layout.findViewById(R.id.cb_favorite_star);
            mMovieRuntime = (TextView) layout.findViewById(R.id.tv_movie_runtime);

            mReviewsHeading = (TextView) layout.findViewById(R.id.tv_reviews_heading);
            mFirstReview_LL = (LinearLayout) layout.findViewById(R.id.ll_review1);
            mFirstReview_TV = (TextView) layout.findViewById(R.id.tv_review1);
            mSecondReview_LL = (LinearLayout) layout.findViewById(R.id.ll_review2);
            mSecondReview_TV = (TextView) layout.findViewById(R.id.tv_review2);

            mPlayButton = (ImageView) layout.findViewById(R.id.iv_play_button);

            //On-click listener is set and a dialog is launched which displays the movie trailers
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

            //Ensure that the cursor actually has data
            if (pagerCursor != null){

                //Move the cursor to the appropriate position
                pagerCursor.moveToPosition(position);

                //Extract all of the data that we need for the details screen from the cursor
                String title = pagerCursor.getString(INDEX_TITLE);
                String overview = pagerCursor.getString(INDEX_OVERVIEW);
                String releaseDate = pagerCursor.getString(INDEX_RELEASE_DATE);
                int voteCount = pagerCursor.getInt(INDEX_VOTE_COUNT);
                float voteAverage = pagerCursor.getFloat(INDEX_VOTE_AVERAGE);
                String posterPath = pagerCursor.getString(INDEX_POSTER_PATH);
                boolean favorite = pagerCursor.getInt(INDEX_FAVORITE) > 0;
                int runtime = pagerCursor.getInt(INDEX_RUNTIME);

                final String firstReviewLink = pagerCursor.getString(INDEX_FIRST_REVIEW_LINK);
                String firstReviewContent = pagerCursor.getString(INDEX_FIRST_REVIEW_CONTENT);

                final String secondReviewLink = pagerCursor.getString(INDEX_SECOND_REVIEW_LINK);
                final String secondReviewContent = pagerCursor.getString(INDEX_SECOND_REVIEW_CONTENT);

                //Set the movie poster to the ImageView
                Picasso.with(getApplicationContext())
                        .load("https://image.tmdb.org/t/p/w185/" + posterPath)
                        .into(mMoviePoster);

                //Set the movie title and overview accordingly
                mMovieTitle.setText(title);
                mMovieOverview.setText(overview);

                //We have the total number of votes every movie received. However, the vote counts
                //is an integer w/ no comma separators (3456, not 3,456). To make this value more
                //presentable, we first format the integer using NumberFormat so that comma
                //separators are included; it is returned to us as a String
                String formattedVoteCount = NumberFormat.getIntegerInstance()
                        .format(voteCount);
                mMovieVoteResults.setText(Float.toString(voteAverage) + "/10"
                        + " (" + formattedVoteCount + ")");

                //Similar case as with the vote count value, the release date data obtained from
                //TheMovieDB API isn't in a format that is seen often (2016-10-24 for example).
                //Therefore, SimpleDateFormat is used to reformat this data and then display it
                //within the TextView set aside for the release date
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

                //We then set the text with our newly-formatted data
                mMovieReleaseDate.setText(formattedDate);

                //Set the star button if depending on if the movie is favorite'd or not
                mFavorite.setChecked(favorite);

                //Set the runtim TextView
                mMovieRuntime.setText(runtime + "m");

                //If there is no data for the first review...
                if (firstReviewContent == null && firstReviewLink == null){
                    //Remove both the review heading and the views that pertain to the first review
                    mReviewsHeading.setVisibility(View.GONE);
                    mFirstReview_LL.setVisibility(View.GONE);
                }

                //Otherwise, set the text accordingly and enable an on-click listener which launches
                //an implicit intent that takes the user to the full review
                else{
                    mFirstReview_TV.setText(firstReviewContent);
                    mFirstReview_LL.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            Uri firstReviewUri = Uri.parse(firstReviewLink);
                            Intent firstReviewIntent = new Intent(Intent.ACTION_VIEW, firstReviewUri);
                            startActivity(firstReviewIntent);
                        }
                    });
                }

                //Likewise with the second review
                if (secondReviewContent == null && secondReviewLink == null){
                    mSecondReview_LL.setVisibility(View.GONE);
                }

                else{
                    mSecondReview_TV.setText(secondReviewContent);
                    mSecondReview_LL.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            Uri secondReviewUri = Uri.parse(secondReviewLink);
                            Intent secondReviewIntent = new Intent(Intent.ACTION_VIEW, secondReviewUri);
                            startActivity(secondReviewIntent);
                        }
                    });
                }
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
            if (pagerCursor != null){
                return pagerCursor.getCount();
            }
            return 0;
        }

        //Checks whether a particular Object (in this case, the movie details layout) belongs to a
        //given view. The Object is the same Object that is returned by instantiateItem()
        @Override
        public boolean isViewFromObject(View view, Object object){
            return view == object;
        }

        //A helper method which loads in new data into the PagerAdapter once the loader has finished
        //obtaining new data
        public void swapCursor(Cursor newData){
            pagerCursor = newData;
            if (pagerCursor != null){
                notifyDataSetChanged();
            }
        }

    }

    /*
    We create a Loader which contains all of the movies for the current sorting criteria that the
    user has selected within SharedPreferences. We need to have all of these movies available
    because the ViewPager depends on it. It must have all of the data available so that it can
    preload any adjacent items.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection;
        String[] selectionArgs;

        switch (id){

            case DETAIL_LOADER_ID:
                String methodFlagKey = getString(R.string.list_preference_sorting_options_key);
                int methodFlag = SettingsFragment.getPreferenceValue(this, methodFlagKey);

                //We adjust the selection statement accordingly for the current value of the method flag
                switch (methodFlag){
                    case 0:
                        selection = MOVIE_DETAIL_PROJECTION[INDEX_SORTED_BY] + " = ?";
                        selectionArgs = new String[]{Movie.SORTED_BY_POPULARITY};
                        break;

                    case 1:
                        selection = MOVIE_DETAIL_PROJECTION[INDEX_SORTED_BY] + " = ?";
                        selectionArgs = new String[]{Movie.SORTED_BY_TOP_RATED};
                        break;

                    case 2:
                        selection = MOVIE_DETAIL_PROJECTION[INDEX_FAVORITE] + " = ?";
                        selectionArgs = new String [] {"1"};
                        break;

                    default:
                        throw new RuntimeException("Unreasonable value for method flag: " + methodFlag);
                }

                return new CursorLoader(this,
                        MovieContract.MovieTable.CONTENT_URI,
                        MOVIE_DETAIL_PROJECTION,
                        selection,
                        selectionArgs,
                        MovieContract.MovieTable._ID);

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

        //We swap in the new data into the PagerAdapter so that it is using the latest and greatest data
        mPagerAdapter.swapCursor(data);

        //This last step iterates through all of the contents of the Cursor. Once the iteration's
        //movie ID matches to mMovieID, we break from the for loop. This is so that when the user
        //clicks on a movie poster, they are taken directly to that movie. If this loop is not
        //performed, then the user will always be taken to the movie at the beginning of the Cursor,
        //regardless of which movie they actually selected.
        for(int k = 0; k < data.getCount(); k++){
            int currentMovieID = data.getInt(INDEX_MOVIE_ID);
            if (currentMovieID == mMovieID){
                mViewPager.setCurrentItem(k);
                break;
            }
            data.moveToNext();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mPagerAdapter.swapCursor(null);
    }

    //This is a helper method which updated the contents of the DB when the user favorites or
    //un-favorites a movie
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
    }

}
