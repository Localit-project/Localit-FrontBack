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
import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiMainActivity extends AppCompatActivity {

    private static final String TAG = "ApiMainActivity";
    private RecyclerView recyclerViewTourism;
    private TourAdapter tourAdapter;
    private List<TourResponse.Item> itemList = new ArrayList<>();

    private TextView textTourismTitle;

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
        setContentView(R.layout.activity_tourism);

        recyclerViewTourism = findViewById(R.id.recyclerViewTourism);
        recyclerViewTourism.setLayoutManager(new LinearLayoutManager(this));
        tourAdapter = new TourAdapter(itemList);
        recyclerViewTourism.setAdapter(tourAdapter);

        textTourismTitle = findViewById(R.id.textTourismTitle); // 관광지/축제 타이틀

        String regionName = getIntent().getStringExtra("regionName");
        int contentTypeId = getIntent().getIntExtra("contentTypeId", 12); // 기본 관광지

        if (regionName == null || regionName.isEmpty()) {
            Toast.makeText(this, "지역 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        int areaCode = getAreaCode(regionName);

        if (contentTypeId == 15) {
            textTourismTitle.setText("축제/행사");
            fetchFestivalList(areaCode);
        } else {
            textTourismTitle.setText("관광지");
            fetchTourList(areaCode);
        }
    }

    private int getAreaCode(String regionName) {
        return AREA_CODE_MAP.getOrDefault(regionName, 1);
    }

    private void fetchTourList(int areaCode) {
        String serviceKey = "wL/Ry8EMiMg43mPRl3wyQhKosVExsJbLLDcZebat4S4eedobtNuBG+eqrj5GPKHvEAxy4NjYPz25Parbyeg8PA==";

        TourApiService apiService = TourApiHelper.getApiService();
        Call<TourResponse> call = apiService.getTourList(
                100, 1, "AND", "localit", "c", 12, areaCode, "json", serviceKey
        );

        Log.d(TAG, "관광지 API: " + call.request().url());
        call.enqueue(getCallback());
    }

    private void fetchFestivalList(int areaCode) {
        String serviceKey = "wL/Ry8EMiMg43mPRl3wyQhKosVExsJbLLDcZebat4S4eedobtNuBG+eqrj5GPKHvEAxy4NjYPz25Parbyeg8PA==";
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        TourApiService apiService = TourApiHelper.getApiService();
        Call<TourResponse> call = apiService.getFestivalList(
                100,
                1,
                "AND",         // MobileOS
                "localit",     // MobileApp
                "json",        // _type
                areaCode,
                today,
                "c",           // arrange
                serviceKey
        );

        Log.d(TAG, "축제 API: " + call.request().url());
        call.enqueue(getCallback());
    }
    private Callback<TourResponse> getCallback() {
        return new Callback<TourResponse>() {
            @Override
            public void onResponse(Call<TourResponse> call, Response<TourResponse> response) {
                if (response.isSuccessful() && response.body() != null &&
                        response.body().response != null &&
                        response.body().response.body != null &&
                        response.body().response.body.items != null) {

                    List<TourResponse.Item> items = response.body().response.body.items.item;
                    itemList.clear();
                    itemList.addAll(items);
                    tourAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(ApiMainActivity.this, "API 응답 오류", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TourResponse> call, Throwable t) {
                Toast.makeText(ApiMainActivity.this, "API 호출 실패: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "API 호출 실패", t);
            }
        };
    }
}