package com.example.dewaagung.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.dewaagung.popularmovies.domains.Movie;
import com.example.dewaagung.popularmovies.utils.utils;

import static com.example.dewaagung.popularmovies.MainActivity.KEY_ARG_MOVIE;

/**
 * Created by Dewa Agung on 30/08/17.
 */

public class DetailMovieActivity extends AppCompatActivity {

    FragmentDetailMovie fragmentDetailMovie;
    private static final String LOG_TAG = DetailMovieActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_movie);
        Intent intent = getIntent();
        Movie movie;
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            movie = (Movie) intent.getSerializableExtra(Intent.EXTRA_TEXT);

            Bundle arguments = new Bundle();
            arguments.putSerializable(KEY_ARG_MOVIE, movie);

            fragmentDetailMovie = new FragmentDetailMovie();
            fragmentDetailMovie.setArguments(arguments);
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.detail_movie_container, fragmentDetailMovie)
                        .commit();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (utils.checkConnection(this) && fragmentDetailMovie != null) {
            fragmentDetailMovie.updateTrailers();
            fragmentDetailMovie.updateReviews();
        }
    }
}
