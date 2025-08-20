package com.inhatc.localit.ui.category;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 관광지 공용 상세 화면
 * 1) 대표 사진
 * 2) 추가사진(보조사진들)
 * 3) 개요(상세설명)
 * 4) 주소
 * 5) 우편번호
 * 6) 문의 및 안내
 * 7) 쉬는날
 * 8) 이용시간
 * 9) 주차시설
 * 10) 입장료(없으면 "입장료 없음")
 */

public class SpotDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_CONTENT_ID      = "extra_content_id";
    public static final String EXTRA_CONTENT_TYPE_ID = "extra_content_type_id";
    public static final String EXTRA_TITLE           = "extra_title";
    public static final String EXTRA_ADDR1           = "extra_addr1";
    public static final String EXTRA_FIRST_IMAGE     = "extra_first_image";

    // intent keys 그대로

    private ImageView imageMain, btnBack;
    private RecyclerView recyclerGallery;
    private GalleryAdapter galleryAdapter;

    private TextView textTitle, textOverview, textAddr, textZipcode,
            textTel, textRestdate, textUsetime, textParking, textUsefee, labelZip;
    private BottomNavigationView navView;

    private final List<String> gallery = new ArrayList<>();
    private MapView mapView;
    private NaverMap naverMap;
    private Marker marker;
    private Double lat, lng;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spot_detail);

        // 1) 인텐트
        Intent intent = getIntent();
        if (intent == null) { finishWithError("잘못된 접근입니다."); return; }

        String contentId = nvl(intent.getStringExtra(EXTRA_CONTENT_ID), intent.getStringExtra("contentId"));
        String contentTypeId = nvl(intent.getStringExtra(EXTRA_CONTENT_TYPE_ID),
                nvl(intent.getStringExtra("contentTypeId"), "12"));
        String passedTitle = intent.getStringExtra(EXTRA_TITLE);
        String passedAddr1 = intent.getStringExtra(EXTRA_ADDR1);
        String passedFirstImage = intent.getStringExtra(EXTRA_FIRST_IMAGE);

        if (TextUtils.isEmpty(contentId)) { finishWithError("상세 조회에 필요한 contentId 가 없습니다."); return; }

        // 2) 뷰
        bindViews();

        // ESC처럼 뒤로가기
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // 하단 내비 (필요 시)
        setupBottomNavigationView();

        // 3) 선표시 (제목/주소/대표사진)
        setTextOrGone(textTitle, passedTitle);
        setTextOrGone(textAddr, passedAddr1);
        if (!TextUtils.isEmpty(passedFirstImage)) {
            Glide.with(this).load(passedFirstImage)
                    .placeholder(R.drawable.sample1).error(R.drawable.sample1).into(imageMain);
        } else {
            imageMain.setImageResource(R.drawable.sample1);
        }

        // 4) 갤러리
        recyclerGallery.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        galleryAdapter = new GalleryAdapter(gallery, url ->
                Glide.with(SpotDetailActivity.this)
                        .load(url).placeholder(R.drawable.sample1).error(R.drawable.sample1)
                        .into(imageMain));
        recyclerGallery.setAdapter(galleryAdapter);

        // 5) 지도
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // 6) 데이터
        SpotApiHelper.fetchDetailCommon(contentId, contentTypeId, item -> runOnUiThread(() -> {
            if (item == null) return;

            // 🧭 XML 순서대로 바인딩 시작 ----------------

            // (1) 개요
            setTextWithBrOrGone(textOverview, item.overview);

            // (2) 위치(주소)
            setTextOrGone(textAddr, joinAddr(item.addr1, item.addr2));

            // (3) 우편번호
            if (!TextUtils.isEmpty(item.zipcode)) {
                labelZip.setVisibility(View.VISIBLE);
                setTextOrGone(textZipcode, item.zipcode);
            } else {
                labelZip.setVisibility(View.GONE);
                textZipcode.setVisibility(View.GONE);
            }

            // (4) 문의 및 안내 (우선 common.tel)
            setTextWithBrOrGone(textTel, item.tel);

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
                }
            } catch (Exception ignore) {}
            // 🧭 XML 순서 (공통 파트) 끝 ----------------
        }));

        SpotApiHelper.fetchDetailIntro(contentId, safeInt(contentTypeId, 12), intro -> runOnUiThread(() -> {
            if (intro == null) return;

            // (5) 휴일
            setTextWithBrOrGone(textRestdate, intro.restdate);

            // (6) 이용시간
            setTextWithBrOrGone(textUsetime, intro.usetime);

            // (7) 주차
            setTextWithBrOrGone(textParking, intro.parking);

            // (8) 입장료
            String fee = nl(intro.usefee);
            textUsefee.setText(TextUtils.isEmpty(fee.trim()) ? "입장료 없음" : fee);
            textUsefee.setVisibility(View.VISIBLE);

            // (4 보완) 문의 및 안내가 비어있다면 intro.infocenter로 보완
            if (TextUtils.isEmpty(textTel.getText())) {
                setTextWithBrOrGone(textTel, intro.infocenter);
            }
        }));

        SpotApiHelper.fetchDetailImages(contentId, urls -> runOnUiThread(() -> {
            gallery.clear();
            for (String u : urls) if (!TextUtils.isEmpty(u)) gallery.add(u);
            galleryAdapter.notifyDataSetChanged();
        }));
    }

    private void bindViews() {
        imageMain      = findViewById(R.id.imageMain);
        btnBack        = findViewById(R.id.btnBack);
        recyclerGallery= findViewById(R.id.recyclerGallery);
        textTitle      = findViewById(R.id.textTitle);
        textOverview   = findViewById(R.id.textOverview);
        textAddr       = findViewById(R.id.textAddr);
        textTel        = findViewById(R.id.textTel);
        textRestdate   = findViewById(R.id.textRestdate);
        textUsetime    = findViewById(R.id.textUsetime);
        textParking    = findViewById(R.id.textParking);
        textUsefee     = findViewById(R.id.textUsefee);
        mapView        = findViewById(R.id.mapView);
        navView        = findViewById(R.id.nav_view);
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
            // 필요 시 메인으로 라우팅하는 기존 로직 유지
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

    // <br> → 줄바꿈, 나머지 태그 제거
    private String nl(String v) {
        if (v == null) return "";
        return v.replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?s)<[^>]*>", "");
    }

    // 줄바꿈 처리 + GONE 처리
    private void setTextWithBrOrGone(TextView tv, String value) {
        String cooked = nl(value);
        if (TextUtils.isEmpty(cooked.trim())) {
            tv.setText("");
            tv.setVisibility(View.GONE);
        } else {
            tv.setVisibility(View.VISIBLE);
            tv.setText(cooked);
            tv.setSingleLine(false);
            tv.setEllipsize(null);
        }
    }

    // 일반 텍스트 + GONE 처리
    private void setTextOrGone(TextView tv, String value) {
        String s = s(value);
        if (TextUtils.isEmpty(s.trim())) {
            tv.setText("");
            tv.setVisibility(View.GONE);
        } else {
            tv.setVisibility(View.VISIBLE);
            tv.setText(s);
        }
    }

    private String joinAddr(String a1, String a2) {
        if (TextUtils.isEmpty(a1)) return s(a2);
        if (TextUtils.isEmpty(a2)) return s(a1);
        return a1 + " " + a2;
    }

    private String nvl(String a, String b) {
        // a가 null 이거나 비어있으면 b 반환
        return TextUtils.isEmpty(a) ? b : a;
    }

    private int safeInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }
}

