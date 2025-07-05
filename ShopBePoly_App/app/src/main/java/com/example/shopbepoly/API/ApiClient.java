package com.example.shopbepoly.API;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String TAG = "ApiClient";
    public static final String IPV4 = "192.168.1.8";

    public static final String BASE_URL = "http://" +IPV4+":3000/";
    public static final String BASE_API_URL = BASE_URL + "api/";
    public static final String IMAGE_URL = BASE_URL + "uploads/";

    private static Retrofit retrofit;

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            try {
                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> {
                    if (message.startsWith("-->") || message.startsWith("<--")) {
                        Log.d(TAG, "OkHttp: " + message);
                    } else {
                        Log.d(TAG, "OkHttp Body: " + message);
                    }
                });
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

                OkHttpClient client = new OkHttpClient.Builder()
                        .addInterceptor(loggingInterceptor)
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .build();

                retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_API_URL) // dùng BASE_API_URL
                        .client(client)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                Log.d(TAG, "Retrofit instance created with base URL: " + BASE_API_URL);
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
