package com.inhatc.localit.Fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.inhatc.localit.R;
import com.inhatc.localit.db.TouristSpot;

import java.util.ArrayList;
import java.util.List;

public class FavoriteFragment extends Fragment {

    // 1. XML 레이아웃에 맞는 UI 변수들
    private ImageButton btnBack;
    private TextView titleText;
    private TextView tabFestival, tabTour, tabNews;
    private View indicator;
    private int selectedTab = 0; // 0: 축제, 1: 관광지, 2: 뉴스

    // 2. 찜 목록 표시를 위한 변수들
    private FrameLayout contentFrame;
    private TextView emptyTextView;
    private RecyclerView recyclerView;
    private WishedSpotAdapter adapter;
    private FavoriteViewModel viewModel;
    private List<TouristSpot> allWishedSpots = new ArrayList<>(); // 필터링을 위한 원본 데이터 저장소

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ViewModel 초기화
        viewModel = new ViewModelProvider(this).get(FavoriteViewModel.class);

        // UI 요소 초기화
        initializeViews(view);
        setupTabs();

        // RecyclerView 및 어댑터 설정
        setupRecyclerView();

        // LiveData 구독하여 원본 데이터 업데이트
        observeViewModel();
    }

    /** XML의 뷰들을 찾아서 변수에 할당합니다. */
    private void initializeViews(View view) {
        btnBack = view.findViewById(R.id.btn_back);
        titleText = view.findViewById(R.id.text_search);
        tabFestival = view.findViewById(R.id.tabFestival);
        tabTour = view.findViewById(R.id.tabTour);
        tabNews = view.findViewById(R.id.tabNews);
        indicator = view.findViewById(R.id.indicator);
        contentFrame = view.findViewById(R.id.favoriteContent);
        emptyTextView = view.findViewById(R.id.textEmptyFavorite); // XML에 정의된 ID 사용

        // 상단바 설정
        titleText.setText("찜 목록");
        btnBack.setOnClickListener(v -> goHomeSingleTop());
    }

    /** 탭 클릭 리스너를 설정하고 초기 상태를 적용합니다. */
    private void setupTabs() {
        View.OnClickListener tabClick = v -> {
            int id = v.getId();
            if (id == R.id.tabFestival) selectedTab = 0;
            else if (id == R.id.tabTour) selectedTab = 1;
            else if (id == R.id.tabNews) selectedTab = 2;

            applyTabState();
            // 탭 변경 시, 저장된 원본 목록으로 필터링만 다시 수행
            filterAndDisplayList();
        };

        tabFestival.setOnClickListener(tabClick);
        tabTour.setOnClickListener(tabClick);
        tabNews.setOnClickListener(tabClick);

        // 뷰가 그려진 후 인디케이터 위치를 계산하도록 post 호출
        View fragmentView = getView();
        if (fragmentView != null) {
            fragmentView.post(this::applyTabState);
        }
    }

    /** RecyclerView를 생성하고 기본 설정을 합니다. */
    private void setupRecyclerView() {
        adapter = new WishedSpotAdapter(getContext());
        recyclerView = new RecyclerView(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
    }

<<<<<<< HEAD
    /** ViewModel의 LiveData를 관찰하여 원본 데이터를 업데이트하고, UI를 갱신합니다. */
    private void observeViewModel() {
        viewModel.getWishedSpots().observe(getViewLifecycleOwner(), wishedSpots -> {
            // DB에서 새 목록을 받으면 원본 데이터 리스트를 업데이트
            this.allWishedSpots = (wishedSpots != null) ? wishedSpots : new ArrayList<>();
            // 업데이트된 원본 데이터로 현재 탭에 맞게 필터링 및 표시
            filterAndDisplayList();
        });
=======
    // 최소 수정: 프래그먼트/뷰 살아있을 때만 Resources 접근
    private int dpToPx(int dp) {
        if (!isAdded() || getView() == null) return dp; // 가드
        float density = requireView().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
>>>>>>> 1513c0ab18a3a890083dbb438950b8a3dccbdd87
    }

    /** 현재 탭에 맞게 목록을 필터링하고 화면에 표시합니다. */
    private void filterAndDisplayList() {
        List<TouristSpot> filteredList = new ArrayList<>();
        for (TouristSpot spot : allWishedSpots) {
            switch (selectedTab) {
                case 0: // 축제·행사 (contentTypeId == 15)
                    if (spot.contentTypeId == 15) filteredList.add(spot);
                    break;
                case 1: // 관광지 (contentTypeId == 12)
                    if (spot.contentTypeId == 12) filteredList.add(spot);
                    break;
                case 2: // 뉴스
                    // TODO: 뉴스 타입 ID가 정해지면 해당 조건 추가
                    break;
            }
        }

        if (filteredList.isEmpty()) {
            emptyTextView.setVisibility(View.VISIBLE);
            contentFrame.removeView(recyclerView); // 목록이 없으면 RecyclerView 제거
        } else {
            emptyTextView.setVisibility(View.GONE);
            if (recyclerView.getParent() == null) {
                contentFrame.addView(recyclerView); // 목록이 있으면 RecyclerView 추가
            }
            adapter.setItems(filteredList);
        }
    }

    // --- 기존 탭 및 인디케이터 관리 코드 (수정 불필요) ---

    private void applyTabState() {
        if (!isAdded() || getView() == null) return;
        if (tabFestival == null || tabTour == null || tabNews == null || indicator == null) return;

        setActive(tabFestival, selectedTab == 0);
        setActive(tabTour, selectedTab == 1);
        setActive(tabNews, selectedTab == 2);

        final TextView target = (selectedTab == 0) ? tabFestival : (selectedTab == 1 ? tabTour : tabNews);
        indicator.post(() -> {
            if (getView() == null) return;
            // 탭의 실제 너비와 위치를 계산하여 인디케이터에 적용
            int width = target.getWidth();
            int left = target.getLeft();

            // FrameLayout 내의 View 위치는 Margin으로 조정
            if(indicator.getLayoutParams() instanceof FrameLayout.LayoutParams) {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) indicator.getLayoutParams();
                params.width = width;
                params.leftMargin = ((View)target.getParent()).getLeft() + left;
                indicator.setLayoutParams(params);
            }
        });
    }

    private void setActive(@NonNull TextView tv, boolean active) {
        tv.setTextColor(Color.parseColor(active ? "#222222" : "#BBBBBB"));
    }

    private void goHomeSingleTop() {
        try {
            NavController nav = NavHostFragment.findNavController(this);
            int homeId = nav.getGraph().getStartDestinationId();
            NavOptions opts = new NavOptions.Builder()
                    .setPopUpTo(homeId, false)
                    .setLaunchSingleTop(true)
                    .build();
            if (nav.getCurrentDestination() != null && nav.getCurrentDestination().getId() != homeId) {
                nav.navigate(homeId, null, opts);
            }
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.nav_view);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.navigation_home);
            }
        } catch (Exception ignored) {}
    }
}