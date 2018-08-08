package com.channey.timerbutton;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtils {
    private static final String FILE_NAME = "timer_button.sp";

    /**
     * Set a long value in the preferences editor
     * @param context
     * @param key The name of the preference to modify.
     * @param value The new value for the preference.
     */
    public static void saveLong(Context context, String key,long value) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    /**
     * Retrieve a long value from the preferences.
     * @param context
     * @param key The name of the preference to modify.
     */
    public static long getLong(Context context,String key) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,Activity.MODE_PRIVATE);
        return sp.getLong(key, 0);
    }

    /**
     * Mark in the editor that a preference value should be removed
     * @param context
     * @param key The name of the preference to modify.
     */
    public static void remove(Context context,String key) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        editor.commit();
    }
}
