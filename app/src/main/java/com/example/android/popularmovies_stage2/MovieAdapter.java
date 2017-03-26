/*
The following code is the property and sole work of Mike Palarz, a student at Udacity
 */

package com.example.android.popularmovies_stage2;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.popularmovies_stage2.activities.MovieDetails;
import com.example.android.popularmovies_stage2.activities.MovieSelection;
import com.example.android.popularmovies_stage2.data.MovieContract;
import com.squareup.picasso.Picasso;

/*
This class is responsible for staying in sync with the data that is stored within the SQLite DB and
displaying within the RecyclerView.
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieHolder> {

    private static final String TAG = "MovieAdapter";   //Tag used for debugging

    //Context which is used to obtain references to the ContentResolver among other things
    private final Context adapterContext;

    //Cursor which is used to obtain the data within the SQLite DB
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

    //This method "binds" the data that is contained within SQLIte DB to the different
    //views contained in the MovieHolder. In this case, since only an ImageView is contained
    //within the layout, the poster path is obtained for the corresponding movie and the image
    //is loaded via Picasso.
    @Override
    public void onBindViewHolder(MovieHolder holder, int position){
        adapterCursor.moveToPosition(position);

        String moviePoster = adapterCursor.getString(MovieSelection.INDEX_POSTER_PATH);

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

    //Used to "swap" in the data once the Loader within MovieSelection has finished obtaining the data
    //This is called within onLoadFinished()
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
            adapterCursor.moveToPosition(this.getAdapterPosition());

            //Then an intent is created with a reference to the MovieDetails class
            Intent movieDetailsIntent = new Intent(adapterContext, MovieDetails.class);

            //We obtain the movie ID from adapterCursor so that the appropriate data may be obtained
            //once the MovieDetails activity is launched
            final int movieID = adapterCursor.getInt(MovieSelection.INDEX_MOVIE_ID);

            //A Uri is then created using movieID
            final Uri movieUri = MovieContract.MovieTable.CONTENT_URI.buildUpon().appendPath(Integer.toString(movieID)).build();

            //An ASyncTask is launched so that additional data tied directly to the particular movie,
            //which includes the runtime, trailer links, and review links, are obtained
            new AsyncTask<Void, Void, ContentValues>(){

                @Override
                protected ContentValues doInBackground(Void... params) {
                    ContentValues movieDetails = MovieFetcher.fetchDetails(movieID);

                    return movieDetails;
                }

                @Override
                protected void onPostExecute(ContentValues contentValues) {
                    adapterContext.getContentResolver().update(movieUri, contentValues, null, null);
                }
            }.execute();

            //The Uri is then set to the Intent and MovieDetails is launched
            movieDetailsIntent.setData(movieUri);
            adapterContext.startActivity(movieDetailsIntent);
        }
    }
}
