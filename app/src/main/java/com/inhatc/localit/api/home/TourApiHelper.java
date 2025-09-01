package com.inhatc.localit.api.home;

import android.util.Log;
import androidx.annotation.Nullable;

import com.inhatc.localit.BuildConfig;
import com.inhatc.localit.api.ApiClient;
import com.inhatc.localit.api.SpotApiService;
import com.inhatc.localit.api.SpotDetailCommonResponse;
import com.inhatc.localit.api.SpotDetailImageResponse;
import com.inhatc.localit.api.SpotDetailInfoResponse;
import com.inhatc.localit.api.SpotDetailIntroResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 홈 고정 코스 7개용 Tour API 헬퍼 (KorService2)
 * - 서비스 키는 원문 그대로 사용 (디코딩/인코딩 금지)
 * - SpotApiHelper와 동일 스타일(로깅, 재시도)로 구현
 */
public final class TourApiHelper {

    private TourApiHelper() {}

    private static final String TAG = "TourApiHelper";
    private static final SpotApiService API = ApiClient.getInstance().create(SpotApiService.class);

    private static final String OS   = "AND";
    private static final String APP  = "localit";
    private static final String TYPE = "json";

    private static final String SERVICE_KEY = BuildConfig.TOUR_API_KEY; // URLDecoder 절대 금지

    // 콜백 타입
    public interface SimpleCallback<T> { void onResult(@Nullable T value); }

    // =============================================================================================
    // 1) 홈 코스 요약: contentId 리스트를 받아 title/firstimage/overview/addr만 추출
    //    - API: detailCommon2 (우선 최소 파라미터 → 비면 확장 파라미터 재시도)
    // =============================================================================================
    public static void fetchCourseSummaries(List<String> contentIds,
                                            SimpleCallback<List<TourItem>> cb) {
        if (contentIds == null || contentIds.isEmpty()) {
            cb.onResult(Collections.emptyList());
            return;
        }

        List<TourItem> acc = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger remain = new AtomicInteger(contentIds.size());

        for (String id : contentIds) {
            fetchOneCourseSummary(id, item -> {
                if (item != null) acc.add(item);
                if (remain.decrementAndGet() == 0) {
                    // 입력 순서 유지하고 싶으면 여기서 contentIds 순서대로 정렬
                    List<TourItem> ordered = new ArrayList<>();
                    for (String cid : contentIds) {
                        for (TourItem t : acc) {
                            if (cid.equals(t.contentid)) {
                                ordered.add(t);
                                break;
                            }
                        }
                    }
                    cb.onResult(ordered);
                }
            });
        }
    }

    private static void fetchOneCourseSummary(String contentId, SimpleCallback<TourItem> cb) {
        // 1차: 최소 파라미터(defaultYN, overviewYN)
        API.getDetailCommonMinimal(
                OS, APP, TYPE,
                contentId,
                "Y",    // defaultYN
                "Y",            // overviewYN
                BuildConfig.TOUR_API_KEY // serviceKey  ← 마지막!
        ).enqueue(new Callback<SpotDetailCommonResponse>() {
            @Override public void onResponse(Call<SpotDetailCommonResponse> call,
                                             Response<SpotDetailCommonResponse> resp) {
                if (resp.isSuccessful()) {
                    SpotDetailCommonResponse.Item it = extractFirst(resp.body());
                    if (it != null) {
                        cb.onResult(mapToTourItem(contentId, it));
                        return;
                    }
                    // 성공/빈결과 → 확장 재시도
                    retryFull(contentId, cb);
                } else {
                    logError("DETAIL_COMMON_MIN_ERR", resp);
                    retryFull(contentId, cb);
                }
            }
            @Override public void onFailure(Call<SpotDetailCommonResponse> call, Throwable t) {
                Log.e(TAG, "detailCommon(min) fail id=" + contentId, t);
                retryFull(contentId, cb);
            }
        });
    }

