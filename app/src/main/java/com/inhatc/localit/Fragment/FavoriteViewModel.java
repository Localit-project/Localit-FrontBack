package com.inhatc.localit.Fragment;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.inhatc.localit.db.TouristSpot;
import com.inhatc.localit.db.TouristSpotRepository;
import java.util.List;

/**
 * FavoriteFragment의 UI 상태와 데이터를 관리하는 ViewModel입니다.
 * FavoriteFragment는 이 ViewModel을 통해 '찜한 목록' 데이터에만 접근합니다.
 */
public class FavoriteViewModel extends AndroidViewModel {

    // 데이터 처리를 담당하는 Repository
    private final TouristSpotRepository mRepository;

    // DB에 저장된 '찜' 목록을 담고 있는 LiveData
    private final LiveData<List<TouristSpot>> mWishedSpots;

    public FavoriteViewModel(@NonNull Application application) {
        super(application);
        // ViewModel이 생성될 때 Repository를 초기화하고,
        // Repository로부터 찜 목록 LiveData를 가져와 변수에 할당합니다.
        mRepository = new TouristSpotRepository(application);
        mWishedSpots = mRepository.getWishedSpots();
    }

    /**
     * Fragment가 UI를 업데이트하기 위해 관찰(observe)할 찜 목록 LiveData를 반환합니다.
     */
    public LiveData<List<TouristSpot>> getWishedSpots() {
        return mWishedSpots;
    }

    /**
     * 찜 목록에서 아이템을 삭제하도록 Repository에 요청합니다.
     * @param spot 삭제할 TouristSpot 객체
     */
    public void removeWishedSpot(TouristSpot spot) {
        mRepository.delete(spot);
    }

    // ▼▼▼▼▼ [추가된 부분] ▼▼▼▼▼
    /**
     * 찜 목록에 아이템을 다시 추가하도록 Repository에 요청합니다. (Undo 기능)
     * @param spot 추가할 TouristSpot 객체
     */
    public void addWishedSpot(TouristSpot spot) {
        // Repository에 구현된 insert 메서드를 호출합니다.
        mRepository.insert(spot);
    }
    // ▲▲▲▲▲ [추가된 부분] ▲▲▲▲▲
}