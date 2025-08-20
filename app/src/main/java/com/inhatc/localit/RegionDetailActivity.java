package com.inhatc.localit;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.inhatc.localit.api.SpotApiHelper;
import com.inhatc.localit.api.SpotApiService;
import com.inhatc.localit.api.SpotResponse;
import com.inhatc.localit.ui.category.FestivalActivity;
import com.inhatc.localit.ui.category.FestivalDetailActivity;
import com.inhatc.localit.ui.category.NewsActivity;
import com.inhatc.localit.ui.category.SpotActivity;
import com.inhatc.localit.ui.category.SpotDetailActivity;
import com.naver.maps.map.NaverMapSdk;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegionDetailActivity extends AppCompatActivity {

    private Spinner spinnerSubRegion;
    private ImageView btnBack, imageTourism1, imageTourism2;
    private TextView btnMoreFestivals, btnMoreTourism, btnMoreNews;
    private BottomNavigationView navView;
    private String regionName;     // 예: 경기→경기도
    private String subRegionName;  // 경기도일 때만 사용

    private TextView textTourism1Title, textTourism1Location, textTourism1Date;
    private TextView textTourism2Title, textTourism2Location, textTourism2Date;

    private ImageView imageFestival1, imageFestival2;
    private TextView textFestival1Title, textFestival1Date, textFestival1DateInfo;
    private TextView textFestival2Title, textFestival2Date, textFestival2DateInfo;

    // 미리보기 데이터
    private List<SpotResponse.Item> tourismPreview = new ArrayList<>();
    private List<SpotResponse.Item> festivalPreview = new ArrayList<>();

    private static final String SERVICE_KEY =
            "wL/Ry8EMiMg43mPRl3wyQhKosVExsJbLLDcZebat4S4eedobtNuBG+eqrj5GPKHvEAxy4NjYPz25Parbyeg8PA==";

    /** 시/도 → areaCode */
    private static final Map<String, Integer> AREA_CODE_MAP = new HashMap<>();
    static {
        AREA_CODE_MAP.put("서울특별시", 1);
        AREA_CODE_MAP.put("부산광역시", 6);
        AREA_CODE_MAP.put("대구광역시", 4);
        AREA_CODE_MAP.put("인천광역시", 2);
        AREA_CODE_MAP.put("광주광역시", 5);
        AREA_CODE_MAP.put("대전광역시", 3);
        AREA_CODE_MAP.put("울산광역시", 7);
        AREA_CODE_MAP.put("세종특별자치시", 8);
        AREA_CODE_MAP.put("경기도", 31);
        AREA_CODE_MAP.put("강원특별자치도", 32);
        AREA_CODE_MAP.put("충청북도", 33);
        AREA_CODE_MAP.put("충청남도", 34);
        AREA_CODE_MAP.put("전북특별자치도", 35);
        AREA_CODE_MAP.put("전라남도", 36);
        AREA_CODE_MAP.put("경상북도", 37);
        AREA_CODE_MAP.put("경상남도", 38);
        AREA_CODE_MAP.put("제주특별자치도", 39);
    }

    /** 경기도 시‧군 → sigunguCode */
    private static final Map<String, Integer> GG_SIGUNGU = new HashMap<>();
    static {
        GG_SIGUNGU.put("수원시", 13); GG_SIGUNGU.put("성남시", 12); GG_SIGUNGU.put("고양시", 2);
        GG_SIGUNGU.put("용인시", 23); GG_SIGUNGU.put("안산시", 15); GG_SIGUNGU.put("안양시", 17);
        GG_SIGUNGU.put("부천시", 11); GG_SIGUNGU.put("화성시", 31); GG_SIGUNGU.put("남양주시", 9);
        GG_SIGUNGU.put("평택시", 28); GG_SIGUNGU.put("의정부시", 25); GG_SIGUNGU.put("시흥시", 14);
        GG_SIGUNGU.put("파주시", 27); GG_SIGUNGU.put("김포시", 8);  GG_SIGUNGU.put("광명시", 4);
        GG_SIGUNGU.put("군포시", 7);  GG_SIGUNGU.put("광주시", 5);  GG_SIGUNGU.put("하남시", 30);
        GG_SIGUNGU.put("오산시", 22); GG_SIGUNGU.put("이천시", 26); GG_SIGUNGU.put("안성시", 16);
        GG_SIGUNGU.put("구리시", 6);  GG_SIGUNGU.put("의왕시", 24); GG_SIGUNGU.put("여주시", 20);
        GG_SIGUNGU.put("양평군", 19); GG_SIGUNGU.put("동두천시", 10);GG_SIGUNGU.put("과천시", 3);
        GG_SIGUNGU.put("포천시", 29); GG_SIGUNGU.put("연천군", 21); GG_SIGUNGU.put("가평군", 1);
        GG_SIGUNGU.put("양주시", 18);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_region_detail);
        initViews();
        setClickListeners();
        setupBottomNavigationView();

        NaverMapSdk.getInstance(this).setClient(
                new NaverMapSdk.NcpKeyClient("j6uu2y26y1"));

        // 인텐트
        String rawRegion = getIntent().getStringExtra("regionName");    // 예: "경기"
        String rawSub    = getIntent().getStringExtra("subRegionName"); // 예: "수원시"

        regionName    = normalizeRegionName(rawRegion);
        subRegionName = rawSub;

        TextView textRegionTitle = findViewById(R.id.textRegionTitle);
        if (textRegionTitle != null) textRegionTitle.setText(regionName != null ? regionName : "");

        // 경기도면 스피너 노출
        View cardSubRegion = findViewById(R.id.cardSubRegion);
        if (isGyeonggi(regionName) && spinnerSubRegion != null && cardSubRegion != null) {
            cardSubRegion.setVisibility(View.VISIBLE);

            if (spinnerSubRegion.getAdapter() == null) {
                ArrayAdapter<CharSequence> ad = ArrayAdapter.createFromResource(
                        this, R.array.textGyeonggi, android.R.layout.simple_spinner_item);
                ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerSubRegion.setAdapter(ad);
            }
            if (!TextUtils.isEmpty(subRegionName)) {
                preselectSpinner(spinnerSubRegion, subRegionName);
            } else if (spinnerSubRegion.getSelectedItem() != null) {
                subRegionName = spinnerSubRegion.getSelectedItem().toString();
            }

            spinnerSubRegion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    subRegionName = String.valueOf(parent.getItemAtPosition(position));
                    fetchTourismPreview();
                    fetchFestivalPreview();
                }
                @Override public void onNothingSelected(AdapterView<?> parent) { }
            });

            fetchTourismPreview();
            fetchFestivalPreview();
        } else {
            if (cardSubRegion != null) cardSubRegion.setVisibility(View.GONE);
            subRegionName = null;
            fetchTourismPreview();
            fetchFestivalPreview();
        }
    }

    // ----------------------------- View / Clicks -----------------------------
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnMoreFestivals = findViewById(R.id.btnMoreFestivals);
        btnMoreTourism = findViewById(R.id.btnMoreTourism);
        btnMoreNews = findViewById(R.id.btnMoreNews);
        navView = findViewById(R.id.nav_view);
        spinnerSubRegion = findViewById(R.id.spinnerSubRegion);

        textTourism1Title = findViewById(R.id.textTourism1Title);
        textTourism1Location = findViewById(R.id.textTourism1Location);
        textTourism1Date = findViewById(R.id.textTourism1Date);

        textTourism2Title = findViewById(R.id.textTourism2Title);
        textTourism2Location = findViewById(R.id.textTourism2Location);
        textTourism2Date = findViewById(R.id.textTourism2Date);

        imageTourism1 = findViewById(R.id.imageTourism1);
        imageTourism2 = findViewById(R.id.imageTourism2);

        imageFestival1 = findViewById(R.id.imageFestival1);
        imageFestival2 = findViewById(R.id.imageFestival2);
        textFestival1Title = findViewById(R.id.textFestival1Title);
        textFestival1Date = findViewById(R.id.textFestival1Date);
        textFestival1DateInfo = findViewById(R.id.textFestival1DateInfo);
        textFestival2Title = findViewById(R.id.textFestival2Title);
        textFestival2Date = findViewById(R.id.textFestival2Date);
        textFestival2DateInfo = findViewById(R.id.textFestival2DateInfo);

        // 관광 카드 클릭 타깃
        View[] tourismTargets = new View[]{ imageTourism1, textTourism1Title, imageTourism2, textTourism2Title };
        for (int i = 0; i < tourismTargets.length; i++) {
            final int idx = (i < 2) ? 0 : 1;
            if (tourismTargets[i] != null) tourismTargets[i].setOnClickListener(v -> onClickTourismCard(idx));
        }
        // 축제 카드 클릭 타깃
        View[] festivalTargets = new View[]{ imageFestival1, textFestival1Title, imageFestival2, textFestival2Title };
        for (int i = 0; i < festivalTargets.length; i++) {
            final int idx = (i < 2) ? 0 : 1;
            if (festivalTargets[i] != null) festivalTargets[i].setOnClickListener(v -> onClickFestivalCard(idx));
        }
    }

    private void setClickListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (btnMoreFestivals != null) btnMoreFestivals.setOnClickListener(v -> openCategoryActivity(FestivalActivity.class));
        if (btnMoreTourism != null) btnMoreTourism.setOnClickListener(v -> openCategoryActivity(SpotActivity.class));
        if (btnMoreNews != null) btnMoreNews.setOnClickListener(v -> openCategoryActivity(NewsActivity.class));
    }

    private void openCategoryActivity(Class<?> activityClass) {
        if (isGyeonggi(regionName) && TextUtils.isEmpty(subRegionName)) {
            Toast.makeText(this, "경기도의 상세 지역을 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, activityClass);
        intent.putExtra("regionName", denormalizeRegionForPassing(regionName));
        if (!TextUtils.isEmpty(subRegionName)) intent.putExtra("subRegionName", subRegionName);
        startActivity(intent);
    }

    private void setupBottomNavigationView() {
        if (navView == null) return;
        navView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = new Intent(this, MainActivity.class);
            if (id == R.id.navigation_home) intent.putExtra("start_fragment", 0);
            else if (id == R.id.navigation_category) intent.putExtra("start_fragment", 1);
            else if (id == R.id.navigation_search) intent.putExtra("start_fragment", 2);
            else if (id == R.id.navigation_favorite) intent.putExtra("start_fragment", 3);
            else if (id == R.id.navigation_mypage) intent.putExtra("start_fragment", 4);
            startActivity(intent);
            finish();
            return true;
        });
    }

    // ----------------------------- Networking -----------------------------
    private Integer getSigunguCodeIfGyeonggi() {
        if (!isGyeonggi(regionName)) return null;
        if (TextUtils.isEmpty(subRegionName)) return null;
        return GG_SIGUNGU.get(subRegionName.trim());
    }

    private void fetchTourismPreview() {
        int areaCode = getAreaCode(regionName);
        Integer sigunguCode = getSigunguCodeIfGyeonggi();

        SpotApiService api = SpotApiHelper.getApiService();
        Call<SpotResponse> call = api.getTourList(
                12, 1, "AND", "localit", "C",
                12, areaCode, sigunguCode,
                "json", SERVICE_KEY
        );

        call.enqueue(new Callback<SpotResponse>() {
            @Override public void onResponse(Call<SpotResponse> call, Response<SpotResponse> response) {
                if (!(response.isSuccessful() && response.body()!=null &&
                        response.body().response!=null &&
                        response.body().response.body!=null &&
                        response.body().response.body.items!=null &&
                        response.body().response.body.items.item!=null)) {
                    tourismPreview = new ArrayList<>();
                    bindTourismPreview(tourismPreview);
                    return;
                }
                List<SpotResponse.Item> items = response.body().response.body.items.item;

                if (sigunguCode != null && !TextUtils.isEmpty(subRegionName)) {
                    items = filterByAddr(items, subRegionName);
                }
                tourismPreview = items;
                bindTourismPreview(items);
            }

            @Override public void onFailure(Call<SpotResponse> call, Throwable t) {
                tourismPreview = new ArrayList<>();
                t.printStackTrace();
                bindTourismPreview(tourismPreview);
            }
        });
    }

    private void fetchFestivalPreview() {
        String startDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        int areaCode = getAreaCode(regionName);
        Integer sigunguCode = getSigunguCodeIfGyeonggi();

        SpotApiService api = SpotApiHelper.getApiService();
        Call<SpotResponse> call = api.getFestivalList(
                10, 1, "AND", "localit", "json",
                areaCode, sigunguCode, startDate, "A", SERVICE_KEY
        );

        call.enqueue(new Callback<SpotResponse>() {
            @Override public void onResponse(Call<SpotResponse> call, Response<SpotResponse> response) {
                if (!(response.isSuccessful() && response.body()!=null &&
                        response.body().response!=null &&
                        response.body().response.body!=null &&
                        response.body().response.body.items!=null &&
                        response.body().response.body.items.item!=null)) {
                    festivalPreview = new ArrayList<>();
                    bindFestivalPreview(festivalPreview);
                    return;
                }
                List<SpotResponse.Item> items = response.body().response.body.items.item;

                if (sigunguCode != null && !TextUtils.isEmpty(subRegionName)) {
                    items = filterByAddr(items, subRegionName);
                }
                festivalPreview = items;
                bindFestivalPreview(items);
            }

            @Override public void onFailure(Call<SpotResponse> call, Throwable t) {
                festivalPreview = new ArrayList<>();
                t.printStackTrace();
                bindFestivalPreview(festivalPreview);
            }
        });
    }

    /** addr1에 하위도시명이 포함된 것만 남기기 (fallback) */
    private List<SpotResponse.Item> filterByAddr(List<SpotResponse.Item> src, String key) {
        if (src == null) return new ArrayList<>();
        if (TextUtils.isEmpty(key)) return src;
        String k = key.trim();
        List<SpotResponse.Item> out = new ArrayList<>();
        for (SpotResponse.Item it : src) {
            String addr = it != null && it.addr1 != null ? it.addr1 : "";
            if (addr.startsWith(k) || addr.contains(" " + k) || addr.contains(k + " ")) {
                out.add(it);
            }
        }
        return out;
    }

    private void bindTourismPreview(List<SpotResponse.Item> items) {
        if (items == null) items = new ArrayList<>();
        if (items.size() >= 2) {
            SpotResponse.Item item1 = items.get(0);
            SpotResponse.Item item2 = items.get(1);

            textTourism1Title.setText(safe(item1.title));
            textTourism1Location.setText(safe(item1.addr1));
            textTourism1Date.setText(formatDate(item1.createdtime));
            Glide.with(this).load(safe(item1.firstimage)).placeholder(R.drawable.sample1).error(R.drawable.sample1).into(imageTourism1);

            textTourism2Title.setText(safe(item2.title));
            textTourism2Location.setText(safe(item2.addr1));
            textTourism2Date.setText(formatDate(item2.createdtime));
            Glide.with(this).load(safe(item2.firstimage)).placeholder(R.drawable.sample1).error(R.drawable.sample1).into(imageTourism2);
        } else if (items.size() == 1) {
            SpotResponse.Item item1 = items.get(0);
            textTourism1Title.setText(safe(item1.title));
            textTourism1Location.setText(safe(item1.addr1));
            textTourism1Date.setText(formatDate(item1.createdtime));
            Glide.with(this).load(safe(item1.firstimage)).placeholder(R.drawable.sample1).error(R.drawable.sample1).into(imageTourism1);

            textTourism2Title.setText(""); textTourism2Location.setText(""); textTourism2Date.setText("");
            imageTourism2.setImageResource(R.drawable.sample1);
        } else {
            textTourism1Title.setText("데이터가 없습니다");
            textTourism1Location.setText(""); textTourism1Date.setText("");
            imageTourism1.setImageResource(R.drawable.sample1);

            textTourism2Title.setText(""); textTourism2Location.setText(""); textTourism2Date.setText("");
            imageTourism2.setImageResource(R.drawable.sample1);
        }
    }

    private void bindFestivalPreview(List<SpotResponse.Item> items) {
        if (items == null) items = new ArrayList<>();
        if (items.size() >= 2) {
            SpotResponse.Item item1 = items.get(0);
            SpotResponse.Item item2 = items.get(1);

            textFestival1Title.setText(safe(item1.title));
            textFestival1Date.setText("시작일: " + formatDate(item1.eventstartdate));
            textFestival1DateInfo.setText("종료일: " + formatDate(item1.eventenddate));
            Glide.with(this).load(safe(item1.firstimage)).placeholder(R.drawable.sample1).error(R.drawable.sample1).into(imageFestival1);

            textFestival2Title.setText(safe(item2.title));
            textFestival2Date.setText("시작일: " + formatDate(item2.eventstartdate));
            textFestival2DateInfo.setText("종료일: " + formatDate(item2.eventenddate));
            Glide.with(this).load(safe(item2.firstimage)).placeholder(R.drawable.sample1).error(R.drawable.sample1).into(imageFestival2);
        } else if (items.size() == 1) {
            SpotResponse.Item item1 = items.get(0);
            textFestival1Title.setText(safe(item1.title));
            textFestival1Date.setText("시작일: " + formatDate(item1.eventstartdate));
            textFestival1DateInfo.setText("종료일: " + formatDate(item1.eventenddate));
            Glide.with(this).load(safe(item1.firstimage)).placeholder(R.drawable.sample1).error(R.drawable.sample1).into(imageFestival1);

            textFestival2Title.setText(""); textFestival2Date.setText(""); textFestival2DateInfo.setText("");
            imageFestival2.setImageResource(R.drawable.sample1);
        } else {
            textFestival1Title.setText("데이터가 없습니다");
            textFestival1Date.setText(""); textFestival1DateInfo.setText("");
            imageFestival1.setImageResource(R.drawable.sample1);

            textFestival2Title.setText(""); textFestival2Date.setText(""); textFestival2DateInfo.setText("");
            imageFestival2.setImageResource(R.drawable.sample1);
        }
    }

    // ---------- 카드 클릭 → 내부 상세 화면 ----------
    private void onClickTourismCard(int idx) {
        if (tourismPreview == null || tourismPreview.size() <= idx) return;
        SpotResponse.Item it = tourismPreview.get(idx);

        Intent i = new Intent(this, SpotDetailActivity.class);
        i.putExtra(SpotDetailActivity.EXTRA_CONTENT_ID, it.contentid);
        i.putExtra(SpotDetailActivity.EXTRA_CONTENT_TYPE_ID,
                TextUtils.isEmpty(it.contenttypeid) ? "12" : it.contenttypeid);

        // (선표시용) 선택사항
        i.putExtra(SpotDetailActivity.EXTRA_TITLE,       it.title != null ? it.title : "");
        i.putExtra(SpotDetailActivity.EXTRA_ADDR1,       it.addr1 != null ? it.addr1 : "");
        i.putExtra(SpotDetailActivity.EXTRA_FIRST_IMAGE, it.firstimage != null ? it.firstimage : "");

        startActivity(i);
    }

    private void onClickFestivalCard(int idx) {
        if (festivalPreview == null || festivalPreview.size() <= idx) return;
        SpotResponse.Item it = festivalPreview.get(idx);

        Intent i = new Intent(this, FestivalDetailActivity.class);
        i.putExtra(FestivalDetailActivity.EXTRA_CONTENT_ID, it.contentid);
        i.putExtra(FestivalDetailActivity.EXTRA_CONTENT_TYPE_ID,
                TextUtils.isEmpty(it.contenttypeid) ? "15" : it.contenttypeid);
        i.putExtra(FestivalDetailActivity.EXTRA_TITLE,       it.title != null ? it.title : "");
        i.putExtra(FestivalDetailActivity.EXTRA_ADDR1,       it.addr1 != null ? it.addr1 : "");
        i.putExtra(FestivalDetailActivity.EXTRA_FIRST_IMAGE, it.firstimage != null ? it.firstimage : "");
        startActivity(i);
    }



    // ----------------------------- Utils -----------------------------
    private String normalizeRegionName(String r) {
        if (r == null) return null;
        String t = r.trim();
        switch (t) {
            case "서울": return "서울특별시";
            case "부산": return "부산광역시";
            case "대구": return "대구광역시";
            case "인천": return "인천광역시";
            case "광주": return "광주광역시";
            case "대전": return "대전광역시";
            case "울산": return "울산광역시";
            case "세종": return "세종특별자치시";
            case "경기": return "경기도";
            case "강원": return "강원특별자치도";
            case "충북": return "충청북도";
            case "충남": return "충청남도";
            case "전북": return "전북특별자치도";
            case "전남": return "전라남도";
            case "경북": return "경상북도";
            case "경남": return "경상남도";
            case "제주": return "제주특별자치도";
            default: return t;
        }
    }

    private String denormalizeRegionForPassing(String full) {
        if (full == null) return null;
        switch (full) {
            case "서울특별시": return "서울";
            case "부산광역시": return "부산";
            case "대구광역시": return "대구";
            case "인천광역시": return "인천";
            case "광주광역시": return "광주";
            case "대전광역시": return "대전";
            case "울산광역시": return "울산";
            case "세종특별자치시": return "세종";
            case "경기도": return "경기";
            case "강원특별자치도": return "강원";
            case "충청북도": return "충북";
            case "충청남도": return "충남";
            case "전북특별자치도": return "전북";
            case "전라남도": return "전남";
            case "경상북도": return "경북";
            case "경상남도": return "경남";
            case "제주특별자치도": return "제주";
            default: return full;
        }
    }

    private boolean isGyeonggi(String r) { return "경기도".equals(r); }

    private int getAreaCode(String r) {
        String key = normalizeRegionName(r);
        return AREA_CODE_MAP.getOrDefault(key, 1);
    }

    private String formatDate(String raw) {
        if (!TextUtils.isEmpty(raw) && raw.length() >= 8) {
            return raw.substring(0, 4) + "." + raw.substring(4, 6) + "." + raw.substring(6, 8);
        }
        return "";
    }

    private String safe(String s) { return s == null ? "" : s; }

    private void preselectSpinner(Spinner sp, String value) {
        if (sp == null || sp.getAdapter() == null || value == null) return;
        for (int i = 0; i < sp.getAdapter().getCount(); i++) {
            if (value.equals(sp.getAdapter().getItem(i).toString())) {
                sp.setSelection(i, false);
                break;
            }
        }
    }
}
