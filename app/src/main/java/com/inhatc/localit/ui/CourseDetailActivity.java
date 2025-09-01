package com.inhatc.localit.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.inhatc.localit.BuildConfig;
import com.inhatc.localit.R;
import com.inhatc.localit.api.ApiClient;
import com.inhatc.localit.api.CourseInfoResponse;
import com.inhatc.localit.api.CourseIntroResponse;
import com.inhatc.localit.api.SpotApiHelper;
import com.inhatc.localit.api.SpotApiService;
import com.inhatc.localit.api.SpotDetailCommonResponse;
import com.inhatc.localit.ui.course.CourseStepAdapter;
import com.inhatc.localit.ui.course.GalleryAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** 여행코스 상세 (KorService2) */
public class CourseDetailActivity extends AppCompatActivity {

    public static final String EXTRA_CONTENT_ID = "extra_content_id";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_FALLBACK_IMAGE_URI = "extra_fallback_image_uri";

    private ImageView imageMain;
    private TextView textTitle;

    // 기본정보 뷰
    private TextView tvAddress, tvPhone, tvHomepage, tvCourseIntro;
    private TextView btnCopyAddress, btnCall, btnOpenSite;

    // 코스 인트로 섹션(옵션)
    private TextView tvCourseSchedule, tvCourseDistance, tvCourseTaketime, tvCourseTheme;

    // 리스트들
    private RecyclerView recyclerGallery, recyclerCourseSteps;
    private final GalleryAdapter galleryAdapter = new GalleryAdapter();
    private final CourseStepAdapter courseAdapter = new CourseStepAdapter();

    private SpotApiService api;
    private String serviceKey;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        api = ApiClient.getInstance().create(SpotApiService.class);
        serviceKey = BuildConfig.TOUR_API_KEY; // 인코딩/디코딩 X

        bindViews();

        ImageButton back = findViewById(R.id.btnBack);
        if (back != null) back.setOnClickListener(v -> finish());

        // 갤러리
        recyclerGallery.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerGallery.setAdapter(galleryAdapter);

        // 코스 스텝 리스트
        recyclerCourseSteps.setLayoutManager(new LinearLayoutManager(this));
        recyclerCourseSteps.setAdapter(courseAdapter);

        // 인텐트
        Intent i = getIntent();
        String contentId = i.getStringExtra(EXTRA_CONTENT_ID);
        String title     = i.getStringExtra(EXTRA_TITLE);
        String fallback  = i.getStringExtra(EXTRA_FALLBACK_IMAGE_URI);

        if (!TextUtils.isEmpty(title)) textTitle.setText(title);
        if (!TextUtils.isEmpty(fallback)) Glide.with(this).load(Uri.parse(fallback)).into(imageMain);

