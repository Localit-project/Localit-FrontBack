package com.inhatc.localit.api;

import java.util.List;

public class TourResponse {
    public Response response;

    public static class Response {
        public Header header;
        public Body body;
    }

    public static class Header {
        public String resultCode;
        public String resultMsg;
    }

    public static class Body {
        public Items items;
    }

    public static class Items {
        public List<Item> item;
    }

    public static class Item {
        public String contentid;
        public String title;
        public String addr1;
        public String firstimage;
        public String createdtime;
     // 축제
        public String eventstartdate;
        public String eventenddate;


    }
}