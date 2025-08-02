package com.inhatc.localit.api;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.inhatc.localit.R;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiMainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TourAdapter tourAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_region);  // 여기서 해당 XML 레이아웃 이름 맞게 써야 함

//        recyclerView = findViewById(R.id.recyclerView);  // 이 ID가 위 XML에 있으니 잘 찾아짐
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchTourData();
    }

    private void fetchTourData() {
        String serviceKey = "cCqikGRYcwdqnAKSVaEQcME%2Fv00UJwxFvFLI5J6OqtD%2B0d6P8mgTtUtWS%2BmLssJgQdsuh2FDJOVS8X43SwbsMA%3D%3D"; // 실제 키로 교체하세요

        TourApiService service = TourApiHelper.getApiService();

        Call<TourResponse> call = service.getTourList(
                serviceKey,
                "AND",
                "MyApp",
                "A",
                12,
                1,   // areaCode: 1 = 서울
                10,
                1,
                "json"
        );

        call.enqueue(new Callback<TourResponse>() {
            @Override
            public void onResponse(Call<TourResponse> call, Response<TourResponse> response) {
                if (response.isSuccessful() && response.body() != null &&
                        response.body().response != null &&
                        response.body().response.body != null &&
                        response.body().response.body.items != null) {

                    List<TourResponse.Item> items = response.body().response.body.items.item;
                    tourAdapter = new TourAdapter(items);
                    recyclerView.setAdapter(tourAdapter);
                } else {
                    Toast.makeText(ApiMainActivity.this, "응답 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TourResponse> call, Throwable t) {
                Toast.makeText(ApiMainActivity.this, "API 호출 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}