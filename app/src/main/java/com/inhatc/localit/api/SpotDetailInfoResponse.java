// SpotDetailInfoResponse.java
package com.inhatc.localit.api;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SpotDetailInfoResponse {
    @SerializedName("response") public Response response;

    public static class Response {
        @SerializedName("header") public Header header;
        @SerializedName("body") public Body body;
    }
    public static class Header {
        @SerializedName("resultCode") public String resultCode;
        @SerializedName("resultMsg")  public String resultMsg;
    }
    public static class Body {
        @SerializedName("items") public Items items;
    }
    public static class Items {
        @SerializedName("item")
        @JsonAdapter(SingleOrArrayAdapter.class)
        public List<Item> item;
    }
    public static class Item {
        @SerializedName("contentid")     public String contentid;
        @SerializedName("contenttypeid") public String contenttypeid;
        @SerializedName("serialnum")     public String serialnum;
        @SerializedName("infoname")      public String infoname;  // 예: 화장실
        @SerializedName("infotext")      public String infotext;  // 예: 있음
        @SerializedName("fldgubun")      public String fldgubun;
    }
}
