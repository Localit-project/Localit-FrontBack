package com.inhatc.localit.api.home;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

final class TourRetrofitClient {

    private static volatile TourApiService INSTANCE;

    private TourRetrofitClient() {}

    static TourApiService get() {
        if (INSTANCE == null) {
            synchronized (TourRetrofitClient.class) {
                if (INSTANCE == null) {
                    HttpLoggingInterceptor log = new HttpLoggingInterceptor();
                    log.setLevel(HttpLoggingInterceptor.Level.BASIC);

                    OkHttpClient ok = new OkHttpClient.Builder()
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .writeTimeout(20, TimeUnit.SECONDS)
                            .addInterceptor(log)
                            // 굳이 Accept 고정하지 않음( XML 폴백 대비 )
                            .build();

                    Retrofit rt = new Retrofit.Builder()
                            .baseUrl("https://apis.data.go.kr/B551011/KorService2/")
                            // RAW(XML/문자) 먼저
                            .addConverterFactory(ScalarsConverterFactory.create())
                            // 그다음 JSON
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(ok)
                            .build();

                    INSTANCE = rt.create(TourApiService.class);
                }
            }
        }
        return INSTANCE;
    }
}
