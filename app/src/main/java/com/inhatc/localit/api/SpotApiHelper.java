package com.inhatc.localit.api;

import android.text.TextUtils;

import com.inhatc.localit.BuildConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                "Y","Y","Y","Y","Y","Y","Y",   // 7개의 YN 모두
                SERVICE_KEY
        ).enqueue(new Callback<SpotDetailCommonResponse>() {
            @Override public void onResponse(Call<SpotDetailCommonResponse> call,
                                             Response<SpotDetailCommonResponse> resp) {
                SpotDetailCommonResponse.Item out = null;
                try {
                    if (resp.isSuccessful()
                            && resp.body()!=null
                            && resp.body().response!=null
                            && resp.body().response.body!=null
                            && resp.body().response.body.items!=null
                            && resp.body().response.body.items.item!=null
                            && !resp.body().response.body.items.item.isEmpty()) {
                        out = resp.body().response.body.items.item.get(0);
                    }
                } catch (Exception ignore) {}
                if (cb!=null) cb.onResult(out);
            }
            @Override public void onFailure(Call<SpotDetailCommonResponse> call, Throwable t) {
                if (cb!=null) cb.onResult(null);
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
                    }
                } catch (Exception ignore) {}
                if (cb!=null) cb.onResult(out);
            }
            @Override public void onFailure(Call<SpotDetailIntroResponse> call, Throwable t) {
                if (cb!=null) cb.onResult(null);
            }
        });
    }

    // -------------------- homepage URL만 추출 --------------------
    public static void fetchHomepageUrl(
            SpotApiService api,
            String serviceKey,
            String contentId,
            String contentTypeId,
            SimpleCallback<String> cb
    ) {
        api.getDetailCommon(
                "AND", "localit", "json",
                contentId, contentTypeId,
                "Y","Y","Y","Y","Y","Y","Y",   // 7개의 YN 모두
                SERVICE_KEY
        ).enqueue(new Callback<SpotDetailCommonResponse>() {
            @Override public void onResponse(Call<SpotDetailCommonResponse> call,
                                             Response<SpotDetailCommonResponse> resp) {
                String url = null;
                try {
                    if (resp.isSuccessful()
                            && resp.body()!=null
                            && resp.body().response!=null
                            && resp.body().response.body!=null
                            && resp.body().response.body.items!=null
                            && resp.body().response.body.items.item!=null
                            && !resp.body().response.body.items.item.isEmpty()) {
                        String homepage = resp.body().response.body.items.item.get(0).homepage;
                        url = extractFirstHref(homepage);
                        if (url != null) {
                            if (url.startsWith("//")) url = "https:" + url;
                            if (url.startsWith("http://")) url = "https://" + url.substring(7);
                            url = url.replace("m.visitkorea.or.kr","korean.visitkorea.or.kr");
                        }
                    }
                } catch (Exception ignore) {}
                if (cb!=null) cb.onResult(url);
            }
            @Override public void onFailure(Call<SpotDetailCommonResponse> call, Throwable t) {
                if (cb!=null) cb.onResult(null);
            }
        });
    }

    // -------------------- 추가 이미지: URL 리스트로 반환 --------------------
    public static void fetchDetailImages(
            String contentId,
            SimpleCallback<java.util.List<String>> cb
    ) {
        SpotApiService api = getApiService();
        Call<SpotDetailImageResponse> call = api.getDetailImages(
                30, 1, "AND", "localit", "json",
                "Y", "Y", contentId, SERVICE_KEY
        );
        call.enqueue(new Callback<SpotDetailImageResponse>() {
            @Override public void onResponse(Call<SpotDetailImageResponse> call,
                                             Response<SpotDetailImageResponse> res) {
                java.util.List<String> urls = new java.util.ArrayList<>();
                try {
                    java.util.List<SpotDetailImageResponse.Item> items =
                            res.body().response.body.items.item;
                    if (items != null) {
                        for (SpotDetailImageResponse.Item it : items) {
                            String u = !TextUtils.isEmpty(it.originimgurl)
                                    ? it.originimgurl : it.smallimageurl;
                            if (!TextUtils.isEmpty(u)) urls.add(u);
                        }
                    }
                } catch (Exception ignore) {}
                if (cb != null) cb.onResult(urls);
            }
            @Override public void onFailure(Call<SpotDetailImageResponse> call, Throwable t) {
                if (cb != null) cb.onResult(new java.util.ArrayList<>());
            }
        });
    }

    // <a href="...">..</a> 또는 순수 URL 모두 대응
    private static String extractFirstHref(String homepageHtml) {
        if (homepageHtml == null) return null;
        Matcher m = Pattern.compile("href\\s*=\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE)
                .matcher(homepageHtml);
        if (m.find()) return m.group(1);
        String s = homepageHtml.trim();
        if (s.startsWith("http")) return s;
        if (s.startsWith("//")) return "https:" + s;
        return null;
    }
}
