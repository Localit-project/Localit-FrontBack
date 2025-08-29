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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.inhatc.localit.Fragment.FavoriteViewModel;
import com.inhatc.localit.MainActivity;
import com.inhatc.localit.R;
import com.inhatc.localit.api.naver.NaverApiService;
import com.inhatc.localit.api.naver.NaverNewsResponse;
import com.inhatc.localit.api.naver.RetrofitClient;
import com.inhatc.localit.db.TouristSpot;
import com.inhatc.localit.db.TouristSpotRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewNews;
    // ▼▼▼▼▼ 'NaverNewsAdapter' -> 'NewsAdapter'로 수정 ▼▼▼▼▼
    private NewsAdapter newsAdapter;
    private List<NaverNewsResponse.Item> newsList;

    private TextView textRegionTitle;
    private ImageView btnBack;
    private BottomNavigationView navView;

    private TextInputLayout searchInputLayout;
    private TextInputEditText etSearch;

    private NaverApiService apiService;
    private final String NAVER_CLIENT_ID = "hjVfnk_wdgYqW0xT86Ts";
    private final String NAVER_CLIENT_SECRET = "yFyvd5aHZ9";

    private String regionBaseQuery = "";

    private TouristSpotRepository touristSpotRepository;
    private FavoriteViewModel favoriteViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        initViews();

        favoriteViewModel = new ViewModelProvider(this).get(FavoriteViewModel.class);
        touristSpotRepository = new TouristSpotRepository(getApplication());

        apiService = RetrofitClient.getInstance().create(NaverApiService.class);
        setupRecyclerView();

        regionBaseQuery = getRegionNameFromIntent();
        setupSearchBar();
        triggerInitialLoad();
        setupClickListeners();
        setupBottomNavigationView();

        observeWishedSpots();
    }

    private void observeWishedSpots() {
        favoriteViewModel.getWishedSpots().observe(this, wishedSpots -> {
            Set<String> wishedIds = new HashSet<>();
            for (TouristSpot spot : wishedSpots) {
                if (spot != null && spot.contentid != null) {
                    wishedIds.add(spot.contentid);
                }
            }
            if (newsAdapter != null) {
                newsAdapter.updateFavorites(wishedIds);
            }
        });
    }

    private void initViews() {
        recyclerViewNews = findViewById(R.id.recyclerViewNews);
        textRegionTitle = findViewById(R.id.textRegionTitle);
        btnBack = findViewById(R.id.btnBack);
        navView = findViewById(R.id.nav_view);
        searchInputLayout = findViewById(R.id.searchInputLayout);
        etSearch = findViewById(R.id.etSearch);
    }

    private String getRegionNameFromIntent() {
        String regionName = getIntent().getStringExtra("subRegionName");
        if (regionName == null || regionName.isEmpty()) {
            regionName = getIntent().getStringExtra("regionName");
        }
        String titleText;
        String query;
        if (regionName == null || regionName.isEmpty()) {
            titleText = "전체 뉴스";
            query = "";
        } else if ("경기".equals(regionName)) {
            titleText = "상세 지역 선택 필요";
            query = "경기도";
        } else {
            titleText = regionName + " 뉴스";
            query = regionName;
        }
        textRegionTitle.setText(titleText);
        return query;
    }

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

    private void triggerInitialLoad() {
        String initQuery;
        if (regionBaseQuery == null || regionBaseQuery.isEmpty()) {
            initQuery = "축제 행사 관광지";
        } else {
            initQuery = regionBaseQuery + " 축제 행사 관광지";
        }
        fetchNaverNews(initQuery);
    }

    private String buildQuery(String region, String keyword) {
        StringBuilder sb = new StringBuilder();
        if (region != null && !region.isEmpty()) sb.append(region);
        if (keyword != null && !keyword.isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(keyword);
        }
        return sb.toString().trim();
    }

    private void fetchNaverNews(String query) {
        if (query == null || query.isEmpty()) {
            Toast.makeText(this, "검색어가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        Call<NaverNewsResponse> call = apiService.getNews(
                NAVER_CLIENT_ID, NAVER_CLIENT_SECRET, query, 30, "sim"
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
        // ▼▼▼▼▼ 'NaverNewsAdapter' -> 'NewsAdapter'로 수정 ▼▼▼▼▼
        newsAdapter = new NewsAdapter(newsList,
                new NewsAdapter.OnNewsClickListener() {
                    @Override
                    public void onNewsClick(NaverNewsResponse.Item item, int position) {
                        if (item.getLink() != null && !item.getLink().isEmpty()) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getLink()));
                            startActivity(intent);
                        } else {
                            Toast.makeText(NewsActivity.this, "기사 링크가 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFavoriteClick(NaverNewsResponse.Item item, int position) {
                        TouristSpot spotToToggle = new TouristSpot();
                        spotToToggle.contentid = item.getLink();
                        spotToToggle.title = item.getTitle();
                        spotToToggle.addr1 = item.getDescription();
                        spotToToggle.firstimage = "";
                        spotToToggle.contenttypeid = 99;

                        touristSpotRepository.toggleFavoriteStatus(spotToToggle);
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

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null && etSearch != null) {
                imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
            }
        } catch (Exception ignored) {}
    }
}
