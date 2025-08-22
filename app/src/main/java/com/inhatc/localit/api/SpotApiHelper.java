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

    public static SpotApiService getApiService() { return API; }

    // BuildConfig에서 주입된 "원본키" 사용 (encoded=true 아님)
    private static final String SERVICE_KEY = BuildConfig.TOUR_API_KEY;

    // -------------------- 공통 콜백 --------------------
    public interface SimpleCallback<T> { void onResult(T value); }

    // -------------------- detailCommon: 전체 1건 --------------------
    public static void fetchDetailCommon(
            String contentId,
            String contentTypeId,
            SimpleCallback<SpotDetailCommonResponse.Item> cb
    ) {
        Log.d("DETAIL_ARGS", "contentId=" + contentId + ", contentTypeId=" + contentTypeId);

        int ctId;
        try { ctId = Integer.parseInt(contentTypeId); } catch (Exception e) { ctId = 12; }

        getApiService().getDetailCommon(
                "AND","localit","json",
                contentId, ctId,
                "Y","Y","Y","Y","Y","Y","Y",
                SERVICE_KEY
        ).enqueue(new Callback<SpotDetailCommonResponse>() {
            @Override public void onResponse(Call<SpotDetailCommonResponse> call,
                                             Response<SpotDetailCommonResponse> resp) {
                Log.d("DETAIL_COMMON_HTTP", "code=" + resp.code());

                if (!resp.isSuccessful()) {
                    logErrorBody("DETAIL_COMMON_ERR", resp);
                    if (cb != null) cb.onResult(null);
                    return;
                }

                SpotDetailCommonResponse.Item out = null;
                try {
                    SpotDetailCommonResponse.Header h =
                            resp.body()!=null && resp.body().response!=null ? resp.body().response.header : null;
                    if (h!=null) Log.d("DETAIL_COMMON_HDR","resultCode="+h.resultCode+", resultMsg="+h.resultMsg);
                } catch (Exception ignore){
                    if (resp.body() != null
                            && resp.body().response != null
                            && resp.body().response.body != null
                            && resp.body().response.body.items != null
                            && resp.body().response.body.items.item != null
                            && !resp.body().response.body.items.item.isEmpty()) {
                        out = resp.body().response.body.items.item.get(0);
                    } else {
                        Log.w(TAG, "detailCommon: body/items null or empty");
                    }

                }
                if (cb != null) cb.onResult(out);
            }

            @Override public void onFailure(Call<SpotDetailCommonResponse> call, Throwable t) {
                Log.e("DETAIL_COMMON", "request fail", t);
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
