/*
The following code is the property and sole work of Mike Palarz, a student at Udacity
 */

package com.example.android.popularmovies_stage2;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.android.popularmovies_stage2.data.MovieContract;
import com.example.android.popularmovies_stage2.fragments.SettingsFragment;
import com.facebook.stetho.Stetho;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLDataException;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Scanner;

/*
This class is responsible for actually obtaining the data from TheMovieDB. It contains several
 helper methods, such as constructing the URL, obtaining the HTTP response, and finally parsing
 through the JSON data.
 */
public class MovieFetcher {
    private static final String TAG = "MovieFetcher";

    //Member variables which store the page number of their respective movie types
    public static int mPopularPageNumber;
    public static int mTopRatedPageNumber;

    //Constants used for building the base URL
    private static final String MOVIEDB_BASE_URL = "https://api.themoviedb.org/3/movie";
    private static final String METHOD_MOVIE_POPULAR = "popular";
    private static final String METHOD_MOVIE_TOP_RATED = "top_rated";

    //Parameters to be used with the query
    private static final String PARAM_API_KEY = "api_key";
    private static final String PARAM_LANGUAGE = "language";
    private static final String PARAM_PAGE = "page";
    private static final String PARAM_APPEND_T0_RESPONSE = "append_to_response";

    //Parameter values to be used when building the URL
    private static final String API_KEY = BuildConfig.THE_MOVIE_DB_API_TOKEN;
    private static final String LANGUAGE = "en-US";
    private static final String APPENDED_REQUESTS = "videos,reviews";

    //Constants which are used when parsing the JSON data
    private static final String JSON_KEY_TITLE = "title";
    private static final String JSON_KEY_ID = "id";
    private static final String JSON_KEY_POSTER_PATH = "poster_path";
    private static final String JSON_KEY_OVERVIEW = "overview";
    private static final String JSON_KEY_RELEASE_DATE = "release_date";
    private static final String JSON_KEY_VOTE_COUNT = "vote_count";
    private static final String JSON_KEY_VOTE_AVERAGE = "vote_average";
    private static final String JSON_KEY_BACKDROP_PATH = "backdrop_path";
    private static final String JSON_KEY_RUNTIME = "runtime";
    private static final String JSON_KEY_VIDEOS = "videos";
    private static final String JSON_KEY_ARRAY_RESULTS = "results";
    private static final String JSON_KEY_TRAILER_KEY = "key";
    private static final String JSON_KEY_TRAILER_NAME = "name";
    private static final String JSON_KEY_REVIEWS = "reviews";
    private static final String JSON_KEY_REVIEWS_COUNT = "total_results";
    private static final String JSON_KEY_REVIEW_URL = "url";
    private static final String JSON_KEY_REVIEW_CONTENT = "content";

    //Getter and setter methods for the member variables
    public static int getPopularPageNumber(){
        return mPopularPageNumber;
    }

    public static void setPopularPageNumber(int newPageNumber){
        mPopularPageNumber = newPageNumber;
    }

    public static int getTopRatedPageNumber(){
        return mTopRatedPageNumber;
    }

    public static void setTopRatedPageNumber(int newPageNumber){
        mTopRatedPageNumber = newPageNumber;
    }

    //Method which constructs a URL for obtaining the initial data needed for all movies
    private static URL buildURL(int methodFlag, int pageNumber){
        String queryMethod;

        //queryMethod is dependent on the current value of the method flag, which is stored within
        //SharedPreferences. The method flag must be provided in order to construct the URL
        switch (methodFlag){
            case 0:
                queryMethod = METHOD_MOVIE_POPULAR;
                break;

            case 1:
                queryMethod = METHOD_MOVIE_TOP_RATED;
                break;

            case 2:
                throw new UnsupportedOperationException("Can't build a URL for favorite movies");

            default:
                queryMethod = METHOD_MOVIE_POPULAR;
                break;
        }


        //A Uri is first built according to TheMovieDB API's documentation
        Uri builtUri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
                .appendPath(queryMethod)
                .appendQueryParameter(PARAM_API_KEY, API_KEY)
                .appendQueryParameter(PARAM_LANGUAGE, LANGUAGE)
                .appendQueryParameter(PARAM_PAGE, Integer.toString(pageNumber))
                .build();

        URL url = null;

        try{
            //The Uri is then converted into a URL if possible
            url = new URL(builtUri.toString());
        }
        catch (MalformedURLException mue){
            mue.printStackTrace();
        }

        return url;
    }

