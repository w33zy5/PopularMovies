package com.example.dewaagung.popularmovies.service;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

import com.example.dewaagung.popularmovies.BuildConfig;
import com.example.dewaagung.popularmovies.R;
import com.example.dewaagung.popularmovies.data.PopularMovieContract;
import com.example.dewaagung.popularmovies.domains.Movie;
import com.example.dewaagung.popularmovies.utils.TheMoviesDBAPI;
import com.example.dewaagung.popularmovies.utils.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static com.example.dewaagung.popularmovies.utils.TheMoviesDBAPI.MOVIES_POPULAR;
import static com.example.dewaagung.popularmovies.utils.TheMoviesDBAPI.MOVIES_TOP_RATED;
import static com.example.dewaagung.popularmovies.utils.TheMoviesDBAPI.PARAM_KEY_API;
import static com.example.dewaagung.popularmovies.utils.TheMoviesDBAPI.URL_DEFAULT;


/**
 * Created by Dewa Agung on 30/08/17.
 */

public class MoviesService extends IntentService {
    private static final String LOG_TAG = MoviesService.class.getSimpleName();

    public static final String PREFERENCES_ORDER_QUERY_EXTRA = "pref_order";
    public static final String KEY_RESULT_QUERY_EXTRA = "result";
    public static final String KEY_RECEIVER = "receiver";

    private ArrayList<Movie> mMovies = new ArrayList<>();

    public MoviesService() {
        super(MoviesService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(LOG_TAG, "onHandleIntent");
        String preferencesOrder = intent.getStringExtra(PREFERENCES_ORDER_QUERY_EXTRA);
        if (!preferencesOrder.equals(getString(R.string.value_favorites_pref_order)) && utils.checkConnection(this) ) {

            mMovies = getMovies(preferencesOrder);

        } else {

            Cursor cursor = this.getContentResolver().query(PopularMovieContract.MovieEntry.CONTENT_URI, null, null, null, null);
            while (cursor != null && cursor.moveToNext()) {
                mMovies.add(new Movie(cursor));
            }
        }

        Bundle resultBundle = new Bundle();
        resultBundle.putSerializable(KEY_RESULT_QUERY_EXTRA, mMovies);
        ResultReceiver resRec = intent.getParcelableExtra(KEY_RECEIVER);

        resRec.send(101, resultBundle);
    }

    private ArrayList<Movie> getMovies(String path_key) {
        String path;
        if (path_key.equals(getString(R.string.value_default_pref_order)))
            path = MOVIES_POPULAR;
        else path = MOVIES_TOP_RATED;

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String resultJsonStr;
        try {
            String strURL = URL_DEFAULT + path + "?" + PARAM_KEY_API + "=" + BuildConfig.THE_MOVIEDB_API_KEY;
            URL url = new URL(strURL);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            resultJsonStr = buffer.toString();
            return parseJsonMovies(resultJsonStr);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private ArrayList<Movie> parseJsonMovies(String json) throws JSONException, ParseException {
        final String MOVIE_RESULTS = "results";

        final String MOVIE_ID = "id";
        final String MOVIE_TITLE = "original_title";
        final String MOVIE_POSTER_PATH = "poster_path";
        final String MOVIE_BACKDROP_PATH = "backdrop_path";
        final String MOVIE_OVERVIEW = "overview";
        final String MOVIE_RATING = "vote_average";
        final String MOVIE_VOTE_COUNT = "vote_count";
        final String MOVIE_RELEASEDATE = "release_date";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        ArrayList<Movie> movies = new ArrayList<>();

        JSONObject jsonData = new JSONObject(json);
        JSONArray results = jsonData.getJSONArray(MOVIE_RESULTS);

        for (int i = 0; i < results.length(); i++) {
            Movie movie = new Movie();
            JSONObject jsonMovie = results.getJSONObject(i);

            movie.setMovieId(jsonMovie.getLong(MOVIE_ID));
            movie.setTitle(jsonMovie.getString(MOVIE_TITLE));
            movie.setOverview(jsonMovie.getString(MOVIE_OVERVIEW));
            movie.setRating((float) jsonMovie.getDouble(MOVIE_RATING));
            movie.setVote_count(jsonMovie.getInt(MOVIE_VOTE_COUNT));
            movie.setPath_poster(jsonMovie.getString(MOVIE_POSTER_PATH));
            movie.setBackdrop_path(jsonMovie.getString(MOVIE_BACKDROP_PATH));
            movie.setReleaseDate(sdf.parse(jsonMovie.getString(MOVIE_RELEASEDATE)));

            movies.add(movie);
        }
        return movies;
    }
}
