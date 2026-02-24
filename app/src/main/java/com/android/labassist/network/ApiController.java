package com.android.labassist.network;

import android.content.Context;
import android.util.Log;

import com.android.labassist.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiController {

    private static ApiController instance;

    private final Context context;
    private final Retrofit publicRetrofit;
    private Retrofit protectedRetrofit; // Lazy

    private ApiController(Context context) {

        this.context = context.getApplicationContext();

        // Public Client
        OkHttpClient publicClient = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder()
                            .header("apikey", BuildConfig.SUPABASE_ANON_KEY)
                            .header("Authorization", "Bearer " + BuildConfig.SUPABASE_ANON_KEY)
                            .build();
                    return chain.proceed(request);
                })
                .build();

        publicRetrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.SUPABASE_URL)
                .client(publicClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized ApiController getInstance(Context context) {
        if (instance == null) {
            instance = new ApiController(context);
        }
        return instance;
    }

    private Retrofit getProtectedRetrofit() {

        if (protectedRetrofit == null) {

            OkHttpClient authClient = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(context))
                    .build();

            protectedRetrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.SUPABASE_URL)
                    .client(authClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return protectedRetrofit;
    }

    public APICalls getPublicApi() {
        return publicRetrofit.create(APICalls.class);
    }

    public APICalls getAuthApi() {
        return getProtectedRetrofit().create(APICalls.class);
    }
}