    //A different URL is needed in order to obtain additional information about a movie. The
    //additional information includes the movie's runtime, trailers, and reviews.
    private static URL buildURLForDetails(int movieID){

        //A Uri is first built according to TheMovieDB API's documentation
        Uri builtUri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
                .appendPath(Integer.toString(movieID))
                .appendQueryParameter(PARAM_API_KEY, API_KEY)
                .appendQueryParameter(PARAM_LANGUAGE, LANGUAGE)
                .appendQueryParameter(PARAM_APPEND_T0_RESPONSE, APPENDED_REQUESTS)
                .build();

        URL url = null;

        try{
            //The Uri is then converted into a URL if possible
            url = new URL(builtUri.toString());
        }
        catch (MalformedURLException mue){
            mue.printStackTrace();
        }

        return url;
    }


    //This method returns the data that is provided by a URL. For the purposes of this project, the
    //URL that is expected to be used with this method is the one which is generated by buildURL()
    //or buildURLForDetails. The String which is returned is the full HTTP response. In the case of
    //this project, that String is an array of JSON data.
    private static String getHTTPResponse(URL url) throws IOException{
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try{
            InputStream in = connection.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            if(scanner.hasNext())
                return scanner.next();
            else
                return null;
        }
        finally {
            connection.disconnect();
        }
    }

    //This method parses through JSON data, which is expected to be provided by a String generated
    //by getHTTPResponse(). The String is parsed through, JSON objects are iteratively created and
    //appended to an array of ContentValues.
    private static ContentValues[] parseMovies(String httpResponse, int methodFlag) throws JSONException{
        JSONObject jsonRoot = new JSONObject(httpResponse);
        //All of the movies are contained within an array. The array in this case may be referred to
        //as "results"
        JSONArray jsonResults = jsonRoot.getJSONArray("results");

        ContentValues[] movies = new ContentValues[jsonResults.length()];

        //The array is then iterated through and a JSON object is created for each entry within
        //jsonResults. The data is then appended to movies.
        for (int i = 0; i < jsonResults.length(); i++){
            JSONObject jsonMovie = jsonResults.getJSONObject(i);

            //All of the pertinent movie data is extracted from the JSON object, jsonMovie.
            String title = jsonMovie.getString(JSON_KEY_TITLE);
            int ID = jsonMovie.getInt(JSON_KEY_ID);
            String posterPath = jsonMovie.getString(JSON_KEY_POSTER_PATH);
            String overview = jsonMovie.getString(JSON_KEY_OVERVIEW);
            String releaseDate = jsonMovie.getString(JSON_KEY_RELEASE_DATE);
            int voteCount = jsonMovie.getInt(JSON_KEY_VOTE_COUNT);

            //getDouble() is being used in this case because a double is essentially a float, only
            //with a higher precision (more decimal values); therefore, the correct value should be
            //obtained if the returned double is cast into a float
            float voteAverage = (float) jsonMovie.getDouble(JSON_KEY_VOTE_AVERAGE);

            //Although this value is never used throughout the app, it was originally implemented
            //in stage 1. Therefore, it is kept here for the sake of completeness.
            String backdropPath = jsonMovie.getString(JSON_KEY_BACKDROP_PATH);

            //The data is then applied to a ContentValues object.
            ContentValues movieCV = new ContentValues();
            movieCV.put(MovieContract.MovieTable.COLUMN_TITLE, title);
            movieCV.put(MovieContract.MovieTable.COLUMN_MOVIE_ID, ID);
            movieCV.put(MovieContract.MovieTable.COLUMN_POSTER_PATH, posterPath);
            movieCV.put(MovieContract.MovieTable.COLUMN_OVERVIEW, overview);
            movieCV.put(MovieContract.MovieTable.COLUMN_RELEASE_DATE, releaseDate);
            movieCV.put(MovieContract.MovieTable.COLUMN_VOTE_COUNT, voteCount);
            movieCV.put(MovieContract.MovieTable.COLUMN_VOTE_AVERAGE, voteAverage);
            movieCV.put(MovieContract.MovieTable.COLUMN_BACKDROP_PATH, backdropPath);

            switch (methodFlag){
                case 0:
                    movieCV.put(MovieContract.MovieTable.COLUMN_SORTED_BY, Movie.SORTED_BY_POPULARITY);
                    break;
                case 1:
                    movieCV.put(MovieContract.MovieTable.COLUMN_SORTED_BY, Movie.SORTED_BY_TOP_RATED);
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown value for the sorted by column");
            }

            movieCV.put(MovieContract.MovieTable.COLUMN_RUNTIME, 0);
            movieCV.put(MovieContract.MovieTable.COLUMN_FAVORITE, false);

            movies[i] = movieCV;

        }
        return movies;
    }

