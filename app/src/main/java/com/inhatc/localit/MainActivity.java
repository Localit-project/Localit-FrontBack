package com.inhatc.localit;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ImageView btnMenu;
    private TextView[] regionTexts;

    private String[] regions = {
            "서울특별시", "인천광역시", "경기도", "강원특별자치도",
            "충청북도", "세종특별자치시", "충청남도", "대전광역시",
            "경상북도", "대구광역시", "울산광역시", "부산광역시",
            "경상남도", "전북특별자치도", "광주광역시", "전라남도", "제주특별자치도"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        setupRegionInteractions();
        setupMenuClick();
    }

    private void initViews() {
        btnMenu = findViewById(R.id.btnMenu);
        regionTexts = new TextView[]{
                findViewById(R.id.textSeoul),
                findViewById(R.id.textIncheon),
                findViewById(R.id.textGyeonggi),
                findViewById(R.id.textGangwon),
                findViewById(R.id.textChungbuk),
                findViewById(R.id.textSejong),
                findViewById(R.id.textChungnam),
                findViewById(R.id.textDaejeon),
                findViewById(R.id.textGyeongbuk),
                findViewById(R.id.textDaegu),
                findViewById(R.id.textUlsan),
                findViewById(R.id.textBusan),
                findViewById(R.id.textGyeongnam),
                findViewById(R.id.textJeonbuk),
                findViewById(R.id.textGwangju),
                findViewById(R.id.textJeonnam),
                findViewById(R.id.textJeju)
        };
    }

    private void setupRegionInteractions() {
        for (TextView tv : regionTexts) {

            // 손가락 닿으면 확대, 떼면 복원
            tv.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 닿자마자 확대
                        v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).start();
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // 떼면 원래 크기
                        v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                        break;
                }
                return false; // 클릭 이벤트도 작동하게 하려면 false
            });

            // 클릭 시 지역 선택 로직
            tv.setOnClickListener(v -> {
                String regionName = ((TextView) v).getText().toString().replace("\n", "");
                onRegionSelected(regionName);
            });
        }
    }



    private void setupMenuClick() {
        btnMenu.setOnClickListener(v ->
                Toast.makeText(this, "메뉴 클릭", Toast.LENGTH_SHORT).show()
        );
    }

    private void onRegionSelected(String regionName) {
        Toast.makeText(this, regionName + " 선택됨", Toast.LENGTH_SHORT).show();

        // 상세 페이지로 전환 예시
        Intent intent = new Intent(MainActivity.this, RegionDetailActivity.class);
        intent.putExtra("regionName", regionName);
        startActivity(intent);
    }
}
