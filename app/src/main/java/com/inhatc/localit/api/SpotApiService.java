package com.inhatc.localit.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SpotApiService {

    // 관광지(12)
    @GET("areaBasedList2")
    Call<SpotResponse> getTourList(
            @Query("numOfRows") int numOfRows,
            @Query("pageNo") int pageNo,
            @Query("MobileOS") String os,
            @Query("MobileApp") String app,
            @Query("arrange") String arrange,           // "A" / "C" 등
            @Query("contentTypeId") int contentTypeId,  // 12
            @Query("areaCode") int areaCode,
            @Query("sigunguCode") Integer sigunguCode,
            @Query("_type") String type,                // "json"
            @Query("serviceKey") String key
    );

    // 축제(15)
    @GET("searchFestival2")
    Call<SpotResponse> getFestivalList(
            @Query("numOfRows") int numOfRows,
            @Query("pageNo") int pageNo,
            @Query("MobileOS") String os,
            @Query("MobileApp") String app,
            @Query("_type") String type,                // "json"
            @Query("areaCode") int areaCode,
            @Query("sigunguCode") Integer sigunguCode,
            @Query("eventStartDate") String eventStartDate,  // yyyyMMdd
            @Query("arrange") String arrange,           // "A"
            @Query("serviceKey") String key
    );

    // --- 검색: 기존 호출을 살리기 위해 오버로드 2개 제공 ---

    // (A) 프로젝트 일부가 이 시그니처를 사용
    @GET("searchKeyword2")
    Call<SpotResponse> searchKeyword(
            @Query("MobileOS") String os,
            @Query("MobileApp") String app,
            @Query("_type") String type,          // "json"
            @Query("keyword") String keyword,
            @Query("contentTypeId") Integer contentTypeId, // null 가능
            @Query("numOfRows") int numOfRows,
            @Query("pageNo") int pageNo,
            @Query("serviceKey") String key
    );

    // (B) 다른 부분이 이 순서를 사용했다면 이것도 커버됨
    @GET("searchKeyword2")
    Call<SpotResponse> searchKeyword(
            @Query("serviceKey") String key,
            @Query("MobileOS") String os,
            @Query("MobileApp") String app,
            @Query("_type") String type,
            @Query("keyword") String keyword,
            @Query("contentTypeId") Integer contentTypeId,
            @Query("numOfRows") int numOfRows,
            @Query("pageNo") int pageNo
    );

    // 공통 상세(대표사진/개요/주소/우편번호/연락처 등)
    // SpotApiService.java
    @GET("detailCommon2")
    Call<SpotDetailCommonResponse> getDetailCommon(
            @Query("MobileOS") String os,
            @Query("MobileApp") String app,
            @Query("_type") String type,
            @Query("contentId") String contentId,
            @Query("contentTypeId") int contentTypeId,
            @Query("defaultYN") String defaultYN,
            @Query("firstImageYN") String firstImageYN,
            @Query("areacodeYN") String areacodeYN,
            @Query("catcodeYN") String catcodeYN,
            @Query("addrinfoYN") String addrinfoYN,
            @Query("mapinfoYN") String mapinfoYN,
            @Query("overviewYN") String overviewYN,
            @Query("serviceKey") String serviceKey
    );

    @GET("detailInfo2")
    Call<SpotDetailInfoResponse> getDetailInfo(
            @Query("MobileOS") String os,
            @Query("MobileApp") String app,
            @Query("_type") String type,               // "json"
            @Query("contentId") String contentId,
            @Query("contentTypeId") int contentTypeId, // 12, 15...
            @Query("serviceKey") String key
    );

    // 인트로 상세(관광지: 문의/쉬는날/이용시간/주차/입장료 등, 축제는 행사기간 등)
    @GET("detailIntro2")
    Call<SpotDetailIntroResponse> getDetailIntro(
            @Query("MobileOS") String os,
            @Query("MobileApp") String app,
            @Query("_type") String type,               // "json"
            @Query("contentId") String contentId,
            @Query("contentTypeId") int contentTypeId, // 12(관광) / 15(축제)
            @Query("serviceKey") String key
    );
    // SpotApiService.java
    @GET("detailImage2")
    Call<SpotDetailImageResponse> getDetailImages(
            @Query("numOfRows") int numOfRows,
            @Query("pageNo") int pageNo,
            @Query("MobileOS") String mobileOS,     // "AND"
            @Query("MobileApp") String mobileApp,   // "localit"
            @Query("_type") String type,            // "json"
            @Query("imageYN") String imageYN,       // "Y"
//            @Query("subImageYN") String subImageYN, // "Y"
            @Query("contentId") String contentId,
            @Query("serviceKey") String serviceKey
    );

}
