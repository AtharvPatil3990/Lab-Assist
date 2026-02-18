package com.android.labassist;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
//    orgCode, role, instituteName, department, registrationID, email, userName;
        public static final String PREF_NAME = "LabAssistSession";
        private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
        private static final String KEY_USERNAME = "username";
        private static final String KEY_ORG_CODE = "orgCode";
        private static final String KEY_ROLE = "role";
        private static final String KEY_USER_NAME = "userName";
        private static final String KEY_INSTITUTION_NAME = "instituteName";
        private static final String KEY_DEPARTMENT = "department";
        private static final String KEY_REGISTRATION_ID = "registration_id";
        private static final String KEY_EMAIL = "email";

        private SessionManager(){}

        public static void saveLogin(Context context, String username, String role, String orgCode) {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_IS_LOGGED_IN, true);
            editor.putString(KEY_USERNAME, username);
            editor.putString(KEY_ORG_CODE, orgCode);
            editor.putString(KEY_ROLE, role);
            editor.apply();
        }
        public static void saveLogin(Context context, String username, String role, String orgCode, String userName, String instituteName, String department, String email, String regID) {
                    SharedPreferences.Editor editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
                    editor.putBoolean(KEY_IS_LOGGED_IN, true);
                    editor.putString(KEY_USERNAME, username);
                    editor.putString(KEY_ORG_CODE, orgCode);
                    editor.putString(KEY_ROLE, role);
                    editor.putString(KEY_USER_NAME, userName);
                    editor.putString(KEY_INSTITUTION_NAME, instituteName);
                    editor.putString(KEY_DEPARTMENT, department);
                    editor.putString(KEY_REGISTRATION_ID, regID);
                    editor.putString(KEY_EMAIL, email);

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
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_USERNAME, null);
        }
        public static String getRole(Context context) {
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_ROLE, null);
        }

        public static String getOrgCode(Context context) {
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_ORG_CODE, null);
        }

        public static String getDepartment(Context context) {
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_DEPARTMENT, null);
        }

        public static String getEmail(Context context) {
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_EMAIL, null);
        }

        public static String getRegID(Context context) {
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_REGISTRATION_ID, null);
        }
}