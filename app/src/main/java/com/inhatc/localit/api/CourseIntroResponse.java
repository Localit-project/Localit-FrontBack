package com.inhatc.localit.api;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/** KorService2 - detailIntro2 (contentTypeId=25, 코스 소개정보) */
public class CourseIntroResponse {

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
        @JsonAdapter(SingleOrArrayAdapter.class)  // 단일/배열 혼용 대응
        public List<CourseIntroItem> item;
    }

    /** distance / schedule / taketime / theme */
    public static class CourseIntroItem {
        @SerializedName("contentid")     public String contentid;
        @SerializedName("contenttypeid") public String contenttypeid;

        @SerializedName("distance") public String distance;
        @SerializedName("schedule") public String schedule;
        @SerializedName("taketime") public String taketime;
        @SerializedName("theme")    public String theme;
    }
}
