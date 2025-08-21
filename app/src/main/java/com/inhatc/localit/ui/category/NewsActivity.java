package com.inhatc.localit.ui.category;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
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

    // ✅ 검색 뷰
    private TextInputLayout searchInputLayout;
    private TextInputEditText etSearch;

    private NaverApiService apiService;
    private final String NAVER_CLIENT_ID = "hjVfnk_wdgYqW0xT86Ts";
    private final String NAVER_CLIENT_SECRET = "yFyvd5aHZ9";

    // 지역 기본 검색어(예: "서울", "경기도")
    private String regionBaseQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        initViews();

        apiService = RetrofitClient.getInstance().create(NaverApiService.class);
        setupRecyclerView();

        // 지역명을 받아와 기본 검색어 확보
        regionBaseQuery = getRegionNameFromIntent();

        // 검색바 동작 연결
        setupSearchBar();

        // 초기 로딩: 지역 기본 검색어 + 카테고리 키워드
        triggerInitialLoad();

        setupClickListeners();
        setupBottomNavigationView();
    }

    private void initViews() {
        recyclerViewNews = findViewById(R.id.recyclerViewNews);
        textRegionTitle = findViewById(R.id.textRegionTitle);
        btnBack = findViewById(R.id.btnBack);
        navView = findViewById(R.id.nav_view);

        // 검색
        searchInputLayout = findViewById(R.id.searchInputLayout);
        etSearch = findViewById(R.id.etSearch);
    }

    // 지역명(타이틀/기본 검색어) 반환
    private String getRegionNameFromIntent() {
        String regionName = getIntent().getStringExtra("subRegionName");
        if (regionName == null || regionName.isEmpty()) {
            regionName = getIntent().getStringExtra("regionName");
        }

        String titleText;
        String query;

        if (regionName == null || regionName.isEmpty()) {
            titleText = "전체 뉴스";
            query = ""; // 전체는 지역 기본어 없음
        } else if ("경기".equals(regionName)) {
            titleText = "상세 지역 선택 필요";
            query = "경기도";
        } else {
            titleText = regionName + " 뉴스";
            query = regionName;
        }
        textRegionTitle.setText(titleText);
        return query; // e.g., "", "경기도", "서울"
    }

    // 검색바 동작
    private void setupSearchBar() {
        if (searchInputLayout != null) {
            searchInputLayout.setEndIconOnClickListener(v -> triggerSearch());
        }
        if (etSearch != null) {
            etSearch.setOnEditorActionListener((tv, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    triggerSearch();
                    return true;
                }
                return false;
            });
        }
    }

    // 실제로 검색을 트리거
    private void triggerSearch() {
        String keyword = etSearch != null && etSearch.getText() != null
                ? etSearch.getText().toString().trim()
                : "";

        String query = buildQuery(regionBaseQuery, keyword);
        if (query.isEmpty()) {
            Toast.makeText(this, "검색어를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        fetchNaverNews(query);
        hideKeyboard();
    }

    // 초기 로딩 쿼리(지역 + 카테고리 키워드)
    private void triggerInitialLoad() {
        String initQuery;
        if (regionBaseQuery == null || regionBaseQuery.isEmpty()) {
            // 전체: 기본 카테고리 키워드만
            initQuery = "축제 행사 관광지";
        } else {
            initQuery = regionBaseQuery + " 축제 행사 관광지";
        }
        fetchNaverNews(initQuery);
    }

    // 지역 기본어 + 입력 키워드 결합
    private String buildQuery(String region, String keyword) {
        StringBuilder sb = new StringBuilder();
        if (region != null && !region.isEmpty()) sb.append(region);
        if (keyword != null && !keyword.isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(keyword);
        }
        return sb.toString().trim();
    }

    // API 호출
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
        newsList = new ArrayList<>();
        newsAdapter = new com.inhatc.localit.api.naver.NaverNewsAdapter(newsList,
                new com.inhatc.localit.api.naver.NaverNewsAdapter.OnNewsClickListener() {
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
                        // 어댑터 내부에서 토글된 상태를 저장/복구하려면 adapter.getFavoriteKeys() 사용
                        Toast.makeText(NewsActivity.this, "즐겨찾기 변경됨", Toast.LENGTH_SHORT).show();
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

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null && etSearch != null) {
                imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
            }
        } catch (Exception ignored) {}
    }
}
