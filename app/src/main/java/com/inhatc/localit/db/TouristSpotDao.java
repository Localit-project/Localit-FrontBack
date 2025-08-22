package com.inhatc.localit.db; // 본인의 패키지 이름으로 변경하세요

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * 데이터베이스에 접근하는 함수들을 정의하는 인터페이스 (Data Access Object)
 */
@Dao
public interface TouristSpotDao {

    /**
     * 새로운 관광지 데이터를 삽입합니다.
     * onConflict = OnConflictStrategy.REPLACE는
     * 만약 동일한 ID의 데이터가 이미 존재하면 새로 받은 데이터로 덮어쓰는 옵션입니다.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TouristSpot spot);

    /**
     * 기존 관광지 데이터의 내용을 업데이트합니다.
     */
    @Update
    void updateSpot(TouristSpot spot);

    /**
     * 'isWished'가 true인, 즉 찜한 모든 관광지 목록을 가져옵니다.
     */
    // ▼▼▼▼▼ 'name' -> 'title'로 수정 ▼▼▼▼▼
    @Query("SELECT * FROM tourist_spots WHERE isWished = 1 ORDER BY title ASC")
    LiveData<List<TouristSpot>> getWishedSpots();

    /**
     * 데이터베이스에 있는 모든 관광지 목록을 가져옵니다.
     */
    // ▼▼▼▼▼ 'name' -> 'title'로 수정 ▼▼▼▼▼
    @Query("SELECT * FROM tourist_spots ORDER BY title ASC")
    LiveData<List<TouristSpot>> getAllSpots();

    /**
     * contentid를 기반으로 특정 관광지 데이터를 찾습니다.
     * 찜 여부를 확인할 때 사용됩니다. LIMIT 1은 결과를 하나로 제한합니다.
     * @param contentid API에서 제공하는 고유 ID
     * @return contentid에 해당하는 TouristSpot 객체. 없으면 null을 반환합니다.
     */
    // ▼▼▼▼▼ 'contentId' -> 'contentid'로 수정 ▼▼▼▼▼
    @Query("SELECT * FROM tourist_spots WHERE contentid = :contentid LIMIT 1")
    TouristSpot getSpotByContentId(String contentid);

    /**
     * 주어진 TouristSpot 객체를 데이터베이스에서 삭제합니다.
     * 찜 취소 기능에 사용됩니다.
     * @param spot 삭제할 관광지 객체
     */
    @Delete
    void delete(TouristSpot spot);
}
