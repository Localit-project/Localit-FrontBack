package com.inhatc.localit.api;

import java.util.List;

public class SpotResponse {
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
        public String addr2;
        public String firstimage;
        public String createdtime;
        // 축제
        public String eventstartdate;
        public String eventenddate;

        // 어댑터에서 관광지/축제 구분용
        private int localContentType;

        // ===== Getter & Setter =====
        public String getContentid() {
            return contentid;
        }

        public String getTitle() {
            return title;
        }

        public String getAddr1() {
            return addr1;
        }
        public String getAddr2() {
            return addr2;
        }


        public String getFirstimage() {
            return firstimage;
        }

        public String getCreatedtime() {
            return createdtime;
        }

        public String getEventstartdate() {
            return eventstartdate;
        }

        public String getEventenddate() {
            return eventenddate;
        }

        public int getLocalContentType() {
            return localContentType;
        }

        public void setLocalContentType(int localContentType) {
            this.localContentType = localContentType;
        }
    }
}