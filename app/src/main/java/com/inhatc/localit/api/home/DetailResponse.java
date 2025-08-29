package com.inhatc.localit.api.home;

import java.util.List;

public class DetailResponse {
    public ResponseBody response;

    public static class ResponseBody { public Body body; }
    public static class Body { public Items items; }
    public static class Items { public List<DetailItem> item; }

    public static class DetailItem {
        public String title;
        public String overview;
        public String addr1;
        public String addr2;
        public String firstimage;
        public String firstimage2;
        public String tel;
        public String homepage;
        public String mapx;
        public String mapy;
    }
}
