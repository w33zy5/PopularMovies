package com.example.dewaagung.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Movie;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.os.ResultReceiver;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dewaagung.popularmovies.data.PopularMovieContract;
import com.example.dewaagung.popularmovies.domains.Review;
import com.example.dewaagung.popularmovies.domains.Trailer;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Locale;

import static com.example.dewaagung.popularmovies.MainActivity.KEY_ARG_MOVIE;
import static com.example.dewaagung.popularmovies.utils.MovieDBAPI.URL_IMAGE;
import static java.security.AccessController.getContext;

/**
 * Created by Dewa Agung on 30/08/17.
 */

public class FragmentDetailMovie extends Fragment {

    private static final int FAVORITE_ON_ID = android.R.drawable.btn_star_big_on;
    private static final int FAVORITE_OFF_ID = android.R.drawable.btn_star_big_off;

    private static final String LOG_TAG = FragmentDetailMovie.class.getSimpleName();

    private Movie mMovie;

    TrailerAdapter mTrailerAdapter;
    ReviewAdapter mReviewAdapter;

    ShareActionProvider mShareActionProvider;

    public FragmentDetailMovie() {
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mMovie = (Movie) arguments.getSerializable(KEY_ARG_MOVIE);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail_movie, container, false);
        ViewHolder viewHolder = new ViewHolder(rootView);
        rootView.setTag(viewHolder);


        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (mMovie == null)
            return;

        final ViewHolder holder = (ViewHolder) view.getTag();

        Cursor cursor = getContext().getContentResolver().query(
                PopularMovieContract.MovieEntry.CONTENT_URI, null, PopularMovieContract.MovieEntry._ID + " = ?", new String[]{String.valueOf(mMovie.getMovieId())}, null);
        if (cursor != null && cursor.moveToNext()) {
            mMovie.setFavorite(true);
            cursor.close();
        }

        Picasso.with(getContext()).load(URL_IMAGE + mMovie.getBackdrop_path()).into(holder.iv_backdrop);

        holder.tv_title_movie.setText(mMovie.getTitle());

        holder.tv_overview.setText(mMovie.getOverview());
        holder.tv_release_date.setText(
                DateFormat.getDateInstance(DateFormat.YEAR_FIELD, Locale.getDefault()).format(mMovie.getReleaseDate())
        );

        holder.tv_rating.setText(getString(R.string.rating_movie, mMovie.getRating(), mMovie.getVote_count()));

        holder.iv_star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMovie.isFavorite()) {
                    holder.iv_star.setImageResource(FAVORITE_OFF_ID);
                    mMovie.setFavorite(false);
                    // remove
                    getContext().getContentResolver().delete(PopularMovieContract.MovieEntry.CONTENT_URI, PopularMovieContract.MovieEntry._ID + " = " + mMovie.getMovieId(), null);

                    if (getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_movie) != null) { // update fragments
                        MainActivity activity = ((MainActivity) getActivity());
                        FragmentMovie fm = (FragmentMovie) getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_movie);
                        if (fm != null) {
                            fm.updateMoviesTask();
                            activity.onMovieChanged(fm.getMovieAdapter().getFirstMovie());
                        }
                    }
                } else {
                    holder.iv_star.setImageResource(FAVORITE_ON_ID);
                    mMovie.setFavorite(true);
                    // insert
                    getContext().getContentResolver().insert(PopularMovieContract.MovieEntry.CONTENT_URI, mMovie.getContentValues());
                }
            }
        });
        if (mMovie.isFavorite()) {
            holder.iv_star.setImageResource(FAVORITE_ON_ID);
        } else holder.iv_star.setImageResource(FAVORITE_OFF_ID);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        holder.rv_trailers.setLayoutManager(layoutManager);
        holder.rv_reviews.setLayoutManager(layoutManager2);

        mTrailerAdapter = new TrailerAdapter(getContext(), new ArrayList<Trailer>());
        mReviewAdapter = new ReviewAdapter(getContext(), new ArrayList<Review>());

        holder.rv_trailers.setAdapter(mTrailerAdapter);
        holder.rv_reviews.setAdapter(mReviewAdapter);
