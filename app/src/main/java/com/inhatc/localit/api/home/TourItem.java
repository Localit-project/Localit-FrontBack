package com.inhatc.localit.api.home;

/** 리스트 공통 아이템(간단 정보) */
public class TourItem {
    public String contentid;
    public String contenttypeid;   // "15"(축제), "25"(코스) 등
    public String title;

    public String addr1;
    public String addr2;

    public String firstimage;
    public String firstimage2;

    public String eventstartdate;  // 축제 시작일(yyyyMMdd)
    public String eventenddate;    // 축제 종료일(yyyyMMdd)

    public Double mapx;            // 경도
    public Double mapy;            // 위도

    public String overview;        // 개요(상세 공통에서 채워짐)
}
