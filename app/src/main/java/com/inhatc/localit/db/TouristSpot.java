package com.inhatc.localit.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

@Entity(tableName = "tourist_spots")
public class TouristSpot {

    @PrimaryKey
    @NonNull
    // API와 이름을 통일 (contentId -> contentid)
    public String contentid;

    // API와 이름을 통일 (name -> title, address -> addr1, imageUrl -> firstimage)
    public String title;
    public String addr1;
    public String firstimage;
    public boolean isWished = false;
    public int contenttypeid;

    // Room이 사용할 기본 생성자
    public TouristSpot() {
        this.contentid = ""; // NonNull 필드는 초기화 필요
    }

    // Repository 등에서 사용할 생성자
    @Ignore
    public TouristSpot(@NonNull String contentid, String title, String addr1,
                       String firstimage, boolean isWished, int contenttypeid) {
        this.contentid = contentid;
        this.title = title;
        this.addr1 = addr1;
        this.firstimage = firstimage;
        this.isWished = isWished;
        this.contenttypeid = contenttypeid;
    }

    // 외부에서 contentid를 안전하게 가져갈 수 있도록 Getter 추가
    public String getContentid() {
        return contentid;
    }
}
