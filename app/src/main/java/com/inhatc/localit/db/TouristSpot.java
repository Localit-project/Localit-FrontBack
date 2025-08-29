package com.inhatc.localit.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;
import androidx.room.TypeConverters;

import java.util.Date;

/**
 * 관광지 / 축제 정보를 저장하는 Room 엔티티
 */
@Entity(tableName = "tourist_spots")
public class TouristSpot {

    @PrimaryKey
    @NonNull
    public String contentid;

    public String title;
    public String addr1;
    public String firstimage;
    public boolean isWished = false;
    public int contenttypeid;

    // 축제 관련
    @TypeConverters({Converters.class})
    public Date startDate;   // 축제 시작일
    @TypeConverters({Converters.class})
    public Date endDate;     // 축제 종료일
    public boolean isFestival = false;

    // 관광지 새로 등록 여부
    public boolean isNew = false;

    // Room용 기본 생성자
    public TouristSpot() {
        this.contentid = "";
    }

    // 기존 6개 인자용 생성자
    @Ignore
    public TouristSpot(@NonNull String contentid, String title, String addr1,
                       String firstimage, boolean isWished, int contenttypeid) {
        this.contentid = contentid;
        this.title = title;
        this.addr1 = addr1;
        this.firstimage = firstimage;
        this.isWished = isWished;
        this.contenttypeid = contenttypeid;
        this.startDate = null;
        this.endDate = null;
        this.isFestival = false;
        this.isNew = false;
    }

    // 모든 필드용 생성자
    @Ignore
    public TouristSpot(@NonNull String contentid, String title, String addr1,
                       String firstimage, boolean isWished, int contenttypeid,
                       Date startDate, Date endDate, boolean isFestival, boolean isNew) {
        this.contentid = contentid;
        this.title = title;
        this.addr1 = addr1;
        this.firstimage = firstimage;
        this.isWished = isWished;
        this.contenttypeid = contenttypeid;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isFestival = isFestival;
        this.isNew = isNew;
    }

    public String getContentid() {
        return contentid;
    }
}