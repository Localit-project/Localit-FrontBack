package com.inhatc.localit.api.naver;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NaverApiHelper {
    private static final String BASE_URL = "https://openapi.naver.com/";
    private static Retrofit retrofit = null;

    public static NaverApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create()) // JSON -> Java 객체 변환
                    .build();
        }
        return retrofit.create(NaverApiService.class);
    }
}