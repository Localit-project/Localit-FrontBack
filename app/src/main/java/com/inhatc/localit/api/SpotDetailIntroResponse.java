// SpotDetailIntroResponse.java
package com.inhatc.localit.api;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import java.util.List;
/** * detailIntro2 응답 모델
 * * - contentTypeId 12(관광지)와 15(축제)에서 공통/개별 필드를 모두 포함
 * * - 앱 코드에서는 필요한 필드만 참조하면 됨(없으면 null) */
public class SpotDetailIntroResponse {
    @SerializedName("response") public Response response;
    public static class Response { @SerializedName("body") public Body body; }
    public static class Body { @SerializedName("items") public Items items; }

    public static class Items {
        /** 단일/배열 혼용 대응 */
        @SerializedName("item")// "12", "15" 등 문자열로 내려옴
        @JsonAdapter(SingleOrArrayAdapter.class)
        public List<Item> item;
    }

    // 이하 Item 클래스는 기존 그대로 (필드들 유지)
    public static class Item {
        @SerializedName("contentid")     public String contentid;
        @SerializedName("contenttypeid") public String contenttypeid; // "12", "15" 등 문자열로 내려옴

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
        @SerializedName("eventstartdate")     public String eventstartdate;     // 행사 시작일 (yyyyMMdd)
        @SerializedName("eventenddate")       public String eventenddate;       // 행사 종료일 (yyyyMMdd)
        @SerializedName(value = "program",    // 행사내용/프로그램
                alternate = { "eventprogram", "overviewProgram", "content", "detailcontent", "eventcontents", "placeinfo" })
        public String program;

        @SerializedName("playtime")           public String playtime;           // 공연시간
        @SerializedName("subevent")           public String subevent;           // 부대행사/진행형태
        @SerializedName("festivalgrade")      public String festivalgrade;      // 축제등급

        @SerializedName("usetimefestival")    public String usetimefestival;    // 이용요금(축제)
        @SerializedName("infocenterfestival") public String infocenterfestival; // 문의 및 안내(축제)

        // 주최/주관 필드는 여기만 유지
        @SerializedName("sponsor1")           public String sponsor1;           // 주최
        @SerializedName("sponsor1tel")        public String sponsor1tel;        // 주최 연락처
        @SerializedName("sponsor2")           public String sponsor2;           // 주관
    }
}