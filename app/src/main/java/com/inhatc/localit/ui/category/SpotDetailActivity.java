package com.inhatc.localit.ui.category;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

// 네이버 지도
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.overlay.Marker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.inhatc.localit.R;
import com.inhatc.localit.api.SpotApiHelper;
import com.inhatc.localit.api.SpotDetailCommonResponse;
import com.inhatc.localit.api.SpotDetailIntroResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * 관광지 공용 상세 화면
 *
 * 표기 순서:
 * 1) 대표 사진
 * 2) 추가사진(보조사진들)
 * 3) 개요(상세설명)
 * 4) 주소
 * 5) 우편번호
 * 6) 문의 및 안내
 * 7) 쉬는날
 * 8) 이용시간
 * 9) 주차시설
 * 10) 입장료 (없으면 "입장료 없음")
 */
public class SpotDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_CONTENT_ID = "extra_content_id";
    public static final String EXTRA_CONTENT_TYPE_ID = "extra_content_type_id";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_ADDR1 = "extra_addr1";
    public static final String EXTRA_FIRST_IMAGE = "extra_first_image";

    private ImageView imageMain;
    private RecyclerView recyclerGallery;
    private GalleryAdapter galleryAdapter;

    private TextView textTitle, textOverview, textAddr, textZipcode,
            textTel, textRestdate, textUsetime, textParking, textUsefee;


    // 전달값
    private String contentId;
    private String contentTypeId;
    private String passedTitle;
    private String passedAddr1;
    private String passedFirstImage;

    // 갤러리
    private final List<String> gallery = new ArrayList<>();

    // 지도
    private MapView mapView;
    private NaverMap naverMap;
    private Marker marker;

    // 좌표 (TourAPI: mapy=위도, mapx=경도)
    private Double lat; // mapy
    private Double lng; // mapx

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spot_detail);

        imageMain    = findViewById(R.id.imageMain);
        recyclerGallery = findViewById(R.id.recyclerGallery);
        textTitle    = findViewById(R.id.textTitle);
        textOverview = findViewById(R.id.textOverview);
        textAddr     = findViewById(R.id.textAddr);
        textZipcode  = findViewById(R.id.textZipcode);
        textTel      = findViewById(R.id.textTel);
        textRestdate = findViewById(R.id.textRestdate);
        textUsetime  = findViewById(R.id.textUsetime);
        textParking  = findViewById(R.id.textParking);
        textUsefee   = findViewById(R.id.textUsefee);
        mapView      = findViewById(R.id.mapView);

        Intent intent = getIntent();
        if (intent == null) {
            finishWithError("잘못된 접근입니다.");
            return;
        }

        // 기본 키(신규) → 없으면 구키 호환
        contentId = intent.getStringExtra(EXTRA_CONTENT_ID);
        if (TextUtils.isEmpty(contentId)) contentId = intent.getStringExtra("contentId");

        contentTypeId = intent.getStringExtra(EXTRA_CONTENT_TYPE_ID);
        if (TextUtils.isEmpty(contentTypeId)) contentTypeId = intent.getStringExtra("contentTypeId");
        if (TextUtils.isEmpty(contentTypeId)) contentTypeId = "12";

        passedTitle = intent.getStringExtra(EXTRA_TITLE);
        passedAddr1 = intent.getStringExtra(EXTRA_ADDR1);
        passedFirstImage = intent.getStringExtra(EXTRA_FIRST_IMAGE);

        // 선표시
        if (!TextUtils.isEmpty(passedTitle)) textTitle.setText(passedTitle);
        if (!TextUtils.isEmpty(passedAddr1)) textAddr.setText(passedAddr1);
        if (!TextUtils.isEmpty(passedFirstImage)) {
            Glide.with(this).load(passedFirstImage)
                    .placeholder(R.drawable.sample1).error(R.drawable.sample1).into(imageMain);
        } else {
            imageMain.setImageResource(R.drawable.sample1);
        }

        if (TextUtils.isEmpty(contentId)) {
            finishWithError("상세 조회에 필요한 contentId 가 없습니다.");
            return;
        }

        // 갤러리 리사이클러
        recyclerGallery.setLayoutManager(
                new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        galleryAdapter = new GalleryAdapter(gallery, url ->
                Glide.with(SpotDetailActivity.this)
                        .load(url)
                        .placeholder(R.drawable.sample1)
                        .error(R.drawable.sample1)
                        .into(imageMain)
        );
        recyclerGallery.setAdapter(galleryAdapter);

        mapView = findViewById(R.id.mapView);

        // 네이버 지도 준비
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // 1) 공통 상세(개요/주소/우편/연락처/대표이미지/좌표)
        SpotApiHelper.fetchDetailCommon(contentId, contentTypeId, item -> {
            if (item == null) return;
            runOnUiThread(() -> bindCommon(item));
        });

        // 2) 인트로 상세(문의/쉬는날/이용시간/주차/입장료 등)
        int ctid = 12;
        try { ctid = Integer.parseInt(contentTypeId); } catch (Exception ignore) {}
        SpotApiHelper.fetchDetailIntro(contentId, ctid, item -> {
            if (item == null) return;
            runOnUiThread(() -> bindIntro(item));
        });

        // 3) 추가 이미지(보조사진들)
        SpotApiHelper.fetchDetailImages(contentId, urls -> {
            if (urls == null) return;
            runOnUiThread(() -> {
                gallery.clear();
                for (String u : urls) {
                    if (TextUtils.isEmpty(u)) continue;
                    if (!TextUtils.isEmpty(passedFirstImage) && passedFirstImage.equals(u)) continue;
                    gallery.add(u);
                }
                galleryAdapter.notifyDataSetChanged();
            });
        });

    }

    private void bindCommon(SpotDetailCommonResponse.Item it) {
        if (textTitle    != null) textTitle.setText(s(it.title));
        if (textOverview != null) textOverview.setText(nl(it.overview)); // 개요(상세설명)
        if (textAddr     != null) textAddr.setText(joinAddr(it.addr1, it.addr2));
        if (textZipcode  != null) textZipcode.setText(s(it.zipcode));
        if (textTel      != null) textTel.setText(s(it.tel));

        // 대표 이미지(서버값이 있으면 덮어씀)
        String img = !TextUtils.isEmpty(it.firstimage) ? it.firstimage : it.firstimage2;
        if (!TextUtils.isEmpty(img)) {
            Glide.with(this).load(img)
                    .placeholder(R.drawable.sample1).error(R.drawable.sample1).into(imageMain);
        }

        // 지도 좌표
        try {
            if (!TextUtils.isEmpty(it.mapy) && !TextUtils.isEmpty(it.mapx)) {
                lat = Double.parseDouble(it.mapy);
                lng = Double.parseDouble(it.mapx);
                updateMapMarker(); // 네이버 지도에 마커 반영
            }
        } catch (Exception ignore) {}
    }

    private void bindIntro(SpotDetailIntroResponse.Item it) {
        if (textTel != null && TextUtils.isEmpty(textTel.getText()))
            textTel.setText(s(it.infocenter));
        if (textRestdate != null) textRestdate.setText(s(it.restdate));
        if (textUsetime  != null) textUsetime.setText(s(it.usetime));
        if (textParking  != null) textParking.setText(s(it.parking));

        // 입장료 기본값
        String fee = s(it.usefee);
        if (textUsefee != null)
            textUsefee.setText(TextUtils.isEmpty(it.usefee) ? "입장료 없음" : it.usefee);

    }

    // -------- 지도 ----------
    @Override
    public void onMapReady(@NonNull NaverMap map) {
        naverMap = map;
        naverMap.getUiSettings().setScaleBarEnabled(false);
        naverMap.getUiSettings().setZoomControlEnabled(true);
        updateMapMarker();
    }

    private void updateMapMarker() {
        if (naverMap == null || lat == null || lng == null) return;
        LatLng pos = new LatLng(lat, lng); // (위도, 경도) 순서 주의

        if (marker == null) marker = new Marker();
        marker.setPosition(pos);
        marker.setMap(naverMap);

        naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(pos, 15.0));
    }

    // -------- lifecycle(MapView 필수) ----------
    @Override protected void onStart()   { super.onStart();   mapView.onStart(); }
    @Override protected void onResume()  { super.onResume();  mapView.onResume(); }
    @Override protected void onPause()   { mapView.onPause();  super.onPause(); }
    @Override protected void onStop()    { mapView.onStop();   super.onStop(); }
    @Override protected void onDestroy() { mapView.onDestroy(); super.onDestroy(); }
    @Override public void onLowMemory()  { super.onLowMemory(); mapView.onLowMemory(); }

    private void finishWithError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        finish();
    }

    // --------- helpers ---------
    private String s(String v) { return v == null ? "" : v; }

    /** 개요에 <br> 같은 태그 제거 + 줄바꿈 반영 */
    private String nl(String v) {
        if (v == null) return "";
        return v.replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?s)<[^>]*>", "");
    }

    private String joinAddr(String a1, String a2) {
        if (TextUtils.isEmpty(a2)) return s(a1);
        if (TextUtils.isEmpty(a1)) return s(a2);
        return a1 + " " + a2;
    }
}
