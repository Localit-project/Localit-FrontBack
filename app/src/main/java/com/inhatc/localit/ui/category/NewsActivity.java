package com.inhatc.localit.ui.category;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.inhatc.localit.MainActivity;
import com.inhatc.localit.R;
import com.inhatc.localit.api.naver.NaverApiService;
import com.inhatc.localit.api.naver.NaverNewsAdapter;
import com.inhatc.localit.api.naver.NaverNewsResponse;
import com.inhatc.localit.api.naver.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewNews;
    private NaverNewsAdapter newsAdapter;
    private List<NaverNewsResponse.Item> newsList;

    private TextView textRegionTitle;
    private ImageView btnBack;
    private BottomNavigationView navView;


    private NaverApiService apiService;
    private final String NAVER_CLIENT_ID = "hjVfnk_wdgYqW0xT86Ts";
    private final String NAVER_CLIENT_SECRET = "yFyvd5aHZ9";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        initViews();

        //  API 서비스 초기화
        apiService = RetrofitClient.getInstance().create(NaverApiService.class);

        //  리사이클러뷰와 어댑터 먼저 설정
        setupRecyclerView();

        //  지역명을 받아와서 API 호출
        String regionQuery = getRegionNameFromIntent();
        fetchNaverNews(regionQuery + "축제"+ "행사"+ "관광지");

        setupClickListeners();
        setupBottomNavigationView();
    }

    private void initViews() {
        recyclerViewNews = findViewById(R.id.recyclerViewNews);
        textRegionTitle = findViewById(R.id.textRegionTitle);
        btnBack = findViewById(R.id.btnBack);
        navView = findViewById(R.id.nav_view);
    }

    //  검색어를 반환하도록 메서드 구조 변경
    private String getRegionNameFromIntent() {
        String regionName = getIntent().getStringExtra("subRegionName");
        if (regionName == null || regionName.isEmpty()) {
            regionName = getIntent().getStringExtra("regionName");
        }

        String titleText;
        String query;

        if (regionName == null || regionName.isEmpty()) {
            titleText = "전체 뉴스";
            query = "뉴스"; // 기본 검색어
        } else if (regionName.equals("경기")) {
            titleText = "상세 지역 선택 필요";
            query = "경기도"; // 기본 검색어
        } else {
            titleText = regionName + " 뉴스";
            query = regionName;
        }
        textRegionTitle.setText(titleText);
        return query;
    }


    //  실제 API 데이터를 받아 처리하는 메서드
    private void fetchNaverNews(String query) {
        if (query == null || query.isEmpty()) {
            Toast.makeText(this, "검색어가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<NaverNewsResponse> call = apiService.getNews(
                NAVER_CLIENT_ID,
                NAVER_CLIENT_SECRET,
                query,
                30,
                "sim"
        );

        call.enqueue(new Callback<NaverNewsResponse>() {
            @Override
            public void onResponse(Call<NaverNewsResponse> call, Response<NaverNewsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    newsAdapter.updateData(response.body().getItems());
                } else {
                    Toast.makeText(NewsActivity.this, "뉴스를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<NaverNewsResponse> call, Throwable t) {
                Log.e("NewsActivity", "API 호출 실패", t);
                Toast.makeText(NewsActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        //  비어있는 리스트로 어댑터 초기화
        newsList = new ArrayList<>();
        newsAdapter = new NaverNewsAdapter(newsList, new NaverNewsAdapter.OnNewsClickListener() {
            @Override
            public void onNewsClick(NaverNewsResponse.Item item, int position) {
                if (item.getLink() != null && !item.getLink().isEmpty()) {

                    Toast.makeText(NewsActivity.this, "뉴스 페이지로 넘어갑니다", Toast.LENGTH_SHORT).show();


                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getLink()));
                    startActivity(intent);
                } else {
                    Toast.makeText(NewsActivity.this, "기사 링크가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFavoriteClick(NaverNewsResponse.Item item, int position) {
                // 즐겨찾기 기능은 별도 구현 필요
                Toast.makeText(NewsActivity.this, item.getTitle() + " 즐겨찾기!", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerViewNews.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewNews.setAdapter(newsAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

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
            finish();
            return true;
        });
    }
}