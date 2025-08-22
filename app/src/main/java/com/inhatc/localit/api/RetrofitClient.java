package com.inhatc.localit.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient{

    private static Retrofit retrofit = null;
    private static final String BASE_URL = "https://apis.data.go.kr/B551011/KorService2/";

    public static Retrofit getInstance() {
        if (retrofit == null) {
            Gson gson = new GsonBuilder().setLenient().create();

            HttpLoggingInterceptor netLog = new HttpLoggingInterceptor();
            netLog.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(netLog)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }
}