    //This method obtains the additional data that is needed when the user clicks on a particular
    //movie poster. This additional data includes the movie runtime, trailers, and reviews.
    private static ContentValues parseDetails(String httpResponse) throws JSONException{
        JSONObject jsonResponse = new JSONObject(httpResponse);

        //updatedValues will contain all of the new data that will be inserted into the DB
        ContentValues updatedValues = new ContentValues();

        //We obtain all of the necessary information from the JSON response
        int runtime = jsonResponse.getInt(JSON_KEY_RUNTIME);
        updatedValues.put(MovieContract.MovieTable.COLUMN_RUNTIME, runtime);

        JSONObject jsonVideos = jsonResponse.getJSONObject(JSON_KEY_VIDEOS);
        JSONArray jsonVideoResults = jsonVideos.getJSONArray(JSON_KEY_ARRAY_RESULTS);
        int numberOfTrailers = jsonVideoResults.length();
        JSONObject firstTrailer;
        JSONObject secondTrailer;

        /*
        Depending on the movie, there may be no trailers, 1 trailer, or 2 trailers. Each case is
        handled accordingly and the data is applied to updatedValues.
         */
        if (numberOfTrailers == 0){
            firstTrailer = null;
            secondTrailer = null;
        }
        else if (numberOfTrailers == 1){
            firstTrailer = jsonVideoResults.getJSONObject(0);
            secondTrailer = null;

            String firstTrailerKey = firstTrailer.getString(JSON_KEY_TRAILER_KEY);
            String firstTrailerName = firstTrailer.getString(JSON_KEY_TRAILER_NAME);
            updatedValues.put(MovieContract.MovieTable.COLUMN_FIRST_TRAILER_KEY, firstTrailerKey);
            updatedValues.put(MovieContract.MovieTable.COLUMN_FIRST_TRAILER_NAME, firstTrailerName);
        }
        else if (numberOfTrailers >= 2){
            firstTrailer = jsonVideoResults.getJSONObject(0);
            secondTrailer = jsonVideoResults.getJSONObject(1);

            String firstTrailerKey = firstTrailer.getString(JSON_KEY_TRAILER_KEY);
            String firstTrailerName = firstTrailer.getString(JSON_KEY_TRAILER_NAME);

            String secondTrailerKey = secondTrailer.getString(JSON_KEY_TRAILER_KEY);
            String secondTrailerName = secondTrailer.getString(JSON_KEY_TRAILER_NAME);

            updatedValues.put(MovieContract.MovieTable.COLUMN_FIRST_TRAILER_KEY, firstTrailerKey);
            updatedValues.put(MovieContract.MovieTable.COLUMN_FIRST_TRAILER_NAME, firstTrailerName);
            updatedValues.put(MovieContract.MovieTable.COLUMN_SECOND_TRAILER_KEY, secondTrailerKey);
            updatedValues.put(MovieContract.MovieTable.COLUMN_SECOND_TRAILER_NAME, secondTrailerName);
        }

        /*
        The same is true for the reviews. Depending on the number of reviews, updatedValues is
        updated accordingly.
         */
        JSONObject jsonReviews = jsonResponse.getJSONObject(JSON_KEY_REVIEWS);
        int numberOfReviews = jsonReviews.getInt(JSON_KEY_REVIEWS_COUNT);

        JSONArray jsonReviewsResults = jsonReviews.getJSONArray(JSON_KEY_ARRAY_RESULTS);
        JSONObject firstReview;
        JSONObject secondReview;

        if (numberOfReviews == 0){
            firstReview = null;
            secondReview = null;
        }
        else if (numberOfReviews == 1){
            firstReview = jsonReviewsResults.getJSONObject(0);
            secondReview = null;

            String firstReviewLink = firstReview.getString(JSON_KEY_REVIEW_URL);
            String firstReviewContent = firstReview.getString(JSON_KEY_REVIEW_CONTENT);

            updatedValues.put(MovieContract.MovieTable.COLUMN_FIRST_REVIEW_LINK, firstReviewLink);
            updatedValues.put(MovieContract.MovieTable.COLUMN_FIRST_REVIEW_CONTENT, firstReviewContent);
        }
        else if (numberOfReviews >= 2){
            firstReview = jsonReviewsResults.getJSONObject(0);
            secondReview = jsonReviewsResults.getJSONObject(1);

            String firstReviewLink = firstReview.getString(JSON_KEY_REVIEW_URL);
            String firstReviewContent = firstReview.getString(JSON_KEY_REVIEW_CONTENT);

            String secondReviewLink = secondReview.getString(JSON_KEY_REVIEW_URL);
            String secondReviewContent = secondReview.getString(JSON_KEY_REVIEW_CONTENT);

            updatedValues.put(MovieContract.MovieTable.COLUMN_FIRST_REVIEW_LINK, firstReviewLink);
            updatedValues.put(MovieContract.MovieTable.COLUMN_FIRST_REVIEW_CONTENT, firstReviewContent);
            updatedValues.put(MovieContract.MovieTable.COLUMN_SECOND_REVIEW_LINK, secondReviewLink);
            updatedValues.put(MovieContract.MovieTable.COLUMN_SECOND_REVIEW_CONTENT, secondReviewContent);
        }

        return updatedValues;
    }

