package com.example.android.popularmovies_stage2.fragments;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.popularmovies_stage2.R;
import com.example.android.popularmovies_stage2.activities.MovieDetails;
import com.example.android.popularmovies_stage2.data.MovieContract;


public class MovieTrailersDialogFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "MovieTrailersFragment";

    TextView mTrailerTitle1;
    TextView mTrailerTitle2;
    Uri mMovieUri;

    private static final int DIALOG_LOADER_ID = 1;

    public static final String[] MOVIE_TRAILER_DIALOG_PROJECTION = {
            MovieContract.MovieTable.COLUMN_FIRST_TRAILER_NAME,
            MovieContract.MovieTable.COLUMN_FIRST_TRAILER_KEY,
            MovieContract.MovieTable.COLUMN_FIRST_TRAILER_THUMBNAIL,
            MovieContract.MovieTable.COLUMN_SECOND_TRAILER_NAME,
            MovieContract.MovieTable.COLUMN_SECOND_TRAILER_KEY,
            MovieContract.MovieTable.COLUMN_SECOND_TRAILER_THUMBNAIL
    };

    public static final int INDEX_FIRST_TRAILER_TITLE = 0;
    public static final int INDEX_FIRST_TRAILER_KEY = 1;
    public static final int INDEX_FIRST_TRAILER_THUMBNAL = 2;
    public static final int INDEX_SECOND_TRAILER_TITLE = 3;
    public static final int INDEX_SECOND_TRAILER_KEY = 4;
    public static final int INDEX_SECOND_TRAILER_THUMBNAIL = 5;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.i(TAG, "onCreateDialog() is called");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_movie_trailers, null);
        mTrailerTitle1 = (TextView) dialogView.findViewById(R.id.tv_movie_trailer_title1);
        mTrailerTitle2 = (TextView) dialogView.findViewById(R.id.tv_movie_trailer_title2);

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

        mTrailerTitle1.setText(firstTrailerTitle);
        mTrailerTitle2.setText(secondTrailerTitle);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Nothing to do here just yet
    }
}
