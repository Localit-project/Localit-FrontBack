package com.inhatc.localit.api.home;

import android.util.Log;

import com.inhatc.localit.BuildConfig;
import com.inhatc.localit.api.ApiClient;
import com.inhatc.localit.api.SpotApiService;
import com.inhatc.localit.api.SpotDetailCommonResponse;
import com.inhatc.localit.api.SpotResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TourApiHelper {

    private static final String TAG = "TourApiHelper";
    private static final String SERVICE_KEY = BuildConfig.TOUR_API_KEY;

    // [수정됨] 앱 전체에서 사용하는 ApiClient를 통해 SpotApiService 인스턴스를 가져옵니다.
    private static final SpotApiService api = ApiClient.getInstance().create(SpotApiService.class);


    // ================== 홈: 전국 카드 2개 ==================

    /** 전국 관광지 2개 (contentTypeId=12) */
    public static void fetchHomeSpots(Consumer<List<TourItem>> cb) {
        // [수정됨] 최신 API(KorService2)의 getTourList 메서드를 호출합니다.
        Call<SpotResponse> call = api.getTourList(
                2, 1, "AND", "Localit",
                "D",      // 최신순 정렬
                12,       // 관광지 타입
                1,        // 지역코드: 1 (서울) - 필요시 변경
                null,
                "json",
                SERVICE_KEY
        );
        enqueueItems(call, cb);
    }

    /** 전국 축제 2개 (contentTypeId=15) */
    public static void fetchHomeFestivals(Consumer<List<TourItem>> cb) {
        // [수정됨] 최신 API(KorService2)의 getFestivalList 메서드를 호출합니다.
        // 이 예시에서는 시작일이 필요 없으므로 null을 전달하거나, 오늘 날짜를 전달할 수 있습니다.
        Call<SpotResponse> call = api.getFestivalList(
                2, 1, "AND", "Localit", "json",
                1,        // 지역코드: 1 (서울) - 필요시 변경
                null,
                null,     // 시작일 (전체 대상)
                "D",      // 최신순 정렬
                SERVICE_KEY
        );
        enqueueItems(call, cb);
    }

    // ================== 여행코스 요약: contentId로 채우기 ==================

    /**
     * 코스 contentId 리스트로 제목/대표이미지/주소 등 요약 수집
     * - [수정됨] detailCommon2(contentTypeId=25)로 개별 조회
     * - 결과 순서는 입력 contentIds 순서를 유지
     */
    public static void fetchCourseSummaries(List<String> contentIds, Consumer<List<TourItem>> cb) {
        if (contentIds == null || contentIds.isEmpty()) {
            cb.accept(Collections.emptyList());
            return;
        }

        List<TourItem> out = new ArrayList<>(Collections.nCopies(contentIds.size(), null));
        AtomicInteger done = new AtomicInteger(0);

        for (int idx = 0; idx < contentIds.size(); idx++) {
            final int pos = idx;
            final String cid = contentIds.get(idx);

            // [수정됨] 최신 API(KorService2)의 getDetailCommonFull 메서드를 호출합니다.
            api.getDetailCommonFull(
                    "AND", "Localit", "json",
                    cid,
                    "Y", // defaultYN
                    "Y", // firstImageYN
                    "Y", // areacodeYN
                    "Y", // catcodeYN
                    "Y", // addrinfoYN
                    "Y", // mapinfoYN
                    "Y", // overviewYN
                    SERVICE_KEY
            ).enqueue(new Callback<SpotDetailCommonResponse>() {
                @Override
                public void onResponse(Call<SpotDetailCommonResponse> call, Response<SpotDetailCommonResponse> res) {
                    TourItem t = new TourItem();
                    t.contentid = cid; // fallback
                    try {
                        // [수정됨] 응답 데이터 구조를 SpotDetailCommonResponse에 맞게 수정
                        if (res.isSuccessful() && res.body() != null &&
                                res.body().response != null &&
                                res.body().response.body != null &&
                                res.body().response.body.items != null &&
                                res.body().response.body.items.item != null &&
                                !res.body().response.body.items.item.isEmpty()) {
                            SpotDetailCommonResponse.Item it = res.body().response.body.items.item.get(0);
                            if (it != null) {
                                t.title = safe(it.title);
                                t.firstimage = safe(it.firstimage);
                                t.addr1 = safe(it.addr1);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "parse course detail failed", e);
                    }
                    out.set(pos, t);
                    if (done.incrementAndGet() == contentIds.size()) cb.accept(out);
                }

                @Override
                public void onFailure(Call<SpotDetailCommonResponse> call, Throwable t) {
                    Log.e(TAG, "course detail fail: " + cid, t);
                    out.set(pos, new TourItem()); // 빈값
                    out.get(pos).contentid = cid;
                    if (done.incrementAndGet() == contentIds.size()) cb.accept(out);
                }
            });
        }
    }

    // ================== 내부 공통 ==================

    private static void enqueueItems(Call<SpotResponse> call, Consumer<List<TourItem>> cb) {
        call.enqueue(new Callback<SpotResponse>() {
            @Override
            public void onResponse(Call<SpotResponse> c, Response<SpotResponse> r) {
                List<TourItem> list = new ArrayList<>();
                try {
                    // [수정됨] 응답 데이터 구조를 SpotResponse에 맞게 수정
                    if (r.isSuccessful() && r.body() != null &&
                            r.body().response != null &&
                            r.body().response.body != null &&
                            r.body().response.body.items != null &&
                            r.body().response.body.items.item != null) {

                        // SpotResponse.Item 리스트를 TourItem 리스트로 변환
                        for (SpotResponse.Item spotItem : r.body().response.body.items.item) {
                            TourItem tourItem = new TourItem();
                            tourItem.contentid = safe(spotItem.contentid);
                            tourItem.title = safe(spotItem.title);
                            tourItem.addr1 = safe(spotItem.addr1);
                            tourItem.firstimage = safe(spotItem.firstimage);
                            list.add(tourItem);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "parse list error", e);
                }
                cb.accept(list);
            }

            @Override
            public void onFailure(Call<SpotResponse> c, Throwable t) {
                Log.e(TAG, "API list fail", t);
                cb.accept(new ArrayList<>());
            }
        });
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
