package com.inhatc.localit.ui.category;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.inhatc.localit.api.SpotDetailCommonResponse;
import com.inhatc.localit.api.SpotDetailIntroResponse;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

/** 축제 상세 화면 — 지도 마커 표시(좌표/주소) */
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
    private TextView tvAddr;

    // 인트로(detailIntro: 축제)
    private TextView tvSponsor1, tvSponsor1Tel, tvSponsor2,
            tvStartDate, tvEndDate, tvPlaytime;

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

        // 공통 상세 (주소/이미지/좌표)
        SpotApiHelper.fetchDetailCommon(contentId, contentTypeId, (SpotDetailCommonResponse.Item item) -> runOnUiThread(() -> {
            if (item == null) { Log.w(TAG, "COMMON item is null"); return; }

            // 주소
            String addrJoined = joinAddr(item.addr1, item.addr2);
            setTextOrGone(tvAddr, addrJoined);

            // 제목 보정
            if (!TextUtils.isEmpty(item.title)) setTextOrGone(textTitle, item.title);

            // 대표 이미지
            String img = !TextUtils.isEmpty(item.firstimage) ? item.firstimage : item.firstimage2;
            if (!TextUtils.isEmpty(img)) {
                Glide.with(this).load(img)
                        .placeholder(R.drawable.sample1).error(R.drawable.sample1).into(imageMain);
            }

            // 좌표 → 마커, 없으면 주소 지오코딩
            try {
                Log.d(TAG, "COMMON raw coords mapx=" + item.mapx + ", mapy=" + item.mapy);
                if (!TextUtils.isEmpty(item.mapy) && !TextUtils.isEmpty(item.mapx)) {
                    lat = Double.parseDouble(item.mapy); // 위도
                    lng = Double.parseDouble(item.mapx); // 경도
                    Log.d(TAG, "COMMON parsed coords lat=" + lat + ", lng=" + lng);
                    updateMapMarker(); // 지도 준비됐으면 바로 표시
                } else if (!TextUtils.isEmpty(addrJoined)) {
                    geocodeAndMove(addrJoined);
                }
            } catch (Exception e) {
                Log.e(TAG, "Parsing map coords failed", e);
            }
        }));

        // 축제 인트로
        SpotApiHelper.fetchDetailIntro(contentId, safeInt(contentTypeId, 15), (SpotDetailIntroResponse.Item intro) -> runOnUiThread(() -> {
            if (intro == null) { Log.w(TAG, "INTRO item is null"); return; }

            // 날짜
            setTextOrGone(tvStartDate, fmtDate(s(intro.eventstartdate)));
            setTextOrGone(tvEndDate,   fmtDate(s(intro.eventenddate)));

            // 기타
            setTextOrGone(tvPlaytime,    intro.playtime);
            setTextOrGone(tvSponsor1,    intro.sponsor1);
            setTextOrGone(tvSponsor1Tel, intro.sponsor1tel);
            setTextOrGone(tvSponsor2,    intro.sponsor2);

            // 입장료
            String fee = nl(s(intro.usefee));
            if (TextUtils.isEmpty(fee.trim())) fee = nl(s(intro.usetimefestival));
            if (TextUtils.isEmpty(fee.trim())) fee = "입장료 없음";

            if (tvUsefee != null) {
                setTextOrGone(tvUsefee, fee);
            }
        }));
    }

    private void bindViews() {
        imageMain = findViewById(R.id.imageMain);
        btnBack   = findViewById(R.id.btnBack);
        textTitle = findViewById(R.id.textTitle);

        tvStartDate    = findViewById(R.id.tvStartDate);
        tvEndDate      = findViewById(R.id.tvEndDate);
        tvPlaytime     = findViewById(R.id.tvPlaytime);

        tvSponsor1     = findViewById(R.id.tvSponsor1);
        tvSponsor1Tel  = findViewById(R.id.tvSponsor1Tel);
        tvSponsor2     = findViewById(R.id.tvSponsor2);

        tvAddr = findViewById(R.id.tvAddr);

        int idUsefee = getResources().getIdentifier("tvUsefee", "id", getPackageName());
        tvUsefee = idUsefee == 0 ? null : findViewById(idUsefee);

        mapView = findViewById(R.id.mapView);
        navView = findViewById(R.id.nav_view);
    }

    // ---------------- 지도 ----------------
    @Override
    public void onMapReady(@NonNull NaverMap map) {
        naverMap = map;
        naverMap.getUiSettings().setScaleBarEnabled(false);
        naverMap.getUiSettings().setZoomControlEnabled(true);

        if (lat == null || lng == null) {
            naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(new LatLng(36.5, 127.9), 6.5));
        }
        updateMapMarker();

        if ((lat == null || lng == null) && tvAddr != null) {
            CharSequence addr = tvAddr.getText();
            if (addr != null && addr.toString().trim().length() > 0) {
                geocodeAndMove(addr.toString());
            } else {
                Toast.makeText(this, "지도 좌표/주소가 아직 준비되지 않았습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateMapMarker() {
        if (naverMap == null || lat == null || lng == null) return;
        LatLng pos = new LatLng(lat, lng);
        if (marker == null) marker = new Marker();
        marker.setPosition(pos);
        marker.setMap(naverMap);

        String caption = textTitle != null && !TextUtils.isEmpty(String.valueOf(textTitle.getText()))
                ? textTitle.getText().toString()
                : (tvAddr != null ? String.valueOf(tvAddr.getText()) : "");
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
                }
            } catch (Exception e) { Log.e(TAG, "Geocoder failed for address=" + address, e); }
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

    private String s(String v) { return v == null ? "" : v; }
    private String nl(String v) { return v == null ? "" : v.replaceAll("(?i)<br\\s*/?>", "\n").replaceAll("(?i)</p>", "\n").replaceAll("(?s)<[^>]*>", "").trim(); }
    private void setTextOrGone(TextView tv, String value) { if (tv == null) return; String val = s(value); if (TextUtils.isEmpty(val.trim())) { tv.setText(""); tv.setVisibility(android.view.View.GONE); } else { tv.setVisibility(android.view.View.VISIBLE); tv.setText(val); } }
    private String joinAddr(String a1, String a2) { if (TextUtils.isEmpty(a1)) return s(a2); if (TextUtils.isEmpty(a2)) return s(a1); return a1 + " " + a2; }
    private String nvl(String a, String b) { return TextUtils.isEmpty(a) ? b : a; }
    private int safeInt(String s, int def) { try { return Integer.parseInt(s); } catch (Exception e) { return def; } }
    private String fmtDate(String raw) { if (raw == null) return ""; String digits = raw.replaceAll("\\D+", ""); if (digits.length() >= 8) digits = digits.substring(0, 8); else return raw; try { return digits.substring(0, 4) + "." + digits.substring(4, 6) + "." + digits.substring(6, 8); } catch (Exception e) { return raw; } }
}