package com.inhatc.localit.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SpotDetailIntroResponse {
    @SerializedName("response") public Response response;

    public static class Response {
        @SerializedName("body") public Body body;
    }
    public static class Body {
        @SerializedName("items") public Items items;
    }
    public static class Items {
        @SerializedName("item") public List<Item> item;
    }

    // 관광지(12) 기준 필드 이름 (TourAPI 스펙)
    public static class Item {
        // 공통(확인용)
        @SerializedName("contentid") public String contentid;
        @SerializedName("contenttypeid") public String contenttypeid;

        // 문의/이용 등
        @SerializedName("infocenter")    public String infocenter;     // 문의 및 안내
        @SerializedName("restdate")      public String restdate;       // 쉬는날
        @SerializedName("usetime")       public String usetime;        // 이용시간
        @SerializedName("parking")       public String parking;        // 주차시설
        @SerializedName("usefee")        public String usefee;         // 입장료

        // (필요 시 확장)
        @SerializedName("chkbabycarriage") public String chkbabycarriage;
        @SerializedName("chkpet")          public String chkpet;
        @SerializedName("chkcreditcard")   public String chkcreditcard;
    }
}
