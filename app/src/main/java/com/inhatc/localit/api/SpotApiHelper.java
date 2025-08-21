package com.inhatc.localit.api;

import android.text.TextUtils;
import android.util.Log;

import com.inhatc.localit.BuildConfig;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SpotApiHelper {

    private static final SpotApiService API =
            RetrofitClient.getInstance().create(SpotApiService.class);

    public static SpotApiService getApiService() { return API; }

    // 공용 서비스키 (BuildConfig에서 주입)
    private static final String SERVICE_KEY = BuildConfig.TOUR_API_KEY;

    // -------------------- 공통 콜백 --------------------
    public interface SimpleCallback<T> { void onResult(T value); }

    // -------------------- detailCommon: 전체 1건 --------------------
    public static void fetchDetailCommon(
            String contentId,
            String contentTypeId,
            SimpleCallback<SpotDetailCommonResponse.Item> cb
    ) {
        API.getDetailCommon(
                "AND", "localit", "json",
                contentId, contentTypeId,
                "Y","Y","Y","Y","Y","Y","Y",   // defaultYN ~ overviewYN 모두 Y
                SERVICE_KEY
        ).enqueue(new Callback<SpotDetailCommonResponse>() {
            @Override
            public void onResponse(Call<SpotDetailCommonResponse> call,
                                   Response<SpotDetailCommonResponse> resp) {
                SpotDetailCommonResponse.Item out = null;
                try {
                    if (resp.isSuccessful()
                            && resp.body() != null
                            && resp.body().response != null
                            && resp.body().response.body != null
                            && resp.body().response.body.items != null
                            && resp.body().response.body.items.item != null
                            && !resp.body().response.body.items.item.isEmpty()) {
                        out = resp.body().response.body.items.item.get(0);
                    }
                } catch (Exception e) {
                    Log.e("DETAIL_COMMON", "parse error", e);
                }
                if (cb != null) cb.onResult(out);
            }

            @Override
            public void onFailure(Call<SpotDetailCommonResponse> call, Throwable t) {
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
        API.getDetailIntro(
                "AND", "localit", "json",
                contentId, contentTypeId,
                SERVICE_KEY
        ).enqueue(new Callback<SpotDetailIntroResponse>() {
            @Override
            public void onResponse(Call<SpotDetailIntroResponse> call,
                                   Response<SpotDetailIntroResponse> resp) {
                SpotDetailIntroResponse.Item out = null;
                try {
                    if (resp.isSuccessful()
                            && resp.body() != null
                            && resp.body().response != null
                            && resp.body().response.body != null
                            && resp.body().response.body.items != null
                            && resp.body().response.body.items.item != null
                            && !resp.body().response.body.items.item.isEmpty()) {
                        out = resp.body().response.body.items.item.get(0);
                    }
                } catch (Exception e) {
                    Log.e("DETAIL_INTRO", "parse error", e);
                }
                if (cb != null) cb.onResult(out);
            }

            @Override
            public void onFailure(Call<SpotDetailIntroResponse> call, Throwable t) {
                Log.e("DETAIL_INTRO", "request fail", t);
                if (cb != null) cb.onResult(null);
            }
        });
    }

    // -------------------- 추가 이미지: URL 리스트로 반환 --------------------
    public static void fetchDetailImages(
            String contentId,
            SimpleCallback<List<String>> cb
    ) {
        Call<SpotDetailImageResponse> call = API.getDetailImages(
                30, 1, "AND", "localit", "json",
                "Y", "Y", contentId, SERVICE_KEY
        );
        call.enqueue(new Callback<SpotDetailImageResponse>() {
            @Override
            public void onResponse(Call<SpotDetailImageResponse> call,
                                   Response<SpotDetailImageResponse> res) {
                List<String> urls = new ArrayList<>();
                try {
                    if (res.isSuccessful() && res.body() != null
                            && res.body().response != null
                            && res.body().response.body != null
                            && res.body().response.body.items != null
                            && res.body().response.body.items.item != null) {
                        for (SpotDetailImageResponse.Item it : res.body().response.body.items.item) {
                            String u = !TextUtils.isEmpty(it.originimgurl)
                                    ? it.originimgurl : it.smallimageurl;
                            if (!TextUtils.isEmpty(u)) urls.add(u);
                        }
                    }
                } catch (Exception e) {
                    Log.e("DETAIL_IMAGE", "parse error", e);
                }
                if (cb != null) cb.onResult(urls);
            }

            @Override
            public void onFailure(Call<SpotDetailImageResponse> call, Throwable t) {
                Log.e("DETAIL_IMAGE", "request fail", t);
                if (cb != null) cb.onResult(new ArrayList<>());
            }
        });
    }
}
