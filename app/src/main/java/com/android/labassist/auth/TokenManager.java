package com.android.labassist.auth;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class TokenManager {
    private static final String PREF_NAME = "secure_tokens";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_EXPIRY_TIME = "expiry_time";

    // Refresh 5 minutes before actual expiry to avoid network lag failures
    private static final long REFRESH_BUFFER_MS = 300000;

    private SharedPreferences preferences;

    public TokenManager(Context context) {
        initializePref(context.getApplicationContext());
    }

    private void initializePref(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            preferences = EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            // Industrial fallback: If encryption fails, use standard prefs (or force logout)
            preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
    }

    public void saveTokens(String accessToken, String refreshToken) {
        long expiryTimestamp = System.currentTimeMillis() + (3600*1000);
        preferences.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .putLong(KEY_EXPIRY_TIME, expiryTimestamp)
                .apply();
    }

    public String getAccessToken() {
        return preferences.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return preferences.getString(KEY_REFRESH_TOKEN, null);
    }

    public void clearTokens() {
        preferences.edit().clear().apply();
    }

    public boolean isAccessTokenExpired() {
        long expiryTime = preferences.getLong(KEY_EXPIRY_TIME, 0);
        // Returns true if current time is within 5 minutes of the expiry timestamp
        return System.currentTimeMillis() >= (expiryTime - REFRESH_BUFFER_MS);
    }
}