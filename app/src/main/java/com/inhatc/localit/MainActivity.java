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
    private TextView[] regionTexts;   // 추가됨
    private String[] regions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // strings.xml에서 지역 이름 불러오기
        regions = getResources().getStringArray(R.array.regions_array);

        initViews();
        setRegionNames();           // 지역 이름 자동 세팅
        setupRegionInteractions();
        setupMenuClick();
    }

    private void initViews() {
        btnMenu = findViewById(R.id.btnMenu);

        // regionTexts 배열에 연결할 TextView 아이디 전부 넣기
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

    private void setRegionNames() {
        for (int i = 0; i < regionTexts.length; i++) {
            regionTexts[i].setText(regions[i]);  // TextView에 지역 이름 적용
        }
    }

    private void setupRegionInteractions() {
        for (TextView tv : regionTexts) {

            // 손가락 닿으면 확대, 떼면 복원
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
                return false; // false로 둬야 클릭 이벤트도 작동함
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
