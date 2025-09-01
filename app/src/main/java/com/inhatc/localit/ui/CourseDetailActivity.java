package com.inhatc.localit.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.inhatc.localit.R;
import com.inhatc.localit.api.home.TourApiHelper;
import com.inhatc.localit.api.home.TourItem;

import java.util.ArrayList;
import java.util.List;

public class CourseDetailActivity extends AppCompatActivity {

    // === Intent Extras ===
    public static final String EXTRA_CONTENT_ID = "extra_content_id";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_FALLBACK_IMAGE_URI = "extra_fallback_image_uri";
    public static final String EXTRA_CONTENT_TYPE_ID = "extra_content_type_id"; // HomeFragment에서 전달
    public static final String EXTRA_HOMEPAGE = "extra_homepage";
    private static final String TAG = "CourseDetailActivity";

    // === Data ===
    private String contentId;
    private String contenttypeid;   // 25면 여행코스
    private String initialTitle;
    private String fallbackImageUri;
    private String homepageExtra;

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
    private LinearLayout rowExtraInfo; // 코스 섹션의 부모 컨테이너
    private TextView tvExtra;          // 개요 텍스트뷰만 토글

    private ProgressBar progress;
    private TextView tvError;

    // 코스 전용 섹션
    private View sectionCourseInfo, sectionCourseList;
    private TextView tvCourseSchedule, tvCourseDistance, tvCourseTaketime, tvCourseTheme;
    private RecyclerView recyclerCourseSteps;
    private StepsAdapter stepsAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        // === Get Intent ===
        Intent intent = getIntent();
        contentId = intent.getStringExtra(EXTRA_CONTENT_ID);
        contenttypeid = intent.getStringExtra(EXTRA_CONTENT_TYPE_ID);
        initialTitle = intent.getStringExtra(EXTRA_TITLE);
        fallbackImageUri = intent.getStringExtra(EXTRA_FALLBACK_IMAGE_URI);
        homepageExtra = intent.getStringExtra(EXTRA_HOMEPAGE);

        // 비어있으면 코스(25)로 기본값
        if (TextUtils.isEmpty(contenttypeid)) contenttypeid = "25";

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

        // 코스 섹션 바인딩
        sectionCourseInfo = findViewById(R.id.sectionCourseInfo);
        sectionCourseList = findViewById(R.id.sectionCourseList);
        tvCourseSchedule = findViewById(R.id.tvCourseSchedule);
        tvCourseDistance = findViewById(R.id.tvCourseDistance);
        tvCourseTaketime = findViewById(R.id.tvCourseTaketime);
        tvCourseTheme = findViewById(R.id.tvCourseTheme);
        recyclerCourseSteps = findViewById(R.id.recyclerCourseSteps);

        recyclerCourseSteps.setLayoutManager(new LinearLayoutManager(this));
        stepsAdapter = new StepsAdapter();
        recyclerCourseSteps.setAdapter(stepsAdapter);

        // === Init UI ===
        textRegionTitle.setText("상세 정보");
        textTitle.setText(!TextUtils.isEmpty(initialTitle) ? initialTitle : "코스 상세");
        loadImage(fallbackImageUri, imageMain);

        btnBack.setOnClickListener(v -> finish());

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

        // ★ 코스 추가정보(인트로/스텝) 먼저 병렬로 호출
        loadCourseExtrasIfNeeded();

