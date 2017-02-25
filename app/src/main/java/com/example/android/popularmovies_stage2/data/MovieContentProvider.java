package com.example.android.popularmovies_stage2.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class MovieContentProvider extends ContentProvider {

    public static final int MOVIES = 100;
    public static final int MOVIE_WITH_ID = 101;

    private MovieDBHelper mDBHelper;

    public static final UriMatcher sMatcher = buildUriMatcher();

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mDBHelper = new MovieDBHelper(context);

        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;

        switch (sMatcher.match(uri)){
            case MOVIES:
                projection = null;
                selection = null;
                selectionArgs = null;
                cursor = mDBHelper.getReadableDatabase().query(MovieContract.MovieTable.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case MOVIE_WITH_ID:
                String movieID = uri.getLastPathSegment();
                projection = null;
                selection = MovieContract.MovieTable._ID + " = ?";
                selectionArgs = new String[]{movieID};

                cursor = mDBHelper.getReadableDatabase().query(MovieContract.MovieTable.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase database = mDBHelper.getWritableDatabase();
        switch (sMatcher.match(uri)){
            case MOVIES:
                database.beginTransaction();
                long newID = database.insert(MovieContract.MovieTable.TABLE_NAME,null,values);
                if (newID == -1){
                    throw new SQLiteException("Unable to insert movie using the following Uri: " + uri.toString());
                }
                database.setTransactionSuccessful();
                database.endTransaction();
                if (newID != -1){
                    getContext().getContentResolver().notifyChange(uri,null);
                }
                return MovieContract.MovieTable.CONTENT_URI.buildUpon().appendPath(Long.toString(newID)).build();

            case MOVIE_WITH_ID:
                throw new UnsupportedOperationException("Unknown uri: " + uri
                        + "; Cannot perform insert with a Uri that corresponds to a single movie");
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }

    }

    @Override
    public int bulkInsert(Uri uri,@NonNull ContentValues[] values) {
        final SQLiteDatabase database = mDBHelper.getWritableDatabase();

        switch (sMatcher.match(uri)){
            case MOVIES:
                database.beginTransaction();
                int insertedRowsCount = 0;
                try{
                    for (ContentValues value: values){
                        long newID = database.insert(MovieContract.MovieTable.TABLE_NAME,null,value);
                        if (newID != -1){
                            insertedRowsCount++;
                        }
                    }
                    database.setTransactionSuccessful();
                }
                finally {
                    database.endTransaction();
                }
                if (insertedRowsCount > 0){
                    getContext().getContentResolver().notifyChange(uri,null);
                }
                return insertedRowsCount;
            case MOVIE_WITH_ID:
                throw new UnsupportedOperationException("Unknown uri: " + uri
                        + "; Cannot perform bulk insert with a Uri that corresponds to a single movie");

            default:
                return super.bulkInsert(uri,values);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int deletedRowsCount;

         /*
         We perform this check because if "null" is passed as the selection, we will indeed delete
         all of the contents of our database. However, we also will not know how many rows were
         deleted. According to the SQLite documentation, passing a value of "1" for the selection
         will delete all of the rows and will also return the number of rows that were deleted,
         which is the expected outcome of this method.
         */
        if (selection == null){
            selection = "1";
        }

        final SQLiteDatabase database = mDBHelper.getWritableDatabase();

        switch (sMatcher.match(uri)){
            case MOVIES:
                deletedRowsCount = database.delete(MovieContract.MovieTable.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;

            case MOVIE_WITH_ID:
                if (selection.equals("1")){
                    throw new UnsupportedOperationException("Unknown uri: " + uri
                            + "; Cannot perform delete of all movies with a Uri that corresponds to a single movie");
                }

                deletedRowsCount = database.delete(MovieContract.MovieTable.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (deletedRowsCount > 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return deletedRowsCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    public static UriMatcher buildUriMatcher(){
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        matcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_MOVIES, MOVIES);
        matcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_MOVIES + "/#", MOVIE_WITH_ID);

        return matcher;
    }
}
