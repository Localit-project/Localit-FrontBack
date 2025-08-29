package com.inhatc.localit.api.home;

import java.util.List;

public class ImageResponse {
    public ResponseBody response;

    public static class ResponseBody { public Body body; }
    public static class Body { public Items items; }
    public static class Items { public List<ImageItem> item; }

    public static class ImageItem {
        public String originimgurl;   // 원본
        public String smallimageurl;  // 썸네일
    }
}