        if (TextUtils.isEmpty(contentId)) {
            Toast.makeText(this, "contentId 가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 공통정보
        fetchCommon(contentId);
        // 이미지
        fetchImages(contentId);
        // 코스 소개(거리/일정/소요/테마)
        fetchCourseIntro(contentId);
        // 코스 구간 리스트
        fetchCourseInfo(contentId);
    }

    private void bindViews() {
        imageMain  = findViewById(R.id.imageMain);
        textTitle  = findViewById(R.id.textTitle);

        tvAddress  = findViewById(R.id.tvAddress);
        tvPhone    = findViewById(R.id.tvPhone);
        tvHomepage = findViewById(R.id.tvHomepage);
        tvCourseIntro = findViewById(R.id.tvCourseIntro);

        btnCopyAddress = findViewById(R.id.btnCopyAddress);
        btnCall        = findViewById(R.id.btnCall);
        btnOpenSite    = findViewById(R.id.btnOpenSite);

        recyclerGallery   = findViewById(R.id.recyclerGallery);
        recyclerCourseSteps = findViewById(R.id.recyclerCourseSteps);
        // (혹시 레이아웃이 recyclerCourse 라면 findViewById(R.id.recyclerCourse)로 바꿔도 됩니다.)

        // 코스 인트로 섹션(있으면 채움)
        tvCourseSchedule = findViewById(R.id.tvCourseSchedule);
        tvCourseDistance = findViewById(R.id.tvCourseDistance);
        tvCourseTaketime = findViewById(R.id.tvCourseTaketime);
        tvCourseTheme    = findViewById(R.id.tvCourseTheme);
    }

    // detailCommon2
    private void fetchCommon(String contentId) {
        SpotApiHelper.fetchDetailCommon(contentId, "25", (SpotDetailCommonResponse.Item it) -> runOnUiThread(() -> {
            if (it == null) return;

            // 대표 이미지
            String img = !TextUtils.isEmpty(it.firstimage) ? it.firstimage : it.firstimage2;
            if (!TextUtils.isEmpty(img)) {
                Glide.with(this).load(img)
                        .placeholder(R.drawable.sample1).error(R.drawable.sample1)
                        .into(imageMain);
            }

            // 주소/전화/홈페이지
            setText(tvAddress, join(it.addr1, it.addr2));
            setText(tvPhone, stripHtml(it.tel));
            if (!TextUtils.isEmpty(it.homepage)) {
                tvHomepage.setText(HtmlCompat.fromHtml(it.homepage.replaceAll("(?i)<br\\s*/?>","\n"),
                        HtmlCompat.FROM_HTML_MODE_LEGACY));
            } else {
                setText(tvHomepage, "");
            }

            // 액션
            if (btnCopyAddress != null) {
                btnCopyAddress.setOnClickListener(v -> {
                    ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    if (cm != null) {
                        cm.setPrimaryClip(ClipData.newPlainText("address", tvAddress.getText()));
                        Toast.makeText(this, "주소를 복사했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            if (btnCall != null) {
                btnCall.setOnClickListener(v -> {
                    String tel = tvPhone.getText() != null ? tvPhone.getText().toString().trim() : "";
                    if (!TextUtils.isEmpty(tel)) {
                        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + tel)));
                    }
                });
            }
            if (btnOpenSite != null) {
                btnOpenSite.setOnClickListener(v -> {
                    CharSequence cs = tvHomepage.getText();
                    String url = cs != null ? cs.toString().replaceAll("<.*?>", "").trim() : "";
                    if (!TextUtils.isEmpty(url)) {
                        if (!url.startsWith("http")) url = "http://" + url;
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    }
                });
            }
        }));
    }

    // detailImage2
    private void fetchImages(String contentId) {
        SpotApiHelper.fetchDetailImages(contentId, urls -> runOnUiThread(() -> {
            List<String> list = urls != null ? urls : new ArrayList<>();
            galleryAdapter.submit(list);
        }));
    }

    // detailIntro2 (코스 전용)
    private void fetchCourseIntro(String contentId) {
        api.getCourseIntro2("AND","localit","json", contentId, 25, serviceKey)
                .enqueue(new Callback<CourseIntroResponse>() {
                    @Override public void onResponse(Call<CourseIntroResponse> call, Response<CourseIntroResponse> res) {
                        if (!res.isSuccessful()
                                || res.body()==null
                                || res.body().response==null
                                || res.body().response.body==null
                                || res.body().response.body.items==null
                                || res.body().response.body.items.item==null
                                || res.body().response.body.items.item.isEmpty()) return;

                        CourseIntroResponse.CourseIntroItem it =
                                res.body().response.body.items.item.get(0);

                        runOnUiThread(() -> {
                            // 상단 요약 한 줄
                            StringBuilder sb = new StringBuilder();
                            if (!TextUtils.isEmpty(it.schedule)) sb.append("일정: ").append(it.schedule).append("  ");
                            if (!TextUtils.isEmpty(it.taketime)) sb.append("소요시간: ").append(it.taketime).append("  ");
                            if (!TextUtils.isEmpty(it.distance)) sb.append("거리: ").append(it.distance).append("  ");
                            if (!TextUtils.isEmpty(it.theme))    sb.append("\n테마: ").append(it.theme);
                            setText(tvCourseIntro, sb.toString());

                            // 개별 라인(섹션이 있으면 채움)
                            setText(tvCourseSchedule, label("일정: ", it.schedule));
                            setText(tvCourseDistance, label("거리: ", it.distance));
                            setText(tvCourseTaketime, label("소요시간: ", it.taketime));
                            setText(tvCourseTheme,    label("테마: ", it.theme));
                        });
                    }
                    @Override public void onFailure(Call<CourseIntroResponse> call, Throwable t) { /* no-op */ }
                });
    }

    // detailInfo2 (코스 전용, 서브코스 리스트)
    private void fetchCourseInfo(String contentId) {
        api.getCourseInfo2("AND","localit","json", contentId, 25, serviceKey)
                .enqueue(new Callback<CourseInfoResponse>() {
                    @Override public void onResponse(Call<CourseInfoResponse> call, Response<CourseInfoResponse> res) {
                        List<CourseInfoResponse.CourseInfoItem> list = new ArrayList<>();
                        if (res.isSuccessful()
                                && res.body()!=null
                                && res.body().response!=null
                                && res.body().response.body!=null
                                && res.body().response.body.items!=null
                                && res.body().response.body.items.item!=null) {
                            list = res.body().response.body.items.item;
                        }
                        List<CourseInfoResponse.CourseInfoItem> finalList = list;
                        runOnUiThread(() -> courseAdapter.submit(finalList));
                    }
                    @Override public void onFailure(Call<CourseInfoResponse> call, Throwable t) { /* no-op */ }
                });
    }

    // utils
    private void setText(TextView tv, String v) {
        if (tv == null) return;
        String s = v == null ? "" : v.trim();
        tv.setText(s);
    }
    private String stripHtml(String html) {
        if (TextUtils.isEmpty(html)) return "";
        return HtmlCompat.fromHtml(html.replaceAll("(?i)<br\\s*/?>","\n"),
                HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim();
    }
    private String join(String a, String b) {
        if (TextUtils.isEmpty(a)) return b == null ? "" : b;
        if (TextUtils.isEmpty(b)) return a;
        return a + " " + b;
    }
    private String label(String head, String body) {
        if (TextUtils.isEmpty(body)) return "";
        return head + body;
    }
}
