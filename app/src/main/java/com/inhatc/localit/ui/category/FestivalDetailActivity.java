//package com.inhatc.localit.ui.category;
//
//import android.content.Intent;
//import android.location.Address;
//import android.location.Geocoder;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.bumptech.glide.Glide;
//import com.inhatc.localit.R;
//import com.inhatc.localit.api.SpotApiHelper;
//import com.inhatc.localit.api.SpotDetailCommonResponse;
//import com.inhatc.localit.api.SpotDetailIntroResponse;
//
//// 지도
//import com.naver.maps.geometry.LatLng;
//import com.naver.maps.map.CameraUpdate;
//import com.naver.maps.map.MapView;
//import com.naver.maps.map.NaverMap;
//import com.naver.maps.map.OnMapReadyCallback;
//import com.naver.maps.map.overlay.Marker;
//
//import java.lang.reflect.Field;
//import java.util.List;
//import java.util.Locale;
//import java.util.concurrent.Executors;
//
//public class FestivalDetailActivity extends AppCompatActivity implements OnMapReadyCallback {
//
//    public static final String EXTRA_CONTENT_ID = "extra_content_id";
//    public static final String EXTRA_CONTENT_TYPE_ID = "extra_content_type_id";
//    public static final String EXTRA_TITLE = "extra_title";
//    public static final String EXTRA_ADDR1 = "extra_addr1";
//    public static final String EXTRA_FIRST_IMAGE = "extra_first_image";
//
//    private ImageView imageMain;
//    private TextView textTitle;
//
//    // 공통
//    private TextView tvZipcode, tvTelName, tvTel, tvAddr, tvOverview;
//
//    // 인트로(축제)
//    private TextView tvSponsor1, tvSponsor1Tel, tvSponsor2, tvStartDate, tvEndDate,
//            tvPlaytime, tvProgress, tvFestivalType, tvProgram, tvContent;
//
//    // 지도
//    private MapView mapView;
//    private NaverMap naverMap;
//    private Marker marker;
//    private Double lat; // mapy(위도)
//    private Double lng; // mapx(경도)
//
//    private String contentId, contentTypeId, passedTitle, passedAddr1, passedFirstImage;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_festival_detail);
//
//        bindViews();
//
//        // 지도 준비
//        mapView.onCreate(savedInstanceState);
//        mapView.getMapAsync(this);
//
//        // 인텐트
//        Intent intent = getIntent();
//        if (intent == null) { finishWithError("잘못된 접근입니다."); return; }
//        contentId        = safe(intent.getStringExtra(EXTRA_CONTENT_ID));
//        contentTypeId    = safe(intent.getStringExtra(EXTRA_CONTENT_TYPE_ID));
//        passedTitle      = safe(intent.getStringExtra(EXTRA_TITLE));
//        passedAddr1      = safe(intent.getStringExtra(EXTRA_ADDR1));
//        passedFirstImage = safe(intent.getStringExtra(EXTRA_FIRST_IMAGE));
//        if (TextUtils.isEmpty(contentId)) { finishWithError("contentId 없음"); return; }
//        if (TextUtils.isEmpty(contentTypeId)) contentTypeId = "15";
//
//        // 선표시
//        if (!TextUtils.isEmpty(passedTitle)) textTitle.setText(passedTitle);
//        if (!TextUtils.isEmpty(passedAddr1)) tvAddr.setText(passedAddr1);
//        if (!TextUtils.isEmpty(passedFirstImage)) {
//            Glide.with(this).load(passedFirstImage)
//                    .placeholder(R.drawable.sample1).error(R.drawable.sample1).into(imageMain);
//        }
//
//        // 공통 상세
//        SpotApiHelper.fetchDetailCommon(contentId, contentTypeId, item -> {
//            if (item == null) return;
//            runOnUiThread(() -> bindCommon(item));
//        });
//
//        // 축제 인트로
//        int ctid = 15;
//        try { ctid = Integer.parseInt(contentTypeId); } catch (Exception ignore) {}
//        SpotApiHelper.fetchDetailIntro(contentId, ctid, item -> {
//            if (item == null) return;
//            runOnUiThread(() -> bindIntroFestival(item));
//        });
//    }
//
//    private void bindViews() {
//        imageMain = findViewById(R.id.imageMain);
//        textTitle = findViewById(R.id.textTitle);
//
//        tvZipcode  = findViewById(R.id.tvZipcode);
//        tvTelName  = findViewById(R.id.tvTelName);
//        tvTel      = findViewById(R.id.tvTel);
//        tvAddr     = findViewById(R.id.tvAddr);
//        tvOverview = findViewById(R.id.tvOverview);
//
//        tvSponsor1     = findViewById(R.id.tvSponsor1);
//        tvSponsor1Tel  = findViewById(R.id.tvSponsor1Tel);
//        tvSponsor2     = findViewById(R.id.tvSponsor2);
//        tvStartDate    = findViewById(R.id.tvStartDate);
//        tvEndDate      = findViewById(R.id.tvEndDate);
//        tvPlaytime     = findViewById(R.id.tvPlaytime);
//        tvProgress     = findViewById(R.id.tvProgressType);
//        tvFestivalType = findViewById(R.id.tvFestivalType);
//        tvProgram      = findViewById(R.id.tvProgram);
//        tvContent      = findViewById(R.id.tvContent);
//
//        mapView        = findViewById(R.id.mapView);
//    }
//
//    // ----- 공통 상세 -----
//    private void bindCommon(SpotDetailCommonResponse.Item it) {
//        if (!TextUtils.isEmpty(it.title)) textTitle.setText(it.title);
//        tvOverview.setText(nl(it.overview));
//        tvAddr.setText(joinAddr(it.addr1, it.addr2));
//        tvZipcode.setText(safe(it.zipcode));
//        tvTel.setText(safe(it.tel));
//
//        // 전화명(telname) – 있을 때만
//        tvTelName.setText(safe(rf(it, "telname")));
//
//        // 대표 이미지
//        String img = !TextUtils.isEmpty(it.firstimage) ? it.firstimage : it.firstimage2;
//        if (!TextUtils.isEmpty(img)) {
//            Glide.with(this).load(img).placeholder(R.drawable.sample1)
//                    .error(R.drawable.sample1).into(imageMain);
//        } else if (TextUtils.isEmpty(passedFirstImage)) {
//            imageMain.setImageResource(R.drawable.sample1);
//        }
//
//        // 지도 좌표 적용 (없으면 지오코딩으로 주소 → 좌표)
//        try {
//            if (!TextUtils.isEmpty(it.mapy) && !TextUtils.isEmpty(it.mapx)) {
//                lat = Double.parseDouble(it.mapy);
//                lng = Double.parseDouble(it.mapx);
//                updateMapMarker();
//            } else {
//                String addr = joinAddr(it.addr1, it.addr2);
//                if (!TextUtils.isEmpty(addr)) geocodeAndMove(addr);
//            }
//        } catch (Exception ignore) {}
//    }
//
//    // ----- 인트로(축제) -----
//    private void bindIntroFestival(SpotDetailIntroResponse.Item it) {
//        String sponsor1     = rf(it, "sponsor1");
//        String sponsor1tel  = rf(it, "sponsor1tel");
//        String sponsor2     = rf(it, "sponsor2");
//        String startDate    = rf(it, "eventstartdate");
//        String endDate      = rf(it, "eventenddate");
//        String playtime     = rf(it, "playtime");
//        String subevent     = rf(it, "subevent");
//        String festivalType = rf(it, "festivalgrade");
//        String program      = rf(it, "program");
//        String placeinfo    = rf(it, "placeinfo");
//
//        tvSponsor1.setText(safe(sponsor1));
//        tvSponsor1Tel.setText(safe(sponsor1tel));
//        tvSponsor2.setText(safe(sponsor2));
//        tvStartDate.setText(fmtDate(startDate));
//        tvEndDate.setText(fmtDate(endDate));
//        tvPlaytime.setText(safe(playtime));
//        tvProgress.setText(nl(subevent));
//        tvFestivalType.setText(safe(festivalType));
//        tvProgram.setText(nl(program));
//        tvContent.setText(nl(placeinfo));
//    }
//
//    // ----- 지도 -----
//    @Override public void onMapReady(NaverMap map) {
//        naverMap = map;
//        naverMap.getUiSettings().setScaleBarEnabled(false);
//        naverMap.getUiSettings().setZoomControlEnabled(true);
//        updateMapMarker();
//    }
//
//    private void geocodeAndMove(String address) {
//        Executors.newSingleThreadExecutor().execute(() -> {
//            try {
//                Geocoder g = new Geocoder(this, Locale.KOREA);
//                List<Address> r = g.getFromLocationName(address, 1);
//                if (r != null && !r.isEmpty()) {
//                    lat = r.get(0).getLatitude();
//                    lng = r.get(0).getLongitude();
//                    runOnUiThread(this::updateMapMarker);
//                }
//            } catch (Exception ignore) {}
//        });
//    }
//
//    private void updateMapMarker() {
//        if (naverMap == null || lat == null || lng == null) return;
//        LatLng pos = new LatLng(lat, lng);
//        if (marker == null) marker = new Marker();
//        marker.setPosition(pos);
//        marker.setMap(naverMap);
//        naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(pos, 15.0));
//    }
//
//    // MapView lifecycle
//    @Override protected void onStart()   { super.onStart();   mapView.onStart(); }
//    @Override protected void onResume()  { super.onResume();  mapView.onResume(); }
//    @Override protected void onPause()   { mapView.onPause(); super.onPause(); }
//    @Override protected void onStop()    { mapView.onStop();  super.onStop(); }
//    @Override protected void onDestroy() { mapView.onDestroy(); super.onDestroy(); }
//    @Override public void onLowMemory()  { super.onLowMemory(); mapView.onLowMemory(); }
//
//    // ----- 유틸 -----
//    private void finishWithError(String msg) { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show(); finish(); }
//    private String safe(String s) { return s == null ? "" : s; }
//    private String nl(String v) {
//        if (v == null) return "";
//        return v.replaceAll("(?i)<br\\s*/?>", "\n")
//                .replaceAll("(?i)</p>", "\n")
//                .replaceAll("<[^>]*>", "").trim();
//    }
//    private String joinAddr(String a1, String a2) {
//        if (TextUtils.isEmpty(a2)) return safe(a1);
//        if (TextUtils.isEmpty(a1)) return safe(a2);
//        return a1 + " " + a2;
//    }
//    private String fmtDate(String raw) {
//        if (TextUtils.isEmpty(raw) || raw.length() < 8) return safe(raw);
//        try { return raw.substring(0,4)+"."+raw.substring(4,6)+"."+raw.substring(6,8); }
//        catch (Exception e) { return safe(raw); }
//    }
//    private String rf(Object obj, String field) {
//        if (obj == null || TextUtils.isEmpty(field)) return "";
//        try {
//            Field f = obj.getClass().getField(field);
//            f.setAccessible(true);
//            Object v = f.get(obj);
//            return v == null ? "" : String.valueOf(v);
//        } catch (Exception ignore) { return ""; }
//    }
//}
