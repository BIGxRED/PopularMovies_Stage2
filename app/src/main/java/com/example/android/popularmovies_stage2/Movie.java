/*
The following code is the property and sole work of Mike Palarz, a student at Udacity
 */

package com.example.android.popularmovies_stage2;

import android.os.Parcel;
import android.os.Parcelable;

/*
This class was created in order to store all of the data obtained through TheMovieDB API. This class
 implements the parcelable interface so that it can be easily passed on to the MovieDetails class
 through an intent within MovieSelection.
 */

public class Movie implements Parcelable {

    String mTitle;  //String used to store the movie title
    int mID;    //integer used to store the unique movie ID
    String mPosterPath; //String used to store the relative path of the movie poster image
    String mOverview;   //String used to store the plot synposis
    String mReleaseDate;    //String used to store the movie's release date
    int mVoteCount; //integer used to store the number of votes a movie received
    float mVoteAverage; //float used to store the average vote value
    String mBackdropPath;   //String used to store the relative path of the movie backdrop image
    int mRuntime;
    boolean mFavorite;

    public static final String SORTED_BY_POPULARITY = "popularity";
    public static final String SORTED_BY_TOP_RATED = "top rated";

    //Default constructor of a Movie object
    public Movie(String title, int ID, String posterPath, String overview, String releaseDate,
                 int voteCount, float voteAverage, String backdropPath, int runtime, boolean favorite){
        mTitle = title;
        mID = ID;
        mPosterPath = posterPath;
        mOverview = overview;
        mReleaseDate = releaseDate;
        mVoteCount = voteCount;
        mVoteAverage = voteAverage;
        mBackdropPath = backdropPath;
        mRuntime = runtime;
        mFavorite = favorite;
    }

    //Constructor required to implement the Parcelable interface
    public Movie(Parcel in){
        mTitle = in.readString();
        mID = in.readInt();
        mPosterPath = in.readString();
        mOverview = in.readString();
        mReleaseDate = in.readString();
        mVoteCount = in.readInt();
        mVoteAverage = in.readFloat();
        mBackdropPath = in.readString();
        mRuntime = in.readInt();
        mFavorite = in.readByte() != 0;
    }

    /*Getter and Setter Methods*/

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public int getID() {
        return mID;
    }

    public void setID(int mID) {
        this.mID = mID;
    }

    public String getPosterPath() {
        return mPosterPath;
    }

    public void setPosterPath(String mPosterPath) {
        this.mPosterPath = mPosterPath;
    }

    public String getOverview() {
        return mOverview;
    }

    public void setOverview(String mOverview) {
        this.mOverview = mOverview;
    }

    public String getReleaseDate(){
        return mReleaseDate;
    }

    public void setReleaseDate(String mReleaseDate){
        this.mReleaseDate = mReleaseDate;
    }

    public int getVoteCount(){
        return mVoteCount;
    }

    public void setVoteCount(int mVoteCount){
        this.mVoteCount = mVoteCount;
    }

    public float getVoteAverage(){
        return mVoteAverage;
    }

    public void setVoteAverage(float mVoteAverage){
        this.mVoteAverage = mVoteAverage;
    }

    public String getBackdropPath(){
        return mBackdropPath;
    }

    public void setBackdropPath(String mBackdropPath){
        this.mBackdropPath = mBackdropPath;
    }

    public int getRuntime(){
        return mRuntime;
    }

    public void setRuntime(int runtime){
        mRuntime = runtime;
    }

    public boolean getFavorite(){
        return mFavorite;
    }

    public void setFavorite(boolean favorite){
        mFavorite = favorite;
    }

    @Override
    public String toString(){
        return "Title: " + mTitle
               + "\nID: " + Integer.toString(mID)
               + "\nPoster path: " + mPosterPath
               + "\nOverview: " + mOverview
               + "\nRelease date: " + mReleaseDate
               + "\nVote count: " + Integer.toString(mVoteCount)
               + "\nVote average: " + Float.toString(mVoteAverage)
               + "\nBackdrop path: " + mBackdropPath
               + "\nRuntime: " + mRuntime
               + "\nFavorite movie?: " + mFavorite
               + "\n";
    }

    /* Methods required for the parcelable interface*/

    //A method which specifies if special Objects are being "flattened" into a Parcel, such as an
    //Object which contained a file descriptor. In the case of a Movie, this is not needed so the
    //method simply returns 0.
    @Override
    public int describeContents(){
        return 0;
    }

    //This method "flattens" a Movie object into a Parcel
    @Override
    public void writeToParcel(Parcel out,int flags){
        out.writeString(mTitle);
        out.writeInt(mID);
        out.writeString(mPosterPath);
        out.writeString(mOverview);
        out.writeString(mReleaseDate);
        out.writeInt(mVoteCount);
        out.writeFloat(mVoteAverage);
        out.writeString(mBackdropPath);
        out.writeInt(mRuntime);
        out.writeByte((byte) (mFavorite ? 1 : 0));
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>(){

        //This method "unpacks" the Movie; a new Movie instance is created which is instantiated by
        //the parcel that had originally been created within writeToParcel()
        public Movie createFromParcel(Parcel in){
            return new Movie(in);
        }

        //This method creates a new array of the parcelable Movie class
        public Movie[] newArray(int size){
            return new Movie[size];
        }
    };

}
