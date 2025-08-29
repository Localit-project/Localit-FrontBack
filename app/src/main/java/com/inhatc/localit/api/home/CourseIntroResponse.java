package com.inhatc.localit.api.home;

import java.util.List;

public class CourseIntroResponse {
    public ResponseBody response;

    public static class ResponseBody { public Body body; }
    public static class Body { public Items items; }
    public static class Items { public List<CourseIntroItem> item; }

    public static class CourseIntroItem {
        public String distance; // 40km
        public String schedule; // 1박2일
        public String taketime; // 약 1시간 15분
        public String theme;    // ----지자체-----
    }
}
