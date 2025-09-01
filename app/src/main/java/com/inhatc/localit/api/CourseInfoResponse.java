package com.inhatc.localit.api;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/** KorService2 - detailInfo2 (contentTypeId=25, 코스 구간 목록) */
public class CourseInfoResponse {

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
        public List<CourseInfoItem> item;
    }

    /** 코스 각 스텝(서브코스) */
    public static class CourseInfoItem {
        @SerializedName("contentid")         public String contentid;
        @SerializedName("contenttypeid")     public String contenttypeid;
        @SerializedName("subnum")            public String subnum;            // 0,1,2...
        @SerializedName("subcontentid")      public String subcontentid;
        @SerializedName("subname")           public String subname;
        @SerializedName("subdetailoverview") public String subdetailoverview;
        @SerializedName("subdetailimg")      public String subdetailimg;
        @SerializedName("subdetailalt")      public String subdetailalt;
    }
}
