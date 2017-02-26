package com.example.android.popularmovies_stage2;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.popularmovies_stage2.data.MovieContract;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

//MovieAdapter class, which is an extension of the RecyclerView.Adapter class. This class keeps
//track of all the views which will be displayed in the RecyclerView.
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieHolder> {

//    private ArrayList<Movie> adapterMovies;
    private Context adapterContext;
//    private int adapterMovieCount;
    private Cursor adapterCursor;

    //Default constructor of the adapter
    public MovieAdapter(@NonNull Context context){
        adapterContext = context;
    }

    //This method simply inflates the layout for each movie poster
    @Override
    public MovieHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.movie_list_item, viewGroup, false);

        return new MovieHolder(view);
    }

    //This method "binds" the data that is contained within adapterMovies to the different
    //views contained in the MovieHolder. In this case, since only an ImageView is contained
    //within the layout, the poster path is obtained for the corresponding movie and the image
    //is loaded via Picasso.
    @Override
    public void onBindViewHolder(MovieHolder holder, int position){
        adapterCursor.moveToPosition(position);

        String moviePoster = adapterCursor.getString(adapterCursor.getColumnIndex(MovieContract.MovieTable.COLUMN_POSTER_PATH));


        Picasso.with(adapterContext)
                .load("https://image.tmdb.org/t/p/w185/" + moviePoster)
                .into(holder.holderImageView);
    }

    //Simply returns the number of movies contained within adapterMovies
    @Override
    public int getItemCount(){
        if (adapterCursor == null){
            return 0;
        }

        return adapterCursor.getCount();
    }

    public void swapCursor(Cursor newData){
        adapterCursor = newData;
        notifyDataSetChanged();
    }


    //This class manages all of the views contained within a list item (individual components within
    //the RecyclerView). It implements View.OnClickListener so that the movie details screen is
    //shown when the user clicks on a movie poster.
    public class MovieHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView holderImageView;  //Reference to the poster ImageView

        //Constructor for the MovieHolder, which essentially sets the reference for holderImageView
        public MovieHolder(View movieView){
            super(movieView);
            holderImageView = (ImageView) movieView.findViewById(R.id.list_item_image_view);
            movieView.setOnClickListener(this);
        }

        //When the user clicks on a movie poster, the movie details screen should be shown.
        @Override
        public void onClick(View view){
            //First we obtain a reference to the movie that was clicked
//            Movie clickedMovie = adapterMovies.get(adapterRecyclerView.getChildAdapterPosition(view));
            adapterCursor.moveToPosition(this.getAdapterPosition());

            //TODO: It may still be necessary to obtain each of these parameters this way. It has
            //been commented out for the time being but I am skeptical of the way that it is currently
            //being implemented

//            String movieTitle = adapterCursor.getString(adapterCursor.getColumnIndex(MovieContract.MovieTable.COLUMN_TITLE));
//            int movieID = adapterCursor.getInt(adapterCursor.getColumnIndex(MovieContract.MovieTable.COLUMN_MOVIE_ID));
//            String movieOverview = adapterCursor.getString(adapterCursor.getColumnIndex(MovieContract.MovieTable.COLUMN_OVERVIEW));
//            String movieReleaseDate = adapterCursor.getString(adapterCursor.getColumnIndex(MovieContract.MovieTable.COLUMN_RELEASE_DATE));
//            int movieVoteCount = adapterCursor.getInt(adapterCursor.getColumnIndex(MovieContract.MovieTable.COLUMN_VOTE_COUNT));
//            float movieVoteAverage = adapterCursor.getFloat(adapterCursor.getColumnIndex(MovieContract.MovieTable.COLUMN_VOTE_AVERAGE));
//            String moviePoster = adapterCursor.getString(adapterCursor.getColumnIndex(MovieContract.MovieTable.COLUMN_POSTER_PATH));
//            String movieBackdrop = adapterCursor.getString(adapterCursor.getColumnIndex(MovieContract.MovieTable.COLUMN_BACKDROP_PATH));

//            Movie clickedMovie = new Movie(movieTitle, movieID,moviePoster, movieOverview,
//                    movieReleaseDate, movieVoteCount, movieVoteAverage, movieBackdrop);

            //Then an intent is created with a reference to the MovieDetails class
            Intent movieDetailsIntent = new Intent(adapterContext, MovieDetails.class);
            int movieID = adapterCursor.getInt(MovieSelection.INDEX_MOVIE_ID);
            Uri intentUri = MovieContract.MovieTable.CONTENT_URI.buildUpon().appendPath(Integer.toString(movieID)).build();

            //Extras are then placed into the intent, which include the entire movie ArrayList and
            //the clicked movie's ID
//            movieDetailsIntent.putParcelableArrayListExtra(MovieSelection.EXTRA_PARCEL, adapterMovies);

            //TODO: I still don't believe that this is going to work. It's very likely that I'll
            //need to implement the ContentResolver and/or a Cursor within the MovieDetails class
            //as well
            //TODO: This was removed, but it is kept here just in case. The previous TODO pertains
            //to the line below.
//            movieDetailsIntent.putExtra(MovieSelection.EXTRA_PARCEL, clickedMovie);

            //The ID is also used as an extra because it becomes useful in the movie details page
            //for the ViewPager
//            movieDetailsIntent.putExtra(MovieSelection.EXTRA_ID, movieID);
            movieDetailsIntent.setData(intentUri);
            adapterContext.startActivity(movieDetailsIntent);
        }
    }
}
