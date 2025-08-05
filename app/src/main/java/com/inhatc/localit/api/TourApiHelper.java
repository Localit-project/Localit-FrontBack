package com.inhatc.localit.api;

public class TourApiHelper {

    private static TourApiService apiService = RetrofitClient.getInstance().create(TourApiService.class);

    public static TourApiService getApiService() {
        return apiService;
    }
}