package com.example.shopbepoly.API;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String TAG = "ApiClient";
    // Thử các base URL khác nhau
    private static final String BASE_URL = "http://192.168.1.3:3000/";  // Bỏ /api/ ở cuối
    private static Retrofit retrofit;

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            try {
                // Create logging interceptor with more detailed logging
                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> {
                    if (message.startsWith("-->") || message.startsWith("<--")) {
                        Log.d(TAG, "OkHttp: " + message);
                    } else {
                        Log.d(TAG, "OkHttp Body: " + message);
                    }
                });
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

                // Create OkHttpClient with timeout and logging
                OkHttpClient client = new OkHttpClient.Builder()
                        .addInterceptor(loggingInterceptor)
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .build();

                // Create Retrofit instance
                retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(client)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                Log.d(TAG, "Retrofit instance created successfully with base URL: " + BASE_URL);
            } catch (Exception e) {
                Log.e(TAG, "Error creating Retrofit instance", e);
            }
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        return getRetrofit().create(ApiService.class);
    }
}