package com.example.dewaagung.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.example.dewaagung.popularmovies.utils.TheMoviesDBAPI.URL_IMAGE;
import com.example.dewaagung.popularmovies.domains.Movie;

/**
 * Created by Dewa Agung on 30/08/17.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MyViewHolder> {

    private ArrayList<Movie> mMovies;
    private MainActivity activity;

    public MovieAdapter(Activity activity, ArrayList<Movie> movies) {
        this.mMovies = movies;
        this.activity = (MainActivity) activity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.movie, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final Movie movie = mMovies.get(position);

        Picasso.with(activity).load(URL_IMAGE + movie.getPath_poster()).into(holder.image_banner);

        final int positionAdapter = holder.getAdapterPosition();
        holder.image_banner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentMovie fm = (FragmentMovie) activity.getSupportFragmentManager().findFragmentById(R.id.fragment_movie);
                fm.setPosition(positionAdapter);

                if (!activity.isTwoPane()) {
                    Intent intent = new Intent(activity, DetailMovieActivity.class);
                    intent.putExtra(Intent.EXTRA_TEXT, movie);
                    activity.startActivity(intent);
                } else {
                    activity.onMovieChanged(movie);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMovies.size();
    }

    public Movie getFirstMovie() {
        try {
            return mMovies.get(0);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public void clear() {
        mMovies.clear();
        notifyDataSetChanged();
    }

    public void add(Movie m) {
        if (m != null) {
            mMovies.add(m);
            notifyItemInserted(mMovies.size() - 1);
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView image_banner;

        public MyViewHolder(View itemView) {
            super(itemView);
            image_banner = (ImageView) itemView.findViewById(R.id.poster_movie);
        }
    }
}
