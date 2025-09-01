// app/src/main/java/com/inhatc/localit/api/home/TourApiHelper.java
package com.inhatc.localit.api.home;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Retrofit 기반 Helper
 * - CourseDetailActivity 에서 쓰는 fetchCourseSummaries(List<String>, cb) 유지
 * - 리스트가 와도 현재는 첫 번째 contentId만 조회 (현 사용처가 단건이기 때문)
 * - defaultYN 제거
 */
public final class TourApiHelper {

    private static final String TAG = "TourApiHelper";

    /** URL-encoded 서비스키를 넣어줘 (예: "wL%2F....%3D%3D") */
    private static final String SERVICE_KEY_ENC = "PUT_YOUR_URL_ENCODED_KEY_HERE";

    private TourApiHelper() {}

    // === 외부에서 쓰는 콜백 시그니처 (람다 호환) ===
    public interface ItemsCallback {
        void onResult(@Nullable List<TourItem> items);
    }

    /**
     * CourseDetailActivity 호환용.
     * - 단건 상세를 불러와서 TourItem 리스트(1개)로 전달
     */
    public static void fetchCourseSummaries(@NonNull List<String> contentIds,
                                            @NonNull ItemsCallback callback) {
        if (contentIds.isEmpty()) {
            callback.onResult(null);
            return;
        }
        String contentId = contentIds.get(0);
        fetchDetailOne(AppCtx.get(), contentId, /*contentTypeId*/ "25", callback);
    }

    // === 실제 호출 ===
    private static void fetchDetailOne(@NonNull Context context,
                                       @NonNull String contentId,
                                       @Nullable String contentTypeIdOrNull,
                                       @NonNull ItemsCallback callback) {

        String mobileApp = AppCtx.getAppName(context);

        TourApiService svc = TourRetrofitClient.get();
        Call<TourApiService.ApiResponse> call = svc.detailCommon2(
                "AND",
                mobileApp,
                "json",
                contentId,
                contentTypeIdOrNull,   // null이면 파라미터 제외
                "Y","Y","Y","Y","Y","Y",
                SERVICE_KEY_ENC
        );

        Log.i(TAG, "detailCommon2 call for contentId=" + contentId);
        call.enqueue(new Callback<TourApiService.ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<TourApiService.ApiResponse> call,
                                   @NonNull Response<TourApiService.ApiResponse> resp) {
                if (!resp.isSuccessful()) {
                    Log.e(TAG, "HTTP " + resp.code());
                    callback.onResult(null);
                    return;
                }

                TourApiService.ApiResponse body = resp.body();
                if (body == null) {
                    callback.onResult(null);
                    return;
                }

                // 최상단 오류 포맷 케이스
                if (!TextUtils.isEmpty(body.resultCode) && !"0000".equals(body.resultCode)) {
                    Log.e(TAG, body.resultCode + " " + body.resultMsg);
                    callback.onResult(null);
                    return;
                }

                if (body.response != null && body.response.header != null) {
                    String rc = body.response.header.resultCode;
                    if (!"0000".equals(rc)) {
                        Log.e(TAG, rc + " " + body.response.header.resultMsg);
                        callback.onResult(null);
                        return;
                    }
                }

                List<TourItem> out = new ArrayList<>(1);
                if (body.response != null
                        && body.response.body != null
                        && body.response.body.items != null
                        && body.response.body.items.item != null
                        && !body.response.body.items.item.isEmpty()) {

                    TourApiService.TourDetailItem src = body.response.body.items.item.get(0);
                    out.add(mapToTourItem(src));
                }
                callback.onResult(out.isEmpty() ? null : out);
            }

            @Override
            public void onFailure(@NonNull Call<TourApiService.ApiResponse> call,
                                  @NonNull Throwable t) {
                Log.e(TAG, "detailCommon2 failed", t);
                callback.onResult(null);
            }
        });
    }

    // === 매핑: API → 기존 앱의 TourItem ===
    private static TourItem mapToTourItem(TourApiService.TourDetailItem s) {
        TourItem t = new TourItem();
        t.contentid  = nz(s.contentid);
        t.title      = nz(s.title);
        t.firstimage = firstNonEmpty(s.firstimage, s.firstimage2);
        t.addr1      = nz(s.addr1);
        t.addr2      = nz(s.addr2);
//        t.mapx       = nz(s.mapx);
//        t.mapy       = nz(s.mapy);
        t.overview   = cleanOverview(nz(s.overview));
        // t.homepage (없다면 무시) → Activity에서 homepageExtra만 사용
        return t;
    }

    private static String firstNonEmpty(String a, String b) {
        return !TextUtils.isEmpty(a) ? a : (!TextUtils.isEmpty(b) ? b : "");
    }
    private static String nz(String v) { return v == null ? "" : v; }

    private static String cleanOverview(String raw) {
        if (TextUtils.isEmpty(raw)) return "";
        return raw.replaceAll("(?is)<br\\s*/?>", "\n")
                .replaceAll("(?is)<[^>]+>", "")
                .trim();
    }

    // === Context / AppName 유틸 ===
    /** Application Context 보관 (AndroidManifest의 Application에서 초기화 권장) */
    public static final class AppCtx {
        private static Context app;
        public static void init(Context applicationContext) { app = applicationContext; }
        public static Context get() { return app; }
        public static String getAppName(Context ctx) {
            try {
                int labelRes = ctx.getApplicationInfo().labelRes;
                return labelRes == 0 ? ctx.getPackageName() : ctx.getString(labelRes);
            } catch (Exception e) {
                return ctx.getPackageName();
            }
        }
    }
}
