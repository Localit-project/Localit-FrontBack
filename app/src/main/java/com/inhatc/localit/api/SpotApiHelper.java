package com.inhatc.localit.api;

import java.util.*;

// 이미 있는 클래스에 아래 내용만 보강
public class SpotApiHelper {

    // 이미 있는 줄: Retrofit 서비스 싱글턴
    private static final SpotApiService apiService =
            RetrofitClient.getInstance().create(SpotApiService.class);

    public static SpotApiService getApiService() {
        return apiService;
    }


    public static final String SERVICE_KEY =
            "wL/Ry8EMiMg43mPRl3wyQhKosVExsJbLLDcZebat4S4eedobtNuBG+eqrj5GPKHvEAxy4NjYPz25Parbyeg8PA==";

    // --- (선택) 지역/시군구 유틸: SearchFragment에서 필요하면 사용 ---
    private static final Map<String, Integer> AREA_CODE_MAP = new HashMap<>();
    private static final Map<String, String> REGION_ALIAS = new HashMap<>();
    private static final Map<String, Integer> GG_SIGUNGU = new HashMap<>();

    static {
        AREA_CODE_MAP.put("서울특별시", 1);
        AREA_CODE_MAP.put("부산광역시", 6);
        AREA_CODE_MAP.put("대구광역시", 4);
        AREA_CODE_MAP.put("인천광역시", 2);
        AREA_CODE_MAP.put("광주광역시", 5);
        AREA_CODE_MAP.put("대전광역시", 3);
        AREA_CODE_MAP.put("울산광역시", 7);
        AREA_CODE_MAP.put("세종특별자치시", 8);
        AREA_CODE_MAP.put("경기도", 31);
        AREA_CODE_MAP.put("강원특별자치도", 32);
        AREA_CODE_MAP.put("충청북도", 33);
        AREA_CODE_MAP.put("충청남도", 34);
        AREA_CODE_MAP.put("전라북도", 35);
        AREA_CODE_MAP.put("전라남도", 36);
        AREA_CODE_MAP.put("경상북도", 37);
        AREA_CODE_MAP.put("경상남도", 38);
        AREA_CODE_MAP.put("제주특별자치도", 39);

        alias("서울특별시", "서울", "서울시");
        alias("부산광역시", "부산", "부산시");
        alias("대구광역시", "대구", "대구시");
        alias("인천광역시", "인천", "인천시");
        alias("광주광역시", "광주", "광주시");
        alias("대전광역시", "대전", "대전시");
        alias("울산광역시", "울산", "울산시");
        alias("세종특별자치시", "세종", "세종시");
        alias("경기도", "경기");
        alias("강원특별자치도", "강원", "강원도");
        alias("충청북도", "충북");
        alias("충청남도", "충남");
        alias("전라북도", "전북");
        alias("전라남도", "전남");
        alias("경상북도", "경북");
        alias("경상남도", "경남");
        alias("제주특별자치도", "제주", "제주도");

        // 경기도 시군구(예시)
        GG_SIGUNGU.put("수원시", 13);
        GG_SIGUNGU.put("성남시", 12);
        GG_SIGUNGU.put("고양시", 2);
        GG_SIGUNGU.put("용인시", 23);
        GG_SIGUNGU.put("안산시", 15);
        GG_SIGUNGU.put("안양시", 17);
        GG_SIGUNGU.put("부천시", 11);
        GG_SIGUNGU.put("화성시", 31);
        GG_SIGUNGU.put("남양주시", 9);
        GG_SIGUNGU.put("평택시", 28);
        GG_SIGUNGU.put("의정부시", 25);
        GG_SIGUNGU.put("시흥시", 14);
        GG_SIGUNGU.put("파주시", 27);
        GG_SIGUNGU.put("김포시", 8);
        GG_SIGUNGU.put("광명시", 4);
        GG_SIGUNGU.put("군포시", 7);
        GG_SIGUNGU.put("광주시", 5);
        GG_SIGUNGU.put("하남시", 30);
        GG_SIGUNGU.put("오산시", 22);
        GG_SIGUNGU.put("이천시", 26);
        GG_SIGUNGU.put("안성시", 16);
        GG_SIGUNGU.put("구리시", 6);
        GG_SIGUNGU.put("의왕시", 24);
        GG_SIGUNGU.put("여주시", 20);
        GG_SIGUNGU.put("양평군", 19);
        GG_SIGUNGU.put("동두천시", 10);
        GG_SIGUNGU.put("과천시", 3);
        GG_SIGUNGU.put("포천시", 29);
        GG_SIGUNGU.put("연천군", 21);
        GG_SIGUNGU.put("가평군", 1);
        GG_SIGUNGU.put("양주시", 18);
    }

    private static void alias(String standard, String... aliases) {
        REGION_ALIAS.put(clean(standard), standard);
        for (String a : aliases) REGION_ALIAS.put(clean(a), standard);
    }
    private static String clean(String s) { return s == null ? "" : s.replaceAll("\\s+", ""); }
    private static String stripSuffix(String k) {
        return k.replaceAll("(광역시|특별자치시|특별자치도|특별시|자치시|자치도|시|도)$", "");
    }

    public static int getAreaCode(String region) {
        if (region == null || region.trim().isEmpty()) return 1;
        String key = clean(region);
        String standard = REGION_ALIAS.get(key);
        if (standard == null) {
            String stripped = stripSuffix(key);
            standard = REGION_ALIAS.get(stripped);
        }
        if (standard == null && AREA_CODE_MAP.containsKey(region)) {
            standard = region;
        }
        if (standard == null) {
            standard = "서울특별시";
        }
        Integer code = AREA_CODE_MAP.get(standard);
        return code != null ? code : 1;
    }

    public static Integer getGgSigungu(String region, String sub) {
        if (!"경기도".equals(region)) return null;
        if (sub == null) return null;
        String s = sub.trim();
        if (s.isEmpty() || "전체".equals(s)) return null;
        return GG_SIGUNGU.get(s);
    }
}