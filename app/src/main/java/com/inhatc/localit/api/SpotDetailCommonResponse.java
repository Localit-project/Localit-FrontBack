package com.inhatc.localit.api;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/** TourAPI detailCommon2 응답 모델 (header 포함) */
public class SpotDetailCommonResponse {

    @SerializedName("response")
    public Response response;

    public static class Response {
        @SerializedName("header") public Header header;   // ← 추가
        @SerializedName("body")   public Body body;
    }

    /** 공통 헤더 */
    public static class Header {
        @SerializedName("resultCode") public String resultCode;
        @SerializedName("resultMsg")  public String resultMsg;
    }

    public static class Body {
        @SerializedName("items")
        public Items items;
    }

    public static class Items {
        /** 단일/배열 혼용 대응 */
        @SerializedName("item")
        @JsonAdapter(SingleOrArrayAdapter.class)
        public List<Item> item;
    }

    /** 실제 상세 1건 데이터 */
    public static class Item {
        @SerializedName("contentid")     public String contentid;
        @SerializedName("contenttypeid") public String contenttypeid;

        @SerializedName("title")         public String title;

        // 대표 이미지
        @SerializedName("firstimage")    public String firstimage;
        @SerializedName("firstimage2")   public String firstimage2;

        // 개요
        @SerializedName("overview")      public String overview;

        // 주소/우편/연락처
        @SerializedName("addr1")         public String addr1;
        @SerializedName("addr2")         public String addr2;
        @SerializedName("zipcode")       public String zipcode;
        @SerializedName("tel")           public String tel;

        // 좌표
        @SerializedName("mapx")          public String mapx;
        @SerializedName("mapy")          public String mapy;
    }
}
