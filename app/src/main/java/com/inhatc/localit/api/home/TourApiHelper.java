package com.inhatc.localit.api.home;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.inhatc.localit.BuildConfig;
import com.inhatc.localit.api.home.TourApiService.ApiResponse;
import com.inhatc.localit.api.home.TourApiService.TourDetailItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class TourApiHelper {

    private static final String TAG = "TourApiHelper";
    private static final String SERVICE_KEY_RAW = BuildConfig.TOUR_API_KEY;

    private TourApiHelper() {}

    // ============== 콜백 ==============
    public interface ItemsCallback { void onResult(@Nullable List<TourItem> items); }

    // ============== 외부 진입점 ==============
    public static void fetchDetailSummary(@NonNull String contentId,
                                          @Nullable String contentTypeIdOrNull,
                                          @NonNull ItemsCallback callback) {
        fetchDetailOne(AppCtx.get(), contentId, contentTypeIdOrNull, callback);
    }

    public static void fetchCourseSummaries(@NonNull List<String> contentIds,
                                            @NonNull ItemsCallback callback) {
        if (contentIds.isEmpty()) { callback.onResult(null); return; }
        List<TourItem> acc = new ArrayList<>();
        final int[] done = {0};
        for (String id : contentIds) {
            fetchDetailOne(AppCtx.get(), id, "25", items -> {
                if (items != null && !items.isEmpty()) acc.addAll(items);
                if (++done[0] == contentIds.size()) {
                    acc.sort((a,b)->a.contentid.compareTo(b.contentid));
                    callback.onResult(acc.isEmpty() ? null : acc);
                }
            });
        }
    }

    // ============== 상세 1건 조회(JSON 우선, XML 폴백) ==============
    private static void fetchDetailOne(@NonNull Context context,
                                       @NonNull String contentId,
                                       @Nullable String contentTypeIdOrNull, // common에는 쓰지 않음
                                       @NonNull ItemsCallback callback) {

        String mobileApp = AppCtx.getAppName(context);
        TourApiService svc = TourRetrofitClient.get();

        Log.i(TAG, "detailCommon2 call for contentId=" + contentId);

        // ★ Common 은 contentTypeId 전달 금지
        Call<ApiResponse> call = svc.detailCommon2(
                "AND", mobileApp, "json", contentId, SERVICE_KEY_RAW
        );

        call.enqueue(new Callback<ApiResponse>() {
            @Override public void onResponse(@NonNull Call<ApiResponse> call,
                                             @NonNull Response<ApiResponse> resp) {
                if (!resp.isSuccessful() || resp.body()==null) {
                    Log.w(TAG, "detailCommon2 JSON not successful, fallback RAW");
                    retryCommonRaw(svc, mobileApp, contentId, callback);
                    return;
                }
                ApiResponse body = resp.body();

                // 두 가지 포맷 모두 OK 처리
                if (!TextUtils.isEmpty(body.resultCode) && !"0000".equals(body.resultCode)) {
                    Log.e(TAG, "top result fail: " + body.resultCode + " " + body.resultMsg);
                    callback.onResult(null);
                    return;
                }
                if (body.response != null && body.response.header != null) {
                    String rc = body.response.header.resultCode;
                    if (!"0000".equals(rc)) {
                        Log.e(TAG, "header result fail: " + rc + " " + body.response.header.resultMsg);
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
                    TourDetailItem src = body.response.body.items.item.get(0);
                    out.add(mapToTourItem(src));
                }
                Log.i(TAG, "detailCommon2 parsed, size=" + out.size());
                callback.onResult(out.isEmpty() ? null : out);
            }

            @Override public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                Log.w(TAG, "detailCommon2 JSON fail: " + t.getClass().getSimpleName() + ", fallback RAW");
                retryCommonRaw(svc, mobileApp, contentId, callback);
            }
        });
    }

    private static void retryCommonRaw(TourApiService svc,
                                       String mobileApp,
                                       String contentId,
                                       ItemsCallback callback) {
        svc.detailCommon2Raw("AND", mobileApp, "xml", contentId, SERVICE_KEY_RAW)
                .enqueue(new Callback<String>() {
                    @Override public void onResponse(@NonNull Call<String> call, @NonNull Response<String> resp) {
                        if (!resp.isSuccessful() || resp.body()==null) { callback.onResult(null); return; }
                        String raw = resp.body().trim();
                        Log.w(TAG, "RAW body (first 300): " + raw.substring(0, Math.min(raw.length(), 300)));

                        if (raw.startsWith("{")) {
                            try {
                                ApiResponse parsed = new Gson().fromJson(raw, ApiResponse.class);
                                if (parsed!=null && parsed.response!=null
                                        && parsed.response.body!=null
                                        && parsed.response.body.items!=null
                                        && parsed.response.body.items.item!=null
                                        && !parsed.response.body.items.item.isEmpty()) {
                                    TourDetailItem s = parsed.response.body.items.item.get(0);
                                    List<TourItem> out = new ArrayList<>(1);
                                    out.add(mapToTourItem(s));
                                    callback.onResult(out);
                                    return;
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "RAW JSON parse fail", e);
                            }
                            callback.onResult(null);
                        } else if (raw.startsWith("<")) {
                            // XML 간이 파싱
                            TourDetailItem s = xmlToDetailItem(raw);
                            if (s != null) {
                                List<TourItem> out = new ArrayList<>(1);
                                out.add(mapToTourItem(s));
                                callback.onResult(out);
                            } else {
                                callback.onResult(null);
                            }
                        } else {
                            Log.e(TAG, "RAW unknown: " + raw);
                            callback.onResult(null);
                        }
                    }
                    @Override public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Log.e(TAG, "detailCommon2Raw failed", t);
                        callback.onResult(null);
                    }
                });
    }

    // ============== 코스(인트로/스텝) ==============
    public interface CourseExtraCallback {
        void onResult(@Nullable CourseIntro intro, @Nullable List<CourseStep> steps);
    }
    public static class CourseIntro { public String schedule, distance, taketime, theme; }
    public static class CourseStep  { public int order; public String title; public String overview; public String image; }

    public static void fetchCourseExtra(@NonNull String contentId,
                                        @NonNull CourseExtraCallback cb) {
        TourApiService svc = TourRetrofitClient.get();
        String app = AppCtx.getAppName(AppCtx.get());
        String key = SERVICE_KEY_RAW;

        final CourseIntro[] outIntro = new CourseIntro[1];
        final List<CourseStep>[] outSteps = new List[]{null};

        // Intro: JSON 우선, XML 폴백
        svc.detailIntro2("AND", app, "json", contentId, "25", key)
                .enqueue(new retrofit2.Callback<TourApiService.IntroResponse>() {
                    @Override public void onResponse(@NonNull retrofit2.Call<TourApiService.IntroResponse> call,
                                                     @NonNull retrofit2.Response<TourApiService.IntroResponse> resp) {
                        if (resp.isSuccessful() && resp.body()!=null
                                && resp.body().response!=null
                                && resp.body().response.body!=null
                                && resp.body().response.body.items!=null
                                && resp.body().response.body.items.item!=null
                                && !resp.body().response.body.items.item.isEmpty()) {
                            TourApiService.IntroResponse.Item i = resp.body().response.body.items.item.get(0);
                            CourseIntro ci = new CourseIntro();
                            ci.schedule = nz(i.schedule); ci.distance = nz(i.distance);
                            ci.taketime = nz(i.taketime); ci.theme    = nz(i.theme);
                            outIntro[0] = ci;
                            Log.i(TAG, "intro2 JSON OK");
                            // 이어서 info
                            fetchInfo2(svc, app, contentId, key, outIntro[0], cb, outSteps);
                        } else {
                            Log.w(TAG, "intro2 JSON empty -> RAW");
                            fetchIntro2RawThenInfo2(svc, app, contentId, key, outIntro, cb, outSteps);
                        }
                    }
                    @Override public void onFailure(@NonNull retrofit2.Call<TourApiService.IntroResponse> call,
                                                    @NonNull Throwable t) {
                        Log.w(TAG, "intro2 JSON fail -> RAW: " + t.getClass().getSimpleName());
                        fetchIntro2RawThenInfo2(svc, app, contentId, key, outIntro, cb, outSteps);
                    }
                });
    }

    private static void fetchIntro2RawThenInfo2(TourApiService svc, String app, String contentId, String key,
                                                CourseIntro[] outIntro, CourseExtraCallback cb, List<CourseStep>[] outSteps) {
        svc.detailIntro2Raw("AND", app, "xml", contentId, "25", key)
                .enqueue(new retrofit2.Callback<String>() {
                    @Override public void onResponse(@NonNull retrofit2.Call<String> call,
                                                     @NonNull retrofit2.Response<String> resp) {
                        if (resp.isSuccessful() && resp.body()!=null) {
                            String raw = resp.body().trim();
                            Log.w(TAG, "RAW body (first 300): " + raw.substring(0, Math.min(raw.length(), 300)));
                            CourseIntro ci = xmlToIntro(raw);
                            if (ci != null) { outIntro[0] = ci; Log.i(TAG, "intro2 XML OK"); }
                        }
                        fetchInfo2(svc, app, contentId, key, outIntro[0], cb, outSteps);
                    }
                    @Override public void onFailure(@NonNull retrofit2.Call<String> call, @NonNull Throwable t) {
                        Log.e(TAG, "intro2 RAW fail", t);
                        fetchInfo2(svc, app, contentId, key, outIntro[0], cb, outSteps);
                    }
                });
    }

    private static void fetchInfo2(TourApiService svc, String app, String contentId, String key,
                                   @Nullable CourseIntro intro, CourseExtraCallback cb,
                                   List<CourseStep>[] outSteps) {
        svc.detailInfo2("AND", app, "json", contentId, "25", 100, 1, key)
                .enqueue(new retrofit2.Callback<TourApiService.InfoResponse>() {
                    @Override public void onResponse(@NonNull retrofit2.Call<TourApiService.InfoResponse> call,
                                                     @NonNull retrofit2.Response<TourApiService.InfoResponse> resp) {
                        if (resp.isSuccessful() && resp.body()!=null
                                && resp.body().response!=null
                                && resp.body().response.body!=null
                                && resp.body().response.body.items!=null
                                && resp.body().response.body.items.item!=null
                                && !resp.body().response.body.items.item.isEmpty()) {
                            List<CourseStep> list = new ArrayList<>();
                            for (TourApiService.InfoResponse.Item it : resp.body().response.body.items.item) {
                                CourseStep s = new CourseStep();
                                try { s.order = Integer.parseInt(nz(it.subnum)); } catch (Exception e) { s.order = 0; }
                                s.title = nz(it.subname);
                                s.overview = cleanOverview(nz(it.subdetailoverview));
                                s.image = nz(it.subdetailimg);
                                list.add(s);
                            }
                            Collections.sort(list, (a,b)->Integer.compare(a.order,b.order));
                            outSteps[0] = list;
                            Log.i(TAG, "info2 JSON OK, steps=" + list.size());
                            cb.onResult(intro, outSteps[0]);
                        } else {
                            Log.w(TAG, "info2 JSON empty -> RAW");
                            fetchInfo2Raw(svc, app, contentId, key, intro, cb);
                        }
                    }
                    @Override public void onFailure(@NonNull retrofit2.Call<TourApiService.InfoResponse> call,
                                                    @NonNull Throwable t) {
                        Log.w(TAG, "info2 JSON fail -> RAW: " + t.getClass().getSimpleName());
                        fetchInfo2Raw(svc, app, contentId, key, intro, cb);
                    }
                });
    }

    private static void fetchInfo2Raw(TourApiService svc, String app, String contentId, String key,
                                      @Nullable CourseIntro intro, CourseExtraCallback cb) {
        svc.detailInfo2Raw("AND", app, "xml", contentId, "25", 100, 1, key)
                .enqueue(new retrofit2.Callback<String>() {
                    @Override public void onResponse(@NonNull retrofit2.Call<String> call,
                                                     @NonNull retrofit2.Response<String> resp) {
                        List<CourseStep> list = null;
                        if (resp.isSuccessful() && resp.body()!=null) {
                            String raw = resp.body().trim();
                            Log.w(TAG, "RAW body (first 300): " + raw.substring(0, Math.min(raw.length(), 300)));
                            list = xmlToSteps(raw);
                            if (list != null) {
                                Collections.sort(list, (a,b)->Integer.compare(a.order,b.order));
                                Log.i(TAG, "info2 XML OK, steps=" + list.size());
                            }
                        }
                        cb.onResult(intro, list);
                    }
                    @Override public void onFailure(@NonNull retrofit2.Call<String> call, @NonNull Throwable t) {
                        Log.e(TAG, "info2 RAW fail", t);
                        cb.onResult(intro, null);
                    }
                });
    }

    // ============== 매핑/유틸 ==============
    private static TourItem mapToTourItem(TourDetailItem s) {
        TourItem t = new TourItem();
        t.contentid     = nz(s.contentid);
        t.contenttypeid = nz(s.contenttypeid);
        t.title         = nz(s.title);
        t.firstimage    = firstNonEmpty(s.firstimage, s.firstimage2);
        t.addr1         = nz(s.addr1);
        t.addr2         = nz(s.addr2);
        t.overview      = cleanOverview(nz(s.overview));
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

    // ====== 간이 XML 파서(필요 태그만) ======
    private static String pick(String xml, String tag) {
        Pattern p = Pattern.compile("<" + tag + ">(<!\\[CDATA\\[)?(.*?)(]]>)?</" + tag + ">", Pattern.DOTALL);
        Matcher m = p.matcher(xml);
        if (m.find()) return m.group(2).trim();
        return "";
    }
    private static TourDetailItem xmlToDetailItem(String xml) {
        if (xml == null || !xml.startsWith("<")) return null;
        if (!"0000".equals(pick(xml, "resultCode"))) return null;
        TourDetailItem s = new TourDetailItem();
        s.contentid     = pick(xml, "contentid");
        s.contenttypeid = pick(xml, "contenttypeid");
        s.title         = pick(xml, "title");
        s.firstimage    = pick(xml, "firstimage");
        s.firstimage2   = pick(xml, "firstimage2");
        s.addr1         = pick(xml, "addr1");
        s.addr2         = pick(xml, "addr2");
        s.overview      = pick(xml, "overview");
        return TextUtils.isEmpty(s.contentid) ? null : s;
    }
    private static CourseIntro xmlToIntro(String xml) {
        if (xml == null || !xml.startsWith("<")) return null;
        if (!"0000".equals(pick(xml, "resultCode"))) return null;
        CourseIntro ci = new CourseIntro();
        ci.schedule = pick(xml, "schedule");
        ci.distance = pick(xml, "distance");
        ci.taketime = pick(xml, "taketime");
        ci.theme    = pick(xml, "theme");
        if (TextUtils.isEmpty(ci.schedule) && TextUtils.isEmpty(ci.distance)
                && TextUtils.isEmpty(ci.taketime) && TextUtils.isEmpty(ci.theme)) return null;
        return ci;
    }
    private static List<CourseStep> xmlToSteps(String xml) {
        if (xml == null || !xml.startsWith("<")) return null;
        if (!"0000".equals(pick(xml, "resultCode"))) return null;
        List<CourseStep> list = new ArrayList<>();
        Pattern pItem = Pattern.compile("<item>(.*?)</item>", Pattern.DOTALL);
        Matcher m = pItem.matcher(xml);
        while (m.find()) {
            String block = m.group(1);
            CourseStep s = new CourseStep();
            try { s.order = Integer.parseInt(pick(block, "subnum")); } catch (Exception e) { s.order = 0; }
            s.title = pick(block, "subname");
            s.overview = cleanOverview(pick(block, "subdetailoverview"));
            s.image = pick(block, "subdetailimg");
            if (!TextUtils.isEmpty(s.title)) list.add(s);
        }
        return list;
    }

    // ============== AppCtx ==============
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
