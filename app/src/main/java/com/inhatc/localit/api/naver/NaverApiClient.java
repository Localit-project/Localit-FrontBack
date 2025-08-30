package com.inhatc.localit.api.naver;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NaverApiClient {

    private static final String NAVER_API_BASE_URL = "https://openapi.naver.com/";
    private static Retrofit retrofit = null;

    public static Retrofit getInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(NAVER_API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}