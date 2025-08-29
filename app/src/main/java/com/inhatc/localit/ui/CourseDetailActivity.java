package com.inhatc.localit.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
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
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CourseDetailActivity extends AppCompatActivity {

    public static final String EXTRA_CONTENT_ID = "extra_content_id";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_FALLBACK_IMAGE_URI = "extra_fallback_image_uri";

    private static final String BASE_URL = "https://apis.data.go.kr/B551011/KorService1/";

    private ImageView imageMain;
    private TextView textTitle;
    private TextView tvAddress, tvPhone, tvHomepage, tvCourseIntro;
    private TextView btnCopyAddress, btnCall, btnOpenSite;
    private RecyclerView recyclerGallery, recyclerCourse;

    private final GalleryAdapter galleryAdapter = new GalleryAdapter();
    private final CourseStepAdapter courseAdapter = new CourseStepAdapter();

    private ApiService api;
    private String serviceKey;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        // Retrofit + ApiService
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(ApiService.class);

        // 키는 디코딩하지 말고 그대로 사용
        serviceKey = BuildConfig.TOUR_API_KEY;

        // 뷰
        imageMain     = findViewById(R.id.imageMain);
        textTitle     = findViewById(R.id.textTitle);
        tvAddress     = findViewById(R.id.tvAddress);
        tvPhone       = findViewById(R.id.tvPhone);
        tvHomepage    = findViewById(R.id.tvHomepage);
        tvCourseIntro = findViewById(R.id.tvCourseIntro);

        btnCopyAddress = findViewById(R.id.btnCopyAddress);
        btnCall        = findViewById(R.id.btnCall);
        btnOpenSite    = findViewById(R.id.btnOpenSite);

        recyclerGallery = findViewById(R.id.recyclerGallery);
        recyclerGallery.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerGallery.setAdapter(galleryAdapter);

        recyclerCourse = findViewById(R.id.recyclerCourse);
        recyclerCourse.setLayoutManager(new LinearLayoutManager(this));
        recyclerCourse.setAdapter(courseAdapter);

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

    private void fetchCommon(String contentId) {
        api.getCourseDetailCommon(
                serviceKey, "AND", "Localit", "json",
                contentId, 25,
                "Y", "Y", "Y", "Y", "Y" // mapinfoYN = "Y" 로 변경
        ).enqueue(new Callback<DetailResponse>() {
            @Override public void onResponse(Call<DetailResponse> call, Response<DetailResponse> res) {
                if (!res.isSuccessful() || res.body() == null ||
                        res.body().response == null ||
                        res.body().response.body == null ||
                        res.body().response.body.items == null ||
                        res.body().response.body.items.item == null ||
                        res.body().response.body.items.item.isEmpty()) return;

                DetailResponse.DetailItem it = res.body().response.body.items.item.get(0);

                if (!TextUtils.isEmpty(it.firstimage)) {
                    Glide.with(CourseDetailActivity.this).load(it.firstimage).into(imageMain);
                }
                tvAddress.setText(!TextUtils.isEmpty(it.addr1) ? it.addr1 : "정보 없음");
                tvPhone.setText(!TextUtils.isEmpty(it.tel) ? it.tel : "정보 없음");
                if (!TextUtils.isEmpty(it.homepage)) {
                    tvHomepage.setText(Html.fromHtml(it.homepage, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    tvHomepage.setText("정보 없음");
                }

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
                        if (!TextUtils.isEmpty(url)) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                        }
                    }
                });
            }
            @Override public void onFailure(Call<DetailResponse> call, Throwable t) { /* no-op */ }
        });
    }

    private void fetchImages(String contentId) {
        api.getDetailImages(serviceKey, "AND", "Localit", "json",
                        contentId, "Y", "Y")
                .enqueue(new Callback<ImageResponse>() {
                    @Override public void onResponse(Call<ImageResponse> call, Response<ImageResponse> res) {
                        List<String> list = new ArrayList<>();
                        if (res.isSuccessful() && res.body() != null &&
                                res.body().response != null &&
                                res.body().response.body != null &&
                                res.body().response.body.items != null &&
                                res.body().response.body.items.item != null) {
                            for (ImageResponse.ImageItem it : res.body().response.body.items.item) {
                                if (!TextUtils.isEmpty(it.originimgurl)) list.add(it.originimgurl);
                            }
                        }
                        galleryAdapter.submit(list);
                    }
                    @Override public void onFailure(Call<ImageResponse> call, Throwable t) { /* no-op */ }
                });
    }

    private void fetchCourseIntro(String contentId) {
        api.getCourseIntro(serviceKey, "AND", "Localit", "json", contentId, 25)
                .enqueue(new Callback<CourseIntroResponse>() {
                    @Override public void onResponse(Call<CourseIntroResponse> call, Response<CourseIntroResponse> res) {
                        if (!res.isSuccessful() || res.body() == null ||
                                res.body().response == null ||
                                res.body().response.body == null ||
                                res.body().response.body.items == null ||
                                res.body().response.body.items.item == null ||
                                res.body().response.body.items.item.isEmpty()) return;

                        CourseIntroResponse.CourseIntroItem it = res.body().response.body.items.item.get(0);
                        StringBuilder sb = new StringBuilder();
                        if (!TextUtils.isEmpty(it.schedule))  sb.append("일정: ").append(it.schedule).append("  ");
                        if (!TextUtils.isEmpty(it.taketime))  sb.append("소요시간: ").append(it.taketime).append("  ");
                        if (!TextUtils.isEmpty(it.distance))  sb.append("거리: ").append(it.distance).append("  ");
                        if (!TextUtils.isEmpty(it.theme))     sb.append("\n테마: ").append(it.theme);
                        tvCourseIntro.setText(sb.toString());
                    }
                    @Override public void onFailure(Call<CourseIntroResponse> call, Throwable t) { /* no-op */ }
                });
    }

    private void fetchCourseInfo(String contentId) {
        api.getCourseInfo(serviceKey, "AND", "Localit", "json", contentId, 25)
                .enqueue(new Callback<CourseInfoResponse>() {
                    @Override public void onResponse(Call<CourseInfoResponse> call, Response<CourseInfoResponse> res) {
                        List<CourseInfoResponse.CourseInfoItem> list = new ArrayList<>();
                        if (res.isSuccessful() && res.body() != null &&
                                res.body().response != null &&
                                res.body().response.body != null &&
                                res.body().response.body.items != null &&
                                res.body().response.body.items.item != null) {
                            list = res.body().response.body.items.item;
                        }
                        courseAdapter.submit(list);
                    }
                    @Override public void onFailure(Call<CourseInfoResponse> call, Throwable t)
                });
    }
}