        // === 기본 상세 (title/이미지/주소/개요) ===
        setLoading(true);
        TourApiHelper.fetchDetailSummary(
                contentId,
                TextUtils.isEmpty(contenttypeid) ? null : contenttypeid,
                items -> runOnUiThread(() -> {
                    setLoading(false);
                    if (items == null || items.isEmpty()) {
                        // 전체 화면을 가리지 말고 토스트만
                        Toast.makeText(this, "공통 상세 정보 없음 (코스 정보만 표시)", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    bind(items.get(0));
                })
        );
        android.util.Log.d("CourseDetail", "contentId=" + contentId + ", contenttypeid=" + contenttypeid);
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

        // 홈페이지 (인텐트로만)
        if (!TextUtils.isEmpty(homepageExtra)) {
            tvHomepage.setText(homepageExtra);
            rowHomepage.setVisibility(View.VISIBLE);
        } else {
            rowHomepage.setVisibility(View.GONE);
        }

        // 개요 텍스트만 토글, 부모(rowExtraInfo)는 항상 VISIBLE
        String overview = safe(() -> item.overview);
        if (!TextUtils.isEmpty(overview)) {
            tvExtra.setText(overview);
            tvExtra.setVisibility(View.VISIBLE);
        } else {
            tvExtra.setText("");
            tvExtra.setVisibility(View.GONE);
        }
        rowExtraInfo.setVisibility(View.VISIBLE);

        // infoSection 가시성 (rowExtraInfo는 코스 섹션 컨테이너이므로 그대로 VISIBLE)
        boolean hasInfo =
                rowAddr.getVisibility() == View.VISIBLE ||
                        rowHomepage.getVisibility() == View.VISIBLE ||
                        rowExtraInfo.getVisibility() == View.VISIBLE;
        infoSection.setVisibility(hasInfo ? View.VISIBLE : View.GONE);
    }

    /** 코스 타입(25)이면 코스 개요/스텝까지 추가 로드 */
    private void loadCourseExtrasIfNeeded() {
        boolean isCourse = "25".equals(contenttypeid);
        if (!isCourse) {
            sectionCourseInfo.setVisibility(View.GONE);
            sectionCourseList.setVisibility(View.GONE);
            return;
        }

        TourApiHelper.fetchCourseExtra(contentId, (intro, steps) -> runOnUiThread(() -> {
            // Intro(일정/거리/소요시간/테마)
            if (intro != null &&
                    (!isEmpty(intro.schedule) || !isEmpty(intro.distance)
                            || !isEmpty(intro.taketime) || !isEmpty(intro.theme))) {
                tvCourseSchedule.setText("일정: " + dash(intro.schedule));
                tvCourseDistance.setText("거리: " + dash(intro.distance));
                tvCourseTaketime.setText("소요시간: " + dash(intro.taketime));
                tvCourseTheme.setText("테마: " + dash(intro.theme));
                sectionCourseInfo.setVisibility(View.VISIBLE);

                // 부모/상위도 켜주기
                rowExtraInfo.setVisibility(View.VISIBLE);
                infoSection.setVisibility(View.VISIBLE);
            } else {
                sectionCourseInfo.setVisibility(View.GONE);
            }

            // Steps(코스 단계)
            if (steps != null && !steps.isEmpty()) {
                stepsAdapter.submit(steps);
                sectionCourseList.setVisibility(View.VISIBLE);

                // 부모/상위도 켜주기
                rowExtraInfo.setVisibility(View.VISIBLE);
                infoSection.setVisibility(View.VISIBLE);
            } else {
                sectionCourseList.setVisibility(View.GONE);
            }

            android.util.Log.d("CourseDetail", "intro=" + (intro != null) +
                    ", steps=" + (steps == null ? -1 : steps.size()));
        }));
        android.util.Log.d("CourseDetail", "isCourse=" + "25".equals(contenttypeid));
    }

    // ===== 이미지 로딩 =====
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

    // ===== 로딩/에러 UI =====
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
        // 전체 가리지는 않도록 유지
        View scroll = findViewById(R.id.scrollView);
        if (scroll != null) scroll.setVisibility(View.VISIBLE);
        if (progress != null) progress.setVisibility(View.GONE);
    }

    // ===== 유틸 =====
    private static boolean isEmpty(String s) { return s == null || s.trim().isEmpty(); }
    private static String dash(String s) { return isEmpty(s) ? "-" : s; }

    // 안전 접근 헬퍼
    private interface Getter<T> { T get() throws Exception; }
    private <T> T safe(Getter<T> g) { try { return g.get(); } catch (Throwable t) { return null; } }

    // ===== 코스 단계 어댑터 =====
    private static class StepsAdapter extends RecyclerView.Adapter<StepsAdapter.StepsVH> {
        private final List<TourApiHelper.CourseStep> data = new ArrayList<>();

        void submit(List<TourApiHelper.CourseStep> list) {
            data.clear();
            if (list != null) data.addAll(list);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public StepsVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_course_step, parent, false);
            return new StepsVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull StepsVH h, int position) {
            h.bind(data.get(position));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class StepsVH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDesc;
            ImageView iv;

            StepsVH(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvStepTitle);
                tvDesc  = itemView.findViewById(R.id.tvStepDesc);
                iv      = itemView.findViewById(R.id.ivStepImage);
            }

            void bind(TourApiHelper.CourseStep s) {
                String title = (s.order > 0 ? s.order + ". " : "") + (s.title == null ? "" : s.title);
                tvTitle.setText(title);
                tvDesc.setText(s.overview == null ? "" : s.overview);

                if (!TextUtils.isEmpty(s.image)) {
                    Glide.with(iv.getContext())
                            .load(s.image)
                            .placeholder(R.drawable.sample1)
                            .error(R.drawable.sample1)
                            .into(iv);
                } else {
                    iv.setImageResource(R.drawable.sample1);
                }
            }
        }
    }
}
