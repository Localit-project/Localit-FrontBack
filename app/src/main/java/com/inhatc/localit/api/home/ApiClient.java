package com.inhatc.localit.api.home;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    private static final String BASE_URL = "https://apis.data.go.kr/B551011/KorService2/";
    private static ApiService s;

    public static ApiService get() {
        if (s != null) return s;

        HttpLoggingInterceptor log = new HttpLoggingInterceptor();
        log.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient ok = new OkHttpClient.Builder()
                .addInterceptor(log)
                .build();

        s = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(ok)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService.class);
        return s;
    }

    private ApiClient() {}
}
