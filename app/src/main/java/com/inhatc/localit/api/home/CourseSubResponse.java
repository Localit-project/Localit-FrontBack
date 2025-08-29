package com.inhatc.localit.api.home;

import java.util.List;

public class CourseSubResponse {
    public ResponseBody response;
    public static class ResponseBody { public Body body; }
    public static class Body { public Items items; }
    public static class Items { public List<Item> item; }
    public static class Item {
        public String contentid;
        public String contenttypeid;
        public Integer subnum;              // 0,1,2...
        public String subcontentid;
        public String subname;              // 스팟명
        public String subdetailoverview;    // 설명
        public String subdetailimg;         // 이미지 (없을 수 있음)
        public String subdetailalt;
    }
}
