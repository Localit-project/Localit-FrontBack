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
<<<<<<< HEAD
        // ✨ 1. API 응답에 포함된 contenttypeid 필드 추가
        public String contenttypeid;

        public String contentid;
=======
        // 기본 필드
        public String contentid;        // 콘텐츠 ID
        public String contenttypeid;    //콘텐츠 유형 ID (관광=12, 축제=15 등)
>>>>>>> 1513c0ab18a3a890083dbb438950b8a3dccbdd87
        public String title;
        public String addr1;
        public String addr2;
        public String firstimage;
        public String createdtime;

        // 축제 전용
        public String eventstartdate;
        public String eventenddate;

        // 어댑터에서 관광지/축제 구분용(로컬)
        private int localContentType;

        // ===== Getter & Setter =====
<<<<<<< HEAD

        // ✨ 2. contenttypeid의 getter 메서드 추가
        public String getContenttypeid() {
            return contenttypeid;
        }

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
=======
        public String getContentid() { return contentid; }
        public String getContenttypeid() { return contenttypeid; } // << 추가 게터
        public String getTitle() { return title; }
        public String getAddr1() { return addr1; }
        public String getAddr2() { return addr2; }
        public String getFirstimage() { return firstimage; }
        public String getCreatedtime() { return createdtime; }
        public String getEventstartdate() { return eventstartdate; }
        public String getEventenddate() { return eventenddate; }

        public int getLocalContentType() { return localContentType; }
        public void setLocalContentType(int localContentType) { this.localContentType = localContentType; }
>>>>>>> 1513c0ab18a3a890083dbb438950b8a3dccbdd87
    }
}