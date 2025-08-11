package com.inhatc.localit;
import com.inhatc.localit.api.ApiMainActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.inhatc.localit.api.TourApiHelper;
import com.inhatc.localit.api.TourApiService;
import com.inhatc.localit.api.TourResponse;
import com.inhatc.localit.ui.category.NewsActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;      // <-- for List
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
    private String regionName;
    private String subRegionName;
    private CardView cardSubRegion;

    private TextView textTourism1Title, textTourism1Location, textTourism1Date;
    private TextView textTourism2Title, textTourism2Location, textTourism2Date;

    private ImageView imageFestival1, imageFestival2;
    private TextView textFestival1Title, textFestival1Date, textFestival1DateInfo;
    private TextView textFestival2Title, textFestival2Date, textFestival2DateInfo;

    private static final String SERVICE_KEY =
            "wL/Ry8EMiMg43mPRl3wyQhKosVExsJbLLDcZebat4S4eedobtNuBG+eqrj5GPKHvEAxy4NjYPz25Parbyeg8PA==";

    // 시/도 → areaCode
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
        AREA_CODE_MAP.put("전라북도", 35);
        AREA_CODE_MAP.put("전라남도", 36);
        AREA_CODE_MAP.put("경상북도", 37);
        AREA_CODE_MAP.put("경상남도", 38);
        AREA_CODE_MAP.put("제주특별자치도", 39);
    }


    private static final Map<String, Integer> GG_SIGUNGU = new HashMap<>();
    static {
        // 일부 예시. 꼭 실제 값으로 갱신하세요!
        GG_SIGUNGU.put("수원시", 13);
        GG_SIGUNGU.put("성남시", 12);
        GG_SIGUNGU.put("고양시", 2);
        GG_SIGUNGU.put("용인시", 23);
        GG_SIGUNGU.put("안산시", 15);
        GG_SIGUNGU.put("안양시", 17);
        GG_SIGUNGU.put("부천시", 11);
        GG_SIGUNGU.put("화성시", 31);
        GG_SIGUNGU.put("남양주시", 9);
        GG_SIGUNGU.put("평택시", 28);
        GG_SIGUNGU.put("의정부시", 25);
        GG_SIGUNGU.put("시흥시", 14);
        GG_SIGUNGU.put("파주시", 27);
        GG_SIGUNGU.put("김포시", 8);
        GG_SIGUNGU.put("광명시", 4);
        GG_SIGUNGU.put("군포시", 7);
        GG_SIGUNGU.put("광주시", 5);
        GG_SIGUNGU.put("하남시", 30);
        GG_SIGUNGU.put("오산시", 22);
        GG_SIGUNGU.put("이천시", 26);
        GG_SIGUNGU.put("안성시", 16);
        GG_SIGUNGU.put("구리시", 6);
        GG_SIGUNGU.put("의왕시", 24);
        GG_SIGUNGU.put("여주시", 20);
        GG_SIGUNGU.put("양평군", 19);
        GG_SIGUNGU.put("동두천시", 10);
        GG_SIGUNGU.put("과천시", 3);
        GG_SIGUNGU.put("포천시", 29);
        GG_SIGUNGU.put("연천군", 21);
        GG_SIGUNGU.put("가평군", 1);
        GG_SIGUNGU.put("양주시", 18);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_region_detail);

        initViews();
        setClickListeners();
        setupBottomNavigationView();

        regionName = getIntent().getStringExtra("regionName");
        subRegionName = getIntent().getStringExtra("subRegionName");

        TextView textRegionTitle = findViewById(R.id.textRegionTitle);
        textRegionTitle.setText(regionName);

        if ("경기도".equals(regionName)) {
            cardSubRegion.setVisibility(View.VISIBLE);

            // 스피너 초깃값 반영
            if (spinnerSubRegion.getSelectedItem() != null) {
                subRegionName = spinnerSubRegion.getSelectedItem().toString();
            }

            spinnerSubRegion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    subRegionName = (String) parent.getItemAtPosition(position);
                    fetchTourismPreview();
                    fetchFestivalPreview();
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    subRegionName = null;
                }
            });

            // 최초 로드
            fetchTourismPreview();
            fetchFestivalPreview();
        } else {
            cardSubRegion.setVisibility(View.GONE);
            fetchTourismPreview();
            fetchFestivalPreview();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnMoreFestivals = findViewById(R.id.btnMoreFestivals);
        btnMoreTourism = findViewById(R.id.btnMoreTourism);
        btnMoreNews = findViewById(R.id.btnMoreNews);
        navView = findViewById(R.id.nav_view);
        spinnerSubRegion = findViewById(R.id.spinnerSubRegion);
        cardSubRegion = findViewById(R.id.cardSubRegion);

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
    }

    private void setClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnMoreFestivals.setOnClickListener(v -> {
            Intent intent = new Intent(this, ApiMainActivity.class);
            intent.putExtra("regionName", regionName);
            intent.putExtra("subRegionName", subRegionName);
            intent.putExtra("contentTypeId", 15);
            startActivity(intent);
        });
        btnMoreTourism.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.inhatc.localit.api.ApiMainActivity.class);
            intent.putExtra("regionName", regionName);
            intent.putExtra("subRegionName", subRegionName);
            intent.putExtra("contentTypeId", 12);
            startActivity(intent);
        });
        btnMoreNews.setOnClickListener(v -> {
            Intent intent = new Intent(this, NewsActivity.class);
            intent.putExtra("regionName", regionName);
            intent.putExtra("subRegionName", subRegionName);
            startActivity(intent);
        });
    }

    private void setupBottomNavigationView() {
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


    private Integer getSigunguCodeIfGyeonggi() {
        if (!"경기도".equals(regionName)) return null;
        if (subRegionName == null || subRegionName.trim().isEmpty()) return null;
        return GG_SIGUNGU.get(subRegionName.trim());
    }

    private void fetchTourismPreview() {
        int areaCode = getAreaCode(regionName);
        Integer sigunguCode = getSigunguCodeIfGyeonggi();

        TourApiService apiService = TourApiHelper.getApiService();
        // 서버 필터 시도 (sigunguCode 전달)
        Call<TourResponse> call = apiService.getTourList(
                12, 1, "AND", "localit", "c", 12, areaCode, sigunguCode, "json", SERVICE_KEY
        );

        call.enqueue(new Callback<TourResponse>() {
            @Override public void onResponse(Call<TourResponse> call, Response<TourResponse> response) {
                if (!(response.isSuccessful() && response.body()!=null &&
                        response.body().response!=null &&
                        response.body().response.body!=null &&
                        response.body().response.body.items!=null &&
                        response.body().response.body.items.item!=null)) {
                    return;
                }
                List<TourResponse.Item> items = response.body().response.body.items.item;

                //서버가 sigungu 무시하거나 코드가 틀릴 수 있으니 addr1로 한 번 더 거르기
                if (sigunguCode != null && subRegionName != null) {
                    items = filterByAddr(items, subRegionName);
                }

                bindTourismPreview(items);
            }
            @Override public void onFailure(Call<TourResponse> call, Throwable t) { t.printStackTrace(); }
        });
    }

    private void fetchFestivalPreview() {
        String startDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        int areaCode = getAreaCode(regionName);
        Integer sigunguCode = getSigunguCodeIfGyeonggi();

        TourApiService apiService = TourApiHelper.getApiService();
        Call<TourResponse> call = apiService.getFestivalList(
                10, 1, "AND", "localit", "json", areaCode, sigunguCode, startDate, "A", SERVICE_KEY
        );

        call.enqueue(new Callback<TourResponse>() {
            @Override public void onResponse(Call<TourResponse> call, Response<TourResponse> response) {
                if (!(response.isSuccessful() && response.body()!=null &&
                        response.body().response!=null &&
                        response.body().response.body!=null &&
                        response.body().response.body.items!=null &&
                        response.body().response.body.items.item!=null)) {
                    return;
                }
                List<TourResponse.Item> items = response.body().response.body.items.item;

                // 보조 필터
                if (sigunguCode != null && subRegionName != null) {
                    items = filterByAddr(items, subRegionName);
                }

                bindFestivalPreview(items);
            }
            @Override public void onFailure(Call<TourResponse> call, Throwable t) { t.printStackTrace(); }
        });
    }

    /** addr1에 하위도시명이 포함된 것만 남기기 (fallback) */
    private List<TourResponse.Item> filterByAddr(List<TourResponse.Item> src, String key) {
        if (src == null) return new ArrayList<>();
        if (key == null || key.trim().isEmpty()) return src;
        String k = key.trim();
        List<TourResponse.Item> out = new ArrayList<>();
        for (TourResponse.Item it : src) {
            String addr = it != null && it.addr1 != null ? it.addr1 : "";
            if (addr.startsWith(k) || addr.contains(" " + k) || addr.contains(k + " ")) {
                out.add(it);
            }
        }
        return out;
    }

    private void bindTourismPreview(List<TourResponse.Item> items) {
        if (items == null) items = new ArrayList<>();
        if (items.size() >= 2) {
            TourResponse.Item item1 = items.get(0);
            TourResponse.Item item2 = items.get(1);

            textTourism1Title.setText(safe(item1.title));
            textTourism1Location.setText(safe(item1.addr1));
            textTourism1Date.setText(formatDate(item1.createdtime));
            Glide.with(this).load(safe(item1.firstimage)).placeholder(R.drawable.sample1).error(R.drawable.sample1).into(imageTourism1);

            textTourism2Title.setText(safe(item2.title));
            textTourism2Location.setText(safe(item2.addr1));
            textTourism2Date.setText(formatDate(item2.createdtime));
            Glide.with(this).load(safe(item2.firstimage)).placeholder(R.drawable.sample1).error(R.drawable.sample1).into(imageTourism2);
        } else if (items.size() == 1) {
            TourResponse.Item item1 = items.get(0);
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

    private void bindFestivalPreview(List<TourResponse.Item> items) {
        if (items == null) items = new ArrayList<>();
        if (items.size() >= 2) {
            TourResponse.Item item1 = items.get(0);
            TourResponse.Item item2 = items.get(1);

            textFestival1Title.setText(safe(item1.title));
            textFestival1Date.setText("시작일: " + formatDate(item1.eventstartdate));
            textFestival1DateInfo.setText("종료일: " + formatDate(item1.eventenddate));
            Glide.with(this).load(safe(item1.firstimage)).placeholder(R.drawable.sample1).error(R.drawable.sample1).into(imageFestival1);

            textFestival2Title.setText(safe(item2.title));
            textFestival2Date.setText("시작일: " + formatDate(item2.eventstartdate));
            textFestival2DateInfo.setText("종료일: " + formatDate(item2.eventenddate));
            Glide.with(this).load(safe(item2.firstimage)).placeholder(R.drawable.sample1).error(R.drawable.sample1).into(imageFestival2);
        } else if (items.size() == 1) {
            TourResponse.Item item1 = items.get(0);
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

    private int getAreaCode(String regionName) {
        return AREA_CODE_MAP.getOrDefault(regionName, 1);
    }

    private String formatDate(String raw) {
        if (raw != null && raw.length() >= 8) {
            return raw.substring(0, 4) + "." + raw.substring(4, 6) + "." + raw.substring(6, 8);
        }
        return "";
    }

    private String safe(String s) { return s == null ? "" : s; }
}
