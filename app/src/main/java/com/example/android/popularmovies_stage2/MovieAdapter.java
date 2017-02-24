package com.example.android.popularmovies_stage2;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

//MovieAdapter class, which is an extension of the RecyclerView.Adapter class. This class keeps
//track of all the views which will be displayed in the RecyclerView.
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieHolder> {

    private ArrayList<Movie> adapterMovies;
    private Context adapterContext;
    private int adapterMovieCount;

    //Default constructor of the adapter
    public MovieAdapter(Context context, ArrayList<Movie> movies, int count){
        adapterContext = context;
        adapterMovies = movies;
        adapterMovieCount = count;
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
        Movie currentMovie = adapterMovies.get(position);
        Picasso.with(adapterContext)
                .load("https://image.tmdb.org/t/p/w185/" + currentMovie.getPosterPath())
                .into(holder.holderImageView);
    }

    //Simply returns the number of movies contained within adapterMovies
    @Override
    public int getItemCount(){
        //TODO: Make sure to adjust this so that it returns adapaterMovieCount later on
        return adapterMovies.size();
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
            Movie clickedMovie = adapterMovies.get(this.getAdapterPosition());

            //Then an intent is created with a reference to the MovieDetails class
            Intent movieDetailsIntent = new Intent(adapterContext, MovieDetails.class);

            //Extras are then placed into the intent, which include the entire movie ArrayList and
            //the clicked movie's ID
            movieDetailsIntent.putParcelableArrayListExtra(MovieSelection.EXTRA_PARCEL, adapterMovies);

            //The ID is also used as an extra because it becomes useful in the movie details page
            //for the ViewPager
            movieDetailsIntent.putExtra(MovieSelection.EXTRA_ID, clickedMovie.getID());
            adapterContext.startActivity(movieDetailsIntent);
        }
    }
}
