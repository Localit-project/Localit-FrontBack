package com.inhatc.localit.ui.category;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.inhatc.localit.MainActivity;
import com.inhatc.localit.R;
import com.inhatc.localit.api.SpotApiHelper;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

/** 축제 상세 화면 (XML: activity_festival_detail) — 행사소개 + 개요 보강 + 로그 강화 */
public class FestivalDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "FestivalDetail";

    public static final String EXTRA_CONTENT_ID      = "extra_content_id";
    public static final String EXTRA_CONTENT_TYPE_ID = "extra_content_type_id";
    public static final String EXTRA_TITLE           = "extra_title";
    public static final String EXTRA_ADDR1           = "extra_addr1";
    public static final String EXTRA_FIRST_IMAGE     = "extra_first_image";

    // 상단/본문
    private ImageView imageMain, btnBack;
    private TextView  textTitle;

    // 공통(detailCommon)
    private TextView tvTel, tvAddr, tvOverview;

    // 인트로(detailIntro: 축제)
    private TextView tvSponsor1, tvSponsor1Tel, tvSponsor2,
            tvStartDate, tvEndDate, tvPlaytime,
            tvProgram /*행사소개*/, tvContent /*행사내용 칸*/;

    // (선택) XML에 있으면 분리 표기
    private TextView tvUsefee; // 없으면 null

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

        // 지도 준비
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

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

        Log.d(TAG, "onCreate: contentId=" + contentId + ", contentTypeId=" + contentTypeId);

        // 선표시
        setTextOrGone(textTitle,  passedTitle);
        setTextOrGone(tvAddr,     passedAddr1);
        if (!TextUtils.isEmpty(passedFirstImage)) {
            Glide.with(this).load(passedFirstImage)
                    .placeholder(R.drawable.sample1).error(R.drawable.sample1).into(imageMain);
        } else {
            imageMain.setImageResource(R.drawable.sample1);
        }

        // 공통 상세 (개요/주소/전화/이미지/좌표)
        SpotApiHelper.fetchDetailCommon(contentId, contentTypeId, item -> runOnUiThread(() -> {
            if (item == null) { Log.w(TAG, "COMMON item is null"); return; }

            // ✅ 개요 로그
            String rawOv = item.overview;
            String cookedOv = cookOverview(rawOv);
            Log.d(TAG, "COMMON overview isNull=" + (rawOv == null)
                    + ", rawLen=" + (rawOv == null ? -1 : rawOv.length())
                    + ", cookedLen=" + cookedOv.length());

            // ✅ 개요: 무조건 한 줄이라도 보이게
            tvOverview.setVisibility(android.view.View.VISIBLE);
            tvOverview.setText(TextUtils.isEmpty(cookedOv) ? "개요 정보가 없습니다." : cookedOv);

            // 주소/전화
            setTextOrGone(tvAddr, joinAddr(item.addr1, item.addr2));
            setTextWithBrOrGone(tvTel, item.tel);

            // 제목 보정
            if (!TextUtils.isEmpty(item.title)) setTextOrGone(textTitle, item.title);

            // 대표 이미지 교체
            String img = !TextUtils.isEmpty(item.firstimage) ? item.firstimage : item.firstimage2;
            if (!TextUtils.isEmpty(img)) {
                Glide.with(this).load(img)
                        .placeholder(R.drawable.sample1).error(R.drawable.sample1).into(imageMain);
            }

            // 좌표
            try {
                if (!TextUtils.isEmpty(item.mapy) && !TextUtils.isEmpty(item.mapx)) {
                    lat = Double.parseDouble(item.mapy);
                    lng = Double.parseDouble(item.mapx);
                    updateMapMarker();
                } else {
                    String addr = joinAddr(item.addr1, item.addr2);
                    if (!TextUtils.isEmpty(addr)) geocodeAndMove(addr);
                }
            } catch (Exception e) {
                Log.e(TAG, "Parsing map coords failed", e);
            }
        }));

        // 축제 인트로
        SpotApiHelper.fetchDetailIntro(contentId, safeInt(contentTypeId, 15), intro -> runOnUiThread(() -> {
            if (intro == null) { Log.w(TAG, "INTRO item is null"); return; }

            // 행사소개 로그
            Log.d(TAG, "INTRO program.isNull=" + (intro.program == null)
                    + ", programLen=" + (intro.program == null ? -1 : intro.program.length())
                    + ", subevent.isNull=" + (intro.subevent == null)
                    + ", subeventLen=" + (intro.subevent == null ? -1 : intro.subevent.length())
                    + ", start=" + intro.eventstartdate + ", end=" + intro.eventenddate);

            // ✅ 행사소개: program → subevent → 개요
            String program = s(intro.program);
            if (TextUtils.isEmpty(program.trim())) program = s(intro.subevent);
            if (TextUtils.isEmpty(program.trim())) {
                CharSequence ovTxt = tvOverview.getText();
                if (!TextUtils.isEmpty(ovTxt)) program = ovTxt.toString();
            }
            setTextWithBrOrGone(tvProgram, program);

            // 행사내용 칸
            tvContent.setText("");
            tvContent.setVisibility(TextUtils.isEmpty(program.trim()) ? android.view.View.GONE : android.view.View.VISIBLE);

            // 날짜
            setTextOrGone(tvStartDate, fmtDate(s(intro.eventstartdate)));
            setTextOrGone(tvEndDate,   fmtDate(s(intro.eventenddate)));

            // 기타
            setTextOrGone(tvPlaytime, intro.playtime);
            setTextOrGone(tvSponsor1, intro.sponsor1);
            setTextOrGone(tvSponsor1Tel, intro.sponsor1tel);
            setTextOrGone(tvSponsor2, intro.sponsor2);

            // 입장료
            String fee = nl(s(intro.usefee));
            if (TextUtils.isEmpty(fee.trim())) fee = nl(s(intro.usetimefestival));
            if (TextUtils.isEmpty(fee.trim())) fee = "입장료 없음";

            if (tvUsefee != null) {
                setTextOrGone(tvUsefee, fee);
            } else {
                String base = s(tvContent.getText() == null ? "" : tvContent.getText().toString()).trim();
                String appended = base.isEmpty() ? ("입장료: " + fee) : (base + "\n\n입장료: " + fee);
                tvContent.setVisibility(TextUtils.isEmpty(appended.trim()) ? android.view.View.GONE : android.view.View.VISIBLE);
                tvContent.setText(appended);
            }

            // 문의 보강
            if (TextUtils.isEmpty(s(tvTel.getText() == null ? null : tvTel.getText().toString()).trim())) {
                String info = !TextUtils.isEmpty(s(intro.infocenterfestival)) ? intro.infocenterfestival : s(intro.infocenter);
                setTextWithBrOrGone(tvTel, info);
            }

            // 링크 자동 인식
            Linkify.addLinks(tvProgram, Linkify.WEB_URLS);
            Linkify.addLinks(tvContent, Linkify.WEB_URLS);
        }));
    }

    private void bindViews() {
        imageMain = findViewById(R.id.imageMain);
        btnBack   = findViewById(R.id.btnBack);
        textTitle = findViewById(R.id.textTitle);

        tvOverview = findViewById(R.id.tvOverview);
        tvProgram  = findViewById(R.id.tvProgram);
        tvContent  = findViewById(R.id.tvContent);

        tvStartDate    = findViewById(R.id.tvStartDate);
        tvEndDate      = findViewById(R.id.tvEndDate);
        tvPlaytime     = findViewById(R.id.tvPlaytime);

        tvSponsor1     = findViewById(R.id.tvSponsor1);
        tvSponsor1Tel  = findViewById(R.id.tvSponsor1Tel);
        tvSponsor2     = findViewById(R.id.tvSponsor2);

        tvAddr = findViewById(R.id.tvAddr);
        tvTel  = findViewById(R.id.tvTel);

        int idUsefee = getResources().getIdentifier("tvUsefee", "id", getPackageName());
        tvUsefee = idUsefee == 0 ? null : findViewById(idUsefee);

        mapView = findViewById(R.id.mapView);
        navView = findViewById(R.id.nav_view);
    }

    // ---------------- 지도 ----------------
    @Override public void onMapReady(@NonNull NaverMap map) {
        naverMap = map;
        naverMap.getUiSettings().setScaleBarEnabled(false);
        naverMap.getUiSettings().setZoomControlEnabled(true);
        updateMapMarker();
    }

    private void updateMapMarker() {
        if (naverMap == null || lat == null || lng == null) return;
        LatLng pos = new LatLng(lat, lng);
        if (marker == null) marker = new Marker();
        marker.setPosition(pos);
        marker.setMap(naverMap);
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
                }
            } catch (Exception e) {
                Log.e(TAG, "Geocoder failed for address=" + address, e);
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

    // ---------- 유틸 ----------
    private String s(String v) { return v == null ? "" : v; }

    private String cookOverview(String v) {
        if (v == null) return "";
        String s = v
                .replaceAll("(?i)&nbsp;|&#160;", " ")
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</p>", "\n")
                .replaceAll("(?s)<[^>]*>", "")
                .replace("\r", "\n")
                .trim();
        s = s.replaceAll("\\n{3,}", "\n\n");
        if (s.replaceAll("\\s+", "").isEmpty()) return "";
        return s;
    }

    private String nl(String v) {
        if (v == null) return "";
        return v.replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</p>", "\n")
                .replaceAll("(?s)<[^>]*>", "")
                .trim();
    }

    private void setTextWithBrOrGone(TextView tv, String value) {
        String cooked = nl(value);
        if (TextUtils.isEmpty(cooked.trim())) {
            tv.setText("");
            tv.setVisibility(android.view.View.GONE);
        } else {
            tv.setVisibility(android.view.View.VISIBLE);
            tv.setText(cooked);
            tv.setSingleLine(false);
            tv.setEllipsize(null);
        }
    }

    private void setTextOrGone(TextView tv, String value) {
        String val = s(value);
        if (TextUtils.isEmpty(val.trim())) {
            tv.setText("");
            tv.setVisibility(android.view.View.GONE);
        } else {
            tv.setVisibility(android.view.View.VISIBLE);
            tv.setText(val);
        }
    }

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
        if (digits.length() >= 8) {
            digits = digits.substring(0, 8);
        } else {
            return raw;
        }
        try {
            String yyyy = digits.substring(0, 4);
            String MM   = digits.substring(4, 6);
            String dd   = digits.substring(6, 8);
            return yyyy + "." + MM + "." + dd;
        } catch (Exception e) {
            return raw;
        }
    }
}