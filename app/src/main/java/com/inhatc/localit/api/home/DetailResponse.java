package com.inhatc.localit.api.home;

// DetailResponse.java
public class DetailResponse {
    public ResponseBody response;
    public static class ResponseBody { public Body body; }
    public static class Body { public Items items; }
    public static class Items { public java.util.List<DetailItem> item; }
    public static class DetailItem {
        public String title;
        public String overview;
        public String addr1;
        public String firstimage;
        // 필요시 추가
    }
}
