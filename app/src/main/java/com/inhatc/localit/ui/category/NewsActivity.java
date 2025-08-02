package com.inhatc.localit.ui.category;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.inhatc.localit.EventDetailActivity;
import com.inhatc.localit.MainActivity;
import com.inhatc.localit.R;
import com.inhatc.localit.model.Event;

import java.util.ArrayList;
import java.util.List;

public class NewsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewNews;
    private NewsAdapter newsAdapter;
    private List<Event> newsList;
    private TextView textRegionTitle;
    private ImageView btnBack;
    private BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);   // 뉴스용 레이아웃

        initViews();
        getRegionNameFromIntent();  // 상단 타이틀에 지역명 표시
        setupData();
        setupRecyclerView();
        setupClickListeners();
        setupBottomNavigationView();  // 하단 네비게이션 설정
    }

    /** XML 뷰 초기화 */
    private void initViews() {
        recyclerViewNews = findViewById(R.id.recyclerViewNews);
        textRegionTitle = findViewById(R.id.textRegionTitle);
        btnBack = findViewById(R.id.btnBack);
        navView = findViewById(R.id.nav_view);
    }

    /** RegionDetailActivity에서 넘긴 지역명을 받아 타이틀에 표시 */
    private void getRegionNameFromIntent() {
        String regionName = getIntent().getStringExtra("region_name");
        if (regionName != null && !regionName.isEmpty()) {
            textRegionTitle.setText(regionName + " 뉴스");
        } else {
            textRegionTitle.setText("뉴스");  // 값이 없으면 기본값
        }
    }

    /** 임시 데이터 (API 연동 전까지 샘플) */
    private void setupData() {
        newsList = new ArrayList<>();

        // Event 객체 대신 News 전용 모델 만들어도 됨, 임시로 Event 재사용
        newsList.add(new Event("서울시 교육청, 새 정책 발표", "2025.08.01", R.drawable.sample1, false));
        newsList.add(new Event("부산 국제 영화제 개막", "2025.07.30", R.drawable.sample1, false));
        newsList.add(new Event("경기도 관광 산업 활성화 소식", "2025.07.28", R.drawable.sample1, false));
        newsList.add(new Event("제주도 자연 보호 캠페인 시작", "2025.07.25", R.drawable.sample1, false));
    }

    /** RecyclerView 연결 */
    private void setupRecyclerView() {
        newsAdapter = new NewsAdapter(newsList, new NewsAdapter.OnNewsClickListener() {
            @Override
            public void onNewsClick(Event news, int position) {
                // 뉴스 아이템 클릭 시 처리 (예: 상세 페이지 이동)
            }

            @Override
            public void onFavoriteClick(Event news, int position) {
                // 즐겨찾기 클릭 시 처리
                news.setFavorite(!news.isFavorite());
                newsAdapter.notifyItemChanged(position);
            }
        });


        recyclerViewNews.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewNews.setAdapter(newsAdapter);
    }

    /** 상단 뒤로가기 버튼 */
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    /** ✅ 네비게이션바 클릭 시 MainActivity 열어서 프래그먼트 전환되게 설정 */
    private void setupBottomNavigationView() {
        navView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = new Intent(NewsActivity.this, MainActivity.class);

            if (id == R.id.navigation_home) {
                intent.putExtra("start_fragment", 0);
            } else if (id == R.id.navigation_category) {
                intent.putExtra("start_fragment", 1);
            } else if (id == R.id.navigation_search) {
                intent.putExtra("start_fragment", 2);
            } else if (id == R.id.navigation_favorite) {
                intent.putExtra("start_fragment", 3);
            } else if (id == R.id.navigation_mypage) {
                intent.putExtra("start_fragment", 4);
            }

            startActivity(intent);
            finish(); // 현재 Activity 종료 (중복 쌓이지 않게)

            return true;
        });
    }
}
