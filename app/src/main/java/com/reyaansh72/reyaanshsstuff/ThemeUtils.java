package com.reyaansh72.reyaanshsstuff;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.color.DynamicColors;

public class ThemeUtils {
    public static final String PREF_NAME = "app_settings";
    public static final String KEY_THEME = "theme_choice"; // "dynamic", "emerald", "blue", "purple", "amber"
    public static final String KEY_MODE = "theme_mode"; // "system", "light", "dark"
    public static final String KEY_AUTOPLAY = "autoplay_shorts";
    public static final String KEY_MUTE_DEFAULT = "mute_default";
    public static final String KEY_OLLAMA_HOST = "ollama_host_url";

    public static void applyTheme(AppCompatActivity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // 1. Apply Night Mode (Light / Dark / System)
        String modeChoice = prefs.getString(KEY_MODE, "system");
        switch (modeChoice) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "system":
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }

        // 2. Apply Custom Color Theme or Dynamic
        String themeChoice = prefs.getString(KEY_THEME, "dynamic");
        switch (themeChoice) {
            case "blue":
                activity.setTheme(R.style.Theme_ReyaanshsStuff_OceanBlue);
                break;
            case "purple":
                activity.setTheme(R.style.Theme_ReyaanshsStuff_RoyalPurple);
                break;
            case "amber":
                activity.setTheme(R.style.Theme_ReyaanshsStuff_SunsetAmber);
                break;
            case "emerald":
                activity.setTheme(R.style.Theme_ReyaanshsStuff_EmeraldGreen);
                break;
            case "dynamic":
            default:
                DynamicColors.applyToActivityIfAvailable(activity);
                break;
        }
    }

    public static String getSelectedMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_MODE, "system");
    }

    public static void saveMode(Context context, String modeChoice) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_MODE, modeChoice).apply();
    }

    public static String getSelectedTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_THEME, "dynamic");
    }

    public static void saveTheme(Context context, String themeChoice) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_THEME, themeChoice).apply();
    }

    public static boolean isAutoplayEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_AUTOPLAY, true);
    }

    public static void setAutoplayEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_AUTOPLAY, enabled).apply();
    }

    public static boolean isMuteDefault(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_MUTE_DEFAULT, false);
    }

    public static void setMuteDefault(Context context, boolean mute) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_MUTE_DEFAULT, mute).apply();
    }

    public static String getOllamaHostUrl(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_OLLAMA_HOST, "http://10.0.2.2:11434");
    }

    public static void setOllamaHostUrl(Context context, String url) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_OLLAMA_HOST, url).apply();
    }
}