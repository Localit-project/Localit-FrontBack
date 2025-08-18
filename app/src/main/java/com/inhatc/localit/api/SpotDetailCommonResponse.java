
package com.inhatc.localit.api;

import java.util.List;

public class SpotDetailCommonResponse {
    public Response response;
    public static class Response { public Body body; }
    public static class Body { public Items items; }
    public static class Items { public List<Item> item; }

    public static class Item {
        public String homepage;
        public String overview;
    }
}