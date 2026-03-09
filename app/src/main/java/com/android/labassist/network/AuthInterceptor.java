package com.android.labassist.network;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.labassist.BuildConfig;
import com.android.labassist.auth.AuthEventBus;
import com.android.labassist.auth.SessionManager;
import com.android.labassist.auth.TokenManager;
import com.android.labassist.network.models.LoginResponse;
import com.android.labassist.network.models.RefreshSessionRequest;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;

public class AuthInterceptor implements Interceptor {
    private final TokenManager tokenManager;
    private final Context context;
    // Object for synchronization lock
    private static final Object lock = new Object();

    public AuthInterceptor(Context context) {
        this.context = context.getApplicationContext();
        this.tokenManager = TokenManager.getInstance(this.context);
        Log.d("Token", "interceptor constructor");
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request.Builder builder = originalRequest.newBuilder();

        String freshToken = BuildConfig.SUPABASE_ANON_KEY;

        // 1. Check & Refresh Token (Synchronized to prevent multiple simultaneous refreshes)
        synchronized (lock) {

            String accessToken = tokenManager.getAccessToken();

            if (accessToken != null && tokenManager.isAccessTokenExpired()) {
                boolean success = refreshAccessToken();
                if (!success) {
                    // Force a logout and stop the chain
                    AuthEventBus.getInstance().triggerLogout();
                    return chain.proceed(originalRequest);
                }
            }
        }
        freshToken = tokenManager.getAccessToken();

        builder.header("Authorization", "Bearer " + freshToken);
        builder.header("apikey", BuildConfig.SUPABASE_ANON_KEY);
        builder.header("Content-Type", "application/json");

        return chain.proceed(builder.build());
    }

    private boolean refreshAccessToken() {
        String refreshToken = tokenManager.getRefreshToken();
        if (refreshToken == null) return false;

        try {
            Log.d("Token", "In Inceptor refreshAccessToken()");
            // CRITICAL: Ensure this Public API does NOT use this interceptor
            Call<LoginResponse> call = ApiController.getInstance(context)
                    .getPublicApi()
                    .getAccessToken(new RefreshSessionRequest(refreshToken));

            retrofit2.Response<LoginResponse> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                LoginResponse body = response.body();
                tokenManager.saveTokens(body.getAccessToken(), body.getRefreshToken());
                return true;
            }
            else{
                Log.e("AuthInterceptor", "onSuccess, Refresh failed: " + response.code() + " " + response.message());
            }
        } catch (Exception e) {
            Log.e("AuthInterceptor", "Catch Block, Refresh failed: " + e.getMessage());
        }
        return false;
    }
}