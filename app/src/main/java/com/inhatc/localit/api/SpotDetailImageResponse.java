// SpotDetailImageResponse.java
package com.inhatc.localit.api;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SpotDetailImageResponse {
    @SerializedName("response") public Response response;

    public static class Response { @SerializedName("body") public Body body; }
    public static class Body { @SerializedName("items") public Items items; }

    public static class Items {
        /** 단일/배열 혼용 대응 */
        @SerializedName("item")
        @JsonAdapter(SingleOrArrayAdapter.class)
        public List<Item> item;
    }

    public static class Item {
        @SerializedName("originimgurl") public String originimgurl;   // 원본
        @SerializedName("smallimageurl") public String smallimageurl; // 썸네일
    }
}
