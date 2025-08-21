package com.inhatc.localit.db;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.inhatc.localit.api.SpotResponse;

import java.util.List;

public class TouristSpotRepository {

    private final TouristSpotDao mTouristSpotDao;
    private final LiveData<List<TouristSpot>> mWishedSpots;
    private final LiveData<List<TouristSpot>> mAllSpots;

    /**
     * 생성자에서 데이터베이스 인스턴스와 DAO를 가져옵니다.
     * Application 객체를 받아 앱의 생명주기와 분리하여 안전하게 사용합니다.
     */
    public TouristSpotRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mTouristSpotDao = db.touristSpotDao();
        mWishedSpots = mTouristSpotDao.getWishedSpots();
        mAllSpots = mTouristSpotDao.getAllSpots();
    }

    /**
     * ViewModel이 관찰할 '찜한 목록' LiveData를 반환합니다.
     */
    public LiveData<List<TouristSpot>> getWishedSpots() {
        return mWishedSpots;
    }

    /**
     * 찜 상태를 토글(추가 또는 삭제)하는 메인 로직입니다.
     * 이 함수는 백그라운드 스레드에서 안전하게 실행됩니다.
     * @param apiItem 사용자가 클릭한 API 아이템 정보
     */
    public void toggleFavoriteStatus(SpotResponse.Item apiItem) {
        if (apiItem == null || apiItem.getContentid() == null) {
            return;
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            TouristSpot spotInDb = mTouristSpotDao.getSpotByContentId(apiItem.getContentid());

            if (spotInDb == null) {
                // contenttypeid를 int로 변환 (API 응답이 숫자가 아닌 문자인 경우를 대비)
                int typeId = 12; // 기본값: 관광지
                try {
                    typeId = Integer.parseInt(apiItem.getContenttypeid());
                } catch (NumberFormatException e) {
                    // 숫자 변환 실패 시 로그를 남기거나 기본값을 사용
                }

                TouristSpot newSpot = new TouristSpot(
                        apiItem.getContentid(),
                        apiItem.getTitle(),
                        apiItem.getAddr1(),
                        apiItem.getFirstimage(),
                        true,
                        typeId // ✨ 안전하게 변환된 타입 ID 사용
                );
                mTouristSpotDao.insert(newSpot);
            } else {
                mTouristSpotDao.delete(spotInDb);
            }
        });
    }
}