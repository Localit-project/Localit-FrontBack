package com.inhatc.localit.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.inhatc.localit.R;
import com.inhatc.localit.api.home.TourApiHelper;
import com.inhatc.localit.api.home.TourItem;

import java.util.Collections;

public class CourseDetailActivity extends AppCompatActivity {

    // === Intent Extras ===
    public static final String EXTRA_CONTENT_ID = "extra_content_id";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_FALLBACK_IMAGE_URI = "extra_fallback_image_uri";
    public static final String EXTRA_CONTENT_TYPE_ID = "extra_content_type_id"; // HomeFragment에서 참조
    public static final String EXTRA_HOMEPAGE = "extra_homepage"; // 선택: 홈페이지만 따로 넘길 때

    // === Data ===
    private String contentId;
    private String contentTypeId;       // 선택
    private String initialTitle;
    private String fallbackImageUri;
    private String homepageExtra;       // 선택

    // === Views ===
    private ImageView btnBack;
    private TextView textRegionTitle;
    private TextView textTitle;
    private ImageView imageMain;

    private LinearLayout rowAddr;
    private TextView tvAddress;
    private TextView btnCopyAddress;

    private LinearLayout rowHomepage;
    private TextView tvHomepage;
    private TextView btnOpenSite;

    private LinearLayout infoSection;
    private LinearLayout rowExtraInfo;
    private TextView tvExtra;

    private ProgressBar progress;
    private TextView tvError;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        // === Get Intent ===
        Intent intent = getIntent();
        contentId = intent.getStringExtra(EXTRA_CONTENT_ID);
        contentTypeId = intent.getStringExtra(EXTRA_CONTENT_TYPE_ID); // 사용 여부는 선택
        initialTitle = intent.getStringExtra(EXTRA_TITLE);
        fallbackImageUri = intent.getStringExtra(EXTRA_FALLBACK_IMAGE_URI);
        homepageExtra = intent.getStringExtra(EXTRA_HOMEPAGE);

        // === Bind Views ===
        btnBack = findViewById(R.id.btnBack);
        textRegionTitle = findViewById(R.id.textRegionTitle);
        textTitle = findViewById(R.id.textTitle);
        imageMain = findViewById(R.id.imageMain);

        rowAddr = findViewById(R.id.textAddr);
        tvAddress = findViewById(R.id.tvAddress);
        btnCopyAddress = findViewById(R.id.btnCopyAddress);

        rowHomepage = findViewById(R.id.rowHomepage);
        tvHomepage = findViewById(R.id.tvHomepage);
        btnOpenSite = findViewById(R.id.btnOpenSite);

        infoSection = findViewById(R.id.infoSection);
        rowExtraInfo = findViewById(R.id.textExtraInfo);
        tvExtra = findViewById(R.id.tvExtra);

        progress = findViewById(R.id.progress);
        tvError = findViewById(R.id.tvError);

        // === Init UI ===
        textRegionTitle.setText("상세 정보");
        textTitle.setText(!TextUtils.isEmpty(initialTitle) ? initialTitle : "코스 상세");
        loadImage(fallbackImageUri, imageMain);

        btnBack.setOnClickListener(v -> finish()); // 심볼 에러 방지

        btnCopyAddress.setOnClickListener(v -> {
            String addr = tvAddress.getText() != null ? tvAddress.getText().toString() : "";
            if (!TextUtils.isEmpty(addr)) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText("address", addr));
                Toast.makeText(this, "주소를 복사했어요.", Toast.LENGTH_SHORT).show();
            }
        });

        btnOpenSite.setOnClickListener(v -> {
            CharSequence url = tvHomepage.getText();
            if (!TextUtils.isEmpty(url)) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url.toString())));
                } catch (Exception e) {
                    Toast.makeText(this, "사이트를 열 수 없어요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (TextUtils.isEmpty(contentId)) {
            showError("잘못된 코스 ID입니다.");
            return;
        }

        // === Load (요약 위주: title/firstimage/addr1/overview 중심) ===
        setLoading(true);
        TourApiHelper.fetchCourseSummaries(Collections.singletonList(contentId), items -> runOnUiThread(() -> {
            setLoading(false);
            if (items == null || items.isEmpty()) {
                showError("상세 정보를 불러오지 못했습니다.");
                return;
            }
            bind(items.get(0));
        }));
    }

    private void bind(TourItem item) {
        // 제목
        String title = !TextUtils.isEmpty(item.title)
                ? item.title
                : (!TextUtils.isEmpty(initialTitle) ? initialTitle : "코스 상세");
        textTitle.setText(title);

        // 대표 이미지
        if (!TextUtils.isEmpty(item.firstimage)) {
            loadImage(item.firstimage, imageMain);
        } else {
            loadImage(fallbackImageUri, imageMain);
        }

        // 주소
        String addr = safe(() -> item.addr1);
        if (!TextUtils.isEmpty(addr)) {
            tvAddress.setText(addr);
            rowAddr.setVisibility(View.VISIBLE);
        } else {
            rowAddr.setVisibility(View.GONE);
        }

        // 홈페이지 (TourItem에 필드가 없을 수 있어 인텐트로만 처리)
        if (!TextUtils.isEmpty(homepageExtra)) {
            tvHomepage.setText(homepageExtra);
            rowHomepage.setVisibility(View.VISIBLE);
        } else {
            rowHomepage.setVisibility(View.GONE);
        }

        // 개요 → 추가정보
        String overview = safe(() -> item.overview);
        if (!TextUtils.isEmpty(overview)) {
            tvExtra.setText(overview);
            rowExtraInfo.setVisibility(View.VISIBLE);
        } else {
            rowExtraInfo.setVisibility(View.GONE);
        }

        // infoSection 보여줄지 결정
        if (rowAddr.getVisibility() == View.GONE
                && rowHomepage.getVisibility() == View.GONE
                && rowExtraInfo.getVisibility() == View.GONE) {
            infoSection.setVisibility(View.GONE);
        } else {
            infoSection.setVisibility(View.VISIBLE);
        }
    }

    private void loadImage(String uriOrUrl, ImageView target) {
        if (TextUtils.isEmpty(uriOrUrl)) {
            target.setImageResource(R.drawable.sample1);
            return;
        }
        Glide.with(this)
                .load(uriOrUrl)
                .placeholder(R.drawable.sample1)
                .error(R.drawable.sample1)
                .into(target);
    }

    private void setLoading(boolean show) {
        if (progress != null) progress.setVisibility(show ? View.VISIBLE : View.GONE);
        View scroll = findViewById(R.id.scrollView);
        if (scroll != null) scroll.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        if (tvError != null) tvError.setVisibility(View.GONE);
    }

    private void showError(String msg) {
        if (tvError != null) {
            tvError.setText(msg);
            tvError.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }
        View scroll = findViewById(R.id.scrollView);
        if (scroll != null) scroll.setVisibility(View.INVISIBLE);
        if (progress != null) progress.setVisibility(View.GONE);
    }

    // 안전 접근 헬퍼
    private interface Getter<T> { T get() throws Exception; }
    private <T> T safe(Getter<T> g) { try { return g.get(); } catch (Throwable t) { return null; } }
}
