// app/src/main/java/com/inhatc/localit/ui/category/TourismActivity.java
package com.inhatc.localit.ui.category;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.inhatc.localit.EventDetailActivity;
import com.inhatc.localit.MainActivity;
import com.inhatc.localit.R;
import com.inhatc.localit.api.TourApiHelper;
import com.inhatc.localit.api.TourApiService;
import com.inhatc.localit.api.TourResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TourismActivity extends AppCompatActivity {

    private static final String TAG = "TourismActivity";
    private static final String SERVICE_KEY = "wL/Ry8EMiMg43mPRl3wyQhKosVExsJbLLDcZebat4S4eedobtNuBG+eqrj5GPKHvEAxy4NjYPz25Parbyeg8PA==";

    private RecyclerView recyclerViewTourism;
    private TourismAdapter tourismAdapter;
    private final List<TourResponse.Item> apiItems = new ArrayList<>();

    private TextView textRegionTitle;
    private ImageView btnBack;
    private BottomNavigationView navView;

    private String regionName;
    private String subRegionName;


    private static final Map<String, Integer> AREA_CODE_MAP = new HashMap<>();

    private static final Map<String, String> REGION_ALIAS = new HashMap<>();

    static {

        AREA_CODE_MAP.put("서울특별시", 1);
        AREA_CODE_MAP.put("인천광역시", 2);
        AREA_CODE_MAP.put("대전광역시", 3);
        AREA_CODE_MAP.put("대구광역시", 4);
        AREA_CODE_MAP.put("광주광역시", 5);
        AREA_CODE_MAP.put("부산광역시", 6);
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

    private static void alias(String standard, String... aliases) {
        REGION_ALIAS.put(clean(standard), standard);
        for (String a : aliases) REGION_ALIAS.put(clean(a), standard);
    }

    private static String clean(String s) { return s == null ? "" : s.replaceAll("\\s+", ""); }

    private static String stripSuffix(String k) {
        return k.replaceAll("(광역시|특별자치시|특별자치도|특별시|자치시|자치도|시|도)$", "");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tourism);

        initViews();
        getRegionNameFromIntent();
        setupRecyclerView();
        setupClickListeners();
        setupBottomNavigationView();

        fetchTourismListFromApi();
    }

    private void initViews() {
        recyclerViewTourism = findViewById(R.id.recyclerViewTourism);
        textRegionTitle     = findViewById(R.id.textRegionTitle);
        btnBack             = findViewById(R.id.btnBack);
        navView             = findViewById(R.id.nav_view);
    }

    private void getRegionNameFromIntent() {
        subRegionName = getIntent().getStringExtra("subRegionName");
        regionName    = getIntent().getStringExtra("regionName");

        String title = !TextUtils.isEmpty(subRegionName) ? subRegionName
                : (!TextUtils.isEmpty(regionName) ? regionName : "관광지");
        textRegionTitle.setText(title);

        Log.d(TAG, "received regionName=" + regionName + ", subRegionName=" + subRegionName);
    }

    private void setupRecyclerView() {
        tourismAdapter = new TourismAdapter(
                apiItems,
                (item, position) -> {
                    // 아이템 클릭 시 동작
                    Intent intent = new Intent(TourismActivity.this, EventDetailActivity.class);
                    intent.putExtra("event_title", item.title != null ? item.title : "");
                    String sub = (item.addr1 != null && !item.addr1.isEmpty()) ? item.addr1
                            : (item.createdtime != null ? item.createdtime : "");
                    intent.putExtra("event_date", sub);
                    intent.putExtra("event_image_url", item.firstimage != null ? item.firstimage : "");
                    startActivity(intent);
                },
                (item, position) -> {
                    // 즐겨찾기 클릭 시 동작 (일단 토스트만)
                    Toast.makeText(TourismActivity.this,
                            "즐겨찾기: " + (item.title != null ? item.title : ""),
                            Toast.LENGTH_SHORT).show();
                }
        );

        recyclerViewTourism.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTourism.setAdapter(tourismAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupBottomNavigationView() {
        if (navView == null) return; // 레이아웃에 nav_view가 없을 수 있음
        navView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = new Intent(TourismActivity.this, MainActivity.class);
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


    private void fetchTourismListFromApi() {
        int areaCode = getAreaCode(regionName);

        TourApiService api = TourApiHelper.getApiService();

        Call<TourResponse> call = api.getTourList(
                30,
                1,
                "AND",
                "localit",
                "A",
                12,
                areaCode,
                "json",
                SERVICE_KEY
        );

        Log.d(TAG, "REQ URL: " + call.request().url());

        call.enqueue(new Callback<TourResponse>() {
            @Override
            public void onResponse(Call<TourResponse> call, Response<TourResponse> response) {
                if (!response.isSuccessful() || response.body() == null ||
                        response.body().response == null ||
                        response.body().response.body == null ||
                        response.body().response.body.items == null ||
                        response.body().response.body.items.item == null) {
                    Toast.makeText(TourismActivity.this, "관광지 데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                List<TourResponse.Item> items = response.body().response.body.items.item;


                tourismAdapter.submitList(items);
            }

            @Override
            public void onFailure(Call<TourResponse> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(TourismActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private int getAreaCode(String region) {
        if (TextUtils.isEmpty(region)) return 1; // 서울
        String key = clean(region);
        String standard = REGION_ALIAS.get(key);
        if (standard == null) {
            String stripped = stripSuffix(key);
            standard = REGION_ALIAS.get(stripped);
        }
        if (standard == null && AREA_CODE_MAP.containsKey(region)) {
            standard = region;
        }
        if (standard == null) {
            Log.w(TAG, "Unknown region '" + region + "', fallback to 서울특별시");
            standard = "서울특별시";
        }
        Integer code = AREA_CODE_MAP.get(standard);
        return code != null ? code : 1;
    }

    private String safe(String s) { return s == null ? "" : s; }
}