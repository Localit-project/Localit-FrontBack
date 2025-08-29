package com.inhatc.localit.api;

import android.text.TextUtils;
import android.util.Log;

import com.inhatc.localit.BuildConfig;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * TourAPI 호출 헬퍼
 * - detailCommon2 / detailIntro2 / detailImage2
 * - HTTP/에러바디 로깅 추가
 */
public class SpotApiHelper {

    private static final String TAG = "SpotApiHelper";

    private static final SpotApiService API =
            RetrofitClient.getInstance().create(SpotApiService.class);

    private static int parseCtId(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 12; }
    }

    public static SpotApiService getApiService() { return API; }

    // BuildConfig에서 주입된 "원본키" 사용 (encoded=true 아님)
    private static final String SERVICE_KEY = BuildConfig.TOUR_API_KEY;

    // -------------------- 공통 콜백 --------------------
    public interface SimpleCallback<T> { void onResult(T value); }

    // -------------------- detailCommon: 전체 1건 --------------------
    public static void fetchDetailCommon(String contentId,
                                         String contentTypeId,
                                         SimpleCallback<SpotDetailCommonResponse.Item> cb) {
        Log.d("DETAIL_ARGS", "contentId=" + contentId);

        final int ctId = parseCtId(contentTypeId);   // ← final 로 확정
        final String cid = contentId;               // (필요하면 같이 final)

        getApiService().getDetailCommon(
                "AND","localit","json",
                cid,
                "Y",                 // defaultYN
                ctId,                // contentTypeId
                "Y","Y","Y","Y","Y","Y",
                SERVICE_KEY
        ).enqueue(new Callback<SpotDetailCommonResponse>() {
            @Override public void onResponse(Call<SpotDetailCommonResponse> call,
                                             Response<SpotDetailCommonResponse> resp) {
                if (!resp.isSuccessful()) {
                    logErrorBody("DETAIL_COMMON_ERR", resp);
                    retryMinimal(cid, ctId, cb);    // ← 이제 오류 없음
                    return;
                }
                SpotDetailCommonResponse body = resp.body();
                SpotDetailCommonResponse.Item out = null;
                if (body!=null && body.response!=null && body.response.body!=null
                        && body.response.body.items!=null
                        && body.response.body.items.item!=null
                        && !body.response.body.items.item.isEmpty()) {
                    out = body.response.body.items.item.get(0);
                } else {
                    Log.w(TAG, "detailCommon: body/items null or empty");
                    // 성공이지만 비어있을 때도 최소파라미터 재시도
                    retryMinimal(cid, ctId, cb);
                    return;
                }
                Log.d("DETAIL_COMMON_OVERVIEW", "overview=" + out.overview);
                if (cb != null) cb.onResult(out);
            }

            @Override public void onFailure(Call<SpotDetailCommonResponse> call, Throwable t) {
                Log.e("DETAIL_COMMON", "request fail", t);
                retryMinimal(cid, ctId, cb);        // ← 여기도 final 사용
            }
        });
    }


    private static void retryMinimal(String contentId, int ctId,
                                     SimpleCallback<SpotDetailCommonResponse.Item> cb) {
        getApiService().getDetailCommonMinimal(
                "AND","localit","json",
                contentId,
                "Y",     // defaultYN
                "Y",     // overviewYN
                SERVICE_KEY
        ).enqueue(new Callback<SpotDetailCommonResponse>() {
            @Override public void onResponse(Call<SpotDetailCommonResponse> call,
                                             Response<SpotDetailCommonResponse> resp) {
                if (!resp.isSuccessful()) {
                    logErrorBody("DETAIL_COMMON_ERR_MIN", resp);
                    retryNoDefault(contentId, ctId, cb);
                    return;
                }
                SpotDetailCommonResponse body = resp.body();
                String rc = body!=null && body.response!=null && body.response.header!=null
                        ? body.response.header.resultCode : null;
                if (!"0000".equals(rc)) {
                    if ("10".equals(rc)) retryNoDefault(contentId, ctId, cb);
                    else if (cb!=null) cb.onResult(null);
                    return;
                }
                SpotDetailCommonResponse.Item out = null;
                if (body!=null && body.response!=null && body.response.body!=null
                        && body.response.body.items!=null
                        && body.response.body.items.item!=null
                        && !body.response.body.items.item.isEmpty()) {
                    out = body.response.body.items.item.get(0);
                }
                Log.d("DETAIL_COMMON_OVERVIEW_MIN",
                        "overview=" + (out != null ? out.overview : "null"));
                if (cb != null) cb.onResult(out);
            }
            @Override public void onFailure(Call<SpotDetailCommonResponse> call, Throwable t) {
                Log.e("DETAIL_COMMON_MIN", "request fail", t);
                retryNoDefault(contentId, ctId, cb);
            }
        });
    }

    private static void retryNoDefault(String contentId, int ctId,
                                       SimpleCallback<SpotDetailCommonResponse.Item> cb) {
        getApiService().getDetailCommonMinimal(
                "AND", "localit", "json",
                contentId,
                null,   // defaultYN 제외
                "Y",
                SERVICE_KEY
        ).enqueue(new Callback<SpotDetailCommonResponse>() {
            @Override
            public void onResponse(Call<SpotDetailCommonResponse> call,
                                   Response<SpotDetailCommonResponse> resp) {
                if (!resp.isSuccessful()) {
                    logErrorBody("DETAIL_COMMON_ERR_NODEF", resp);
                    if (cb != null) cb.onResult(null);
                    return;
                }

                SpotDetailCommonResponse body = resp.body();
                SpotDetailCommonResponse.Item out = null;
                try {
                    if (body != null
                            && body.response != null
                            && body.response.body != null
                            && body.response.body.items != null
                            && body.response.body.items.item != null
                            && !body.response.body.items.item.isEmpty()) {
                        out = body.response.body.items.item.get(0);
                    } else {
                        Log.w(TAG, "detailCommon(noDefault): items empty");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "detailCommon(noDefault) parse error", e);
                }

                Log.d("DETAIL_COMMON_OVERVIEW_NODEF", "overview=" + (out != null ? out.overview : "null"));

                if (cb != null) cb.onResult(out);
            }

            @Override
            public void onFailure(Call<SpotDetailCommonResponse> call, Throwable t) {
                Log.e("DETAIL_COMMON_NODEF", "request fail", t);
                if (cb != null) cb.onResult(null);
            }
        });
    }

        // -------------------- detailIntro: 이용정보 --------------------
    public static void fetchDetailIntro(
            String contentId,
            int contentTypeId,
            SimpleCallback<SpotDetailIntroResponse.Item> cb
    ) {
        getApiService().getDetailIntro(
                "AND","localit","json",
                contentId, contentTypeId,
                SERVICE_KEY
        ).enqueue(new Callback<SpotDetailIntroResponse>() {
            @Override public void onResponse(Call<SpotDetailIntroResponse> call,
                                             Response<SpotDetailIntroResponse> resp) {
                Log.d("DETAIL_INTRO_HTTP", "code=" + resp.code());

                if (!resp.isSuccessful()) {
                    logErrorBody("DETAIL_INTRO_ERR", resp);
                    if (cb != null) cb.onResult(null);
                    return;
                }

                SpotDetailIntroResponse.Item out = null;
                try {
                    if (resp.body() != null
                            && resp.body().response != null
                            && resp.body().response.body != null
                            && resp.body().response.body.items != null
                            && resp.body().response.body.items.item != null
                            && !resp.body().response.body.items.item.isEmpty()) {
                        out = resp.body().response.body.items.item.get(0);
                    } else {
                        Log.w(TAG, "detailIntro: body/items null or empty");
                    }
                } catch (Exception e) {
                    Log.e("DETAIL_INTRO", "parse error", e);
                }
                if (cb != null) cb.onResult(out);
            }

            @Override public void onFailure(Call<SpotDetailIntroResponse> call, Throwable t) {
                Log.e("DETAIL_INTRO", "request fail", t);
                if (cb != null) cb.onResult(null);
            }
        });
    }

    // -------------------- detailImage: URL 리스트로 반환 --------------------
    public static void fetchDetailImages(String contentId, SimpleCallback<List<String>> cb) {
        getApiService().getDetailImages(
                30, 1, "AND", "localit", "json",
                "Y",                  // imageYN
                /* subImageYN 제거됨 */
                contentId, SERVICE_KEY
        ).enqueue(new Callback<SpotDetailImageResponse>() {
            @Override public void onResponse(Call<SpotDetailImageResponse> call,
                                             Response<SpotDetailImageResponse> res) {
                Log.d("DETAIL_IMAGE_HTTP", "code=" + res.code());
                if (!res.isSuccessful()) {
                    logErrorBody("DETAIL_IMAGE_ERR", res);
                    if (cb != null) cb.onResult(new ArrayList<>());
                    return;
                }
                // 헤더 로깅(결과코드/메시지)
                try {
                    SpotDetailImageResponse.Header h =
                            res.body()!=null && res.body().response!=null ? res.body().response.header : null;
                    if (h != null) Log.d("DETAIL_IMAGE_HDR","resultCode="+h.resultCode+", resultMsg="+h.resultMsg);
                } catch (Exception ignore){}

                List<String> urls = new ArrayList<>();
                try {
                    if (res.body()!=null &&
                            res.body().response!=null &&
                            res.body().response.body!=null &&
                            res.body().response.body.items!=null &&
                            res.body().response.body.items.item!=null) {
                        for (SpotDetailImageResponse.Item it : res.body().response.body.items.item) {
                            String u = !TextUtils.isEmpty(it.originimgurl) ? it.originimgurl : it.smallimageurl;
                            if (!TextUtils.isEmpty(u)) urls.add(u);
                        }
                    } else {
                        Log.w(TAG, "detailImage: body/items null or empty");
                    }
                } catch (Exception e) {
                    Log.e("DETAIL_IMAGE", "parse error", e);
                }
                if (cb != null) cb.onResult(urls);
            }
            @Override public void onFailure(Call<SpotDetailImageResponse> call, Throwable t) {
                Log.e("DETAIL_IMAGE", "request fail", t);
                if (cb != null) cb.onResult(new ArrayList<>());
            }
        });
    }


    // -------------------- 내부 유틸: 에러바디 로깅 --------------------
    private static void logErrorBody(String tag, Response<?> resp) {
        try {
            String err = resp.errorBody() != null ? resp.errorBody().string() : "";
            Log.e(TAG, tag + " http=" + resp.code() + " body=" + err);
        } catch (Exception e) {
            Log.e(TAG, tag + " errorBody read fail", e);
        }
    }
    public static void fetchDetailInfo(
            String contentId,
            int contentTypeId,
            SimpleCallback<List<SpotDetailInfoResponse.Item>> cb
    ) {
        getApiService().getDetailInfo(
                "AND","localit","json",
                contentId, contentTypeId,
                BuildConfig.TOUR_API_KEY
        ).enqueue(new Callback<SpotDetailInfoResponse>() {
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
                    }
                } catch (Exception e) {
                    Log.e("DETAIL_INFO", "parse error", e);
                }
                if (cb != null) cb.onResult(out);
            }
            @Override public void onFailure(Call<SpotDetailInfoResponse> call, Throwable t) {
                Log.e("DETAIL_INFO", "request fail", t);
                if (cb != null) cb.onResult(new ArrayList<>());
            }
        });
    }
}
