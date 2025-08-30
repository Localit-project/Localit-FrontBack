package com.inhatc.localit.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofit = null;
    private static final String BASE_URL = "https://apis.data.go.kr/B551011/KorService2/";

    public static Retrofit getInstance() {
        if (retrofit == null) {
            // 비표준 JSON을 처리하기 위해 setLenient() 옵션을 추가한 Gson 객체 생성
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            // 네트워크 통신 로그를 확인하기 위한 인터셉터
            HttpLoggingInterceptor netLog = new HttpLoggingInterceptor();
            netLog.setLevel(HttpLoggingInterceptor.Level.BODY);

            // OkHttpClient에 로깅 인터셉터 추가
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(netLog)
                    .build();

            // Retrofit 인스턴스 생성
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }
}
