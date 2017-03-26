/*
The following code is the property and sole work of Mike Palarz, a student at Udacity
 */

package com.example.android.popularmovies_stage2.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.popularmovies_stage2.R;
import com.example.android.popularmovies_stage2.activities.MovieDetails;
import com.example.android.popularmovies_stage2.data.MovieContract;
import com.squareup.picasso.Picasso;


public class MovieTrailersDialogFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "MovieTrailersFragment";  //Tag used for debugging

    //TextViews which show the title of the trailer
    TextView mTrailer1Title;
    TextView mTrailer2Title;

    //ImageViews which display a thumbnail of the trailer, obtained via a Youtube network call
    ImageView mTrailer1Thumbnail;
    ImageView mTrailer2Thumbnail;
    LinearLayout mTrailer1;
    LinearLayout mTrailer2;

    //Uri used to obtain the trailer data
    Uri mMovieUri;

    /*
    ID for loader which obtains the trailer data from the SQLIte DB
     */
    private static final int DIALOG_LOADER_ID = 1;

    //Array which is used to simplify queries into the DB
    public static final String[] MOVIE_TRAILER_DIALOG_PROJECTION = {
            MovieContract.MovieTable.COLUMN_FIRST_TRAILER_NAME,
            MovieContract.MovieTable.COLUMN_FIRST_TRAILER_KEY,
            MovieContract.MovieTable.COLUMN_SECOND_TRAILER_NAME,
            MovieContract.MovieTable.COLUMN_SECOND_TRAILER_KEY
    };

    //Integer values that refer to the Strings within MOVIE_TRAILER_DIALOG_PROJECTION
    public static final int INDEX_FIRST_TRAILER_TITLE = 0;
    public static final int INDEX_FIRST_TRAILER_KEY = 1;
    public static final int INDEX_SECOND_TRAILER_TITLE = 2;
    public static final int INDEX_SECOND_TRAILER_KEY = 3;


    @NonNull
    @Override
    /*
    This method is overriden in order to create our own, custom Dialog. In addition, because we are
    creating an AlertDialog, we only need to override onCreateDialog(). It is not necessary to
    override onCreateView because AlertDialog takes care of that for us.
     */
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //First we instantiate a builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //...and obtain a LayoutInflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        //We inflate that layout that's been created for the dialog
        View dialogView = inflater.inflate(R.layout.dialog_movie_trailers, null);

        //And instatiate all of the views within the layout
        mTrailer1Title = (TextView) dialogView.findViewById(R.id.tv_movie_trailer_title1);
        mTrailer2Title = (TextView) dialogView.findViewById(R.id.tv_movie_trailer_title2);
        mTrailer1Thumbnail = (ImageView) dialogView.findViewById(R.id.iv_first_trailer_thumbnail);
        mTrailer2Thumbnail = (ImageView) dialogView.findViewById(R.id.iv_second_trailer_thumbnail);
        mTrailer1 = (LinearLayout) dialogView.findViewById(R.id.ll_first_trailer);
        mTrailer2 = (LinearLayout) dialogView.findViewById(R.id.ll_second_trailer);

        //We then obtain the Uri that was passed into the bundle
        if (getArguments().containsKey(MovieDetails.EXTRA_URI_FOR_DIALOG))
            mMovieUri = Uri.parse(getArguments().getString(MovieDetails.EXTRA_URI_FOR_DIALOG));

        //Loader is launched to obtain the necessary data
        LoaderManager loaderManager = getActivity().getSupportLoaderManager();
        loaderManager.initLoader(DIALOG_LOADER_ID, null, this);

        //The AlertDialog is then constructed using the builder object that was instantiated earlier
        return builder.setView(dialogView)
                .setTitle(R.string.alert_dialog_title)
                .setNegativeButton(R.string.alert_dialog_cancel_button_text,null)
                .create();
    }

    //We create a Loader which obtains the data that applies to the movie trailers
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case DIALOG_LOADER_ID:

                return new CursorLoader(getContext(),
                        mMovieUri,
                        MOVIE_TRAILER_DIALOG_PROJECTION,
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

        //The data for the trailers is extracted from the Cursor
        String firstTrailerTitle = data.getString(INDEX_FIRST_TRAILER_TITLE);
        String secondTrailerTitle = data.getString(INDEX_SECOND_TRAILER_TITLE);

        final String firstTrailerKey = data.getString(INDEX_FIRST_TRAILER_KEY);
        final String secondTrailerKey = data.getString(INDEX_SECOND_TRAILER_KEY);

        //We make sure to remove the views that pertain to each of the trailers if there was no
        //data available for the given trailer
        if (firstTrailerTitle == null && firstTrailerKey == null){
            mTrailer1.setVisibility(View.GONE);
        }

        //If data is available, then the views are populated accordingly
        else{
            Picasso.with(getContext())
                    .load("https://img.youtube.com/vi/" + firstTrailerKey + "/default.jpg")
                    .into(mTrailer1Thumbnail);

            mTrailer1Title.setText(firstTrailerTitle);

            //An on-click listener is created so that the trailer is launched when the user
            //clicks on the LinearLayout
            mTrailer1.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {

                    //A Uri is constructed using the key obtained from TMDB
                    Uri firstTrailerUri = Uri.parse("https://www.youtube.com/watch").buildUpon()
                            .appendQueryParameter("v",firstTrailerKey)
                            .build();

                    //An implicit intent is then created
                    Intent firstTrailerIntent = new Intent(Intent.ACTION_VIEW, firstTrailerUri);

                    //We allow the user to choose which app they'd like to view the trailer in
                    //by creating a chooser intent
                    String chooserTitle = getString(R.string.alert_dialog_chooser_title);
                    Intent chooser = Intent.createChooser(firstTrailerIntent, chooserTitle);
                    startActivity(chooser);
                }
            });
        }

        if (secondTrailerTitle == null && secondTrailerKey == null){
            mTrailer2.setVisibility(View.GONE);
        }

        else{
            Picasso.with(getContext())
                    .load("https://img.youtube.com/vi/" + secondTrailerKey + "/default.jpg")
                    .into(mTrailer2Thumbnail);

            mTrailer2Title.setText(secondTrailerTitle);

            mTrailer2.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Uri secondTrailerUri = Uri.parse("https://www.youtube.com/watch").buildUpon()
                            .appendQueryParameter("v", secondTrailerKey)
                            .build();
                    Intent firstTrailerIntent = new Intent(Intent.ACTION_VIEW, secondTrailerUri);

                    String chooserTitle = getString(R.string.alert_dialog_chooser_title);
                    Intent chooser = Intent.createChooser(firstTrailerIntent,chooserTitle);
                    startActivity(chooser);
                }
            });
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Nothing to do here
    }
}