    //A helper method, which essentially combines buildURL(), getHTTPResponse(), and parseMovies()
    //all within one method. This form of the method doesn't require a Context as one of the inputs.
    //The reason for this form is because it is only meant to initialize the DB within start up.
    public static ContentValues[] fetchMovies(){

        //The array that we will actually return at the end of this method
        ContentValues[] movies;

        //We use an ArrayList here so that we can more efficiently load all of the movie data
        //within a single array.
        ArrayList<ContentValues> moviesList = new ArrayList<>();
        int pageNumber;

        //We use this array so that we can parse all of the movie types (popular and top rated)
        //within one for loop
        int[] pageNumbers = {mPopularPageNumber, mTopRatedPageNumber};
        int max;    //The max number of pages of data that will be parsed

        for (int methodFlag = 0; methodFlag < 2; methodFlag++){
            pageNumber = pageNumbers[methodFlag];

             /*
            * The first time the app is ever loaded (upon installation), the values of both page number
            * variables will be the default value of an int, which is 0. This will cause the app to
            * attempt to query TheMovieDB w/ a 0 page number, which will return an empty result because
            * no such page number exists. To get around this issue, we check for this and set the
            * pageNumber variable to 1.
            */
            if (pageNumber == 0){
                pageNumber = 1;
            }

            //Within atart up, we load in the first 5 pages of data for both movie types
            max = pageNumber + 5;

            //We then append all of the movie data to moviesList, both popular and top rated
            try {
                while(pageNumber < max) {
                    String httpResponse = getHTTPResponse(buildURL(methodFlag, pageNumber));
                    moviesList.addAll(Arrays.asList(parseMovies(httpResponse, methodFlag)));
                    pageNumber++;
                }
            }

            catch(IOException ioe){
                movies = null;
                ioe.printStackTrace();
            }

            catch(JSONException je){
                movies = null;
                je.printStackTrace();
            }

            //At the end of the loop, we update the values of the page numbers
            if (methodFlag == 0){
                setPopularPageNumber(pageNumber);
            }
            else{
                setTopRatedPageNumber(pageNumber);
            }
        }

        //Finally, we pass all of the data contained within the ArrayList to the ContentValues array
        movies = moviesList.toArray(new ContentValues[0]);

        return movies;
    }