    private static void retryFull(String contentId, SimpleCallback<TourItem> cb) {
        // 2차: 확장 파라미터(이미지/주소/지도 등)
        API.getDetailCommon(
                "AND",                 // MobileOS
                "localit",             // MobileApp
                "json",                // _type
                contentId,             // contentId
                "Y",                   // defaultYN
                25,                    // contentTypeId
                "Y",                   // firstImageYN
                "Y",                   // areacodeYN
                "Y",                   // catcodeYN
                "Y",                   // addrinfoYN
                "Y",                   // mapinfoYN
                "Y",                   // overviewYN
                BuildConfig.TOUR_API_KEY // serviceKey
        ).enqueue(new Callback<SpotDetailCommonResponse>()  {
            @Override public void onResponse(Call<SpotDetailCommonResponse> call,
                                             Response<SpotDetailCommonResponse> resp) {
                if (resp.isSuccessful()) {
                    SpotDetailCommonResponse.Item it = extractFirst(resp.body());
                    cb.onResult(it != null ? mapToTourItem(contentId, it) : null);
                } else {
                    logError("DETAIL_COMMON_FULL_ERR", resp);
                    cb.onResult(null);
                }
            }
            @Override public void onFailure(Call<SpotDetailCommonResponse> call, Throwable t) {
                Log.e(TAG, "detailCommon(full) fail id=" + contentId, t);
                cb.onResult(null);
            }
        });
    }

    private static SpotDetailCommonResponse.Item extractFirst(SpotDetailCommonResponse body) {
        try {
            if (body != null
                    && body.response != null
                    && body.response.body != null
                    && body.response.body.items != null
                    && body.response.body.items.item != null
                    && !body.response.body.items.item.isEmpty()) {
                return body.response.body.items.item.get(0);
            }
        } catch (Exception e) {
            Log.w(TAG, "extractFirst parse fail", e);
        }
        return null;
    }

    private static Double toDouble(String s) {
        try { return s == null || s.trim().isEmpty() ? null : Double.valueOf(s); }
        catch (Exception e) { return null; }
    }

    private static TourItem mapToTourItem(String contentId, SpotDetailCommonResponse.Item it) {
        TourItem t = new TourItem();
        t.contentid     = contentId;
        t.contenttypeid = "25";
        t.title         = safe(it.title);
        t.firstimage    = safe(it.firstimage);
        t.overview      = safe(it.overview);
        t.addr1         = safe(it.addr1);
        t.mapx          = toDouble(it.mapx);   // ← String → Double
        t.mapy          = toDouble(it.mapy);   // ← String → Double
        return t;
    }

    private static String safe(String s) { return s == null ? "" : s; }

    private static void logError(String tag, Response<?> resp) {
        try {
            String err = resp.errorBody() != null ? resp.errorBody().string() : "";
            Log.e(TAG, tag + " http=" + resp.code() + " body=" + err);
        } catch (Exception e) {
            Log.e(TAG, tag + " errorBody read fail", e);
        }
    }

    // =============================================================================================
    // 2) (옵션) 상세 화면에서도 SpotApiHelper 스타일을 그대로 쓰고 싶을 때 쓸 수 있는 래퍼
    //    - CourseDetailActivity가 이미 개별 호출 중이면 아래는 사용 안 해도 됨.
    // =============================================================================================
    public static void fetchDetailCommonForDetail(String contentId,
                                                  SimpleCallback<SpotDetailCommonResponse.Item> cb) {
        fetchOneCourseSummary(contentId, item -> {
            if (cb != null) cb.onResult(item == null ? null : extractFirstFieldAgain(contentId));
        });
    }

