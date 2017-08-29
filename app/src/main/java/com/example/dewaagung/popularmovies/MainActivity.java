package com.example.dewaagung.popularmovies;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.dewaagung.popularmovies.utils.utils;

public class MainActivity extends AppCompatActivity {

    public static final String KEY_ARG_MOVIE = "MARG";
    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private String mCriteriaOrder;

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCriteriaOrder = utils.getPreferredOrder(this);
    }
}
