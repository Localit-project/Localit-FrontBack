package com.inhatc.localit.api;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/** detailIntro2 응답 모델 (header 포함) */
public class SpotDetailIntroResponse {
    @SerializedName("response") public Response response;

    public static class Response {
        @SerializedName("header") public Header header;
        @SerializedName("body")   public Body body;
    }

    public static class Header {
        @SerializedName("resultCode") public String resultCode;
        @SerializedName("resultMsg")  public String resultMsg;
    }

    public static class Body { @SerializedName("items") public Items items; }

    public static class Items {
        @SerializedName("item")
        @JsonAdapter(SingleOrArrayAdapter.class)
        public List<Item> item;
    }

    /** Intro item (관광지/축제 공용) */
    public static class Item {
        // 공통 식별자
        @SerializedName("contentid")     public String contentid;
        @SerializedName("contenttypeid") public String contenttypeid; // "12", "15" 등

        // 관광지(12)
        @SerializedName("restdate")      public String restdate;
        @SerializedName("usetime")       public String usetime;
        @SerializedName("parking")       public String parking;
        @SerializedName("usefee")        public String usefee;
        @SerializedName("chkbabycarriage") public String chkbabycarriage;
        @SerializedName("chkpet")          public String chkpet;
        @SerializedName("chkcreditcard")   public String chkcreditcard;

        // 축제(15)
        @SerializedName("eventstartdate")     public String eventstartdate;
        @SerializedName("eventenddate")       public String eventenddate;
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

        // 문의/안내(전화): 모든 변형 키를 infocenter로 통합 매핑
        @SerializedName(
                value = "infocenter",
                alternate = {
                        "infocentershopping",
                        "infocenterfood",
                        "infocenterleports",
                        "infocenterlodging"
                }
        )
        public String infocenter;

        // 홈페이지(HTML 앵커 포함)
        @SerializedName("homepage")
        public String homepage;
    }
}
