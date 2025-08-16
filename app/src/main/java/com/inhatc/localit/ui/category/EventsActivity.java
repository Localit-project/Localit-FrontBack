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
import com.inhatc.localit.FestivalDetailActivity;
import com.inhatc.localit.MainActivity;
import com.inhatc.localit.R;
import com.inhatc.localit.api.SpotApiHelper;
import com.inhatc.localit.api.SpotApiService;
import com.inhatc.localit.api.SpotResponse;

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

    private static final String TAG = "EventsActivity";
    private static final String SERVICE_KEY =
            "wL/Ry8EMiMg43mPRl3wyQhKosVExsJbLLDcZebat4S4eedobtNuBG+eqrj5GPKHvEAxy4NjYPz25Parbyeg8PA==";

    private RecyclerView recyclerViewEvents;
    private EventsAdapter eventsAdapter;
    private final List<SpotResponse.Item> apiItems = new ArrayList<>();

    private TextView textRegionTitle;
    private ImageView btnBack;
    private BottomNavigationView navView;

    private String regionName;
    private String subRegionName;

    // ===== 코드/매핑 =====
    private static final Map<String, Integer> AREA_CODE_MAP = new HashMap<>();
    private static final Map<String, String>  REGION_ALIAS  = new HashMap<>();
    private static final Map<String, Integer> GG_SIGUNGU    = new HashMap<>();

    static {
        // 시/도 → areaCode
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

        // 별칭/약칭 → 표준명
        alias("서울특별시", "서울", "서울시");
        alias("인천광역시", "인천", "인천시");
        alias("대전광역시", "대전", "대전시");
        alias("대구광역시", "대구", "대구시");
        alias("광주광역시", "광주", "광주시");
        alias("부산광역시", "부산", "부산시");
        alias("울산광역시", "울산", "울산시");
        alias("세종특별자치시", "세종", "세종시");

        alias("경기도", "경기");
        alias("강원특별자치도", "강원", "강원도");
        alias("충청북도", "충북");
        alias("충청남도", "충남");
        alias("전라북도", "전북");
        alias("전라남도", "전남");
        alias("경상북도", "경북");
        alias("경상남도", "경남");
        alias("제주특별자치도", "제주", "제주도");

        // 경기도 하위 시/군 (예시 코드, 실제 API로 갱신 권장)
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
        setContentView(R.layout.activity_festival);

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
            Intent intent = new Intent(EventsActivity.this, FestivalDetailActivity.class);
            intent.putExtra("event_title", safe(item.title));
            intent.putExtra("event_date", buildDateText(formatDate(item.eventstartdate), formatDate(item.eventenddate)));
            intent.putExtra("event_image_url", safe(item.firstimage));
            startActivity(intent);
        }, (item, position) -> {
            // TODO: 즐겨찾기 저장
        });

        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewEvents.setAdapter(eventsAdapter);
    }

    private void setupClickListeners() { btnBack.setOnClickListener(v -> finish()); }

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

    /** 축제 호출 (경기도 하위 시/군 코드 적용 + 약칭 대응) */
    private void fetchFestivalListFromApi() {
        int areaCode = getAreaCode(regionName);
        String startDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        Integer sigunguCode = getSigunguIfGyeonggi(regionName, subRegionName);

        Log.d(TAG, "[Festival] areaCode=" + areaCode + ", sigungu=" + sigunguCode + ", region=" + regionName + ", sub=" + subRegionName);

        SpotApiService api = SpotApiHelper.getApiService();
        Call<SpotResponse> call = api.getFestivalList(
                30, 1, "AND", "localit", "json",
                areaCode,
                sigunguCode,
                startDate,
                "A",
                SERVICE_KEY
        );

        Log.d(TAG, "REQ URL: " + call.request().url());

        call.enqueue(new Callback<SpotResponse>() {
            @Override
            public void onResponse(Call<SpotResponse> call, Response<SpotResponse> response) {
                if (!response.isSuccessful() || response.body() == null ||
                        response.body().response == null ||
                        response.body().response.body == null ||
                        response.body().response.body.items == null ||
                        response.body().response.body.items.item == null) {
                    Toast.makeText(EventsActivity.this, "축제 데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                List<SpotResponse.Item> items = response.body().response.body.items.item;

                // 서버가 sigungu를 무시/불일치할 대비 보조 필터
                if (sigunguCode != null && !TextUtils.isEmpty(subRegionName)) {
                    items = filterByAddr(items, subRegionName);
                }

                apiItems.clear();
                apiItems.addAll(items);
                eventsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<SpotResponse> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(EventsActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** 경기도일 때만 하위도시 코드를 매핑 ("전체"면 null). 약칭 대응을 위해 areaCode==31 판정 */
    private Integer getSigunguIfGyeonggi(String region, String sub) {
        if (TextUtils.isEmpty(sub)) return null;
        int area = getAreaCode(region);
        if (area != 31) return null; // 경기도만 적용
        String s = sub.trim();
        if ("전체".equals(s)) return null;
        return GG_SIGUNGU.get(s);
    }

    /** addr1 보조 필터 */
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

    // 지역명 정규화 → areaCode
    private int getAreaCode(String region) {
        if (TextUtils.isEmpty(region)) return 1;
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