//
        if (savedInstanceState != null) {
            updateTrailers();
            updateReviews();
        }

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareTrailerIntent());
        }

        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
        updateTrailers();
        updateReviews();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.detailfragment, menu);

        MenuItem menuItem = menu.findItem(R.id.menu_item_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (createShareTrailerIntent() != null)
            mShareActionProvider.setShareIntent(createShareTrailerIntent());

    }

    private Intent createShareTrailerIntent() {
        if (mTrailerAdapter != null) {
            Trailer trailer = mTrailerAdapter.getFirstTrailer();
            if (trailer != null) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, trailer.getName() + ": " + TrailerAdapter.PATH_YOUTUBE + trailer.getKey());
                return shareIntent;
            }
        }
        return null;
    }


    public void updateReviews() {

        try {


            MyReviewReceiver reviewReceiver = new MyReviewReceiver(new Handler());

            Intent intent = new Intent(getActivity(), ReviewsService.class);
            intent.putExtra(ReviewsService.MOVIE_ID_QUERY_EXTRA, String.valueOf(mMovie.getMovieId()));
            intent.putExtra(KEY_RECEIVER, reviewReceiver);

            getActivity().startService(intent);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void updateTrailers() {
        try {
            MyTrailerReceiver trailerReceiver = new MyTrailerReceiver(new Handler());

            Intent intent = new Intent(getActivity(), TrailersService.class);
            intent.putExtra(TrailersService.MOVIE_ID_QUERY_EXTRA, String.valueOf(mMovie.getMovieId()));
            intent.putExtra(KEY_RECEIVER, trailerReceiver);

            getActivity().startService(intent);

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    class MyTrailerReceiver extends ResultReceiver {

        public MyTrailerReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (mTrailerAdapter != null) {
                mTrailerAdapter.clear();

                ArrayList<Trailer> trailers = (ArrayList<Trailer>) resultData.getSerializable(KEY_RESULT_TRAILERS);

                if ((getView() != null && getView().findViewById(R.id.empty_trailers) != null) &&
                        (trailers != null && trailers.isEmpty())) {
                    getView().findViewById(R.id.empty_trailers).setVisibility(View.VISIBLE);
                }

                for (Trailer t : trailers) {
                    mTrailerAdapter.add(t);
                }

                if (mShareActionProvider != null) {
                    mShareActionProvider.setShareIntent(createShareTrailerIntent());
                }
            }
        }
    }

    class MyReviewReceiver extends ResultReceiver {

        public MyReviewReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (mReviewAdapter != null) {
                mReviewAdapter.clear();

                ArrayList<Review> reviews = (ArrayList<Review>) resultData.getSerializable(ReviewsService.KEY_RESULT_REVIEWS);

                if ((getView() != null && getView().findViewById(R.id.empty_review) != null) &&
                        (reviews != null && reviews.isEmpty())) {
                    getView().findViewById(R.id.empty_review).setVisibility(View.VISIBLE);
                }

                for (Review r : reviews) {
                    mReviewAdapter.add(r);
                }
            }
        }
    }

    class ViewHolder {
        ImageView iv_backdrop;
        ImageView iv_star;
        TextView tv_title_movie;
        TextView tv_overview;
        TextView tv_release_date;
        TextView tv_rating;
        RecyclerView rv_trailers;
        RecyclerView rv_reviews;

        public ViewHolder(View view) {
            this.iv_backdrop = ((ImageView) view.findViewById(R.id.iv_backdrop));
            this.iv_star = (ImageView) view.findViewById(R.id.iv_favorite);
            this.tv_title_movie = (TextView) view.findViewById(R.id.tv_title_movie);
            this.tv_overview = (TextView) view.findViewById(R.id.tv_overview);
            this.tv_release_date = (TextView) view.findViewById(R.id.tv_release_date);
            this.tv_rating = (TextView) view.findViewById(R.id.tv_rating);
            this.rv_trailers = (RecyclerView) view.findViewById(R.id.rv_trailers);
            this.rv_reviews = (RecyclerView) view.findViewById(R.id.rv_reviews);
        }
    }
}
