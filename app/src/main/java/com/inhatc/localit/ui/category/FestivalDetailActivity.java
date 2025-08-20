package com.inhatc.localit.ui.category;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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
import com.inhatc.localit.api.SpotDetailCommonResponse;
import com.inhatc.localit.api.SpotDetailIntroResponse;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class FestivalDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_CONTENT_ID      = "extra_content_id";
    public static final String EXTRA_CONTENT_TYPE_ID = "extra_content_type_id";
    public static final String EXTRA_TITLE           = "extra_title";
    public static final String EXTRA_ADDR1           = "extra_addr1";
    public static final String EXTRA_FIRST_IMAGE     = "extra_first_image";

    // 상단/본문
    private ImageView imageMain, btnBack;
    private TextView  textTitle;

    // 공통(detailCommon2)
    private TextView tvZipcode, tvTelName, tvTel, tvAddr, tvOverview;

    // 인트로(detailIntro2: 축제)
    private TextView tvSponsor1, tvSponsor1Tel, tvSponsor2,
            tvStartDate, tvEndDate, tvPlaytime,
            tvProgress, tvFestivalType, tvProgram, tvContent;

    // 네비
    private BottomNavigationView navView;

    // 지도
    private MapView mapView;
    private NaverMap naverMap;
    private Marker marker;
    private Double lat;  // mapy(위도)
    private Double lng;  // mapx(경도)

    // 전달값
    private String contentId, contentTypeId, passedTitle, passedAddr1, passedFirstImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_festival_detail);

        bindViews();

        // 뒤로가기(ESC처럼)
        if (btnBack != null) btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        setupBottomNavigationView();

        // 지도 준비
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // 인텐트 파싱
        Intent intent = getIntent();
        if (intent == null) { finishWithError("잘못된 접근입니다."); return; }
        contentId        = s(intent.getStringExtra(EXTRA_CONTENT_ID));
        contentTypeId    = nvl(intent.getStringExtra(EXTRA_CONTENT_TYPE_ID), "15");
        passedTitle      = s(intent.getStringExtra(EXTRA_TITLE));
        passedAddr1      = s(intent.getStringExtra(EXTRA_ADDR1));
        passedFirstImage = s(intent.getStringExtra(EXTRA_FIRST_IMAGE));
        if (TextUtils.isEmpty(contentId)) { finishWithError("contentId 없음"); return; }

        // 선표시
        setTextOrGone(textTitle,  passedTitle);
        setTextOrGone(tvAddr,     passedAddr1);
        if (!TextUtils.isEmpty(passedFirstImage)) {
            Glide.with(this).load(passedFirstImage)
                    .placeholder(R.drawable.sample1).error(R.drawable.sample1).into(imageMain);
        } else {
            imageMain.setImageResource(R.drawable.sample1);
        }

        // 공통 상세
        SpotApiHelper.fetchDetailCommon(contentId, contentTypeId, item -> runOnUiThread(() -> {
            if (item == null) return;

            // XML 순서에 맞춰 노출
            setTextWithBrOrGone(tvOverview, item.overview);                 // 개요
            setTextOrGone(tvAddr, joinAddr(item.addr1, item.addr2));        // 주소
            setTextOrGone(tvZipcode, item.zipcode);                         // 우편번호
            setTextWithBrOrGone(tvTel, item.tel);                           // 전화번호
            setTextOrGone(tvTelName, rf(item, "telname"));                  // 전화명(있을 때만)

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
                    lat = Double.parseDouble(item.mapy); // mapy=위도
                    lng = Double.parseDouble(item.mapx); // mapx=경도
                    updateMapMarker();
                } else {
                    // 좌표 없으면 주소 지오코딩
                    String addr = joinAddr(item.addr1, item.addr2);
                    if (!TextUtils.isEmpty(addr)) geocodeAndMove(addr);
                }
            } catch (Exception ignore) {}
        }));

        // 축제 인트로
        int ctid = 15;
        try { ctid = Integer.parseInt(contentTypeId); } catch (Exception ignore) {}
        SpotApiHelper.fetchDetailIntro(contentId, ctid, it -> runOnUiThread(() -> {
            if (it == null) return;

            String sponsor1     = rf(it, "sponsor1");
            String sponsor1tel  = rf(it, "sponsor1tel");
            String sponsor2     = rf(it, "sponsor2");
            String startDate    = rf(it, "eventstartdate");
            String endDate      = rf(it, "eventenddate");
            String playtime     = rf(it, "playtime");
            String subevent     = rf(it, "subevent");          // 진행형태/진행내용
            String festivalType = rf(it, "festivalgrade");     // 축제형태
            String program      = rf(it, "program");           // 행사소개
            String placeinfo    = rf(it, "placeinfo");         // 행사내용(장소/상세)

            setTextOrGone(tvSponsor1,    sponsor1);
            setTextOrGone(tvSponsor1Tel, sponsor1tel);
            setTextOrGone(tvSponsor2,    sponsor2);
            setTextOrGone(tvStartDate,   fmtDate(startDate));
            setTextOrGone(tvEndDate,     fmtDate(endDate));
            setTextOrGone(tvPlaytime,    playtime);
            setTextWithBrOrGone(tvProgress,     subevent);
            setTextOrGone(tvFestivalType, festivalType);
            setTextWithBrOrGone(tvProgram,      program);
            setTextWithBrOrGone(tvContent,      placeinfo);
        }));
    }

    private void bindViews() {
        imageMain = findViewById(R.id.imageMain);
        btnBack   = findViewById(R.id.btnBack);
        textTitle = findViewById(R.id.textTitle);

        tvTel      = findViewById(R.id.tvTel);
        tvAddr     = findViewById(R.id.tvAddr);
        tvOverview = findViewById(R.id.tvOverview);

        tvSponsor1     = findViewById(R.id.tvSponsor1);
        tvSponsor1Tel  = findViewById(R.id.tvSponsor1Tel);
        tvSponsor2     = findViewById(R.id.tvSponsor2);
        tvStartDate    = findViewById(R.id.tvStartDate);
        tvEndDate      = findViewById(R.id.tvEndDate);
        tvPlaytime     = findViewById(R.id.tvPlaytime);
        tvProgress     = findViewById(R.id.tvProgressType);
        tvFestivalType = findViewById(R.id.tvFestivalType);
        tvProgram      = findViewById(R.id.tvProgram);
        tvContent      = findViewById(R.id.tvContent);

        mapView = findViewById(R.id.mapView);
        navView = findViewById(R.id.nav_view); // 없으면 null일 수 있음
    }

    // ---------- 지도 ----------
    @Override public void onMapReady(@NonNull NaverMap map) {
        naverMap = map;
        naverMap.getUiSettings().setScaleBarEnabled(false);
        naverMap.getUiSettings().setZoomControlEnabled(true);
        updateMapMarker();
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
            } catch (Exception ignore) {}
        });
    }

    private void updateMapMarker() {
        if (naverMap == null || lat == null || lng == null) return;
        LatLng pos = new LatLng(lat, lng);
        if (marker == null) marker = new Marker();
        marker.setPosition(pos);
        marker.setMap(naverMap);
        naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(pos, 15.0));
    }

    // ---------- MapView lifecycle ----------
    @Override protected void onStart()   { super.onStart();   mapView.onStart(); }
    @Override protected void onResume()  { super.onResume();  mapView.onResume(); }
    @Override protected void onPause()   { mapView.onPause(); super.onPause(); }
    @Override protected void onStop()    { mapView.onStop();  super.onStop(); }
    @Override protected void onDestroy() { mapView.onDestroy(); super.onDestroy(); }
    @Override public void onLowMemory()  { super.onLowMemory(); mapView.onLowMemory(); }

    // ---------- 네비 ----------
    private void setupBottomNavigationView() {
        if (navView == null) return;
        navView.setOnItemSelectedListener(item -> {
            int start =
                    item.getItemId() == R.id.navigation_home     ? 0 :
                            item.getItemId() == R.id.navigation_category ? 1 :
                                    item.getItemId() == R.id.navigation_search   ? 2 :
                                            item.getItemId() == R.id.navigation_favorite ? 3 : 4;
            startActivity(new Intent(this, MainActivity.class).putExtra("start_fragment", start));
            finish();
            return true;
        });
    }

    // ---------- 유틸 ----------
    private void finishWithError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        finish();
    }
    private String s(String v) { return v == null ? "" : v; }
    private String nvl(String a, String b) { return TextUtils.isEmpty(a) ? b : a; }

    /** <br> → 줄바꿈, 기타 태그 제거 */
    private String nl(String v) {
        if (v == null) return "";
        return v.replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</p>", "\n")
                .replaceAll("(?s)<[^>]*>", "")
                .trim();
    }

    /** 비어있으면 GONE, 아니면 보이기 */
    private void setTextOrGone(TextView tv, String value) {
        String val = s(value).trim();
        if (val.isEmpty()) {
            tv.setText("");
            tv.setVisibility(View.GONE);
        } else {
            tv.setVisibility(View.VISIBLE);
            tv.setText(val);
        }
    }

    /** HTML <br> 처리 + GONE */
    private void setTextWithBrOrGone(TextView tv, String value) {
        String cooked = nl(value);
        if (cooked.trim().isEmpty()) {
            tv.setText("");
            tv.setVisibility(View.GONE);
        } else {
            tv.setVisibility(View.VISIBLE);
            tv.setText(cooked);
            tv.setSingleLine(false);
            tv.setEllipsize(null);
        }
    }

    private String joinAddr(String a1, String a2) {
        if (TextUtils.isEmpty(a1)) return s(a2);
        if (TextUtils.isEmpty(a2)) return s(a1);
        return a1 + " " + a2;
    }

    private String fmtDate(String raw) {
        if (TextUtils.isEmpty(raw) || raw.length() < 8) return s(raw);
        try { return raw.substring(0,4)+"."+raw.substring(4,6)+"."+raw.substring(6,8); }
        catch (Exception e) { return s(raw); }
    }

    /** 리플렉션으로 필드 꺼내기(필드명이 응답마다 다를 수 있어 안전하게 처리) */
    private String rf(Object obj, String field) {
        if (obj == null || TextUtils.isEmpty(field)) return "";
        try {
            Field f = obj.getClass().getField(field);
            f.setAccessible(true);
            Object v = f.get(obj);
            return v == null ? "" : String.valueOf(v);
        } catch (Exception ignore) { return ""; }
    }
}
