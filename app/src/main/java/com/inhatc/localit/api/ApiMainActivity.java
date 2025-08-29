package com.inhatc.localit.api;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.inhatc.localit.R;

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

public class ApiMainActivity extends AppCompatActivity {

    private static final String TAG = "ApiMainActivity";
    private RecyclerView recyclerViewTourism;
    private SpotAdapter spotAdapter;
    private final List<SpotResponse.Item> itemList = new ArrayList<>();

    private TextView textTourismTitle;

    private String regionName;
    private String subRegionName;

    private static final String SERVICE_KEY =
            "wL/Ry8EMiMg43mPRl3wyQhKosVExsJbLLDcZebat4S4eedobtNuBG+eqrj5GPKHvEAxy4NjYPz25Parbyeg8PA==";

    /** 시/도 → areaCode */
    private static final Map<String, Integer> AREA_CODE_MAP = new HashMap<>();
    /** 별칭(약칭) → 표준명 매핑 */
    private static final Map<String, String> REGION_ALIAS = new HashMap<>();

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

        // 별칭 매핑 (약칭, ~시 등 들어와도 표준명으로 정규화)
        alias("서울특별시", "서울", "서울시");
        alias("부산광역시", "부산", "부산시");
        alias("대구광역시", "대구", "대구시");
        alias("인천광역시", "인천", "인천시");
        alias("광주광역시", "광주", "광주시");
        alias("대전광역시", "대전", "대전시");
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
    }

    /** 경기도 하위 시·군 → sigunguCode (★ 실제 areaCode1 응답으로 갱신 권장, 아래는 예시/임시) */
    private static final Map<String, Integer> GG_SIGUNGU = new HashMap<>();
    static {
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
        setContentView(R.layout.activity_spot);

        recyclerViewTourism = findViewById(R.id.recyclerViewTourism);
        recyclerViewTourism.setLayoutManager(new LinearLayoutManager(this));
        spotAdapter = new SpotAdapter(itemList);
        recyclerViewTourism.setAdapter(spotAdapter);

        textTourismTitle = findViewById(R.id.textTourismTitle);

        regionName = getIntent().getStringExtra("regionName");
        subRegionName = getIntent().getStringExtra("subRegionName");
        int contentTypeId = getIntent().getIntExtra("contentTypeId", 12); // 12: 관광지, 15: 축제

        if (regionName == null || regionName.isEmpty()) {
            Toast.makeText(this, "지역 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        int areaCode = getAreaCode(regionName);
        Integer sigunguCode = getSigunguIfGyeonggi(regionName, subRegionName);

        if (contentTypeId == 15) {
            textTourismTitle.setText("축제/행사");
            fetchFestivalList(areaCode, sigunguCode);
        } else {
            textTourismTitle.setText("관광지");
            fetchTourList(areaCode, sigunguCode);
        }
    }

    /** 별칭/접미사 제거를 포함한 areaCode 산출 (서울 폴백 방지) */
    private int getAreaCode(String region) {
        if (region == null || region.trim().isEmpty()) return 1;
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

    /** 경기도일 때만 하위도시 코드를 매핑 */
    private Integer getSigunguIfGyeonggi(String region, String sub) {
        if (!"경기도".equals(region)) return null;
        if (sub == null) return null;
        String s = sub.trim();
        if (s.isEmpty() || "전체".equals(s)) return null;
        return GG_SIGUNGU.get(s);
    }

    private void fetchTourList(int areaCode, Integer sigunguCode) {
        SpotApiService apiService = SpotApiHelper.getApiService();
        Call<SpotResponse> call = apiService.getTourList(
                100, 1, "AND", "localit", "c", 12, areaCode, sigunguCode, "json", SERVICE_KEY
        );

        Log.d(TAG, "관광지 API: " + call.request().url());
        call.enqueue(makeCallback(sigunguCode));
    }

    private void fetchFestivalList(int areaCode, Integer sigunguCode) {
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        SpotApiService apiService = SpotApiHelper.getApiService();
        Call<SpotResponse> call = apiService.getFestivalList(
                100, 1, "AND", "localit", "json",
                areaCode,
                sigunguCode,
                today,
                "c",
                SERVICE_KEY
        );

        Log.d(TAG, "축제 API: " + call.request().url());
        call.enqueue(makeCallback(sigunguCode));
    }

    /** 서버가 sigunguCode를 무시하거나 코드가 맞지 않을 경우 addr1로 보조 필터 */
    private Callback<SpotResponse> makeCallback(Integer sigunguCode) {
        return new Callback<SpotResponse>() {
            @Override
            public void onResponse(Call<SpotResponse> call, Response<SpotResponse> response) {
                if (response.isSuccessful() && response.body() != null &&
                        response.body().response != null &&
                        response.body().response.body != null &&
                        response.body().response.body.items != null &&
                        response.body().response.body.items.item != null) {

                    List<SpotResponse.Item> items = response.body().response.body.items.item;

                    if (sigunguCode != null && subRegionName != null && !subRegionName.trim().isEmpty()) {
                        items = filterByAddr(items, subRegionName.trim());
                    }

                    itemList.clear();
                    itemList.addAll(items);
                    spotAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(ApiMainActivity.this, "API 응답 오류", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SpotResponse> call, Throwable t) {
                Toast.makeText(ApiMainActivity.this, "API 호출 실패: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "API 호출 실패", t);
            }
        };
    }

    private List<SpotResponse.Item> filterByAddr(List<SpotResponse.Item> src, String key) {
        if (src == null) return new ArrayList<>();
        if (key == null || key.isEmpty()) return src;

        String k = key.trim();
        List<SpotResponse.Item> out = new ArrayList<>();
        for (SpotResponse.Item it : src) {
            String addr = (it != null && it.addr1 != null) ? it.addr1 : "";
            if (addr.startsWith(k) || addr.contains(" " + k) || addr.contains(k + " ")) {
                out.add(it);
            }
        }
        return out;
    }
}