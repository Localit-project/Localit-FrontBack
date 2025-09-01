// CourseIntroResponse.java
package com.inhatc.localit.api.home;

import java.util.List;

public class CourseIntroResponse {
    public R response;
    public static class R { public B body; }
    public static class B { public Items items; }
    public static class Items { public List<Item> item; }

    public static class Item {
        public String contentid, contenttypeid;
        public String distance, schedule, taketime, theme;
    }
}
