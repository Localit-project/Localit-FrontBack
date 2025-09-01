package com.inhatc.localit.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.inhatc.localit.BuildConfig;
import com.inhatc.localit.R;
import com.inhatc.localit.api.home.ApiClient;
import com.inhatc.localit.api.home.ApiService;
import com.inhatc.localit.api.home.CourseInfoResponse;
import com.inhatc.localit.api.home.CourseIntroResponse;
import com.inhatc.localit.api.home.DetailResponse;
import com.inhatc.localit.api.home.ImageResponse;
import com.inhatc.localit.ui.course.CourseStepAdapter;
import com.inhatc.localit.ui.course.GalleryAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseDetailActivity extends AppCompatActivity {

    public static final String EXTRA_CONTENT_ID = "extra_content_id";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_FALLBACK_IMAGE_URI = "extra_fallback_image_uri";

    private ImageView imageMain;
    private TextView textTitle;

    private TextView tvAddress;
    private TextView tvPhone;
    private TextView tvHomepage;
    private TextView tvCourseIntro;
    private TextView tvExtra;

    private TextView btnCopyAddress;
    private TextView btnCall;
    private TextView btnOpenSite;

    private RecyclerView recyclerGallery;
    private RecyclerView recyclerCourseSteps;

    private View sectionCourseInfo;
    private View sectionCourseList;

    // 코스정보(인트로) 디테일 텍스트
    private TextView tvCourseSchedule;
    private TextView tvCourseDistance;
    private TextView tvCourseTaketime;
    private TextView tvCourseTheme;

    private final GalleryAdapter galleryAdapter = new GalleryAdapter();
    private final CourseStepAdapter courseAdapter = new CourseStepAdapter();

    private ApiService api;
    private String serviceKey; // URLDecode 하지 말 것

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        // API
        api = ApiClient.get();
        serviceKey = BuildConfig.TOUR_API_KEY;

        // 바인딩
        imageMain      = findViewById(R.id.imageMain);
        textTitle      = findViewById(R.id.textTitle);
        tvAddress      = findViewById(R.id.tvAddress);
        tvPhone        = findViewById(R.id.tvPhone);
        tvHomepage     = findViewById(R.id.tvHomepage);
        tvCourseIntro  = findViewById(R.id.tvCourseIntro);
        tvExtra        = findViewById(R.id.tvExtra);

        btnCopyAddress = findViewById(R.id.btnCopyAddress);
        btnCall        = findViewById(R.id.btnCall);
        btnOpenSite    = findViewById(R.id.btnOpenSite);

        sectionCourseInfo = findViewById(R.id.sectionCourseInfo);
        sectionCourseList = findViewById(R.id.sectionCourseList);

        tvCourseSchedule = findViewById(R.id.tvCourseSchedule);
        tvCourseDistance = findViewById(R.id.tvCourseDistance);
        tvCourseTaketime = findViewById(R.id.tvCourseTaketime);
        tvCourseTheme    = findViewById(R.id.tvCourseTheme);

        recyclerGallery = findViewById(R.id.recyclerGallery);
        recyclerGallery.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerGallery.setAdapter(galleryAdapter);

        // 코스 단계 리스트는 sectionCourseList 안의 RecyclerView 사용
        recyclerCourseSteps = findViewById(R.id.recyclerCourseSteps);
        if (recyclerCourseSteps == null) {
            // 레이아웃에 recyclerCourseSteps가 없는 경우(구버전 호환) recyclerCourse를 시도
            recyclerCourseSteps = findViewById(R.id.recyclerCourse);
        }
        recyclerCourseSteps.setLayoutManager(new LinearLayoutManager(this));
        recyclerCourseSteps.setAdapter(courseAdapter);

        ImageButton back = findViewById(R.id.btnBack);
        if (back != null) back.setOnClickListener(v -> finish());

        // 인텐트
        Intent i = getIntent();
        String contentId = i.getStringExtra(EXTRA_CONTENT_ID);
        String title     = i.getStringExtra(EXTRA_TITLE);
        String fallback  = i.getStringExtra(EXTRA_FALLBACK_IMAGE_URI);

        if (!TextUtils.isEmpty(title)) textTitle.setText(title);
        if (!TextUtils.isEmpty(fallback)) Glide.with(this).load(Uri.parse(fallback)).into(imageMain);

        if (!TextUtils.isEmpty(contentId)) {
            fetchCommon(contentId);
            fetchImages(contentId);
            fetchCourseIntro(contentId);
            fetchCourseInfo(contentId);
        } else {
            Toast.makeText(this, "contentId 가 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // detailCommon2
    private void fetchCommon(String contentId) {
        api.getCourseDetail(
                serviceKey, "AND", "Localit", "json",
                contentId, 25,
                "Y", "Y", "Y", "Y", "Y"
        ).enqueue(new Callback<DetailResponse>() {
            @Override public void onResponse(Call<DetailResponse> call, Response<DetailResponse> res) {
                if (!res.isSuccessful() || res.body()==null ||
                        res.body().response==null ||
                        res.body().response.body==null ||
                        res.body().response.body.items==null ||
                        res.body().response.body.items.item==null ||
                        res.body().response.body.items.item.isEmpty()) return;

                DetailResponse.Item it = res.body().response.body.items.item.get(0);

                if (!TextUtils.isEmpty(it.firstimage)) {
                    Glide.with(CourseDetailActivity.this).load(it.firstimage).into(imageMain);
                }
                tvAddress.setText(!TextUtils.isEmpty(it.addr1) ? it.addr1 : "정보 없음");
                // 공통응답(v2)에 tel이 없을 수 있으므로 안전 처리
                // 필요 시 detailIntro/Info에서 전화 정보가 없다면 "정보 없음"
                tvPhone.setText("정보 없음");
                // 홈페이지(HTML 링크일 수 있음)
                if (!TextUtils.isEmpty(it.overview)) {
                    // 개요는 tvExtra에 표시 (섹션 "추가정보")
                    tvExtra.setText(it.overview);
                } else {
                    tvExtra.setText("");
                }

                // 홈페이지는 공통 응답에서 비어 있는 경우가 많음. (detailCommon2의 homepage 필드가 없거나 빈 값일 수 있음)
                tvHomepage.setText("정보 없음");

                btnCopyAddress.setOnClickListener(v -> {
                    try {
                        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        cm.setPrimaryClip(ClipData.newPlainText("address", tvAddress.getText()));
                        Toast.makeText(CourseDetailActivity.this, "주소를 복사했습니다.", Toast.LENGTH_SHORT).show();
                    } catch (Exception ignored) {}
                });
                btnCall.setOnClickListener(v -> {
                    String tel = tvPhone.getText().toString();
                    if (!TextUtils.isEmpty(tel) && !"정보 없음".contentEquals(tel)) {
                        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + tel)));
                    }
                });
                btnOpenSite.setOnClickListener(v -> {
                    CharSequence cs = tvHomepage.getText();
                    if (cs != null) {
                        String url = cs.toString().replaceAll("<.*?>", "");
                        if (!TextUtils.isEmpty(url) && url.startsWith("http")) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                        }
                    }
                });
            }
            @Override public void onFailure(Call<DetailResponse> call, Throwable t) { /* no-op */ }
        });
    }

    // detailImage2
    private void fetchImages(String contentId) {
        api.getDetailImages(serviceKey, "AND", "Localit", "json", contentId, "Y", "Y")
                .enqueue(new Callback<ImageResponse>() {
                    @Override public void onResponse(Call<ImageResponse> call, Response<ImageResponse> res) {
                        List<String> list = new ArrayList<>();
                        if (res.isSuccessful() && res.body()!=null &&
                                res.body().response!=null &&
                                res.body().response.body!=null &&
                                res.body().response.body.items!=null &&
                                res.body().response.body.items.item!=null) {
                            for (ImageResponse.Item it : res.body().response.body.items.item) {
                                if (!TextUtils.isEmpty(it.originimgurl)) list.add(it.originimgurl);
                            }
                        }
                        galleryAdapter.submit(list);
                    }
                    @Override public void onFailure(Call<ImageResponse> call, Throwable t) { /* no-op */ }
                });
    }

    // detailIntro2
    private void fetchCourseIntro(String contentId) {
        api.getCourseIntro(serviceKey, "AND", "Localit", "json", contentId, 25)
                .enqueue(new Callback<CourseIntroResponse>() {
                    @Override public void onResponse(Call<CourseIntroResponse> call, Response<CourseIntroResponse> res) {
                        if (!res.isSuccessful() || res.body()==null ||
                                res.body().response==null ||
                                res.body().response.body==null ||
                                res.body().response.body.items==null ||
                                res.body().response.body.items.item==null ||
                                res.body().response.body.items.item.isEmpty()) return;

                        CourseIntroResponse.Item it = res.body().response.body.items.item.get(0);

                        StringBuilder sb = new StringBuilder();
                        if (!TextUtils.isEmpty(it.schedule))  sb.append("일정: ").append(it.schedule).append("  ");
                        if (!TextUtils.isEmpty(it.taketime))  sb.append("소요시간: ").append(it.taketime).append("  ");
                        if (!TextUtils.isEmpty(it.distance))  sb.append("거리: ").append(it.distance).append("  ");
                        if (!TextUtils.isEmpty(it.theme))     sb.append("\n테마: ").append(it.theme);
                        tvCourseIntro.setText(sb.toString().trim());

                        if (sectionCourseInfo != null) sectionCourseInfo.setVisibility(View.VISIBLE);
                        if (tvCourseSchedule != null) tvCourseSchedule.setText("일정: " + (it.schedule==null?"-":it.schedule));
                        if (tvCourseDistance != null) tvCourseDistance.setText("거리: " + (it.distance==null?"-":it.distance));
                        if (tvCourseTaketime != null) tvCourseTaketime.setText("소요시간: " + (it.taketime==null?"-":it.taketime));
                        if (tvCourseTheme != null)    tvCourseTheme.setText("테마: " + (it.theme==null?"-":it.theme));
                    }
                    @Override public void onFailure(Call<CourseIntroResponse> call, Throwable t) { /* no-op */ }
                });
    }

    // detailInfo2
    private void fetchCourseInfo(String contentId) {
        api.getCourseInfo(serviceKey, "AND", "Localit", "json", contentId, 25)
                .enqueue(new Callback<CourseInfoResponse>() {
                    @Override public void onResponse(Call<CourseInfoResponse> call, Response<CourseInfoResponse> res) {
                        List<CourseInfoResponse.Item> list = new ArrayList<>();
                        if (res.isSuccessful() && res.body()!=null &&
                                res.body().response!=null &&
                                res.body().response.body!=null &&
                                res.body().response.body.items!=null &&
                                res.body().response.body.items.item!=null) {
                            list = res.body().response.body.items.item;
                        }
                        if (sectionCourseList != null && !list.isEmpty()) {
                            sectionCourseList.setVisibility(View.VISIBLE);
                        }
                        courseAdapter.submit(list);
                    }
                    @Override public void onFailure(Call<CourseInfoResponse> call, Throwable t) { /* no-op */ }
                });
    }
}
