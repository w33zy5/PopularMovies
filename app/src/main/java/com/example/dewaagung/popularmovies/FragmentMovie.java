package com.example.dewaagung.popularmovies;

import android.content.Intent;
import android.graphics.Movie;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.os.ResultReceiver;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import com.example.dewaagung.popularmovies.utils.utils;

import java.util.ArrayList;

import static android.view.View.GONE;

/**
 * Created by Dewa Agung on 30/08/17.
 */

public class FragmentMovie extends android.support.v4.app.Fragment {
    private final String LOG_TAG = FragmentMovie.class.getSimpleName();
    private MovieAdapter mMovieAdapter;
    private int mPosition;
    public static final String SELECTED_KEY = "selected_position";

    public View onCreateView(LayoutInflater inflate, ViewGroup container, Bundle savedInstaceState){
        Log.v(LOG_TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_movie, container, false);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

        GridLayoutManager gridLayoutManager;
        gridLayoutManager = new GridLayoutManager(getContext(), 2);

        recyclerView.setLayoutManager(gridLayoutManager);

        mMovieAdapter = new MovieAdapter(getActivity(), new ArrayList<Movie>());

        recyclerView.setAdapter(mMovieAdapter);

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
            recyclerView.scrollToPosition(mPosition);
        }

        return rootView;
    }

    public void onViewCreated(View view, Bundle savedInstanceState){
        Log.v(LOG_TAG, "onViewCreated");
        updateMoviesTask();

        super.onViewCreated(view, savedInstanceState);
    }

    public void setPosition(int mPosition){
        this.mPosition = mPosition;
    }

    public MovieAdapter getmMovieAdapter(){
        return mMovieAdapter;
    }

    public void onSaveInstanceState(Bundle outState){
        if(mPosition != ListView.INVALID_POSITION){
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    public void updateMoviesTask(){
        MyReceiverResult receiverResult = new MyReceiverResult(new Handler());

        Intent intent = new Intent(getActivity(), MoviesService.class);
        intent.putExtra(MoviesService.PREFERENCES_ORDER_QUERY_EXTRA,
                utils.getPreferredOrder(getActivity()));
        intent.putExtra(KEY_RECEIVER, receiverResult);

        getActivity().startService(intent);
    }

    class MyReceiverResult extends ResultReceiver{
        public MyReceiverResult(Handler handler){
            super(handler);
        }

        protected void onReceiverResult(int resultCode, Bundle resultData){
            Log.v(LOG_TAG, "onReceiveResult");
            mMovieAdapter.clear();
            ArrayList<Movie> movies = (ArrayList<Movie>) resultData.getSerializable(KEY_RESULT_QUERY_EXTRA);
            for(Movie m : movies)
                mMovieAdapter.add(m);

            if(((MainActivity) getActivity()).isTwoPane())
                ((MainActivity) getActivity()).onMovieChanged(mMovieAdapter.getFirstMovie());

            setEmptyMessage(movies);
        }

        private void setEmptyMessage(ArrayList<Movie> movies){
            if(utils.getPreferredOrder(getContext()).equals("favorites")&&
                    !movies.isEmpty()){
                getView().findViewById(R.id.empty_view).setVisibility(GONE);
            } else if(utils.getPreferredOrder(getContext()).equals("favorites")){
                TextView textView = (TextView) getView().findViewById(R.id.empty_view);
                textView.setText("No favorite movies.");
                textView.setVisibility(View.VISIBLE);
            }else if(utils.checkConnection(getContext()) && getView().findViewById(R.id.empty_view) != null){
                getView().findViewById(R.id.empty_view).setVisibility(GONE);
            }else{
                getView.findViewById(R.id.empty_view).setVisibility(View.INVISIBLE);
            }
        }
    }
}
