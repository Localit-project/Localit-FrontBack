// app/src/main/java/com/inhatc/localit/api/home/TourRetrofitClient.java
package com.inhatc.localit.api.home;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class TourRetrofitClient {

    private static volatile TourApiService service;

    public static TourApiService get() {
        if (service == null) {
            synchronized (TourRetrofitClient.class) {
                if (service == null) {
                    OkHttpClient client = new OkHttpClient.Builder().build();

                    Gson gson = new GsonBuilder()
                            .setLenient()
                            .create();

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl("https://apis.data.go.kr/B551011/KorService2/")
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .build();

                    service = retrofit.create(TourApiService.class);
                }
            }
        }
        return service;
    }

    private TourRetrofitClient() {}
}
