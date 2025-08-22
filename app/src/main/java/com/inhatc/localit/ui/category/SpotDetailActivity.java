package com.inhatc.localit.ui.category;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.inhatc.localit.MainActivity;
import com.inhatc.localit.R;
import com.inhatc.localit.api.SpotApiHelper;
import com.inhatc.localit.api.SpotDetailCommonResponse;
import com.inhatc.localit.api.SpotDetailIntroResponse;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class SpotDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "SpotDetail";

    public static final String EXTRA_CONTENT_ID      = "extra_content_id";
    public static final String EXTRA_CONTENT_TYPE_ID = "extra_content_type_id";
    public static final String EXTRA_TITLE           = "extra_title";
    public static final String EXTRA_ADDR1           = "extra_addr1";
    public static final String EXTRA_FIRST_IMAGE     = "extra_first_image";

    private ImageView imageMain, btnBack;
    private RecyclerView recyclerGallery;
    private GalleryAdapter galleryAdapter;

    private TextView textTitle, textOverview, textAddr, textZipcode,
            textTel, textRestdate, textUsetime, textParking, textUsefee, labelZip;
    private BottomNavigationView navView;

    // --- Gallery UI ---
    private View          galleryContainer;     // 전체 컨테이너 (비어있으면 숨김)
    private ImageButton   btnGalleryPrev, btnGalleryNext;
    private LinearLayout  galleryDots;
    private LinearLayoutManager galleryLm;
    private int galleryStep = 3;                // 화살표 클릭 시 이동 칸 수
    private int visiblePerPage = 1;             // 한 화면에 보이는 썸네일 개수(실측 후 계산)
    private static final int GALLERY_ITEM_DP = 118; // item width(110dp) + marginEnd(8dp) ≈ 118dp

    private final List<String> gallery = new ArrayList<>();
    private MapView mapView;
    private NaverMap naverMap;
    private Marker marker;
    private Double lat, lng;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spot_detail);

        Intent intent = getIntent();
        if (intent == null) { finishWithError("잘못된 접근입니다."); return; }

        String contentId = nvl(intent.getStringExtra(EXTRA_CONTENT_ID), intent.getStringExtra("contentId"));
        String contentTypeId = nvl(
                intent.getStringExtra(EXTRA_CONTENT_TYPE_ID),
                nvl(intent.getStringExtra("contentTypeId"), "12")
        );
        String passedTitle = intent.getStringExtra(EXTRA_TITLE);
        String passedAddr1 = intent.getStringExtra(EXTRA_ADDR1);
        String passedFirstImage = intent.getStringExtra(EXTRA_FIRST_IMAGE);

        if (TextUtils.isEmpty(contentId)) { finishWithError("상세 조회에 필요한 contentId 가 없습니다."); return; }

        bindViews();

        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        setupBottomNavigationView();

        // 선표시
        setTextOrGone(textTitle, passedTitle);
        setTextOrGone(textAddr, passedAddr1);
        if (!TextUtils.isEmpty(passedFirstImage)) {
            Glide.with(this).load(passedFirstImage)
                    .placeholder(R.drawable.sample1)
                    .error(R.drawable.sample1)
                    .into(imageMain);
        } else {
            imageMain.setImageResource(R.drawable.sample1);
        }

        // ===== Gallery 세팅 =====
        galleryLm = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        recyclerGallery.setLayoutManager(galleryLm);
        galleryAdapter = new GalleryAdapter(gallery, url ->
                Glide.with(SpotDetailActivity.this)
                        .load(url)
                        .placeholder(R.drawable.sample1)
                        .error(R.drawable.sample1)
                        .into(imageMain));
        recyclerGallery.setAdapter(galleryAdapter);

        // 화면 폭 기준으로 한 페이지에 몇 개 보일지 계산
        recyclerGallery.post(() -> {
            int rvWidth = recyclerGallery.getWidth();
            float density = getResources().getDisplayMetrics().density;
            int itemWidth = (int) (GALLERY_ITEM_DP * density);
            visiblePerPage = Math.max(1, rvWidth / Math.max(1, itemWidth));
            updateDots();
        });

        // 스크롤 시 점 갱신
        recyclerGallery.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                updateDots();
            }
        });

        // 좌우 버튼
        if (btnGalleryPrev != null) {
            btnGalleryPrev.setOnClickListener(v -> {
                int first = galleryLm.findFirstVisibleItemPosition();
                int target = Math.max(0, first - galleryStep);
                recyclerGallery.smoothScrollToPosition(target);
            });
        }
        if (btnGalleryNext != null) {
            btnGalleryNext.setOnClickListener(v -> {
                int last = galleryLm.findLastVisibleItemPosition();
                int target = Math.min(Math.max(0, gallery.size() - 1), last + galleryStep);
                recyclerGallery.smoothScrollToPosition(target);
            });
        }

        // ===== 지도 =====
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // ===== detailCommon (개요/주소/연락처/좌표/대표이미지 등) =====
        SpotApiHelper.fetchDetailCommon(contentId, contentTypeId, (SpotDetailCommonResponse.Item item) -> runOnUiThread(() -> {
            if (item == null) return;

            // 개요 (HTML 파싱으로 교체)
            setTextWithHtmlOrGone(textOverview, item.overview);

            // 주소
            setTextOrGone(textAddr, joinAddr(item.addr1, item.addr2));

            // 우편번호
            if (!TextUtils.isEmpty(item.zipcode)) {
                if (labelZip != null) labelZip.setVisibility(View.VISIBLE);
                setTextOrGone(textZipcode, item.zipcode);
            } else {
                if (labelZip != null) labelZip.setVisibility(View.GONE);
                if (textZipcode != null) textZipcode.setVisibility(View.GONE);
            }

            // 문의 및 안내 (HTML 포함 가능)
            setTextWithHtmlOrGone(textTel, item.tel);

            // 대표 이미지 교체
            String img = !TextUtils.isEmpty(item.firstimage) ? item.firstimage :
                    !TextUtils.isEmpty(item.firstimage2) ? item.firstimage2 :
                            passedFirstImage;  // ← 인텐트로 받은 선표시 이미지까지 폴백
            Glide.with(this).load(img)
                    .placeholder(R.drawable.sample1)
                    .error(R.drawable.sample1)
                    .into(imageMain);



            // 좌표 처리
            try {
                Log.d(TAG, "COMMON raw coords mapx=" + item.mapx + ", mapy=" + item.mapy);
                if (!TextUtils.isEmpty(item.mapy) && !TextUtils.isEmpty(item.mapx)) {
                    lat = Double.parseDouble(item.mapy); // 위도
                    lng = Double.parseDouble(item.mapx); // 경도
                    updateMapMarker();
                } else {
                    String addr = joinAddr(item.addr1, item.addr2);
                    if (!TextUtils.isEmpty(addr)) geocodeAndMove(addr);
                    else Toast.makeText(this, "좌표/주소 없음", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Parsing map coords failed", e);
                Toast.makeText(this, "좌표 파싱 실패", Toast.LENGTH_SHORT).show();
            }
        }));

        // ===== detailIntro (휴무/이용시간/주차/입장료 등) =====
        SpotApiHelper.fetchDetailIntro(contentId, safeInt(contentTypeId, 12), (SpotDetailIntroResponse.Item intro) -> runOnUiThread(() -> {
            if (intro == null) return;

            setTextWithHtmlOrGone(textRestdate, intro.restdate);
            setTextWithHtmlOrGone(textUsetime,  intro.usetime);
            setTextWithHtmlOrGone(textParking,  intro.parking);

            String fee = stripHtml(intro.usefee);
            textUsefee.setText(TextUtils.isEmpty(fee.trim()) ? "입장료 없음" : fee);
            textUsefee.setVisibility(View.VISIBLE);

            if (TextUtils.isEmpty(textTel.getText())) {
                setTextWithHtmlOrGone(textTel, intro.infocenter);
            }
        }));

        // ===== detailImage (보조 이미지) =====
        SpotApiHelper.fetchDetailImages(contentId, urls -> runOnUiThread(() -> {
            gallery.clear();
            for (String u : urls) if (!TextUtils.isEmpty(u)) gallery.add(u);
            galleryAdapter.notifyDataSetChanged();

            // 데이터 유무에 따라 컨테이너 가시성 / 점 갱신
            if (galleryContainer != null) {
                galleryContainer.setVisibility(gallery.isEmpty() ? View.GONE : View.VISIBLE);
            }
            updateDots();
        }));
    }

    private void bindViews() {
        imageMain        = findViewById(R.id.imageMain);
        btnBack          = findViewById(R.id.btnBack);
        recyclerGallery  = findViewById(R.id.recyclerGallery);
        textTitle        = findViewById(R.id.textTitle);
        textOverview     = findViewById(R.id.textOverview);
        textAddr         = findViewById(R.id.textAddr);
        textTel          = findViewById(R.id.textTel);
        textRestdate     = findViewById(R.id.textRestdate);
        textUsetime      = findViewById(R.id.textUsetime);
        textParking      = findViewById(R.id.textParking);
        textUsefee       = findViewById(R.id.textUsefee);
        mapView          = findViewById(R.id.mapView);
        navView          = findViewById(R.id.nav_view);

        // gallery UI
        galleryContainer = findViewById(R.id.galleryContainer);
        btnGalleryPrev   = findViewById(R.id.btnGalleryPrev);
        btnGalleryNext   = findViewById(R.id.btnGalleryNext);
        galleryDots      = findViewById(R.id.galleryDots);
    }

    // ---------------- 지도 ----------------
    @Override public void onMapReady(@NonNull NaverMap map) {
        naverMap = map;
        naverMap.getUiSettings().setScaleBarEnabled(false);
        naverMap.getUiSettings().setZoomControlEnabled(true);

        if (lat == null || lng == null) {
            naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(new LatLng(36.5, 127.9), 6.5));
        }
        updateMapMarker();

        if ((lat == null || lng == null) && textAddr != null) {
            CharSequence addr = textAddr.getText();
            if (addr != null && addr.toString().trim().length() > 0) geocodeAndMove(addr.toString());
        }
    }

    /** 준비된 좌표를 지도에 마커로 표시 + 캡션 + 카메라 이동 */
    private void updateMapMarker() {
        if (naverMap == null || lat == null || lng == null) return;

        LatLng pos = new LatLng(lat, lng);

        if (marker == null) marker = new Marker();
        marker.setPosition(pos);
        marker.setMap(naverMap);

        String caption = (textTitle != null && !TextUtils.isEmpty(String.valueOf(textTitle.getText())))
                ? textTitle.getText().toString()
                : (textAddr != null ? String.valueOf(textAddr.getText()) : "");
        if (!TextUtils.isEmpty(caption)) marker.setCaptionText(caption);

        naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(pos, 15.0));
    }

    /** 주소를 좌표로 변환하고 지도 갱신(폴백) */
    private void geocodeAndMove(String address) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Geocoder g = new Geocoder(this, Locale.KOREA);
                List<Address> r = g.getFromLocationName(address, 1);
                if (r != null && !r.isEmpty()) {
                    lat = r.get(0).getLatitude();
                    lng = r.get(0).getLongitude();
                    runOnUiThread(this::updateMapMarker);
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "주소 지오코딩 실패: " + address, Toast.LENGTH_SHORT).show()
                    );
                }
            } catch (Exception e) {
                Log.e(TAG, "Geocoder failed for address=" + address, e);
                runOnUiThread(() ->
                        Toast.makeText(this, "지오코더 오류", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    // -------- MapView lifecycle --------
    @Override protected void onStart()   { super.onStart();   mapView.onStart(); }
    @Override protected void onResume()  { super.onResume();  mapView.onResume(); }
    @Override protected void onPause()   { mapView.onPause(); super.onPause(); }
    @Override protected void onStop()    { mapView.onStop();  super.onStop(); }
    @Override protected void onDestroy() { mapView.onDestroy(); super.onDestroy(); }
    @Override public void onLowMemory()  { super.onLowMemory(); mapView.onLowMemory(); }

    private void setupBottomNavigationView() {
        if (navView == null) return;
        navView.setOnItemSelectedListener(item -> {
            startActivity(new Intent(this, MainActivity.class)
                    .putExtra("start_fragment",
                            item.getItemId() == R.id.navigation_home ? 0 :
                                    item.getItemId() == R.id.navigation_category ? 1 :
                                            item.getItemId() == R.id.navigation_search ? 2 :
                                                    item.getItemId() == R.id.navigation_favorite ? 3 : 4));
            finish();
            return true;
        });
    }

    private void finishWithError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        finish();
    }

    // ---------- Gallery dots ----------
    private int getCurrentPage() {
        if (gallery.isEmpty() || visiblePerPage <= 0) return 0;
        int first = Math.max(0, galleryLm.findFirstVisibleItemPosition());
        return first / visiblePerPage;
    }

    private int getTotalPages() {
        if (gallery.isEmpty() || visiblePerPage <= 0) return 1;
        int pages = (int) Math.ceil(gallery.size() / (double) visiblePerPage);
        return Math.max(1, pages);
    }

    private void updateDots() {
        if (galleryDots == null) return;
        int total = getTotalPages();
        int cur   = getCurrentPage();

        if (galleryDots.getChildCount() != total) {
            galleryDots.removeAllViews();
            int size = (int) (6 * getResources().getDisplayMetrics().density);
            int margin = (int) (4 * getResources().getDisplayMetrics().density);
            for (int i = 0; i < total; i++) {
                View dot = new View(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
                lp.setMargins(margin, 0, margin, 0);
                dot.setLayoutParams(lp);
                dot.setBackgroundResource(R.drawable.shape_dot_inactive);
                galleryDots.addView(dot);
            }
        }
        for (int i = 0; i < galleryDots.getChildCount(); i++) {
            galleryDots.getChildAt(i).setBackgroundResource(
                    i == cur ? R.drawable.shape_dot_active : R.drawable.shape_dot_inactive
            );
        }
    }

    // ---------- 유틸 ----------
    private String s(String v) { return v == null ? "" : v; }

    /** 태그 제거만 필요한 곳에서 사용 */
    private String stripHtml(String v) {
        if (v == null) return "";
        return v.replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?s)<[^>]*>", "");
    }

    /** 개요/문의/시간 등 HTML 포함 텍스트 표시용 (핵심 변경) */
    private void setTextWithHtmlOrGone(TextView tv, String html) {
        if (TextUtils.isEmpty(html)) {
            tv.setText("");
            tv.setVisibility(View.GONE);
            return;
        }
        CharSequence spanned = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY);
        if (TextUtils.isEmpty(spanned)) {
            tv.setText("");
            tv.setVisibility(View.GONE);
        } else {
            tv.setVisibility(View.VISIBLE);
            tv.setText(spanned);
            tv.setSingleLine(false);
            tv.setEllipsize(null);
            tv.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    private void setTextOrGone(TextView tv, String value) {
        String ss = s(value);
        if (TextUtils.isEmpty(ss.trim())) {
            tv.setText("");
            tv.setVisibility(View.GONE);
        } else {
            tv.setVisibility(View.VISIBLE);
            tv.setText(ss);
        }
    }

    private String joinAddr(String a1, String a2) {
        if (TextUtils.isEmpty(a1)) return s(a2);
        if (TextUtils.isEmpty(a2)) return s(a1);
        return a1 + " " + a2;
    }

    private String nvl(String a, String b) { return TextUtils.isEmpty(a) ? b : a; }

    private int safeInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }
}
