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

    private static final String TAG = "MovieTrailersFragment";

    TextView mTrailer1Title;
    TextView mTrailer2Title;
    ImageView mTrailer1Thumbnail;
    ImageView mTrailer2Thumbnail;
    LinearLayout mTrailer1;
    LinearLayout mTrailer2;

    Uri mMovieUri;

    private static final int DIALOG_LOADER_ID = 1;

    public static final String[] MOVIE_TRAILER_DIALOG_PROJECTION = {
            MovieContract.MovieTable.COLUMN_FIRST_TRAILER_NAME,
            MovieContract.MovieTable.COLUMN_FIRST_TRAILER_KEY,
            MovieContract.MovieTable.COLUMN_SECOND_TRAILER_NAME,
            MovieContract.MovieTable.COLUMN_SECOND_TRAILER_KEY
    };

    public static final int INDEX_FIRST_TRAILER_TITLE = 0;
    public static final int INDEX_FIRST_TRAILER_KEY = 1;
    public static final int INDEX_SECOND_TRAILER_TITLE = 2;
    public static final int INDEX_SECOND_TRAILER_KEY = 3;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.i(TAG, "onCreateDialog() is called");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_movie_trailers, null);
        mTrailer1Title = (TextView) dialogView.findViewById(R.id.tv_movie_trailer_title1);
        mTrailer2Title = (TextView) dialogView.findViewById(R.id.tv_movie_trailer_title2);
        mTrailer1Thumbnail = (ImageView) dialogView.findViewById(R.id.iv_first_trailer_thumbnail);
        mTrailer2Thumbnail = (ImageView) dialogView.findViewById(R.id.iv_second_trailer_thumbnail);
        mTrailer1 = (LinearLayout) dialogView.findViewById(R.id.ll_first_trailer);
        mTrailer2 = (LinearLayout) dialogView.findViewById(R.id.ll_second_trailer);

        if (getArguments().containsKey(MovieDetails.EXTRA_URI_FOR_DIALOG))
            mMovieUri = Uri.parse(getArguments().getString(MovieDetails.EXTRA_URI_FOR_DIALOG));

        LoaderManager loaderManager = getActivity().getSupportLoaderManager();
        loaderManager.initLoader(DIALOG_LOADER_ID, null, this);

        builder.setView(dialogView)
                .setTitle(R.string.alert_dialog_title)
                .setNegativeButton(R.string.alert_dialog_cancel_button_text,null);
        return builder.create();
    }

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

        String firstTrailerTitle = data.getString(INDEX_FIRST_TRAILER_TITLE);
        String secondTrailerTitle = data.getString(INDEX_SECOND_TRAILER_TITLE);

        final String firstTrailerKey = data.getString(INDEX_FIRST_TRAILER_KEY);
        final String secondTrailerKey = data.getString(INDEX_SECOND_TRAILER_KEY);

        if (firstTrailerTitle == null && firstTrailerKey == null){
            mTrailer1.setVisibility(View.GONE);
        }

        else{
            Picasso.with(getContext())
                    .load("https://img.youtube.com/vi/" + firstTrailerKey + "/default.jpg")
                    .into(mTrailer1Thumbnail);

            mTrailer1Title.setText(firstTrailerTitle);

            mTrailer1.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Uri firstTrailerUri = Uri.parse("https://www.youtube.com/watch").buildUpon()
                            .appendQueryParameter("v",firstTrailerKey)
                            .build();
                    Intent firstTrailerIntent = new Intent(Intent.ACTION_VIEW, firstTrailerUri);

                    //TODO: Make sure to add this to the Strings XML later on
                    String chooserTitle = "Choose app to view trailer:";
                    Intent chooser = Intent.createChooser(firstTrailerIntent,chooserTitle);
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

                    String chooserTitle = "Choose app to view trailer:";
                    Intent chooser = Intent.createChooser(firstTrailerIntent,chooserTitle);
                    startActivity(chooser);
                }
            });
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Nothing to do here just yet
    }
}
