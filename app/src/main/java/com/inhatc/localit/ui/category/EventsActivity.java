package com.inhatc.localit.ui.category;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.inhatc.localit.Fragment.CategoryFragment;
import com.inhatc.localit.Fragment.FavoriteFragment;
import com.inhatc.localit.Fragment.HomeFragment;
import com.inhatc.localit.Fragment.MypageFragment;
import com.inhatc.localit.Fragment.SearchFragment;
import com.inhatc.localit.MainActivity;
import com.inhatc.localit.R;
import com.inhatc.localit.EventDetailActivity;
import com.inhatc.localit.model.Event;

import java.util.ArrayList;
import java.util.List;

public class EventsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewEvents;
    private EventsAdapter eventsAdapter;
    private List<Event> eventList;
    private TextView textRegionTitle;
    private ImageView btnBack;
    private BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        initViews();
        getRegionNameFromIntent();  // 상단 타이틀에 지역명 표시
        setupData();
        setupRecyclerView();
        setupClickListeners();

        /** ✅ ✅ ✅ 여기 추가 */
        setupBottomNavigationView();
    }

    /** XML 뷰 초기화 */
    private void initViews() {
        recyclerViewEvents = findViewById(R.id.recyclerViewEvents);
        textRegionTitle = findViewById(R.id.textRegionTitle);
        btnBack = findViewById(R.id.btnBack);
        navView = findViewById(R.id.nav_view);
    }

    /** RegionDetailActivity에서 넘긴 지역명을 받아 타이틀에 표시 */
    private void getRegionNameFromIntent() {
        String regionName = getIntent().getStringExtra("region_name");
        if (regionName != null && !regionName.isEmpty()) {
            textRegionTitle.setText(regionName);
        } else {
            textRegionTitle.setText("축제·행사");  // 값이 없으면 기본값
        }
    }

    /** 임시 데이터 (API 연동 전까지 샘플) */
    private void setupData() {
        eventList = new ArrayList<>();

        eventList.add(new Event("동대문구 맥주축제", "2025.08.29", R.drawable.sample1, false));
        eventList.add(new Event("서대문 도림축제", "2025.08.14 ~ 2025.08.16", R.drawable.sample1, false));
        eventList.add(new Event("2025 서울썸머비치 (SEOUL SUMMER BEACH)", "2025.07.19 ~ 2025.08.08", R.drawable.sample1, false));
        eventList.add(new Event("한강페스티벌", "2025.07.26 ~ 2025.08.24", R.drawable.sample1, false));
    }

    /** RecyclerView 연결 */
    private void setupRecyclerView() {
        eventsAdapter = new EventsAdapter(eventList, new EventsAdapter.OnEventClickListener() {
            @Override
            public void onEventClick(Event event, int position) {
                Intent intent = new Intent(EventsActivity.this, EventDetailActivity.class);
                intent.putExtra("event_title", event.getTitle());
                intent.putExtra("event_date", event.getDate());
                intent.putExtra("event_image", event.getImageResId());
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(Event event, int position) {
                event.setFavorite(!event.isFavorite());
                eventsAdapter.notifyItemChanged(position);
            }
        });

        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewEvents.setAdapter(eventsAdapter);
    }

    /** 상단 뒤로가기 버튼 */
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    /** ✅ ✅ ✅ 네비게이션바 클릭 시 MainActivity 열어서 프래그먼트 전환되게 설정 */
    private void setupBottomNavigationView() {
        navView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            Intent intent = new Intent(EventsActivity.this, MainActivity.class);

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
            finish(); // ✅ 현재 Activity 종료 (중복 쌓이지 않게)

            return true;
        });
    }
}
