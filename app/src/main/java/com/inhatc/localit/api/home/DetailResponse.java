// DetailResponse.java
package com.inhatc.localit.api.home;

import java.util.List;

public class DetailResponse {
    public R response;
    public static class R { public B body; }
    public static class B { public Items items; }
    public static class Items { public List<Item> item; }

    public static class Item {
        public String contentid, contenttypeid, title, overview;
        public String addr1, addr2, zipcode, firstimage, firstimage2;
        public Double mapx, mapy;
    }
}
