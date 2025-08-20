package com.inhatc.localit.ui.category;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.inhatc.localit.MainActivity;
import com.inhatc.localit.R;
import com.inhatc.localit.api.SpotApiHelper;
import com.inhatc.localit.api.SpotApiService;
import com.inhatc.localit.api.SpotResponse;

import java.net.URLEncoder;
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

public class FestivalActivity extends AppCompatActivity {

    private static final String TAG = "FestivalActivity";
    private static final String SERVICE_KEY =
            "wL/Ry8EMiMg43mPRl3wyQhKosVExsJbLLDcZebat4S4eedobtNuBG+eqrj5GPKHvEAxy4NjYPz25Parbyeg8PA==";

    private RecyclerView recyclerViewEvents;
    private FestivalAdapter festivalAdapter;

    //  API 원본 목록
    private final List<SpotResponse.Item> fullItems = new ArrayList<>();

    private TextView textRegionTitle;
    private ImageView btnBack;
    private BottomNavigationView navView;

    //  검색 뷰
    private TextInputLayout searchInputLayout;
    private TextInputEditText etSearch;

    private String regionName;
    private String subRegionName;

    private static final Map<String, Integer> AREA_CODE_MAP = new HashMap<>();
    private static final Map<String, String>  REGION_ALIAS  = new HashMap<>();
    private static final Map<String, Integer> GG_SIGUNGU    = new HashMap<>();

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
        setContentView(R.layout.activity_festival);

        initViews();
        getRegionNameFromIntent();
        setupRecyclerView();
        setupSearchBar();
        setupClickListeners();
        setupBottomNavigationView();

        fetchFestivalListFromApi();
    }

    private void initViews() {
        recyclerViewEvents   = findViewById(R.id.recyclerViewEvents);
        textRegionTitle      = findViewById(R.id.textRegionTitle);
        btnBack              = findViewById(R.id.btnBack);
        navView              = findViewById(R.id.nav_view);

        searchInputLayout    = findViewById(R.id.searchInputLayout);
        etSearch             = findViewById(R.id.etSearch);
    }

    private void getRegionNameFromIntent() {
        subRegionName = getIntent().getStringExtra("subRegionName");
        regionName    = getIntent().getStringExtra("regionName");

        String title = !TextUtils.isEmpty(subRegionName) ? subRegionName
                : (!TextUtils.isEmpty(regionName) ? regionName : "축제·행사");
        textRegionTitle.setText(title);

        Log.d(TAG, "received regionName=" + regionName + ", subRegionName=" + subRegionName);
    }

    private void setupRecyclerView() {
        festivalAdapter = new FestivalAdapter(
                new ArrayList<>(),
                (item, position) -> openHomepageFor(item),
                (item, position) -> { /* TODO: 즐겨찾기 저장 필요시 처리 */ }
        );
        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewEvents.setAdapter(festivalAdapter);
    }

    /**  검색바: 돋보기/IME 검색으로 로컬 필터 */
    private void setupSearchBar() {
        if (searchInputLayout != null) {
            searchInputLayout.setEndIconOnClickListener(v -> triggerSearch());
        }
        if (etSearch != null) {
            etSearch.setOnEditorActionListener((tv, actionId, ev) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    triggerSearch();
                    return true;
                }
                return false;
            });
        }
    }

    private void triggerSearch() {
        String q = (etSearch != null && etSearch.getText() != null)
                ? etSearch.getText().toString().trim() : "";
        applyFilter(q);
        hideKeyboard();
    }

    /** ✅로컬 필터: 제목/주소에 키워드 포함 */
    private void applyFilter(String keyword) {
        if (TextUtils.isEmpty(keyword)) {
            festivalAdapter.submitList(new ArrayList<>(fullItems));
            return;
        }
        String k = keyword.toLowerCase();
        List<SpotResponse.Item> out = new ArrayList<>();
        for (SpotResponse.Item it : fullItems) {
            String t = it != null && it.title != null ? it.title : "";
            String a = it != null && it.addr1 != null ? it.addr1 : "";
            if (t.toLowerCase().contains(k) || a.toLowerCase().contains(k)) {
                out.add(it);
            }
        }
        festivalAdapter.submitList(out);
    }

    private void setupClickListeners() { btnBack.setOnClickListener(v -> finish()); }

    private void setupBottomNavigationView() {
        navView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = new Intent(FestivalActivity.this, MainActivity.class);
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

    private void fetchFestivalListFromApi() {
        int areaCode = getAreaCode(regionName);
        String startDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        Integer sigunguCode = getSigunguIfGyeonggi(regionName, subRegionName);

        Log.d(TAG, "[FestivalItem] areaCode=" + areaCode + ", sigungu=" + sigunguCode
                + ", region=" + regionName + ", sub=" + subRegionName);

        SpotApiService api = SpotApiHelper.getApiService();
        Call<SpotResponse> call = api.getFestivalList(
                30, 1, "AND", "localit", "json",
                areaCode, sigunguCode, startDate, "A", SERVICE_KEY
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
                    Toast.makeText(FestivalActivity.this, "축제 데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                List<SpotResponse.Item> items = response.body().response.body.items.item;

                if (sigunguCode != null && !TextUtils.isEmpty(subRegionName)) {
                    items = filterByAddr(items, subRegionName);
                }

                fullItems.clear();
                fullItems.addAll(items);

                String currentQuery = etSearch != null && etSearch.getText() != null
                        ? etSearch.getText().toString().trim() : "";
                applyFilter(currentQuery);
            }

            @Override
            public void onFailure(Call<SpotResponse> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(FestivalActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** 카드 클릭 시: 홈페이지 있으면 열고, 없으면 '대한민국 구석구석' 검색으로 이동 */
    private void openHomepageFor(SpotResponse.Item item) {
        if (item == null) return;

        String contentId = item.contentid;
        if (TextUtils.isEmpty(contentId)) {
            Toast.makeText(this, "콘텐츠 ID가 없어 이동할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        String contentTypeId = !TextUtils.isEmpty(item.contenttypeid) ? item.contenttypeid : "15";
        final String titleFinal = item.title == null ? "" : item.title;
    }

    private void openInCustomTab(String url) {
        try {
            new CustomTabsIntent.Builder().build()
                    .launchUrl(this, android.net.Uri.parse(url));
        } catch (Exception e) {
            try { startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))); }
            catch (Exception ignored) {}
        }
    }

    private Integer getSigunguIfGyeonggi(String region, String sub) {
        if (TextUtils.isEmpty(sub)) return null;
        int area = getAreaCode(region);
        if (area != 31) return null;
        String s = sub.trim();
        if ("전체".equals(s)) return null;
        return GG_SIGUNGU.get(s);
    }

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

    private int getAreaCode(String region) {
        if (TextUtils.isEmpty(region)) return 1;
        String key = clean(region);
        String standard = REGION_ALIAS.get(key);
        if (standard == null) {
            String stripped = stripSuffix(key);
            standard = REGION_ALIAS.get(stripped);
        }
        if (standard == null && AREA_CODE_MAP.containsKey(region)) standard = region;
        if (standard == null) { Log.w(TAG, "Unknown region '"+region+"', fallback to 서울특별시"); standard = "서울특별시"; }
        Integer code = AREA_CODE_MAP.get(standard);
        return code != null ? code : 1;
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null && etSearch != null) {
                imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
            }
        } catch (Exception ignored) {}
    }
}
