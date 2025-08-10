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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewEvents;
    private EventsAdapter eventsAdapter;
    private final List<TourResponse.Item> apiItems = new ArrayList<>();

    private TextView textRegionTitle;
    private ImageView btnBack;
    private BottomNavigationView navView;

    private String regionName;
    private String subRegionName;

    private static final String TAG = "EventsActivity";
    private static final String SERVICE_KEY = "wL/Ry8EMiMg43mPRl3wyQhKosVExsJbLLDcZebat4S4eedobtNuBG+eqrj5GPKHvEAxy4NjYPz25Parbyeg8PA==";


    private static final Map<String, Integer> AREA_CODE_MAP = new HashMap<>();

    private static final Map<String, String> REGION_ALIAS = new HashMap<>();

    static {
        // 표준명
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
        for (String a : aliases) {
            REGION_ALIAS.put(clean(a), standard);
        }
    }

    private static String clean(String s) {
        return s == null ? "" : s.replaceAll("\\s+", "");
    }

    private static String stripSuffix(String k) {
        // "서울시" → "서울" 같은 케이스 대응
        return k.replaceAll("(광역시|특별자치시|특별자치도|특별시|자치시|자치도|시|도)$", "");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        initViews();
        getRegionNameFromIntent();
        setupRecyclerView();
        setupClickListeners();
        setupBottomNavigationView();

        fetchFestivalListFromApi();
    }

    private void initViews() {
        recyclerViewEvents = findViewById(R.id.recyclerViewEvents);
        textRegionTitle = findViewById(R.id.textRegionTitle);
        btnBack = findViewById(R.id.btnBack);
        navView = findViewById(R.id.nav_view);
    }

    private void getRegionNameFromIntent() {
        subRegionName = getIntent().getStringExtra("subRegionName");
        regionName = getIntent().getStringExtra("regionName");

        String title = !TextUtils.isEmpty(subRegionName) ? subRegionName :
                !TextUtils.isEmpty(regionName) ? regionName : "축제·행사";
        textRegionTitle.setText(title);

        Log.d(TAG, "received regionName=" + regionName + ", subRegionName=" + subRegionName);
    }

    private void setupRecyclerView() {
        eventsAdapter = new EventsAdapter(apiItems, (item, position) -> {
            Intent intent = new Intent(EventsActivity.this, EventDetailActivity.class);
            intent.putExtra("event_title", safe(item.title));
            intent.putExtra("event_date", buildDateText(formatDate(item.eventstartdate), formatDate(item.eventenddate)));
            intent.putExtra("event_image_url", safe(item.firstimage));
            startActivity(intent);
        }, (item, position) -> {
            // TODO: 즐겨찾기 저장 붙일 때 구현
        });

        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewEvents.setAdapter(eventsAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupBottomNavigationView() {
        navView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = new Intent(EventsActivity.this, MainActivity.class);
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

    //축제호출
    private void fetchFestivalListFromApi() {
        int areaCode = getAreaCode(regionName);
        String startDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        Log.d(TAG, "fetch with areaCode=" + areaCode + " (" + regionName + ")");
        TourApiService api = TourApiHelper.getApiService();
        Call<TourResponse> call = api.getFestivalList(
                30, 1, "AND", "localit", "json", areaCode, startDate, "A", SERVICE_KEY
        );

        call.enqueue(new Callback<TourResponse>() {
            @Override
            public void onResponse(Call<TourResponse> call, Response<TourResponse> response) {
                if (!response.isSuccessful() || response.body() == null ||
                        response.body().response == null ||
                        response.body().response.body == null ||
                        response.body().response.body.items == null ||
                        response.body().response.body.items.item == null) {
                    Toast.makeText(EventsActivity.this, "축제 데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                List<TourResponse.Item> items = response.body().response.body.items.item;
                apiItems.clear();
                apiItems.addAll(items);
                eventsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<TourResponse> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(EventsActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 지역명 정규화 → 표준명 → areaCode
    private int getAreaCode(String region) {
        if (TextUtils.isEmpty(region)) return 1; // 기본 서울

        String key = clean(region);                       // 공백 제거
        String standard = REGION_ALIAS.get(key);          // 별칭 매핑
        if (standard == null) {
            // 접미사 제거 후 재시도
            String stripped = stripSuffix(key);
            standard = REGION_ALIAS.get(stripped);
        }
        if (standard == null) {
            // 혹시 이미 표준형으로 들어왔을 수도
            if (AREA_CODE_MAP.containsKey(region)) standard = region;
        }
        if (standard == null) {
            Log.w(TAG, "Unknown region '" + region + "', fallback to 서울특별시");
            standard = "서울특별시";
        }
        Integer code = AREA_CODE_MAP.get(standard);
        return code != null ? code : 1;
    }

    private String formatDate(String raw) {
        if (!TextUtils.isEmpty(raw) && raw.length() >= 8) {
            return raw.substring(0, 4) + "." + raw.substring(4, 6) + "." + raw.substring(6, 8);
        }
        return "";
    }

    private String buildDateText(String start, String end) {
        if (!TextUtils.isEmpty(start) && !TextUtils.isEmpty(end)) return start + " ~ " + end;
        if (!TextUtils.isEmpty(start)) return start;
        if (!TextUtils.isEmpty(end)) return end;
        return "일정 미정";
    }

    private String safe(String s) { return s == null ? "" : s; }
}