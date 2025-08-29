package com.inhatc.localit.api.home;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface ApiService {

    // (A) Travel Courses
    @Headers({"Accept: application/json"})
    @GET("areaBasedList1")
    Call<ApiResponse> getTravelCourses(
            @Query("serviceKey") String key,
            @Query("MobileOS") String os,
            @Query("MobileApp") String app,
            @Query("_type") String type,
            @Query("contentTypeId") int contentTypeId,
            @Query("imageYN") String imageYN,
            @Query("listYN") String listYN,
            @Query("numOfRows") int rows,
            @Query("pageNo") int page,
            @Query("arrange") String arrange
    );

    // (B) Traditional Markets
    @Headers({"Accept: application/json"})
    @GET("searchKeyword1")
    Call<ApiResponse> getMarkets(
            @Query("serviceKey") String key,
            @Query("MobileOS") String os,
            @Query("MobileApp") String app,
            @Query("_type") String type,
            @Query("contentTypeId") int contentTypeId,

            @Query("keyword") String keyword,
            @Query("imageYN") String imageYN,
            @Query("listYN") String listYN,
            @Query("numOfRows") int rows,
            @Query("pageNo") int page,
            @Query("arrange") String arrange
    );

    // (C) Festivals (Replaced with a more flexible method)
    @Headers({"Accept: application/json"})
    @GET("areaBasedList1")
    Call<ApiResponse> getFestivals(
            @Query("serviceKey") String key,
            @Query("MobileOS") String os,
            @Query("MobileApp") String app,
            @Query("_type") String type,
            @Query("numOfRows") int rows,

            @Query("pageNo") int page,
            @Query("arrange") String arrange,
            @Query("contentTypeId") int contentTypeId,
            @Query("areaCode") Integer areaCode      // Changed to Integer to allow null
    );

    // Course Detail
    @Headers({"Accept: application/json"})
    @GET("detailCommon1")
    Call<DetailResponse> getCourseDetail(
            @Query("serviceKey") String key,
            @Query("MobileOS") String os,
            @Query("MobileApp") String app,
            @Query("_type") String type,
            @Query("contentId") String contentId,
            @Query("contentTypeId") Integer contentTypeId,
            @Query("defaultYN") String defaultYN,
            @Query("firstImageYN") String firstImageYN,
            @Query("overviewYN") String overviewYN,
            @Query("addrinfoYN") String addrinfoYN,
            @Query("mapinfoYN") String mapinfoYN
    );
}