    //A helper method, which essentially combines buildURL(), getHTTPResponse(), and parseMovies()
    //all within one method. A Context object is required in this case so that the method flag
    //can be obtained from SharedPreferences. This form of fetchMovies is used only for the purposes
    //of pagination (endless scrolling) so that movies are loaded more efficient;y. This way,
    //when the user is viewing top-rated movies, only additional top-rated movies are loaded (and
    //vice versa for the other sorting options).
    public static ContentValues[] fetchMovies(Context context){

        //The majority of the process here is the same as the other form of fetchMovies()
        ContentValues[] movies;
        ArrayList<ContentValues> moviesList = new ArrayList<>();
        int pageNumber;

        String methodFlagKey = context.getString(R.string.list_preference_sorting_options_key);
        int methodFlag = SettingsFragment.getPreferenceValue(context, methodFlagKey);

        switch (methodFlag){
            case 0:
                pageNumber = mPopularPageNumber;
                break;

            case 1:
                pageNumber = mTopRatedPageNumber;
                break;

            case 2:
                throw new UnsupportedOperationException("Can't fetch data with favorite movies");

            default:
                throw new UnsupportedOperationException("Unknown value for method flag");
        }


        /*
        * The first time the app is ever loaded (upon installation), the values of both page number
        * variables will be the default value of an int, which is 0. This will cause the app to
        * attempt to query TheMovieDB w/ a 0 page number, which will return an empty result because
        * no such page number exists. To get around this issue, we check for this and set the
        * pageNumber variable to 1.
        */

        if (pageNumber == 0){
            pageNumber = 1;
        }

        int max = pageNumber + 5;

        try {
            while(pageNumber < max) {
                String httpResponse = getHTTPResponse(buildURL(methodFlag, pageNumber));
                moviesList.addAll(Arrays.asList(parseMovies(httpResponse, methodFlag)));
                pageNumber++;
            }
        }

        catch(IOException ioe){
            movies = null;
            ioe.printStackTrace();
        }

        catch(JSONException je){
            movies = null;
            je.printStackTrace();
        }

        updatePageNumber(context, pageNumber);

        movies = moviesList.toArray(new ContentValues[0]);

        return movies;
    }

    //A helper method which obtains all of the additional data for a movie (runtime, trailers, and
    //reviews). It essentially combines buildURLForDetails(), getHTTPResponse(), and parseDetails().
    public static ContentValues fetchDetails(int movieID){
        URL detailsURL = buildURLForDetails(movieID);
        ContentValues detailsCV;

        try {
            String httpResponse = getHTTPResponse(detailsURL);
            detailsCV = parseDetails(httpResponse);
        }

        catch (IOException ioe){
            detailsCV = null;
            ioe.printStackTrace();
        }

        catch (JSONException je){
            detailsCV = null;
            je.printStackTrace();
        }

        return detailsCV;
    }

    //A helper method which updates the page number depending on the current method flag
    private static void updatePageNumber(Context context, int newPageNumber){

        String methodFlagKey = context.getString(R.string.list_preference_sorting_options_key);
        int methodFlag = SettingsFragment.getPreferenceValue(context, methodFlagKey);

        switch (methodFlag){
            case 0:
                setPopularPageNumber(newPageNumber);
                break;

            case 1:
                setTopRatedPageNumber(newPageNumber);
                break;

            case 2:
                throw new UnsupportedOperationException("Cannot update page number for favorites");

            default:
                throw new UnsupportedOperationException("Unknown value for the page number");
        }
    }

}
