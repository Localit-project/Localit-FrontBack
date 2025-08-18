package com.inhatc.localit.ui.category;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.inhatc.localit.MainActivity;
import com.inhatc.localit.R;
import com.inhatc.localit.api.SpotApiHelper;
import com.inhatc.localit.api.SpotApiService;
import com.inhatc.localit.api.SpotResponse;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TourismActivity extends AppCompatActivity {

    private static final String TAG = "TourismActivity";
    private static final String SERVICE_KEY =
            "wL/Ry8EMiMg43mPRl3wyQhKosVExsJbLLDcZebat4S4eedobtNuBG+eqrj5GPKHvEAxy4NjYPz25Parbyeg8PA==";

    private RecyclerView recyclerViewTourism;
    private TourismAdapter tourismAdapter;
    private final List<SpotResponse.Item> apiItems = new ArrayList<>();

    private TextView textRegionTitle;
    private ImageView btnBack;
    private BottomNavigationView navView;

    private String regionName;
    private String subRegionName;

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

        // 약칭/별칭 → 표준명 매핑
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

        // 경기도 하위 시/군 → sigunguCode (예시)
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
    private static String stripSuffix(String k) { return k.replaceAll("(광역시|특별자치시|특별자치도|특별시|자치시|자치도|시|도)$", ""); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spot);

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

                    openHomepageFor(item);
                },
                (item, position) -> Toast.makeText(
                        TourismActivity.this,
                        "즐겨찾기: " + (item.title != null ? item.title : ""),
                        Toast.LENGTH_SHORT
                ).show()
        );

        recyclerViewTourism.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTourism.setAdapter(tourismAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupBottomNavigationView() {
        if (navView == null) return;
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
        Integer sigunguCode = getSigunguIfGyeonggi(regionName, subRegionName);

        Log.d(TAG, "[CHECK] region=" + regionName + ", sub=" + subRegionName
                + ", areaCode=" + areaCode + ", sigungu=" + sigunguCode);

        SpotApiService api = SpotApiHelper.getApiService();

        Call<SpotResponse> call = api.getTourList(
                30, 1, "AND", "localit", "A",
                12, areaCode, sigunguCode,
                "json", SERVICE_KEY
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
                    Toast.makeText(TourismActivity.this, "관광지 데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                List<SpotResponse.Item> items = response.body().response.body.items.item;

                // 서버가 sigungu를 무시/불일치하면 addr1로 보조 필터
                if (sigunguCode != null && !TextUtils.isEmpty(subRegionName)) {
                    items = filterByAddr(items, subRegionName);
                }

                tourismAdapter.submitList(items);
            }

            @Override
            public void onFailure(Call<SpotResponse> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(TourismActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** 아이템 클릭 시 홈페이지 열기 (없으면 '대한민국 구석구석' 검색으로 Fallback) */
    private void openHomepageFor(SpotResponse.Item item) {
        if (item == null) return;

        String contentId = item.contentid;
        if (TextUtils.isEmpty(contentId)) {
            Toast.makeText(this, "콘텐츠 ID가 없어 이동할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 관광지 탭이므로 기본은 12, 응답에 contenttypeid가 있으면 사용
        String contentTypeId = !TextUtils.isEmpty(item.contenttypeid) ? item.contenttypeid : "12";

        // 제목 (검색 Fallback에 사용) - final 변수로 고정
        String t = item.title;
        final String titleFinal = (t == null ? "" : t);

        SpotApiHelper.fetchHomepageUrl(
                SpotApiHelper.getApiService(),
                SERVICE_KEY,
                contentId,
                contentTypeId,
                url -> {
                    if (url != null && url.startsWith("http")) {
                        openInCustomTab(url);
                    } else {
                        String q;
                        try {
                            q = URLEncoder.encode(titleFinal, "UTF-8");
                        } catch (Exception e) {
                            q = titleFinal;
                        }
                        String gukSearch = "https://korean.visitkorea.or.kr/search/search_list.do?keyword=" + q;
                        openInCustomTab(gukSearch);
                        Toast.makeText(this, "'대한민국 구석구석' 검색으로 이동합니다.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /** Custom Tabs로 URL 열기 (실패 시 브라우저) */
    private void openInCustomTab(String url) {
        try {
            new CustomTabsIntent.Builder().build()
                    .launchUrl(this, android.net.Uri.parse(url));
        } catch (Exception e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)));
            } catch (Exception ignored) {}
        }
    }

    /** 경기도 하위 시/군 코드 매핑 ("전체"면 null). 약칭(경기) 대응을 위해 areaCode==31로 판정 */
    private Integer getSigunguIfGyeonggi(String region, String sub) {
        if (TextUtils.isEmpty(sub)) return null;
        int area = getAreaCode(region);        // 별칭/접미사 정규화 포함
        if (area != 31) return null;           // 경기도만 sigungu 적용
        String s = sub.trim();
        if ("전체".equals(s)) return null;
        return GG_SIGUNGU.get(s);
    }

    /** addr1에 하위도시명이 포함돼 있는지로 2차 필터 */
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

    /** 별칭/접미사 제거 포함해서 areaCode 계산 (서울 폴백 방지) */
    private int getAreaCode(String region) {
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
}