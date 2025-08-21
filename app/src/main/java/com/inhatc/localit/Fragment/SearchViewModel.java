package com.inhatc.localit.Fragment;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.inhatc.localit.api.SpotResponse;
import com.inhatc.localit.db.TouristSpot;
import com.inhatc.localit.db.TouristSpotRepository;

import java.util.List;

/**
 * SearchFragment의 UI 상태와 데이터를 관리하는 ViewModel.
 * UI와 데이터 로직(Repository)을 분리하는 역할을 합니다.
 */
public class SearchViewModel extends AndroidViewModel {

    // 데이터 처리를 담당하는 Repository
    private final TouristSpotRepository mRepository;
    // DB에 저장된 모든 '찜' 목록을 담고 있는 LiveData
    private final LiveData<List<TouristSpot>> mAllWishedSpots;

    public SearchViewModel(@NonNull Application application) {
        super(application);
        // ViewModel이 생성될 때 Repository를 초기화하고,
        // Repository로부터 찜 목록 LiveData를 가져옵니다.
        mRepository = new TouristSpotRepository(application);
        mAllWishedSpots = mRepository.getWishedSpots();
    }

    /**
     * Fragment가 UI를 업데이트하기 위해 관찰(observe)할 찜 목록 LiveData를 반환합니다.
     * 이 LiveData는 DB 내용이 바뀌면 자동으로 최신 데이터를 발행합니다.
     */
    public LiveData<List<TouristSpot>> getAllWishedSpots() {
        return mAllWishedSpots;
    }

    /**
     * Fragment로부터 찜 상태를 변경해달라는 요청을 받으면,
     * 실제 데이터 처리 로직을 가지고 있는 Repository에 작업을 위임합니다.
     * @param apiItem 사용자가 클릭한 API 아이템 정보
     */
    public void toggleFavorite(SpotResponse.Item apiItem) {
        mRepository.toggleFavoriteStatus(apiItem);
    }
}