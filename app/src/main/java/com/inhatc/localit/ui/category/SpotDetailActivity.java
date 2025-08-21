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
        String contentTypeId = nvl(intent.getStringExtra(EXTRA_CONTENT_TYPE_ID),
                nvl(intent.getStringExtra("contentTypeId"), "12"));
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
                    .placeholder(R.drawable.sample1).error(R.drawable.sample1).into(imageMain);
        } else {
            imageMain.setImageResource(R.drawable.sample1);
        }

        // 갤러리
        recyclerGallery.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        galleryAdapter = new GalleryAdapter(gallery, url ->
                Glide.with(SpotDetailActivity.this)
                        .load(url).placeholder(R.drawable.sample1).error(R.drawable.sample1)
                        .into(imageMain));
        recyclerGallery.setAdapter(galleryAdapter);

        // 지도
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // 데이터
        SpotApiHelper.fetchDetailCommon(contentId, contentTypeId, (SpotDetailCommonResponse.Item item) -> runOnUiThread(() -> {
            if (item == null) return;

            // 개요
            setTextWithBrOrGone(textOverview, item.overview);

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

            // 문의 및 안내
            setTextWithBrOrGone(textTel, item.tel);

            // 대표 이미지 교체
            String img = !TextUtils.isEmpty(item.firstimage) ? item.firstimage : item.firstimage2;
            if (!TextUtils.isEmpty(img)) {
                Glide.with(this).load(img)
                        .placeholder(R.drawable.sample1).error(R.drawable.sample1).into(imageMain);
            }

            // 좌표 처리 + 로그/토스트
            try {
                Log.d(TAG, "COMMON raw coords mapx=" + item.mapx + ", mapy=" + item.mapy);
                if (!TextUtils.isEmpty(item.mapy) && !TextUtils.isEmpty(item.mapx)) {
                    lat = Double.parseDouble(item.mapy); // 위도
                    lng = Double.parseDouble(item.mapx); // 경도
                    Log.d(TAG, "COMMON parsed coords lat=" + lat + ", lng=" + lng);
                    Toast.makeText(this, "좌표 수신 완료", Toast.LENGTH_SHORT).show();
                    updateMapMarker();
                } else {
                    String addr = joinAddr(item.addr1, item.addr2);
                    Log.w(TAG, "COMMON no coords. fallback geocode addr=" + addr);
                    if (!TextUtils.isEmpty(addr)) geocodeAndMove(addr);
                    else Toast.makeText(this, "좌표/주소 없음", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Parsing map coords failed", e);
                Toast.makeText(this, "좌표 파싱 실패", Toast.LENGTH_SHORT).show();
            }
        }));

        SpotApiHelper.fetchDetailIntro(contentId, safeInt(contentTypeId, 12), (SpotDetailIntroResponse.Item intro) -> runOnUiThread(() -> {
            if (intro == null) return;

            // 휴일/시간/주차/입장료
            setTextWithBrOrGone(textRestdate, intro.restdate);
            setTextWithBrOrGone(textUsetime, intro.usetime);
            setTextWithBrOrGone(textParking, intro.parking);

            String fee = nl(intro.usefee);
            textUsefee.setText(TextUtils.isEmpty(fee.trim()) ? "입장료 없음" : fee);
            textUsefee.setVisibility(View.VISIBLE);

            // 문의 보완
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

        Log.d(TAG, "onMapReady: map ready. lat=" + lat + ", lng=" + lng);
        // 지도 보이는지 확인용(디버그): 초기 카메라(대한민국 중부)
        if (lat == null || lng == null) {
            naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(new LatLng(36.5, 127.9), 6.5));
        }

        // 이미 좌표가 있으면 바로 마커
        updateMapMarker();

        // 좌표 없고 주소가 있으면 바로 지오코딩 폴백
        if ((lat == null || lng == null) && textAddr != null) {
            CharSequence addr = textAddr.getText();
            if (addr != null && addr.toString().trim().length() > 0) {
                Log.d(TAG, "onMapReady: no coords yet -> geocode fallback with addr=" + addr);
                geocodeAndMove(addr.toString());
            } else {
                Log.w(TAG, "onMapReady: no coords and no address available");
                Toast.makeText(this, "지도 좌표/주소가 아직 준비되지 않았습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** 준비된 좌표를 지도에 마커로 표시 + 캡션 + 카메라 이동 */
    private void updateMapMarker() {
        Log.d(TAG, "updateMapMarker called. naverMap=" + (naverMap != null)
                + ", lat=" + lat + ", lng=" + lng);

        if (naverMap == null) {
            Log.w(TAG, "updateMapMarker: naverMap is null");
            return;
        }
        if (lat == null || lng == null) {
            Log.w(TAG, "updateMapMarker: lat/lng is null");
            return;
        }

        LatLng pos = new LatLng(lat, lng);

        if (marker == null) marker = new Marker();
        marker.setPosition(pos);
        marker.setMap(naverMap);

        String caption = (textTitle != null && !TextUtils.isEmpty(String.valueOf(textTitle.getText())))
                ? textTitle.getText().toString()
                : (textAddr != null ? String.valueOf(textAddr.getText()) : "");
        if (!TextUtils.isEmpty(caption)) marker.setCaptionText(caption);

        naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(pos, 15.0));
        Log.d(TAG, "updateMapMarker: moved camera to lat=" + lat + ", lng=" + lng);
        Toast.makeText(this, "지도 위치가 설정되었습니다.", Toast.LENGTH_SHORT).show();
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
                    Log.d(TAG, "GEOCODER lat=" + lat + ", lng=" + lng + " for addr=" + address);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "주소로 위치 설정: " + address, Toast.LENGTH_SHORT).show();
                        updateMapMarker();
                    });
                } else {
                    Log.w(TAG, "GEOCODER no result for addr=" + address);
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

    // ---------- 유틸 ----------
    private String s(String v) { return v == null ? "" : v; }

    private String nl(String v) {
        if (v == null) return "";
        return v.replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?s)<[^>]*>", "");
    }

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

    private String nvl(String a, String b) { return TextUtils.isEmpty(a) ? b : a; }

    private int safeInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }
}