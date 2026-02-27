package com.android.labassist.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    public static final String PREF_NAME = "LabAssistSession";

    public static final String ACTION_LOGOUT_EXPIRED = "com.android.labassist.ACTION_LOGOUT_EXPIRED";
    public static final String ROLE_AUTHENTICATED = "authenticated";
    public static final String LAST_LAB_SYNC_TIME = "last_lab_sync_time";

    // Keys
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";
    private static final String KEY_ID = "id";
    private static final String KEY_ORGANISATION_NAME = "organisation_name";
    private static final String KEY_ORGANISATION_ID = "organisation_id";
    private static final String KEY_DEPARTMENT = "department";
    private static final String KEY_DEPARTMENT_ID = "department_id";
    private static final String KEY_REGISTRATION_ID = "registration_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_LEVEL = "level";

    // Roles
    public static final String ROLE_STUDENT = "STUDENT";
    public static final String ROLE_TECH = "TECHNICIAN";
    public static final String ROLE_ADMIN = "ADMIN";

    private static SessionManager instance;
    private final SharedPreferences prefs;

    // Private constructor for Singleton
    private SessionManager(Context context) {
//        try {
//            MasterKey masterKey = new MasterKey.Builder(context)
//                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
//                    .build();

            prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
//        } catch (GeneralSecurityException | IOException e) {
//            Log.e("TokenManager", "Encryption corrupted, clearing and retrying...", e);
//
//            // 1. Manually clear the corrupted XML file from disk
//            context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE).edit().clear().apply();
//
//            // 2. Optional: If the Keystore itself is the issue, you might need to try one more time
//            // or fallback to standard SharedPreferences for this session
//        }
    }

    // Thread-safe Singleton Access
    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }

    public void saveLogin(String id, String email, String role, String username,
                          String orgName, String orgId, String departmentName, String depId, String regID) {
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_ID, id)
                .putString(KEY_EMAIL, email)
                .putString(KEY_ROLE, role)
                .putString(KEY_USERNAME, username)
                .putString(KEY_ORGANISATION_NAME, orgName)
                .putString(KEY_DEPARTMENT, departmentName)
                .putString(KEY_REGISTRATION_ID, regID)
                .putString(KEY_ORGANISATION_ID, orgId)
                .putString(KEY_DEPARTMENT_ID, depId)
                .apply();
    }
    public void saveLogin(Context context, String email, String role) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_ROLE, role).putBoolean(KEY_IS_LOGGED_IN, true);

        editor.apply();
    }

    public void setLastLabSyncTime(long timeInMills){
        prefs.edit().putLong(LAST_LAB_SYNC_TIME, timeInMills).apply();
    }

    public long getLastLabSyncTime(){
        return prefs.getLong(LAST_LAB_SYNC_TIME, 0);
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, null);
    }

    public boolean isRoleSet() {
        return getRole() != null && !getRole().equals(ROLE_AUTHENTICATED);
    }

    public void logout() {
        prefs.edit().clear().apply();
    }

    public void setRoleAuthenticated(){
        prefs.edit().putString(KEY_ROLE, ROLE_AUTHENTICATED).apply();
    }

    // Individual Getters (No Context needed anymore!)
    public String getUsername() { return prefs.getString(KEY_USERNAME, null); }
    public String getEmail() { return prefs.getString(KEY_EMAIL, null); }
    public String getOrganisationName() { return prefs.getString(KEY_ORGANISATION_NAME, null); }
    public String getOrganisationId() { return prefs.getString(KEY_ORGANISATION_ID, null); }
    public String getDepartment() { return prefs.getString(KEY_DEPARTMENT, null); }
    public String getDepartmentID() { return prefs.getString(KEY_DEPARTMENT_ID, null); }
    public String getRegID() { return prefs.getString(KEY_REGISTRATION_ID, null); }
    public String getId() { return prefs.getString(KEY_ID, null); }
    public String getInstitutionName() { return prefs.getString(KEY_ORGANISATION_NAME, null); }
}