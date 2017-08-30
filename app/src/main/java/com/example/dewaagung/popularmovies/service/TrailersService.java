package com.example.dewaagung.popularmovies.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;

import com.example.dewaagung.popularmovies.BuildConfig;
import com.example.dewaagung.popularmovies.domains.Trailer;

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
import java.util.ArrayList;

import static com.example.dewaagung.popularmovies.utils.TheMoviesDBAPI.PARAM_KEY_API;
import static com.example.dewaagung.popularmovies.utils.TheMoviesDBAPI.URL_DEFAULT;

/**
 * Created by Dewa Agung on 30/08/17.
 */

public class TrailersService extends IntentService {

    public static final String MOVIE_ID_QUERY_EXTRA = "movie_id";

    public static final String KEY_RESULT_TRAILERS = "result";
    public static final String KEY_RECEIVER = "receiver";

    ArrayList<Trailer> mTrailers = new ArrayList<>();

    public TrailersService() {
        super(TrailersService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String movie_id = intent.getStringExtra(MOVIE_ID_QUERY_EXTRA);

        mTrailers = getTrailers(movie_id);

        Bundle resultBundle = new Bundle();
        resultBundle.putSerializable(KEY_RESULT_TRAILERS, mTrailers);
        ResultReceiver resRec = intent.getParcelableExtra(KEY_RECEIVER);

        resRec.send(101, resultBundle);
    }

    ArrayList<Trailer> getTrailers(String movie_id) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String resultJsonStr;
        try {
            String TRAILERS_MOVIE = "/movie/{movie_id}/videos";
            String strURL = URL_DEFAULT + TRAILERS_MOVIE.replace("{movie_id}", movie_id + "") + "?" + PARAM_KEY_API + "=" + BuildConfig.THE_MOVIEDB_API_KEY;
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
            return parseJsonTrailers(resultJsonStr);
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

    private ArrayList<Trailer> parseJsonTrailers(String json) throws JSONException, ParseException {
        final String TRAILERS_RESULTS = "results";
        final String TRAILER_NAME = "name";
        final String TRAILER_SITE = "site";
        final String TRAILER_KEY = "key";

        ArrayList<Trailer> trailers = new ArrayList<>();

        JSONObject jsonData = new JSONObject(json);
        JSONArray results = jsonData.getJSONArray(TRAILERS_RESULTS);

        for (int i = 0; i < results.length(); i++) {
            Trailer trailer = new Trailer();
            JSONObject jsonTrailer = results.getJSONObject(i);

            trailer.setName(jsonTrailer.getString(TRAILER_NAME));
            trailer.setSite(jsonTrailer.getString(TRAILER_SITE));
            trailer.setKey(jsonTrailer.getString(TRAILER_KEY));

            trailers.add(trailer);
        }
        return trailers;
    }
}
