package com.inhatc.localit;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.inhatc.localit.databinding.ActivityMapBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.inhatc.localit.ui.CourseDetailActivity;


public class MapActivity extends AppCompatActivity {

    private ActivityMapBinding binding;
    private TextView[] regionTexts;
    private String[] regions;
    private BottomNavigationView navView;
    private ImageView btnBack;

    private void initViews() {
        btnBack             = findViewById(R.id.btnBack);
        navView             = findViewById(R.id.nav_view);
    }
    private void setupBottomNavigationView() {
        if (navView == null) return;
        navView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = new Intent(MapActivity.this, MainActivity.class);
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


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        navView = findViewById(R.id.nav_view);
        setupBottomNavigationView();

        // strings.xml의 지역 배열
        regions = getResources().getStringArray(R.array.regions_array);

        // 레이아웃의 지역 TextView 묶기 (activity_map.xml의 id들과 일치해야 함)
        regionTexts = new TextView[]{
                binding.textSeoul,
                binding.textIncheon,
                binding.textGyeonggi,
                binding.textGangwon,
                binding.textChungbuk,
                binding.textSejong,
                binding.textChungnam,
                binding.textDaejeon,
                binding.textGyeongbuk,
                binding.textDaegu,
                binding.textUlsan,
                binding.textBusan,
                binding.textGyeongnam,
                binding.textJeonbuk,
                binding.textGwangju,
                binding.textJeonnam,
                binding.textJeju
        };

        setRegionNames();
        setupRegionInteractions();

        // 알림 버튼: MainActivity의 알림 화면 바로 오픈
        ImageView btnNotifications = binding.btnNotifications;
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> {
                Intent intent = new Intent(MapActivity.this, MainActivity.class)
                        .putExtra("open_notifications", true)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }

        // 뒤로가기(툴바가 있다면 사용), 없으면 시스템 백버튼 사용
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setRegionNames() {
        for (int i = 0; i < regionTexts.length; i++) {
            regionTexts[i].setText(regions[i]);
        }
    }

    private void setupRegionInteractions() {
        for (TextView tv : regionTexts) {
            tv.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).start();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                        break;
                }
                return false;
            });

            tv.setOnClickListener(v -> {
                String regionName = ((TextView) v).getText().toString().replace("\n", "");
                Toast.makeText(this, regionName + " 선택됨", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, RegionDetailActivity.class);
                intent.putExtra("regionName", regionName);
                startActivity(intent);
            });
        }
    }

}
