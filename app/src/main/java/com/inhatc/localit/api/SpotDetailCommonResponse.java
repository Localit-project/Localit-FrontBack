package com.inhatc.localit.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/** TourAPI detailCommon2 응답 모델 */
public class SpotDetailCommonResponse {
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
    public String firstimage, firstimage2, overview, addr1, addr2, zipcode, tel, mapx, mapy, title;


    /** 실제 상세 1건 데이터 */
    public static class Item {
        @SerializedName("contentid")     public String contentid;
        @SerializedName("contenttypeid") public String contenttypeid;

        @SerializedName("title")         public String title;

        // 대표 이미지
        @SerializedName("firstimage")    public String firstimage;
        @SerializedName("firstimage2")   public String firstimage2;

        // 홈페이지(종종 <a href="..."> 링크 형식)
        @SerializedName("homepage")      public String homepage;

        // 개요
        @SerializedName("overview")      public String overview;

        // 주소/우편/연락처
        @SerializedName("addr1")         public String addr1;
        @SerializedName("addr2")         public String addr2;
        @SerializedName("zipcode")       public String zipcode;
        @SerializedName("tel")           public String tel;

        // 좌표(선택)
        @SerializedName("mapx")          public String mapx;
        @SerializedName("mapy")          public String mapy;

    }
}
