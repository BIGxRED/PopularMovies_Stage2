package com.example.android.popularmovies_stage2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MovieDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 5;


    public MovieDBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + MovieContract.MovieTable.TABLE_NAME + " (" +
            MovieContract.MovieTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            MovieContract.MovieTable.COLUMN_TITLE + " TEXT NOT NULL, " +
            MovieContract.MovieTable.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
            MovieContract.MovieTable.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
            MovieContract.MovieTable.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
            MovieContract.MovieTable.COLUMN_VOTE_COUNT + " INTEGER NOT NULL, " +
            MovieContract.MovieTable.COLUMN_VOTE_AVERAGE + " REAL NOT NULL, " +
            MovieContract.MovieTable.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
            MovieContract.MovieTable.COLUMN_BACKDROP_PATH + " TEXT NOT NULL, " +
            MovieContract.MovieTable.COLUMN_SORTED_BY + " TEXT NOT NULL, " +
            MovieContract.MovieTable.COLUMN_RUNTIME + " INTEGER NOT NULL, " +
            MovieContract.MovieTable.COLUMN_FAVORITE + " INTEGER NOT NULL, " +
            MovieContract.MovieTable.COLUMN_FIRST_TRAILER_KEY + " TEXT, " +
            MovieContract.MovieTable.COLUMN_FIRST_TRAILER_NAME + " TEXT, " +
            MovieContract.MovieTable.COLUMN_FIRST_TRAILER_THUMBNAIL + " TEXT, " +
            MovieContract.MovieTable.COLUMN_SECOND_TRAILER_KEY + " TEXT, " +
            MovieContract.MovieTable.COLUMN_SECOND_TRAILER_NAME + " TEXT, " +
            MovieContract.MovieTable.COLUMN_SECOND_TRAILER_THUMBNAIL + " TEXT, " +
            MovieContract.MovieTable.COLUMN_FIRST_REVIEW_LINK + " TEXT, " +
            MovieContract.MovieTable.COLUMN_FIRST_REVIEW_CONTENT + " TEXT, " +
            MovieContract.MovieTable.COLUMN_SECOND_REVIEW_LINK + " TEXT, " +
            MovieContract.MovieTable.COLUMN_SECOND_REVIEW_CONTENT + " TEXT" + "); ";

        db.execSQL(SQL_CREATE_MOVIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.MovieTable.TABLE_NAME);
        onCreate(db);
    }
}
