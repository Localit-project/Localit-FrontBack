package com.inhatc.localit.api.home;

import java.util.List;

// ApiResponse.java (필요 필드만)
public class ApiResponse {
    public ResponseBody response;
    public static class ResponseBody {
        public Body body;
    }
    public static class Body {
        public Items items;
    }
    public static class Items {
        public List<TourItem> item; // 실제 리스트
    }
}
