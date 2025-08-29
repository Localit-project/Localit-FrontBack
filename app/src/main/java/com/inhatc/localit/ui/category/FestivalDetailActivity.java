package com.inhatc.localit.ui.category;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.util.Patterns;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 축제 상세 화면 — VisitKorea 공통/상세/이미지 매핑 */
public class FestivalDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "FestivalDetail";

    // 갤러리
    private RecyclerView recyclerGallery;
    private GalleryAdapter galleryAdapter;
    private LinearLayout galleryContainer, galleryDots;
    private ImageButton btnGalleryPrev, btnGalleryNext;
    private LinearLayoutManager galleryLm;
    private final List<String> gallery = new ArrayList<>();
    private int galleryStep = 3;
    private int visiblePerPage = 1;
    private static final int GALLERY_ITEM_DP = 118;

    public static final String EXTRA_CONTENT_ID      = "extra_content_id";
    public static final String EXTRA_CONTENT_TYPE_ID = "extra_content_type_id";
    public static final String EXTRA_TITLE           = "extra_title";
    public static final String EXTRA_ADDR1           = "extra_addr1";
    public static final String EXTRA_FIRST_IMAGE     = "extra_first_image";

    // 상단/본문
    private ImageView imageMain, btnBack;
    private TextView  textTitle;

    // 행 컨테이너(가시성 제어)
    private LinearLayout rowAddr, rowTel, rowHomepage, rowRestdate, rowUsetime, rowParking, rowExtra, rowUsefee;

    // 공통 텍스트
    private TextView tvAddress, tvPhone, tvHomepage, tvHoliday, tvHours, tvParking, tvExtra;

    // 액션
    private TextView btnCopyAddress, btnCall, btnOpenSite;

    // 축제 전용(상세 intro)
    private TextView tvStartDate, tvEndDate, tvPlaytime, tvSponsor1, tvSponsor1Tel, tvSponsor2, tvUsefee;

    // 하단 내비 / 지도
    private BottomNavigationView navView;
    private MapView mapView;
    private NaverMap naverMap;
    private Marker marker;
    private Double lat, lng;

    // 전달값
    private String contentId, contentTypeId, passedTitle, passedAddr1, passedFirstImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_festival_detail);

        bindViews();

        // 뒤로가기
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        setupBottomNavigationView();

        // 지도
        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
        }

        // 인텐트
        Intent intent = getIntent();
        if (intent == null) { finishWithError("잘못된 접근입니다."); return; }
        contentId        = nvl(intent.getStringExtra(EXTRA_CONTENT_ID), intent.getStringExtra("contentId"));
        contentTypeId    = nvl(intent.getStringExtra(EXTRA_CONTENT_TYPE_ID),
                nvl(intent.getStringExtra("contentTypeId"), "15"));
        passedTitle      = intent.getStringExtra(EXTRA_TITLE);
        passedAddr1      = intent.getStringExtra(EXTRA_ADDR1);
        passedFirstImage = intent.getStringExtra(EXTRA_FIRST_IMAGE);
        if (TextUtils.isEmpty(contentId)) { finishWithError("contentId 없음"); return; }

        Log.d(TAG, "contentId=" + contentId + ", contentTypeId=" + contentTypeId);

        // 선표시
        setTextOrGone(textTitle,  passedTitle);
        setTextOrGone(tvAddress,  passedAddr1);
        if (!TextUtils.isEmpty(passedFirstImage)) {
            Glide.with(this).load(passedFirstImage)
                    .placeholder(R.drawable.sample1).error(R.drawable.sample1).into(imageMain);
        } else {
            imageMain.setImageResource(R.drawable.sample1);
        }

        // 갤러리 초기화
        setupGallery();

        // ============== detailCommon ==============
        SpotApiHelper.fetchDetailCommon(contentId, contentTypeId, (SpotDetailCommonResponse.Item item) -> runOnUiThread(() -> {
            if (item == null) { Log.w(TAG, "COMMON item is null"); return; }

            // 제목
            if (!TextUtils.isEmpty(item.title)) setTextOrGone(textTitle, item.title);

            // 주소
            String addrJoined = joinAddr(item.addr1, item.addr2);
            setTextOrGone(tvAddress, addrJoined);
            if (rowAddr != null) rowAddr.setVisibility(TextUtils.isEmpty(tvAddress.getText()) ? View.GONE : View.VISIBLE);

            // 전화
            setTextWithHtmlOrGone(tvPhone, item.tel);
            if (rowTel != null) rowTel.setVisibility(TextUtils.isEmpty(tvPhone.getText()) ? View.GONE : View.VISIBLE);

            // 홈페이지
            setHomepageOrGone(tvHomepage, item.homepage);
            if (rowHomepage != null) rowHomepage.setVisibility(TextUtils.isEmpty(tvHomepage.getText()) ? View.GONE : View.VISIBLE);

            // 대표 이미지
            String img = !TextUtils.isEmpty(item.firstimage) ? item.firstimage
                    : !TextUtils.isEmpty(item.firstimage2) ? item.firstimage2 : passedFirstImage;
            Glide.with(this).load(img).placeholder(R.drawable.sample1).error(R.drawable.sample1).into(imageMain);

            // 행사소개(overview)
            setTextWithHtmlOrGone(tvExtra, item.overview);
            if (rowExtra != null) rowExtra.setVisibility(TextUtils.isEmpty(s(tvExtra != null ? tvExtra.getText().toString() : "")) ? View.GONE : View.VISIBLE);

            // 좌표/지오코딩
            try {
                if (!TextUtils.isEmpty(item.mapy) && !TextUtils.isEmpty(item.mapx)) {
                    lat = Double.parseDouble(item.mapy); // 위도
                    lng = Double.parseDouble(item.mapx); // 경도
                    updateMapMarker();
                } else if (!TextUtils.isEmpty(addrJoined)) {
                    geocodeAndMove(addrJoined);
                }
            } catch (Exception e) {
                Log.e(TAG, "Parsing map coords failed", e);
            }
        }));

        // ============== detailIntro(축제: 15) ==============
        SpotApiHelper.fetchDetailIntro(contentId, safeInt(contentTypeId, 15), (SpotDetailIntroResponse.Item intro) -> runOnUiThread(() -> {
            if (intro == null) { Log.w(TAG, "INTRO item is null"); return; }

            // 날짜
            setTextOrGone(tvStartDate, fmtDate(s(intro.eventstartdate)));
            setTextOrGone(tvEndDate,   fmtDate(s(intro.eventenddate)));

            // 공연시간
            setTextOrGone(tvPlaytime, intro.playtime);

            // 영업/이용시간(축제: usetimefestival) -> "영업시간" 섹션에만 표시
            setTextWithHtmlOrGone(tvHours, intro.usetimefestival);
            if (rowUsetime != null) rowUsetime.setVisibility(TextUtils.isEmpty(tvHours.getText()) ? View.GONE : View.VISIBLE);

            // 이용요금 -> 전용 섹션에 표시 (무료 등)
            setTextWithHtmlOrGone(tvUsefee, intro.usefee);
            if (rowUsefee != null) rowUsefee.setVisibility(TextUtils.isEmpty(tvUsefee.getText()) ? View.GONE : View.VISIBLE);

            // 주차/휴무
            setTextWithHtmlOrGone(tvParking, intro.parking);
            if (rowParking != null) rowParking.setVisibility(TextUtils.isEmpty(tvParking.getText()) ? View.GONE : View.VISIBLE);

            setTextWithHtmlOrGone(tvHoliday, intro.restdate);
            if (rowRestdate != null) rowRestdate.setVisibility(TextUtils.isEmpty(tvHoliday.getText()) ? View.GONE : View.VISIBLE);

            // 전화: common이 비었으면 intro의 안내전화로 대체
            if (TextUtils.isEmpty(tvPhone.getText())) {
                String tel = firstNonEmpty(intro.infocenterfestival, intro.infocenter);
                setTextWithHtmlOrGone(tvPhone, tel);
                if (rowTel != null) rowTel.setVisibility(TextUtils.isEmpty(tvPhone.getText()) ? View.GONE : View.VISIBLE);
            }

            // 홈페이지 보강
            if (TextUtils.isEmpty(tvHomepage.getText())) {
                setHomepageOrGone(tvHomepage, intro.homepage);
                if (rowHomepage != null) rowHomepage.setVisibility(TextUtils.isEmpty(tvHomepage.getText()) ? View.GONE : View.VISIBLE);
            }

            // 주최/연락처/주관
            setTextOrGone(tvSponsor1,    intro.sponsor1);
            setTextOrGone(tvSponsor1Tel, intro.sponsor1tel);
            setTextOrGone(tvSponsor2,    intro.sponsor2);
        }));

        // ============== 추가이미지 ==============
        SpotApiHelper.fetchDetailImages(contentId, urls -> runOnUiThread(() -> {
            gallery.clear();
            if (urls != null) for (String u : urls) if (!TextUtils.isEmpty(u)) gallery.add(u);
            if (galleryAdapter != null) galleryAdapter.notifyDataSetChanged();

            if (galleryContainer != null) {
                galleryContainer.setVisibility(gallery.isEmpty() ? View.GONE : View.VISIBLE);
            }
            updateDots();
        }));

        // ===== 액션 버튼 =====
        if (btnCopyAddress != null) {
            btnCopyAddress.setOnClickListener(v -> {
                CharSequence addr = tvAddress != null ? tvAddress.getText() : "";
                if (!TextUtils.isEmpty(addr)) {
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    if (cm != null) {
                        cm.setPrimaryClip(ClipData.newPlainText("address", addr));
                        Toast.makeText(this, "주소가 복사되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        if (btnCall != null) {
            btnCall.setOnClickListener(v -> {
                String raw = tvPhone != null && tvPhone.getText()!=null ? tvPhone.getText().toString() : null;
                String number = extractFirstPhone(raw);
                if (!TextUtils.isEmpty(number)) {
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number)));
                } else {
                    Toast.makeText(this, "전화번호가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }
        if (btnOpenSite != null) {
            btnOpenSite.setOnClickListener(v -> {
                CharSequence sp = tvHomepage != null ? tvHomepage.getText() : null;
                String url = extractFirstUrl(sp);
                if (TextUtils.isEmpty(url) && sp != null) url = sp.toString().trim();
                if (!TextUtils.isEmpty(url)) {
                    if (!url.startsWith("http")) url = "http://" + url;
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } else {
                    Toast.makeText(this, "홈페이지 주소가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void bindViews() {
        imageMain = findViewById(R.id.imageMain);
        btnBack   = findViewById(R.id.btnBack);
        textTitle = findViewById(R.id.textTitle);

        // 행 컨테이너
        rowAddr      = findViewById(R.id.textAddr);
        rowTel       = findViewById(R.id.textTel);
        rowHomepage  = findViewById(R.id.rowHomepage);
        rowRestdate  = findViewById(R.id.textRestdate);
        rowUsetime   = findViewById(R.id.textUsetime);
        rowParking   = findViewById(R.id.textParking);
        rowExtra     = findViewById(R.id.textExtraInfo);
        rowUsefee    = findViewById(R.id.textUsefee);

        // 텍스트
        tvAddress  = findViewById(R.id.tvAddress);
        tvPhone    = findViewById(R.id.tvPhone);
        tvHomepage = findViewById(R.id.tvHomepage);
        tvHoliday  = findViewById(R.id.tvHoliday);
        tvHours    = findViewById(R.id.tvHours);
        tvParking  = findViewById(R.id.tvParking);
        tvExtra    = findViewById(R.id.tvExtra);
        tvUsefee   = findViewById(R.id.tvUsefee);

        // 액션
        btnCopyAddress = findViewById(R.id.btnCopyAddress);
        btnCall        = findViewById(R.id.btnCall);
        btnOpenSite    = findViewById(R.id.btnOpenSite);

        // 축제 전용
        tvStartDate   = findViewById(R.id.tvStartDate);
        tvEndDate     = findViewById(R.id.tvEndDate);
        tvPlaytime    = findViewById(R.id.tvPlaytime);
        tvSponsor1    = findViewById(R.id.tvSponsor1);
        tvSponsor1Tel = findViewById(R.id.tvSponsor1Tel);

        // 갤러리
        recyclerGallery  = findViewById(R.id.recyclerGallery);
        galleryContainer = findViewById(R.id.galleryContainer);
        btnGalleryPrev   = findViewById(R.id.btnGalleryPrev);
        btnGalleryNext   = findViewById(R.id.btnGalleryNext);
        galleryDots      = findViewById(R.id.galleryDots);

        // 지도/네비
        mapView = findViewById(R.id.mapView);
        navView = findViewById(R.id.nav_view);

        // 초기 가림
        if (rowHomepage != null && tvHomepage != null && TextUtils.isEmpty(tvHomepage.getText())) {
            rowHomepage.setVisibility(View.GONE);
        }
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

        if ((lat == null || lng == null) && tvAddress != null) {
            CharSequence addr = tvAddress.getText();
            if (addr != null && addr.toString().trim().length() > 0) geocodeAndMove(addr.toString());
        }
    }

    private void updateMapMarker() {
        if (naverMap == null || lat == null || lng == null) return;
        LatLng pos = new LatLng(lat, lng);
        if (marker == null) marker = new Marker();
        marker.setPosition(pos);
        marker.setMap(naverMap);

        String caption = (textTitle != null && !TextUtils.isEmpty(String.valueOf(textTitle.getText())))
                ? textTitle.getText().toString()
                : (tvAddress != null ? String.valueOf(tvAddress.getText()) : "");
        if (!TextUtils.isEmpty(caption)) marker.setCaptionText(caption);

        naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(pos, 15.0));
    }

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
                    runOnUiThread(() -> Toast.makeText(this, "주소 지오코딩 실패: " + address, Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                Log.e(TAG, "Geocoder failed for address=" + address, e);
                runOnUiThread(() -> Toast.makeText(this, "지오코더 오류", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // -------- MapView lifecycle --------
    @Override protected void onStart()   { super.onStart();   if (mapView != null) mapView.onStart(); }
    @Override protected void onResume()  { super.onResume();  if (mapView != null) mapView.onResume(); }
    @Override protected void onPause()   { if (mapView != null) mapView.onPause();  super.onPause(); }
    @Override protected void onStop()    { if (mapView != null) mapView.onStop();   super.onStop(); }
    @Override protected void onDestroy() { if (mapView != null) mapView.onDestroy(); super.onDestroy(); }
    @Override public void onLowMemory()  { super.onLowMemory(); if (mapView != null) mapView.onLowMemory(); }

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

    private void finishWithError(String msg) { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show(); finish(); }

    // ---------- 공용 유틸 ----------
    private String s(String v) { return v == null ? "" : v; }
    private String nl(String v) {
        return v == null ? "" :
                v.replaceAll("(?i)<br\\s*/?>", "\n")
                        .replaceAll("(?i)</p>", "\n")
                        .replaceAll("(?s)<[^>]*>", "")
                        .trim();
    }
    private void setTextOrGone(TextView tv, String value) {
        if (tv == null) return;
        String val = s(value);
        if (TextUtils.isEmpty(val.trim())) { tv.setText(""); tv.setVisibility(View.GONE); }
        else { tv.setVisibility(View.VISIBLE); tv.setText(val); }
    }
    private void setTextWithHtmlOrGone(TextView tv, String html) {
        if (tv == null) return;
        if (TextUtils.isEmpty(html)) { tv.setText(""); tv.setVisibility(View.GONE); return; }
        String normalized = html.replaceAll("(?i)<br\\s*/?>", "\n");
        CharSequence spanned = HtmlCompat.fromHtml(normalized, HtmlCompat.FROM_HTML_MODE_LEGACY);
        if (TextUtils.isEmpty(spanned) || spanned.toString().trim().isEmpty()) {
            tv.setText(""); tv.setVisibility(View.GONE);
        } else {
            tv.setVisibility(View.VISIBLE);
            tv.setText(spanned);
            tv.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }
    private void setHomepageOrGone(TextView tv, String html) { setTextWithHtmlOrGone(tv, html); }
    private String joinAddr(String a1, String a2) {
        if (TextUtils.isEmpty(a1)) return s(a2);
        if (TextUtils.isEmpty(a2)) return s(a1);
        return a1 + " " + a2;
    }
    private String nvl(String a, String b) { return TextUtils.isEmpty(a) ? b : a; }
    private int safeInt(String s, int def) { try { return Integer.parseInt(s); } catch (Exception e) { return def; } }
    private String fmtDate(String raw) {
        if (raw == null) return "";
        String digits = raw.replaceAll("\\D+", "");
        if (digits.length() < 8) return raw;
        digits = digits.substring(0, 8);
        try { return digits.substring(0,4)+"."+digits.substring(4,6)+"."+digits.substring(6,8); }
        catch (Exception e) { return raw; }
    }
    private @Nullable String extractFirstUrl(CharSequence sp) {
        if (sp instanceof Spanned) {
            URLSpan[] spans = ((Spanned) sp).getSpans(0, sp.length(), URLSpan.class);
            if (spans != null && spans.length > 0) return spans[0].getURL();
        }
        if (sp == null) return null;
        Matcher m = Patterns.WEB_URL.matcher(sp.toString());
        return m.find() ? m.group() : null;
    }
    private @Nullable String extractFirstPhone(String htmlOrText) {
        if (TextUtils.isEmpty(htmlOrText)) return null;
        CharSequence sp = HtmlCompat.fromHtml(htmlOrText, HtmlCompat.FROM_HTML_MODE_LEGACY);
        if (sp instanceof Spanned) {
            URLSpan[] spans = ((Spanned) sp).getSpans(0, sp.length(), URLSpan.class);
            if (spans != null) {
                for (URLSpan u : spans) {
                    if (u.getURL() != null && u.getURL().startsWith("tel:")) {
                        return u.getURL().substring(4);
                    }
                }
            }
        }
        Matcher m = Pattern.compile("(0\\d{1,2}-?\\d{3,4}-?\\d{4})").matcher(sp.toString());
        return m.find() ? m.group(1) : null;
    }
    private String firstNonEmpty(String... arr) {
        for (String s : arr) if (!TextUtils.isEmpty(s) && s.trim().length() > 0) return s;
        return null;
    }

    // ----- 갤러리 페이지 계산 -----
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

    // 사진 좌우 움직이는 점
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

    /** 갤러리 초기 설정 */
    private void setupGallery() {
        if (recyclerGallery == null) return;

        galleryLm = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        recyclerGallery.setLayoutManager(galleryLm);

        galleryAdapter = new GalleryAdapter(gallery, url ->
                Glide.with(FestivalDetailActivity.this)
                        .load(url)
                        .placeholder(R.drawable.sample1)
                        .error(R.drawable.sample1)
                        .into(imageMain));
        recyclerGallery.setAdapter(galleryAdapter);

        recyclerGallery.post(() -> {
            int rvWidth = recyclerGallery.getWidth();
            float density = getResources().getDisplayMetrics().density;
            int itemWidth = (int) (GALLERY_ITEM_DP * density);
            visiblePerPage = Math.max(1, rvWidth / Math.max(1, itemWidth));
            updateDots();
        });

        recyclerGallery.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) { updateDots(); }
        });

        if (btnGalleryPrev != null) {
            btnGalleryPrev.setOnClickListener(v -> {
                int first = galleryLm.findFirstVisibleItemPosition();
                recyclerGallery.smoothScrollToPosition(Math.max(0, first - galleryStep));
            });
        }
        if (btnGalleryNext != null) {
            btnGalleryNext.setOnClickListener(v -> {
                int last = galleryLm.findLastVisibleItemPosition();
                recyclerGallery.smoothScrollToPosition(
                        Math.min(Math.max(0, gallery.size() - 1), last + galleryStep));
            });
        }
    }
}
