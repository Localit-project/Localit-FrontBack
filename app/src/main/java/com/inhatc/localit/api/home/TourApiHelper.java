package com.inhatc.localit.api.home;

import android.util.Log;
import com.inhatc.localit.BuildConfig;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import retrofit2.*;

public final class TourApiHelper {
    private static final String TAG = "TourApiHelper";
    private static final String OS = "AND";
    private static final String APP = "Localit";
    private static final String TYPE = "json";

    private static String key() {
        try { return URLDecoder.decode(BuildConfig.TOUR_API_KEY, "UTF-8"); }
        catch (Exception e) { return BuildConfig.TOUR_API_KEY; }
    }

    /** 홈에서 쓰는 간단 요약(제목/대표사진/개요 일부/주소) */
    public static void fetchCourseSummaries(List<String> contentIds, java.util.function.Consumer<List<TourItem>> cb) {
        if (contentIds == null || contentIds.isEmpty()) { cb.accept(new ArrayList<>()); return; }

        List<TourItem> acc = new ArrayList<>();
        AtomicInteger remain = new AtomicInteger(contentIds.size());
        ApiService api = ApiClient.get();

        for (String id : contentIds) {
            api.getCourseDetail(key(), OS, APP, TYPE, id, 25,
                    "Y","Y","Y","Y","Y"
            ).enqueue(new Callback<DetailResponse>() {
                @Override public void onResponse(Call<DetailResponse> call, Response<DetailResponse> res) {
                    try {
                        DetailResponse.Item it = (res.body()!=null && res.body().response!=null &&
                                res.body().response.body!=null && res.body().response.body.items!=null &&
                                res.body().response.body.items.item!=null && !res.body().response.body.items.item.isEmpty())
                                ? res.body().response.body.items.item.get(0) : null;
                        if (it != null) {
                            TourItem t = new TourItem();
                            t.contentid = id;
                            t.contenttypeid = "25";
                            t.title = it.title;
                            t.firstimage = it.firstimage;
                            t.overview = it.overview;
                            t.addr1 = it.addr1;
                            t.mapx = it.mapx;
                            t.mapy = it.mapy;
                            acc.add(t);
                        }
                    } catch (Exception e) { Log.w(TAG, "parse detailCommon fail id="+id, e); }
                    if (remain.decrementAndGet()==0) cb.accept(acc);
                }
                @Override public void onFailure(Call<DetailResponse> call, Throwable t) {
                    Log.e(TAG,"detailCommon fail id="+id,t);
                    if (remain.decrementAndGet()==0) cb.accept(acc);
                }
            });
        }
    }

    // 상세 각 파트
    public static Call<DetailResponse> getDetail(String contentId) {
        return ApiClient.get().getCourseDetail(key(), OS, APP, TYPE, contentId, 25,
                "Y","Y","Y","Y","Y");
    }
    public static Call<CourseIntroResponse> getIntro(String contentId) {
        return ApiClient.get().getCourseIntro(key(), OS, APP, TYPE, contentId, 25);
    }
    public static Call<CourseInfoResponse> getInfo(String contentId) {
        return ApiClient.get().getCourseInfo(key(), OS, APP, TYPE, contentId, 25);
    }
    public static Call<ImageResponse> getImages(String contentId) {
        return ApiClient.get().getDetailImages(key(), OS, APP, TYPE, contentId, "Y", "Y");
    }
}
