package com.inhatc.localit.api;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/** detailImage2 응답 모델 (header 포함) */
public class SpotDetailImageResponse {
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

    public static class Item {
        @SerializedName("originimgurl")  public String originimgurl;   // 원본
        @SerializedName("smallimageurl") public String smallimageurl;  // 썸네일
    }
}
