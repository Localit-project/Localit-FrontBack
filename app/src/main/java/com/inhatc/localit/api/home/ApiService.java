package com.inhatc.localit.api.home;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * VisitKorea KorService v1 (…/KorService1/)
 * - baseUrl 이 반드시 .../KorService1/ 로 끝나야 합니다.
 * - 리스트: areaBasedList1
 * - 상세: detailCommon1 / detailIntro1 / detailInfo1 / detailImage1
 */
public interface ApiService {

    // ================== HOME 리스트들 ==================

    /** 지역기반 리스트 (관광지=12, 축제/공연/행사=15, 여행코스=25 등) */
    @Headers({"Accept: application/json"})
    @GET("areaBasedList1")
    Call<ApiResponse> getAreaBasedList(
            @Query("serviceKey") String key,
            @Query("MobileOS") String os,
            @Query("MobileApp") String app,
            @Query("_type") String type,
            @Query("contentTypeId") Integer contentTypeId, // 12/15/25, 필요없으면 null
            @Query("imageYN") String imageYN,              // "Y"
            @Query("listYN") String listYN,                // "Y"
            @Query("numOfRows") int rows,
            @Query("pageNo") int page,
            @Query("arrange") String arrange,              // "D"=최신
            @Query("areaCode") Integer areaCode            // 전국은 null
    );

    /** 키워드 검색 (필요시 사용) */
    @Headers({"Accept: application/json"})
    @GET("searchKeyword1")
    Call<ApiResponse> searchKeyword(
            @Query("serviceKey") String key,
            @Query("MobileOS") String os,
            @Query("MobileApp") String app,
            @Query("_type") String type,
            @Query("contentTypeId") Integer contentTypeId, // null 허용
            @Query("keyword") String keyword,
            @Query("imageYN") String imageYN,
            @Query("listYN") String listYN,
            @Query("numOfRows") int rows,
            @Query("pageNo") int page,
            @Query("arrange") String arrange
    );

    // ================== 상세 (여행코스 = contentTypeId 25) ==================

    /** 공통 상세: 제목/개요/주소/대표사진/좌표 등 */
    @Headers({"Accept: application/json"})
    @GET("detailCommon1")
    Call<DetailResponse> getCourseDetailCommon(
            @Query("serviceKey") String serviceKey,
            @Query("MobileOS") String mobileOS,
            @Query("MobileApp") String mobileApp,
            @Query("_type") String type,
            @Query("contentId") String contentId,
            @Query("contentTypeId") Integer contentTypeId, // 25 (nullable 허용)
            @Query("defaultYN") String defaultYN,          // "Y"
            @Query("firstImageYN") String firstImageYN,    // "Y"
            @Query("overviewYN") String overviewYN,        // "Y" 권장
            @Query("addrinfoYN") String addrinfoYN,        // "Y"
            @Query("mapinfoYN") String mapinfoYN           // "Y"
    );

    // 동일 엔드포인트(호출부 호환용)
    @Headers({"Accept: application/json"})
    @GET("detailCommon1")
    Call<DetailResponse> getCourseDetail(
            @Query("serviceKey") String serviceKey,
            @Query("MobileOS") String mobileOS,
            @Query("MobileApp") String mobileApp,
            @Query("_type") String type,
            @Query("contentId") String contentId,
            @Query("contentTypeId") Integer contentTypeId, // 25
            @Query("defaultYN") String defaultYN,
            @Query("firstImageYN") String firstImageYN,
            @Query("overviewYN") String overviewYN,
            @Query("addrinfoYN") String addrinfoYN,
            @Query("mapinfoYN") String mapinfoYN
    );

    /** 보조사진(갤러리) */
    @Headers({"Accept: application/json"})
    @GET("detailImage1")
    Call<ImageResponse> getDetailImages(
            @Query("serviceKey") String key,
            @Query("MobileOS") String os,
            @Query("MobileApp") String app,
            @Query("_type") String type,
            @Query("contentId") String contentId,
            @Query("imageYN") String imageYN,       // "Y"
            @Query("subImageYN") String subImageYN  // "Y"
    );

    /** 코스 인트로(거리/일정/소요시간/테마 등) */
    @Headers({"Accept: application/json"})
    @GET("detailIntro1")
    Call<CourseIntroResponse> getCourseIntro(
            @Query("serviceKey") String key,
            @Query("MobileOS") String os,
            @Query("MobileApp") String app,
            @Query("_type") String type,
            @Query("contentId") String contentId,
            @Query("contentTypeId") int contentTypeId // 25
    );

    /** 코스 상세 순서(서브 코스 목록) */
    @Headers({"Accept: application/json"})
    @GET("detailInfo1")
    Call<CourseInfoResponse> getCourseInfo(
            @Query("serviceKey") String key,
            @Query("MobileOS") String os,
            @Query("MobileApp") String app,
            @Query("_type") String type,
            @Query("contentId") String contentId,
            @Query("contentTypeId") int contentTypeId // 25
    );
}
