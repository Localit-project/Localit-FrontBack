package com.inhatc.localit.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
public class RetrofitClient {

    private static Retrofit retrofit = null;
    private static final String BASE_URL = "https://apis.data.go.kr/B551011/KorService2/";

    public static Retrofit getInstance() {
        if (retrofit == null) {
            // Gson 객체를 lenient 모드로 설정
            Gson gson = new GsonBuilder()
                    .setLenient()  // lenient 설정
                    .create();

            // Retrofit 객체를 생성하고, GsonConverterFactory를 사용하여 설정
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))  // 커스터마이즈된 Gson을 사용
                    .build();
        }
        return retrofit;
    }
}