    // 실제 상세 화면은 SpotApiHelper를 이미 쓰고 있으니 위 메서드는 보조용입니다.
    // 필요 시 아래처럼 intro/info/images도 같은 패턴으로 래핑 가능:
    public static void fetchDetailIntro(String contentId, SimpleCallback<SpotDetailIntroResponse.Item> cb) {
        API.getDetailIntro(OS, APP, TYPE, contentId, 25, SERVICE_KEY)
                .enqueue(new Callback<SpotDetailIntroResponse>() {
                    @Override public void onResponse(Call<SpotDetailIntroResponse> call,
                                                     Response<SpotDetailIntroResponse> resp) {
                        SpotDetailIntroResponse.Item out = null;
                        try {
                            if (resp.isSuccessful()
                                    && resp.body()!=null
                                    && resp.body().response!=null
                                    && resp.body().response.body!=null
                                    && resp.body().response.body.items!=null
                                    && resp.body().response.body.items.item!=null
                                    && !resp.body().response.body.items.item.isEmpty()) {
                                out = resp.body().response.body.items.item.get(0);
                            } else {
                                logError("DETAIL_INTRO_ERR", resp);
                            }
                        } catch (Exception e) { Log.e(TAG, "intro parse", e); }
                        if (cb != null) cb.onResult(out);
                    }
                    @Override public void onFailure(Call<SpotDetailIntroResponse> call, Throwable t) {
                        Log.e(TAG, "intro fail", t);
                        if (cb != null) cb.onResult(null);
                    }
                });
    }

    public static void fetchDetailInfo(String contentId, SimpleCallback<List<SpotDetailInfoResponse.Item>> cb) {
        API.getDetailInfo(OS, APP, TYPE, contentId, 25, SERVICE_KEY)
                .enqueue(new Callback<SpotDetailInfoResponse>() {
                    @Override public void onResponse(Call<SpotDetailInfoResponse> call,
                                                     Response<SpotDetailInfoResponse> resp) {
                        List<SpotDetailInfoResponse.Item> out = new ArrayList<>();
                        try {
                            if (resp.isSuccessful()
                                    && resp.body()!=null
                                    && resp.body().response!=null
                                    && resp.body().response.body!=null
                                    && resp.body().response.body.items!=null
                                    && resp.body().response.body.items.item!=null) {
                                out = resp.body().response.body.items.item;
                            } else {
                                logError("DETAIL_INFO_ERR", resp);
                            }
                        } catch (Exception e) { Log.e(TAG, "info parse", e); }
                        if (cb != null) cb.onResult(out);
                    }
                    @Override public void onFailure(Call<SpotDetailInfoResponse> call, Throwable t) {
                        Log.e(TAG, "info fail", t);
                        if (cb != null) cb.onResult(new ArrayList<>());
                    }
                });
    }

    public static void fetchDetailImages(String contentId, SimpleCallback<List<String>> cb) {
        API.getDetailImages(
                30, 1,
                OS, APP, TYPE,
                "Y",            // imageYN
                contentId,
                SERVICE_KEY
        ).enqueue(new Callback<SpotDetailImageResponse>() {
            @Override public void onResponse(Call<SpotDetailImageResponse> call,
                                             Response<SpotDetailImageResponse> res) {
                List<String> urls = new ArrayList<>();
                if (!res.isSuccessful()) {
                    logError("DETAIL_IMAGE_ERR", res);
                    if (cb != null) cb.onResult(urls);
                    return;
                }
                try {
                    if (res.body()!=null
                            && res.body().response!=null
                            && res.body().response.body!=null
                            && res.body().response.body.items!=null
                            && res.body().response.body.items.item!=null) {
                        for (SpotDetailImageResponse.Item it : res.body().response.body.items.item) {
                            String u = it.originimgurl != null && !it.originimgurl.isEmpty()
                                    ? it.originimgurl : it.smallimageurl;
                            if (u != null && !u.isEmpty()) urls.add(u);
                        }
                    }
                } catch (Exception e) { Log.e(TAG, "images parse", e); }
                if (cb != null) cb.onResult(urls);
            }
            @Override public void onFailure(Call<SpotDetailImageResponse> call, Throwable t) {
                Log.e(TAG, "images fail", t);
                if (cb != null) cb.onResult(new ArrayList<>());
            }
        });
    }

    // 보조: 위 요약 호출과 별개로 다시 전체 아이템을 원할 때 사용할 수 있도록 한 번 더 호출
    private static SpotDetailCommonResponse.Item extractFirstFieldAgain(String contentId) {
        // 필요시 확장 가능. 현재는 홈 요약 용도가 메인이라 미사용.
        return null;
    }
}
