package com.inhatc.localit.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SpotApiService {

    // 관광지 목록 (contentTypeId = 12)
    @GET("areaBasedList2")
    Call<SpotResponse> getTourList(
            @Query("numOfRows") int numOfRows,
            @Query("pageNo") int pageNo,
            @Query("MobileOS") String os,            // "AND"
            @Query("MobileApp") String app,          // "localit"
            @Query("arrange") String arrange,        // "c" 등
            @Query("contentTypeId") int contentTypeId,
            @Query("areaCode") int areaCode,
            @Query("sigunguCode") Integer sigunguCode,
            @Query("_type") String type,             // "json"
            @Query("serviceKey") String key
    );

    // 축제 목록 (contentTypeId = 15)
    @GET("searchFestival2")
    Call<SpotResponse> getFestivalList(
            @Query("numOfRows") int numOfRows,
            @Query("pageNo") int pageNo,
            @Query("MobileOS") String mobileOS,
            @Query("MobileApp") String mobileApp,
            @Query("_type") String type,             // "json"
            @Query("areaCode") int areaCode,
            @Query("sigunguCode") Integer sigunguCode,
            @Query("eventStartDate") String eventStartDate, // yyyyMMdd
            @Query("arrange") String arrange,        // "A"
            @Query("serviceKey") String serviceKey
    );

    // 키워드 검색 (관광지/축제 공통)
    @GET("searchKeyword2")
    Call<SpotResponse> searchKeyword(
            @Query("serviceKey") String serviceKey,
            @Query("MobileOS") String mobileOS,
            @Query("MobileApp") String mobileApp,
            @Query("_type") String type,             // "json"
            @Query("keyword") String keyword,
            @Query("contentTypeId") int contentTypeId,
            @Query("numOfRows") int numOfRows,
            @Query("pageNo") int pageNo
    );

    // 상세 조회 (홈페이지, 개요 등)
    @GET("detailCommon2")   // KorService2 기준. 404 뜨면 detailCommon 으로 변경
    Call<SpotDetailCommonResponse> getDetailCommon(
            @Query("MobileOS") String mobileOS,           // "AND"
            @Query("MobileApp") String mobileApp,         // "localit"
            @Query("_type") String type,                  // "json"
            @Query("contentId") String contentId,
            @Query("contentTypeId") String contentTypeId, // "12"/"15"
            @Query("defaultYN") String defaultYN,         // "Y"
            @Query("overviewYN") String overviewYN,       // "Y"
            @Query("serviceKey") String serviceKey
    );
}