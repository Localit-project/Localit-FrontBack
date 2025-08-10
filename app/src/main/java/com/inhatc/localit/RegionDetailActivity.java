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
    private String regionName;
    private String subRegionName;
    private CardView cardSubRegion;

    private TextView textTourism1Title, textTourism1Location, textTourism1Date;
    private TextView textTourism2Title, textTourism2Location, textTourism2Date;

    private ImageView imageFestival1, imageFestival2;
    private TextView textFestival1Title, textFestival1Date, textFestival1DateInfo;
    private TextView textFestival2Title, textFestival2Date, textFestival2DateInfo;

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
            subRegionName = spinnerSubRegion.getSelectedItem().toString();

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
            Intent intent = new Intent(this, ApiMainActivity.class); // ✅ ApiMainActivity로
            intent.putExtra("regionName", regionName);
            intent.putExtra("subRegionName", subRegionName); // 필요시
            intent.putExtra("contentTypeId", 15); // ✅ 축제용 contentTypeId
            startActivity(intent);
        });
        btnMoreTourism.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.inhatc.localit.api.ApiMainActivity.class);
            intent.putExtra("regionName", regionName);
            intent.putExtra("subRegionName", subRegionName);
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

            if (id == R.id.navigation_home) {
                intent.putExtra("start_fragment", 0);
            } else if (id == R.id.navigation_category) {
                intent.putExtra("start_fragment", 1);
            } else if (id == R.id.navigation_search) {
                intent.putExtra("start_fragment", 2);
            } else if (id == R.id.navigation_favorite) {
                intent.putExtra("start_fragment", 3);
            } else if (id == R.id.navigation_mypage) {
                intent.putExtra("start_fragment", 4);
            }

            startActivity(intent);
            finish();
            return true;
        });
    }

    private void fetchTourismPreview() {
        String serviceKey = "wL/Ry8EMiMg43mPRl3wyQhKosVExsJbLLDcZebat4S4eedobtNuBG+eqrj5GPKHvEAxy4NjYPz25Parbyeg8PA==";
        int areaCode = getAreaCode(regionName);

        TourApiService apiService = TourApiHelper.getApiService();
        Call<TourResponse> call = apiService.getTourList(12, 1, "AND", "localit", "c", 12, areaCode, "json", serviceKey);

        call.enqueue(new Callback<TourResponse>() {
            @Override
            public void onResponse(Call<TourResponse> call, Response<TourResponse> response) {
                if (response.isSuccessful() && response.body() != null &&
                        response.body().response != null &&
                        response.body().response.body != null &&
                        response.body().response.body.items != null) {

                    List<TourResponse.Item> items = response.body().response.body.items.item;

                    if (items.size() >= 2) {
                        TourResponse.Item item1 = items.get(0);
                        TourResponse.Item item2 = items.get(1);

                        textTourism1Title.setText(item1.title);
                        textTourism1Location.setText(item1.addr1);
                        textTourism1Date.setText(formatDate(item1.createdtime));

                        textTourism2Title.setText(item2.title);
                        textTourism2Location.setText(item2.addr1);
                        textTourism2Date.setText(formatDate(item2.createdtime));

                        Glide.with(RegionDetailActivity.this).load(item1.firstimage).into(imageTourism1);
                        Glide.with(RegionDetailActivity.this).load(item2.firstimage).into(imageTourism2);
                    }
                }
            }

            @Override
            public void onFailure(Call<TourResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void fetchFestivalPreview() {
        String serviceKey = "wL/Ry8EMiMg43mPRl3wyQhKosVExsJbLLDcZebat4S4eedobtNuBG+eqrj5GPKHvEAxy4NjYPz25Parbyeg8PA==";
        String startDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        int areaCode = getAreaCode(regionName);

        TourApiService apiService = TourApiHelper.getApiService();
        Call<TourResponse> call = apiService.getFestivalList(
                10, 1, "AND", "localit", "json", areaCode, startDate, "A", serviceKey
        );

        call.enqueue(new Callback<TourResponse>() {
            @Override
            public void onResponse(Call<TourResponse> call, Response<TourResponse> response) {
                if (response.isSuccessful() && response.body() != null &&
                        response.body().response != null &&
                        response.body().response.body != null &&
                        response.body().response.body.items != null &&
                        response.body().response.body.items.item != null) {

                    List<TourResponse.Item> items = response.body().response.body.items.item;

                    if (items.size() >= 2) {
                        TourResponse.Item item1 = items.get(0);
                        TourResponse.Item item2 = items.get(1);

                        textFestival1Title.setText(item1.title);
                        textFestival1Date.setText("시작일: " + formatDate(item1.eventstartdate));
                        textFestival1DateInfo.setText("종료일: " + formatDate(item1.eventenddate));
                        Glide.with(RegionDetailActivity.this).load(item1.firstimage).into(imageFestival1);

                        textFestival2Title.setText(item2.title);
                        textFestival2Date.setText("시작일: " + formatDate(item2.eventstartdate));
                        textFestival2DateInfo.setText("종료일: " + formatDate(item2.eventenddate));
                        Glide.with(RegionDetailActivity.this).load(item2.firstimage).into(imageFestival2);
                    }
                }
            }

            @Override
            public void onFailure(Call<TourResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
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
}
