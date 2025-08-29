// CourseDetailActivity.java
package com.inhatc.localit.ui.course;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.inhatc.localit.R;
import com.inhatc.localit.api.home.ApiClient;
import com.inhatc.localit.api.home.ApiService;
import com.inhatc.localit.api.home.DetailResponse;

import retrofit2.*;

public class CourseDetailActivity extends AppCompatActivity {

    private static final String SERVICE_KEY = "URL_ENCODED_YOUR_KEY";
    private static final String OS = "ETC";
    private static final String APP = "Localit";

    private ImageView imgCover;
    private TextView tvTitle, tvAddr, tvOverview;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        imgCover = findViewById(R.id.imgCover);
        tvTitle  = findViewById(R.id.tvTitle);
        tvAddr   = findViewById(R.id.tvAddr);
        tvOverview = findViewById(R.id.tvOverview);

        String contentId = getIntent().getStringExtra("contentId");
        String imageUri  = getIntent().getStringExtra("imageUri");

        // 상단 커버(홈에서 넘겨준 리소스/URL)
        if (imageUri != null) {
            Object src = imageUri.startsWith("android.resource://") ? Uri.parse(imageUri) : imageUri;
            Glide.with(this).load(src).placeholder(R.drawable.sample1).into(imgCover);
        }

        // 상세 정보 불러오기
        ApiService api = ApiClient.service();
        api.getCourseDetail(
                SERVICE_KEY, OS, APP, "json",
                contentId, 25,
                "Y","Y","Y","Y","Y"
        ).enqueue(new Callback<DetailResponse>() {
            @Override public void onResponse(Call<DetailResponse> call, Response<DetailResponse> res) {
                DetailResponse d = res.body();
                if (d != null && d.response != null && d.response.body != null &&
                        d.response.body.items != null && d.response.body.items.item != null &&
                        !d.response.body.items.item.isEmpty()) {

                    DetailResponse.DetailItem it = d.response.body.items.item.get(0);
                    tvTitle.setText(nullToEmpty(it.title));
                    tvAddr.setText(nullToEmpty(it.addr1));
                    tvOverview.setText(nullToEmpty(it.overview));

                    // API 쪽 이미지가 있으면 커버 교체
                    if (it.firstimage != null && !it.firstimage.isEmpty()) {
                        Glide.with(CourseDetailActivity.this).load(it.firstimage).into(imgCover);
                    }
                }
            }
            @Override public void onFailure(Call<DetailResponse> call, Throwable t) { /* TODO: 에러 처리 */ }
        });
    }

    private String nullToEmpty(String s) { return s == null ? "" : s; }
}
