package com.inhatc.localit.api.home;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface ApiService {

    // 공통 상세
    @Headers({"Accept: application/json"})
    @GET("detailCommon2")
    Call<DetailResponse> getCourseDetail(
            @Query("serviceKey") String key,
            @Query("MobileOS") String os,
            @Query("MobileApp") String app,
            @Query("_type") String type,
            @Query("contentId") String contentId,
            @Query("contentTypeId") Integer contentTypeId, // 25
            @Query("defaultYN") String defaultYN,          // "Y"
            @Query("firstImageYN") String firstImageYN,    // "Y"
            @Query("overviewYN") String overviewYN,        // "Y"
            @Query("addrinfoYN") String addrinfoYN,        // "Y"
            @Query("mapinfoYN") String mapinfoYN           // "Y"
    );

    // 인트로(거리/일정/소요시간/테마)
    @Headers({"Accept: application/json"})
    @GET("detailIntro2")
    Call<CourseIntroResponse> getCourseIntro(
            @Query("serviceKey") String key,
            @Query("MobileOS") String os,
            @Query("MobileApp") String app,
            @Query("_type") String type,
            @Query("contentId") String contentId,
            @Query("contentTypeId") int contentTypeId // 25
    );

    // 코스 단계 목록
    @Headers({"Accept: application/json"})
    @GET("detailInfo2")
    Call<CourseInfoResponse> getCourseInfo(
            @Query("serviceKey") String key,
            @Query("MobileOS") String os,
            @Query("MobileApp") String app,
            @Query("_type") String type,
            @Query("contentId") String contentId,
            @Query("contentTypeId") int contentTypeId // 25
    );

    // 보조 이미지
    @Headers({"Accept: application/json"})
    @GET("detailImage2")
    Call<ImageResponse> getDetailImages(
            @Query("serviceKey") String key,
            @Query("MobileOS") String os,
            @Query("MobileApp") String app,
            @Query("_type") String type,
            @Query("contentId") String contentId,
            @Query("imageYN") String imageYN,       // "Y"
            @Query("subImageYN") String subImageYN  // "Y"
    );
}
