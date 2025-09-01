// app/src/main/java/com/inhatc/localit/api/home/TourApiService.java
package com.inhatc.localit.api.home;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Tour API v2 (KorService2) - detailCommon2
 * - defaultYN 절대 넣지 않음 (서버 에러 원인)
 * - 필요한 필드만 요청: firstImageYN/areacodeYN/catcodeYN/addrinfoYN/mapinfoYN/overviewYN
 */
public interface TourApiService {

    @GET("detailCommon2")
    Call<ApiResponse> detailCommon2(
            @Query("MobileOS") String mobileOS,                 // "AND"
            @Query("MobileApp") String mobileApp,               // 앱 이름
            @Query("_type") String type,                        // "json"
            @Query("contentId") String contentId,
            @Query("contentTypeId") String contentTypeId,       // null 가능 → 쿼리에서 제외
            @Query("firstImageYN") String firstImageYN,         // "Y"
            @Query("areacodeYN") String areaYN,                 // "Y"
            @Query("catcodeYN") String catYN,                   // "Y"
            @Query("addrinfoYN") String addrYN,                 // "Y"
            @Query("mapinfoYN") String mapYN,                   // "Y"
            @Query("overviewYN") String overviewYN,             // "Y"
            @Query(value = "serviceKey", encoded = true) String serviceKey // URL-encoded 키
    );

    // ===== GSON 모델 =====
    class ApiResponse {
        @SerializedName("response") public Response response;
        // 오류 포맷이 간혹 상위에 resultCode만 오는 케이스를 대비
        @SerializedName("resultCode") public String resultCode;
        @SerializedName("resultMsg")  public String resultMsg;
    }

    class Response {
        @SerializedName("header") public Header header;
        @SerializedName("body")   public Body body;
    }

    class Header {
        @SerializedName("resultCode") public String resultCode;
        @SerializedName("resultMsg")  public String resultMsg;
    }

    class Body {
        @SerializedName("items") public Items items;
        @SerializedName("totalCount") public Integer totalCount;
    }

    class Items {
        @SerializedName("item") public List<TourDetailItem> item;
    }

    /**
     * detailCommon2의 item 구조 (필요 필드만)
     */
    class TourDetailItem {
        @SerializedName("contentid")  public String contentid;
        @SerializedName("title")      public String title;
        @SerializedName("firstimage") public String firstimage;
        @SerializedName("firstimage2") public String firstimage2;
        @SerializedName("addr1")      public String addr1;
        @SerializedName("addr2")      public String addr2;
        @SerializedName("mapx")       public String mapx;
        @SerializedName("mapy")       public String mapy;
        @SerializedName("overview")   public String overview;
        @SerializedName("homepage")   public String homepage; // 있을 수도, 없을 수도
    }
}
