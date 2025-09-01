// CourseInfoResponse.java
package com.inhatc.localit.api.home;

import java.util.List;

public class CourseInfoResponse {
    public R response;
    public static class R { public B body; }
    public static class B { public Items items; }
    public static class Items { public List<Item> item; }

    // 코스 단계(서브)
    public static class Item {
        public String contentid, contenttypeid;
        public Integer subnum;
        public String subcontentid, subname, subdetailoverview, subdetailimg, subdetailalt;
    }
}
