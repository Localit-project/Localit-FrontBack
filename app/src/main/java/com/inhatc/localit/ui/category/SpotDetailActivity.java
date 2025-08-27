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
import com.inhatc.localit.api.SpotDetailInfoResponse;
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

    private TextView textTitle, textAddr, textZipcode,
            textTel, textRestdate, textUsetime, textParking, labelZip;

    private BottomNavigationView navView;

    // Gallery UI
    private View          galleryContainer;
    private ImageButton   btnGalleryPrev, btnGalleryNext;
    private LinearLayout  galleryDots;
    private LinearLayoutManager galleryLm;

    private int galleryStep = 3;
    private int visiblePerPage = 1;
    private static final int GALLERY_ITEM_DP = 118;

    private final List<String> gallery = new ArrayList<>();
    private MapView mapView;
    private NaverMap naverMap;
    private Marker marker;
    private Double lat, lng;

    private TextView textExtraInfo;

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
        Log.d("DETAIL_ARGS", "contentId=" + contentId + ", contentTypeId=" + contentTypeId);

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

        // Gallery
        galleryLm = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        recyclerGallery.setLayoutManager(galleryLm);
        galleryAdapter = new GalleryAdapter(gallery, url ->
                Glide.with(SpotDetailActivity.this)
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
            @Override public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                updateDots();
            }
        });

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

        // 지도
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // detailCommon (주소/연락처/좌표/대표이미지 등, 개요 제거)
        SpotApiHelper.fetchDetailCommon(contentId, contentTypeId, (SpotDetailCommonResponse.Item item) -> runOnUiThread(() -> {
            if (item == null) {
                return;
            }
            setTextOrGone(textAddr, joinAddr(item.addr1, item.addr2));
            setTextWithHtmlOrGone(textTel, item.tel);

            String img = !TextUtils.isEmpty(item.firstimage) ? item.firstimage :
                    !TextUtils.isEmpty(item.firstimage2) ? item.firstimage2 : passedFirstImage;
            Glide.with(this).load(img).placeholder(R.drawable.sample1).error(R.drawable.sample1).into(imageMain);

            try {
                if (!TextUtils.isEmpty(item.mapy) && !TextUtils.isEmpty(item.mapx)) {
                    lat = Double.parseDouble(item.mapy);
                    lng = Double.parseDouble(item.mapx);
                    updateMapMarker();
                } else {
                    String addr = joinAddr(item.addr1, item.addr2);
                    if (!TextUtils.isEmpty(addr)) geocodeAndMove(addr);
                }
            } catch (Exception e) { Log.e(TAG,"Parsing map coords failed", e); }
        }));

        // detailIntro
        SpotApiHelper.fetchDetailIntro(contentId, safeInt(contentTypeId, 12), intro -> runOnUiThread(() -> {
            if (intro == null) return;
            setTextWithHtmlOrGone(textRestdate, intro.restdate);
            setTextWithHtmlOrGone(textUsetime,  intro.usetime);
            setTextWithHtmlOrGone(textParking,  intro.parking);
            if (TextUtils.isEmpty(textTel.getText())) setTextWithHtmlOrGone(textTel, intro.infocenter);
        }));

        // detailInfo2
        int realTypeForInfo = safeInt(contentTypeId, 12);
        SpotApiHelper.fetchDetailInfo(contentId, realTypeForInfo, items -> runOnUiThread(() -> {
            if (textExtraInfo == null) return;
            if (items == null || items.isEmpty()) {
                textExtraInfo.setVisibility(View.GONE);
                return;
            }
            StringBuilder sb = new StringBuilder();
            for (SpotDetailInfoResponse.Item it : items) {
                if (!TextUtils.isEmpty(it.infoname) && !TextUtils.isEmpty(it.infotext)) {
                    if (sb.length() > 0) sb.append("\n");
                    sb.append(it.infoname.replaceAll("\\s+", " ").trim())
                            .append(" : ")
                            .append(it.infotext.trim());
                }
            }
            if (sb.length() > 0) {
                textExtraInfo.setVisibility(View.VISIBLE);
                textExtraInfo.setText(sb.toString());
            } else {
                textExtraInfo.setVisibility(View.GONE);
            }
        }));

        // detailImage
        SpotApiHelper.fetchDetailImages(contentId, urls -> runOnUiThread(() -> {
            gallery.clear();
            for (String u : urls) if (!TextUtils.isEmpty(u)) gallery.add(u);
            galleryAdapter.notifyDataSetChanged();

            if (galleryContainer != null) {
                galleryContainer.setVisibility(gallery.isEmpty() ? View.GONE : View.VISIBLE);
            }
            updateDots();
        }));
    }

    private void bindViews() {
        imageMain       = findViewById(R.id.imageMain);
        btnBack         = findViewById(R.id.btnBack);
        recyclerGallery = findViewById(R.id.recyclerGallery);
        textTitle       = findViewById(R.id.textTitle);

        textAddr        = findViewById(R.id.textAddr);
        textTel         = findViewById(R.id.textTel);
        textRestdate    = findViewById(R.id.textRestdate);
        textUsetime     = findViewById(R.id.textUsetime);
        textParking     = findViewById(R.id.textParking);
        mapView         = findViewById(R.id.mapView);
        navView         = findViewById(R.id.nav_view);
        textExtraInfo   = findViewById(R.id.textExtraInfo);

        galleryContainer= findViewById(R.id.galleryContainer);
        btnGalleryPrev  = findViewById(R.id.btnGalleryPrev);
        btnGalleryNext  = findViewById(R.id.btnGalleryNext);
        galleryDots     = findViewById(R.id.galleryDots);
    }

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

    private String s(String v) { return v == null ? "" : v; }

    private void setTextWithHtmlOrGone(TextView tv, String html) {
        if (TextUtils.isEmpty(html) ||
                HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim().isEmpty()) {
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