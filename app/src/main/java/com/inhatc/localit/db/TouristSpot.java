package com.inhatc.localit.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tourist_spots")
public class TouristSpot {

    /**
     * API에서 제공하는 contentid를 기본 키(Primary Key)로 사용합니다.
     * API ID는 고유하므로 자동 생성이 필요 없습니다. @NonNull로 null이 아님을 보장합니다.
     */
    @PrimaryKey
    @NonNull
    public String contentId;

    public String name;
    public String address;
    public String imageUrl;
    public boolean isWished = false;

    // ✨ 1. 타입(12:관광지, 15:축제 등)을 저장할 변수 추가
    public int contentTypeId;

    // Room이 사용하는 기본 생성자
    public TouristSpot() {
        this.contentId = ""; // NonNull 필드는 초기화가 필요합니다.
    }

    // ✨ 2. Repository에서 사용할 생성자에 contentTypeId 파라미터 추가
    public TouristSpot(@NonNull String contentId, String name, String address, String imageUrl, boolean isWished, int contentTypeId) {
        this.contentId = contentId;
        this.name = name;
        this.address = address;
        this.imageUrl = imageUrl;
        this.isWished = isWished;
        this.contentTypeId = contentTypeId; // ✨ 3. 생성자에서 값 할당
    }
}