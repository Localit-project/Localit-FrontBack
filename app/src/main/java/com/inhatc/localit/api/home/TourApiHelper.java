package com.inhatc.localit.api.home;

import android.util.Log;

import com.inhatc.localit.BuildConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TourApiHelper {

    private static final String TAG = "TourApiHelper";

    private static final String BASE_URL = "https://apis.data.go.kr/B551011/KorService1/";
    // ⚠ 절대 디코딩하지 말고 원본 그대로 사용
    private static final String SERVICE_KEY = BuildConfig.TOUR_API_KEY;

    private static final ApiService api;

    static {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(ApiService.class);
    }

    // ================== 홈: 전국 카드 2개 ==================

    /** 전국 관광지 2개 (contentTypeId=12) */
    public static void fetchHomeSpots(Consumer<List<TourItem>> cb) {
        Call<ApiResponse> call = api.getAreaBasedList(
                SERVICE_KEY, "AND", "Localit", "json",
                12,           // 관광지
                "Y", "Y",
                2, 1,         // 2개
                "D",          // 최신 정렬
                null          // 전국
        );
        enqueueItems(call, cb);
    }

    /** 전국 축제 2개 (contentTypeId=15) */
    public static void fetchHomeFestivals(Consumer<List<TourItem>> cb) {
        Call<ApiResponse> call = api.getAreaBasedList(
                SERVICE_KEY, "AND", "Localit", "json",
                15,           // 축제/공연/행사
                "Y", "Y",
                2, 1,
                "D",
                null          // 전국
        );
        enqueueItems(call, cb);
    }

    // ================== 여행코스 요약: contentId로 채우기 ==================

    /**
     * 코스 contentId 리스트로 제목/대표이미지/주소 등 요약 수집
     * - detailCommon1(contentTypeId=25)로 개별 조회
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

            api.getCourseDetailCommon(
                    SERVICE_KEY, "AND", "Localit", "json",
                    cid, 25,
                    "Y", "Y", "Y", "Y", "Y"
            ).enqueue(new Callback<DetailResponse>() {
                @Override public void onResponse(Call<DetailResponse> call, Response<DetailResponse> res) {
                    TourItem t = new TourItem();
                    t.contentid = cid; // fallback
                    try {
                        if (res.isSuccessful() && res.body()!=null &&
                                res.body().response!=null &&
                                res.body().response.body!=null &&
                                res.body().response.body.items!=null &&
                                res.body().response.body.items.item!=null &&
                                !res.body().response.body.items.item.isEmpty()) {
                            DetailResponse.DetailItem it = res.body().response.body.items.item.get(0);
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
                @Override public void onFailure(Call<DetailResponse> call, Throwable t) {
                    Log.e(TAG, "course detail fail: " + cid, t);
                    out.set(pos, new TourItem()); // 빈값
                    out.get(pos).contentid = cid;
                    if (done.incrementAndGet() == contentIds.size()) cb.accept(out);
                }
            });
        }
    }

    // ================== 내부 공통 ==================

    private static void enqueueItems(Call<ApiResponse> call, Consumer<List<TourItem>> cb) {
        call.enqueue(new Callback<ApiResponse>() {
            @Override public void onResponse(Call<ApiResponse> c, Response<ApiResponse> r) {
                List<TourItem> list = new ArrayList<>();
                try {
                    if (r.isSuccessful() && r.body()!=null &&
                            r.body().response!=null &&
                            r.body().response.body!=null &&
                            r.body().response.body.items!=null &&
                            r.body().response.body.items.item!=null) {
                        list = r.body().response.body.items.item;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "parse list error", e);
                }
                cb.accept(list);
            }
            @Override public void onFailure(Call<ApiResponse> c, Throwable t) {
                Log.e(TAG, "API list fail", t);
                cb.accept(new ArrayList<>());
            }
        });
    }

    private static String safe(String s) { return s == null ? "" : s; }
}
