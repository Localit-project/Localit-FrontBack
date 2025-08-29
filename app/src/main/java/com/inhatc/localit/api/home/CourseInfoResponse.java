package com.inhatc.localit.api.home;

import java.util.List;

public class CourseInfoResponse {
    public ResponseBody response;

    public static class ResponseBody { public Body body; }
    public static class Body { public Items items; }
    public static class Items { public List<CourseInfoItem> item; }

    public static class CourseInfoItem {
        public String subnum;            // 0..N
        public String subcontentid;      // 개별 POI id
        public String subname;           // 코스명
        public String subdetailoverview; // 설명
        public String subdetailimg;      // 이미지 (없을 수도)
        public String subdetailalt;      // 대체텍스트
    }
}
