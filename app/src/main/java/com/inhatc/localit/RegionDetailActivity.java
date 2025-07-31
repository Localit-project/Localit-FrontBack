package com.inhatc.localit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.inhatc.localit.ui.category.EventsActivity;
import com.inhatc.localit.ui.category.NewsActivity;
import com.inhatc.localit.ui.category.TourismActivity;

public class RegionDetailActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView btnMoreFestivals, btnMoreTourism, btnMoreNews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_region_detail);

        initViews();
        setClickListeners();

        // MainActivity에서 넘긴 지역 이름 받기
        String regionName = getIntent().getStringExtra("regionName");

        // Title TextView 표시
        TextView textRegionTitle = findViewById(R.id.textRegionTitle);
        textRegionTitle.setText(regionName);

        // CardView 제어
        CardView cardSubRegion = findViewById(R.id.cardSubRegion);
        if ("경기도".equals(regionName)) {
            cardSubRegion.setVisibility(View.VISIBLE);  // 경기도일 때만 보임
        } else {
            cardSubRegion.setVisibility(View.GONE);     // 그 외에는 숨김
        }

        // [1] 축제·행사 섹션
        TextView textFestival1Title = findViewById(R.id.textFestival1Title);
        TextView textFestival1Date = findViewById(R.id.textFestival1Date);
        TextView textFestival1DateInfo = findViewById(R.id.textFestival1DateInfo);

        TextView textFestival2Title = findViewById(R.id.textFestival2Title);
        TextView textFestival2Date = findViewById(R.id.textFestival2Date);
        TextView textFestival2DateInfo = findViewById(R.id.textFestival2DateInfo);

        // [2] 관광지 섹션
        TextView textTourism1Title = findViewById(R.id.textTourism1Title);
        TextView textTourism1Location = findViewById(R.id.textTourism1Location);
        TextView textTourism1Date = findViewById(R.id.textTourism1Date);

        TextView textTourism2Title = findViewById(R.id.textTourism2Title);
        TextView textTourism2Location = findViewById(R.id.textTourism2Location);
        TextView textTourism2Date = findViewById(R.id.textTourism2Date);

        // [3] 뉴스 섹션
        TextView textNews1Title = findViewById(R.id.textNews1Title);
        TextView textNews1Source = findViewById(R.id.textNews1Source);
        TextView textNews1Date = findViewById(R.id.textNews1Date);

        TextView textNews2Title = findViewById(R.id.textNews2Title);
        TextView textNews2Source = findViewById(R.id.textNews2Source);
        TextView textNews2Date = findViewById(R.id.textNews2Date);

        // 임시 데이터 넣기
        setDummyData(
                textFestival1Title, textFestival1Date, textFestival1DateInfo,
                textFestival2Title, textFestival2Date, textFestival2DateInfo,
                textTourism1Title, textTourism1Location, textTourism1Date,
                textTourism2Title, textTourism2Location, textTourism2Date,
                textNews1Title, textNews1Source, textNews1Date,
                textNews2Title, textNews2Source, textNews2Date
        );
    }

    /** 버튼과 뷰 연결 */
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);  // 뒤로가기
        btnMoreFestivals = findViewById(R.id.btnMoreFestivals);
        btnMoreTourism = findViewById(R.id.btnMoreTourism);
        btnMoreNews = findViewById(R.id.btnMoreNews);
    }

    /** 클릭 이벤트 연결 */
    private void setClickListeners() {
        // 🔙 뒤로가기
        btnBack.setOnClickListener(v -> finish());

        // 축제·행사 → EventsActivity로 이동
        btnMoreFestivals.setOnClickListener(v -> {
            Intent intent = new Intent(RegionDetailActivity.this, EventsActivity.class);
            intent.putExtra("region_name", getIntent().getStringExtra("regionName")); // 선택한 지역 이름 전달
            startActivity(intent);
        });

        // 관광지 → TourismActivity로 이동
        btnMoreTourism.setOnClickListener(v -> {
            Intent intent = new Intent(RegionDetailActivity.this, TourismActivity.class);
            intent.putExtra("region_name", getIntent().getStringExtra("regionName"));
            startActivity(intent);
        });

        // 뉴스 → NewsActivity로 이동
        btnMoreNews.setOnClickListener(v -> {
            Intent intent = new Intent(RegionDetailActivity.this, NewsActivity.class);
            intent.putExtra("region_name", getIntent().getStringExtra("regionName"));
            startActivity(intent);
        });
    }

    /** 임시 데이터 넣는 메소드 */
    private void setDummyData(
            TextView f1Title, TextView f1Date, TextView f1DateInfo,
            TextView f2Title, TextView f2Date, TextView f2DateInfo,
            TextView t1Title, TextView t1Location, TextView t1Date,
            TextView t2Title, TextView t2Location, TextView t2Date,
            TextView n1Title, TextView n1Source, TextView n1Date,
            TextView n2Title, TextView n2Source, TextView n2Date
    ) {
        // 축제·행사
        f1Title.setText("2025년 수원화성문화제");
        f1Date.setText("기간 : 2025.07.19 ~ 2025.08.08");
        f1DateInfo.setText("2025.07.28");

        f2Title.setText("2025년 수원화성문화제");
        f2Date.setText("기간 : 2025.07.19 ~ 2025.08.08");
        f2DateInfo.setText("2025.07.28");

        // 관광지
        t1Title.setText("수원화성");
        t1Location.setText("경기도 수원시");
        t1Date.setText("2025.07.28");

        t2Title.setText("남한산성");
        t2Location.setText("경기도 광주시");
        t2Date.setText("2025.07.28");

        // 뉴스
        n1Title.setText("경기도 주요 뉴스");
        n1Source.setText("뉴스1");
        n1Date.setText("2025.07.28");

        n2Title.setText("경기도 핫이슈");
        n2Source.setText("뉴스2");
        n2Date.setText("2025.07.28");
    }
}
