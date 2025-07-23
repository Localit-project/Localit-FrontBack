package com.inhatc.localit;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RegionDetailActivity extends AppCompatActivity {

    private TextView textRegionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_region_detail);

        textRegionName = findViewById(R.id.textRegionName);

        String regionName = getIntent().getStringExtra("regionName");
        textRegionName.setText(regionName + " 상세 정보");
        // 여기에 실제 데이터 바인딩 로직을 추가
    }
}
