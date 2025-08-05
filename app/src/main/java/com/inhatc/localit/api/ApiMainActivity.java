package com.inhatc.localit.api;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.inhatc.localit.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiMainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewTourism;  // ID 이름 맞춤
    private TourAdapter tourAdapter;
    private List<TourResponse.Item> itemList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tourism);  // 이 XML에는 recyclerViewTourism이 존재해야 함

        recyclerViewTourism = findViewById(R.id.recyclerViewTourism);  // XML에 맞게 수정
        recyclerViewTourism.setLayoutManager(new LinearLayoutManager(this));

        // ★ 빈 리스트로 초기 어댑터 설정
        tourAdapter = new TourAdapter(itemList);
        recyclerViewTourism.setAdapter(tourAdapter);

        fetchAreaBasedList(); // Retrofit API 호출
    }

    private void fetchAreaBasedList() {
        String serviceKey = "cCqikGRYcwdqnAKSVaEQcME%2Fv00UJwxFvFLI5J6OqtD%2B0d6P8mgTtUtWS%2BmLssJgQdsuh2FDJOVS8X43SwbsMA%3D%3D";

        TourApiService apiService = TourApiHelper.getApiService();

        Call<TourResponse> call = apiService.getTourList(
                12, 1, "AND", "localit", "c", 12, 1, "20250101", serviceKey
        );

        Log.d("API_CALL", "요청 보냄: " + call.request().url());

        call.enqueue(new Callback<TourResponse>() {
            @Override
            public void onResponse(Call<TourResponse> call, Response<TourResponse> response) {
                if (response.isSuccessful() && response.body() != null &&
                        response.body().response != null &&
                        response.body().response.body != null &&
                        response.body().response.body.items != null) {

                    Log.d("API 응답 성공", new Gson().toJson(response.body()));

                    List<TourResponse.Item> items = response.body().response.body.items.item;

                    if (items != null && !items.isEmpty()) {
                        itemList.clear();                // 기존 리스트 초기화
                        itemList.addAll(items);         // 새 데이터 추가
                        tourAdapter.notifyDataSetChanged();  // 변경 사항 UI 반영
                    } else {
                        Log.e("API Error", "빈 데이터");
                    }
                } else {
                    try {
                        Log.e("API 응답 실패", response.errorBody().string());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(ApiMainActivity.this, "응답 실패", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<TourResponse> call, Throwable t) {
                Toast.makeText(ApiMainActivity.this, "API 호출 실패: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
