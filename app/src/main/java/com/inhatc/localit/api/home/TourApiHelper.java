package com.inhatc.localit.api.home;

import android.util.Log;
import com.inhatc.localit.api.home.ApiResponse;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TourApiHelper {

    private static final String BASE_URL = "https://apis.data.go.kr/B551011/KorService1/";
    private static final String SERVICE_KEY = "wL/Ry8EMiMg43mPRl3wyQhKosVExsJbLLDcZebat4S4eedobtNuBG+eqrj5GPKHvEAxy4NjYPz25Parbyeg8PA==";
    private static final ApiService apiService;

    static {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    public static void fetchHomeMarkets(Consumer<List<TourItem>> callback) {
        String decodedKey = URLDecoder.decode(SERVICE_KEY, StandardCharsets.UTF_8);
        Call<ApiResponse> call = apiService.getMarkets(
                decodedKey, "AND", "Localit", "json",
                38, "시장", "Y", "Y", 2, 1, "B"
        );
        enqueueAndExtractTourItems(call, callback);
    }

    public static void fetchHomeFestivals(Consumer<List<TourItem>> callback) {
        String decodedKey = URLDecoder.decode(SERVICE_KEY, StandardCharsets.UTF_8);
        Call<ApiResponse> call = apiService.getFestivals(
                decodedKey,
                "AND",
                "Localit",
                "json",
                2,  // Fetches 2 items
                1,
                "D",
                15,
                null
        );
        enqueueAndExtractTourItems(call, callback);
    }

    private static void enqueueAndExtractTourItems(Call<ApiResponse> call, Consumer<List<TourItem>> callback) {
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                List<TourItem> resultList = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null &&
                        response.body().response != null &&
                        response.body().response.body != null &&
                        response.body().response.body.items != null &&
                        response.body().response.body.items.item != null) {

                    resultList = response.body().response.body.items.item;
                }
                callback.accept(resultList);
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("TourApiHelper", "API Call Failed: ", t);
                callback.accept(new ArrayList<>());
            }
        });
    }
}