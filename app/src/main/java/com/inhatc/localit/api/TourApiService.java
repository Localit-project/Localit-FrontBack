package com.inhatc.localit.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TourApiService {

    // 관광지 목록
    @GET("areaBasedList2")
    Call<TourResponse> getTourList(
            @Query("numOfRows") int numOfRows,
            @Query("pageNo") int pageNo,
            @Query("MobileOS") String os,
            @Query("MobileApp") String app,
            @Query("arrange") String arrange,
            @Query("contentTypeId") int contentTypeId,
            @Query("areaCode") int areaCode,
            @Query("sigunguCode") Integer sigunguCode,
            @Query("_type") String type,
            @Query("serviceKey") String key
    );

    // 축제 목록
    @GET("searchFestival2")
    Call<TourResponse> getFestivalList(
            @Query("numOfRows") int numOfRows,
            @Query("pageNo") int pageNo,
            @Query("MobileOS") String mobileOS,
            @Query("MobileApp") String mobileApp,
            @Query("_type") String type,
            @Query("areaCode") int areaCode,
            @Query("sigunguCode") Integer sigunguCode,
            @Query("eventStartDate") String eventStartDate,
            @Query("arrange") String arrange,
            @Query("serviceKey") String serviceKey
    );


    // 반드시 KorService2 + searchKeyword2
    @GET("searchKeyword2")
    Call<TourResponse> searchKeyword(
            @Query("serviceKey") String serviceKey,
            @Query("MobileOS") String mobileOS,
            @Query("MobileApp") String mobileApp,
            @Query("_type") String type,
            @Query("keyword") String keyword,
            @Query("contentTypeId") int contentTypeId,
            @Query("numOfRows") int numOfRows,
            @Query("pageNo") int pageNo
    );
}