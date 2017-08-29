package com.example.dewaagung.popularmovies.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import static android.R.attr.value;

/**
 * Created by Dewa Agung on 29/08/17.
 */

public class utils {

    public static String getPreferredOrder(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("order_movies", "most_popular");
    }

    public static void setPreferredOrder(Context context, String value){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        prefs.edit().putString("order_movies", value).commit();
    }

    public static boolean checkConnection(Context context){
        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo i = conMgr.getActiveNetworkInfo();
        if(i == null)
            return false;
        if(!i.isConnected())
            return false;
        return i.isAvailable();
    }
}
