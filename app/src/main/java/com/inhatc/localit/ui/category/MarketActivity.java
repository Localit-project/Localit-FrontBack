package com.inhatc.localit.ui.category;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.inhatc.localit.R;

public class MarketActivity extends AppCompatActivity {

    private RecyclerView marketRecyclerView;
    private MarketAdapter marketAdapter;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market);

        // UI 요소 초기화
        btnBack = findViewById(R.id.btnBack);
        marketRecyclerView = findViewById(R.id.marketRecyclerView);

        // 뒤로가기 버튼 클릭 리스너 설정
        btnBack.setOnClickListener(v -> finish());

        // TODO: 1. RecyclerView 및 어댑터 설정하기
        // 예: marketRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //     marketAdapter = new MarketAdapter(...);
        //     marketRecyclerView.setAdapter(marketAdapter);

        // TODO: 2. API를 통해 전체 시장 목록 데이터 불러오기
        // 예: fetchMarketData();
    }
}