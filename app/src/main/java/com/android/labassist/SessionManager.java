package com.android.labassist;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
        private static final String PREF_NAME = "LabAssistSession";
        private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
        private static final String KEY_USERNAME = "username";
        private static final String KEY_ORG_CODE = "orgCode";
        private static final String KEY_ROLE = "role";

        public static void saveLogin(Context context, String username, String role, String orgCode) {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_IS_LOGGED_IN, true);
            editor.putString(KEY_USERNAME, username);
            editor.putString(KEY_ORG_CODE, orgCode);
            editor.putString(KEY_ROLE, role);
            editor.apply();
        }

        public static boolean isLoggedIn(Context context) {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        }

        public static void logout(Context context) {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();
        }

        public static String getUsername(Context context) {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            return prefs.getString(KEY_USERNAME, null);
        }

        public static String getRole(Context context) {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            return prefs.getString(KEY_ROLE, null);
        }

    public static String getOrgCode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_ORG_CODE, null);
    }
}
