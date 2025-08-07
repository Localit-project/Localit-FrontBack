package com.inhatc.localit.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TourApiService {

    @GET("areaBasedList2")
    Call<TourResponse> getTourList(
            @Query("numOfRows") int numOfRows,
            @Query("pageNo") int pageNo,
            @Query("MobileOS") String os,
            @Query("MobileApp") String app,
            @Query("arrange") String arrange,
            @Query("contentTypeId") int contentTypeId,
            @Query("areaCode") int areaCode,
            @Query("_type") String type,
            @Query("serviceKey") String key
    );
    @GET("searchFestival2")
    Call<TourResponse> getFestivalList(
            @Query("numOfRows") int numOfRows,
            @Query("pageNo") int pageNo,
            @Query("MobileOS") String mobileOS,
            @Query("MobileApp") String mobileApp,
            @Query("_type") String type,
            @Query("areaCode") int areaCode,
            @Query("eventStartDate") String eventStartDate,
            @Query("arrange") String arrange,
            @Query("serviceKey") String serviceKey
    );
}
