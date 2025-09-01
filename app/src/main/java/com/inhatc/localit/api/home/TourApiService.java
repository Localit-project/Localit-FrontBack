package com.inhatc.localit.api.home;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TourApiService {

    // ===== Intro / Info: contentTypeId 필수 (문자열로 통일) =====
    @GET("detailIntro2")
    Call<IntroResponse> detailIntro2(
            @Query("MobileOS") String mobileOs,         // "AND"
            @Query("MobileApp") String app,             // 앱 이름
            @Query("_type") String type,                // "json"
            @Query("contentId") String contentId,       // 문자열로 통일
            @Query("contentTypeId") String contentTypeId, // "25" 등
            @Query("serviceKey") String serviceKey
    );

    @GET("detailInfo2")
    Call<InfoResponse> detailInfo2(
            @Query("MobileOS") String mobileOs,
            @Query("MobileApp") String app,
            @Query("_type") String type,                 // "json"
            @Query("contentId") String contentId,
            @Query("contentTypeId") String contentTypeId,
            @Query("numOfRows") int rows,
            @Query("pageNo") int page,
            @Query("serviceKey") String serviceKey
    );

    // ===== Common: contentTypeId 제거(필요 없음) =====
    @GET("detailCommon2")
    Call<ApiResponse> detailCommon2(
            @Query("MobileOS") String mobileOs,
            @Query("MobileApp") String app,
            @Query("_type") String type,        // "json"
            @Query("contentId") String contentId,
            @Query("serviceKey") String serviceKey
    );

    // ===== RAW 폴백 (Scalars) =====
    @GET("detailCommon2")
    Call<String> detailCommon2Raw(
            @Query("MobileOS") String mobileOs,
            @Query("MobileApp") String app,
            @Query("_type") String type,        // "xml" 권장
            @Query("contentId") String contentId,
            @Query("serviceKey") String serviceKey
    );

    @GET("detailIntro2")
    Call<String> detailIntro2Raw(
            @Query("MobileOS") String mobileOs,
            @Query("MobileApp") String app,
            @Query("_type") String type,        // "xml"
            @Query("contentId") String contentId,
            @Query("contentTypeId") String contentTypeId,
            @Query("serviceKey") String serviceKey
    );

    @GET("detailInfo2")
    Call<String> detailInfo2Raw(
            @Query("MobileOS") String mobileOs,
            @Query("MobileApp") String app,
            @Query("_type") String type,        // "xml"
            @Query("contentId") String contentId,
            @Query("contentTypeId") String contentTypeId,
            @Query("numOfRows") int rows,
            @Query("pageNo") int page,
            @Query("serviceKey") String serviceKey
    );

    // ===================== 공통 JSON 모델 =====================
    class ApiResponse {
        public String resultCode;    // 어떤 응답 포맷에서도 최상단에 올 수 있어 대비용
        public String resultMsg;
        public Response response;

        public static class Response {
            public Header header;
            public Body body;
        }
        public static class Header {
            public String resultCode;
            public String resultMsg;
        }
        public static class Body {
            public Items items;
            public static class Items {
                public java.util.List<TourDetailItem> item;
            }
        }
    }

    // detailCommon2 항목(필요 필드만)
    class TourDetailItem {
        public String contentid;
        public String contenttypeid;
        public String title;
        public String firstimage;
        public String firstimage2;
        public String addr1;
        public String addr2;
        public String overview;
    }

    // ===================== Intro / Info JSON 모델 =====================
    class IntroResponse {
        public Common.Response response;
        public static class Common {
            public static class Response { public Header header; public Body body; }
            public static class Header { public String resultCode; public String resultMsg; }
            public static class Body { public Items items; }
            public static class Items { public java.util.List<Item> item; }
        }
        public static class Item {
            public String schedule;
            public String distance;
            public String taketime;
            public String theme;
        }
    }

    class InfoResponse {
        public Common.Response response;
        public static class Common {
            public static class Response { public Header header; public Body body; }
            public static class Header { public String resultCode; public String resultMsg; }
            public static class Body { public Items items; }
            public static class Items { public java.util.List<Item> item; }
        }
        public static class Item {
            public String subname;
            public String subdetailoverview;
            public String subdetailimg;
            public String subnum;
        }
    }
}
