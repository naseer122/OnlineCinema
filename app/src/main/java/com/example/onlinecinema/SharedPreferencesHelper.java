package com.example.onlinecinema;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPreferencesHelper {

    private static final String REMAINING_TIME_KEY = "remaining_time";
    private static SharedPreferences sharedPreferences;
    private static final String FILM_NAME_KEY = "film_name";

    public static void saveRemainingTime(Context context, long remainingTimeMillis) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(REMAINING_TIME_KEY, remainingTimeMillis);
        editor.apply();
    }
    public static void saveFilmName(Context context, String filmName) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(FILM_NAME_KEY, filmName);
        editor.apply();
    }
    public static long getRemainingTime(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getLong(REMAINING_TIME_KEY, 0);
    }
    public static String getFilmName(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(FILM_NAME_KEY, "");
    }
}

