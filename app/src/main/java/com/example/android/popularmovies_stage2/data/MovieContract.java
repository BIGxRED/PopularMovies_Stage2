package com.example.android.popularmovies_stage2.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class MovieContract {

    private MovieContract(){

    }

    public static final String AUTHORITY = "com.example.android.popularmovies_stage2";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_MOVIES = "movies";

    public static final class MovieTable implements BaseColumns{
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String TABLE_NAME = "movies";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_MOVIE_ID = "movieID";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RELEASE_DATE = "releaseDate";
        public static final String COLUMN_VOTE_COUNT = "voteCount";
        public static final String COLUMN_VOTE_AVERAGE = "voteAverage";
        public static final String COLUMN_POSTER_PATH = "posterPath";
        public static final String COLUMN_BACKDROP_PATH = "backdropPath";
        public static final String COLUMN_SORTED_BY = "sortedBy";
        public static final String COLUMN_RUNTIME = "runtime";
        public static final String COLUMN_FAVORITE = "favorite";
        public static final String COLUMN_FIRST_TRAILER_KEY = "firstTrailerKey";
        public static final String COLUMN_FIRST_TRAILER_NAME = "firstTrailerName";
        public static final String COLUMN_SECOND_TRAILER_KEY = "secondTrailerKey";
        public static final String COLUMN_SECOND_TRAILER_NAME = "secondTrailerName";

    }
}
