package com.example.lyos.Models;

import android.content.Context;
import android.content.SharedPreferences;

public class AccountUtils {
    private static final String ACCOUNT_PREFS = "account_prefs";
    private static final String KEY_ACCOUNT_NAME = "account_name";

    public static void saveAccount(Context context, String accountName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(ACCOUNT_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_ACCOUNT_NAME, accountName);
        editor.apply();
    }

    public static String getSavedAccount(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(ACCOUNT_PREFS, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_ACCOUNT_NAME, null);
    }

    public static boolean isAccountExists(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(ACCOUNT_PREFS, Context.MODE_PRIVATE);
        return sharedPreferences.contains(KEY_ACCOUNT_NAME);
    }
    public static void removeAccount(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(ACCOUNT_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_ACCOUNT_NAME);
        editor.apply();
    }
}
