package com.inhatc.localit.api;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * detailIntro2 응답 모델 (header 포함)
 * - contentTypeId 12(관광지)와 15(축제)에서 공통/개별 필드 모두 포함
 */
public class SpotDetailIntroResponse {
    @SerializedName("response") public Response response;

    public static class Response {
        @SerializedName("header") public Header header;  // ← 추가
        @SerializedName("body")   public Body body;
    }

    /** 공통 헤더 */
    public static class Header {
        @SerializedName("resultCode") public String resultCode;
        @SerializedName("resultMsg")  public String resultMsg;
    }

    public static class Body { @SerializedName("items") public Items items; }

    public static class Items {
        /** 단일/배열 혼용 대응 */
        @SerializedName("item")
        @JsonAdapter(SingleOrArrayAdapter.class)
        public List<Item> item;
    }

    /** Intro item (관광지/축제 공용) */
    public static class Item {
        // ---- 공통 식별자 ----
        @SerializedName("contentid")     public String contentid;
        @SerializedName("contenttypeid") public String contenttypeid; // "12", "15" 등 문자열

        // =========================
        // = 관광지(12) 관련 필드 =
        // =========================
        @SerializedName("infocenter")    public String infocenter;   // 문의 및 안내
        @SerializedName("restdate")      public String restdate;     // 쉬는날
        @SerializedName("usetime")       public String usetime;      // 이용시간
        @SerializedName("parking")       public String parking;      // 주차시설
        @SerializedName("usefee")        public String usefee;       // 입장료/이용요금(관광지)

        @SerializedName("chkbabycarriage") public String chkbabycarriage;
        @SerializedName("chkpet")          public String chkpet;
        @SerializedName("chkcreditcard")   public String chkcreditcard;

        // ======================
        // = 축제(15) 관련 필드 =
        // ======================
        @SerializedName("eventstartdate")     public String eventstartdate;     // yyyyMMdd
        @SerializedName("eventenddate")       public String eventenddate;       // yyyyMMdd
        @SerializedName(value = "program",
                alternate = { "eventprogram", "overviewProgram", "content", "detailcontent", "eventcontents", "placeinfo" })
        public String program;

        @SerializedName("playtime")           public String playtime;
        @SerializedName("subevent")           public String subevent;
        @SerializedName("festivalgrade")      public String festivalgrade;

        @SerializedName("usetimefestival")    public String usetimefestival;
        @SerializedName("infocenterfestival") public String infocenterfestival;

        // 주최/주관
        @SerializedName("sponsor1")           public String sponsor1;
        @SerializedName("sponsor1tel")        public String sponsor1tel;
        @SerializedName("sponsor2")           public String sponsor2;
    }
}
