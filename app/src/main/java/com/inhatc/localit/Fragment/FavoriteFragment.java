package com.inhatc.localit.Fragment;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
import com.inhatc.localit.ui.category.FestivalDetailActivity;
import com.inhatc.localit.ui.category.SpotDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class FavoriteFragment extends Fragment {

    // 상단바/탭
    private ImageButton btnBack;
    private TextView titleText;
    private TextView tabFestival, tabTour, tabNews;
    private View indicator;
    private int selectedTab = 0; // 0: 축제, 1: 관광지, 2: 뉴스

    // 콘텐츠 영역
    private FrameLayout contentFrame;
    private TextView emptyTextView;
    private RecyclerView recyclerView;
    private WishedSpotAdapter adapter;
    private FavoriteViewModel viewModel;
    private List<TouristSpot> allWishedSpots = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(FavoriteViewModel.class);

        initializeViews(view);
        setupTabs();
        setupRecyclerView();
        observeViewModel();
    }

    /** XML의 뷰들을 찾아서 변수에 할당합니다. */
    private void initializeViews(View view) {
        btnBack       = view.findViewById(R.id.btn_back);
        titleText     = view.findViewById(R.id.text_search);
        tabFestival   = view.findViewById(R.id.tabFestival);
        tabTour       = view.findViewById(R.id.tabTour);
        tabNews       = view.findViewById(R.id.tabNews);
        indicator     = view.findViewById(R.id.indicator);
        contentFrame  = view.findViewById(R.id.favoriteContent);
        emptyTextView = view.findViewById(R.id.textEmptyFavorite);

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
            filterAndDisplayList();
        };

        tabFestival.setOnClickListener(tabClick);
        tabTour.setOnClickListener(tabClick);
        tabNews.setOnClickListener(tabClick);

        View fragmentView = getView();
        if (fragmentView != null) fragmentView.post(this::applyTabState);
    }

    /** RecyclerView 및 어댑터 설정 (아이템 클릭 → 상세 이동 포함) */
    private void setupRecyclerView() {
        adapter = new WishedSpotAdapter(requireContext(), this::openDetailFor);
        recyclerView = new RecyclerView(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
    }

    /** LiveData 구독하여 원본 데이터 업데이트 */
    private void observeViewModel() {
        viewModel.getWishedSpots().observe(getViewLifecycleOwner(), wishedSpots -> {
            this.allWishedSpots = (wishedSpots != null) ? wishedSpots : new ArrayList<>();
            filterAndDisplayList();
        });
    }

    /** 현재 탭에 맞게 목록 필터링 후 표시 */
    private void filterAndDisplayList() {
        List<TouristSpot> filteredList = new ArrayList<>();
        for (TouristSpot spot : allWishedSpots) {
            switch (selectedTab) {
                case 0: // 축제(15)
                    if (spot.contenttypeid == 15) filteredList.add(spot);
                    break;
                case 1: // 관광지(12)
                    if (spot.contenttypeid == 12) filteredList.add(spot);
                    break;
                case 2: // 뉴스(99)
                    if (spot.contenttypeid == 99) filteredList.add(spot);
                    break;
            }
        }

        if (filteredList.isEmpty()) {
            emptyTextView.setVisibility(View.VISIBLE);
            contentFrame.removeView(recyclerView);
        } else {
            emptyTextView.setVisibility(View.GONE);
            if (recyclerView.getParent() == null) contentFrame.addView(recyclerView);
            adapter.setItems(filteredList);
        }
    }

    // ========= 상세/브라우저 이동 로직 =========
    private void openDetailFor(@NonNull TouristSpot spot) {
        String contentId   = String.valueOf(spot.contentid);
        String title       = spot.title == null ? "" : spot.title;
        String addr1       = spot.addr1 == null ? "" : spot.addr1;
        String firstImage  = spot.firstimage == null ? "" : spot.firstimage;

        if (spot.contenttypeid == 12) {
            // 관광지 → SpotDetailActivity (activity_spot_detail.xml 로딩)
            Intent intent = new Intent(requireContext(), SpotDetailActivity.class)
                    .putExtra(SpotDetailActivity.EXTRA_CONTENT_ID, contentId)
                    .putExtra(SpotDetailActivity.EXTRA_CONTENT_TYPE_ID, "12")
                    .putExtra(SpotDetailActivity.EXTRA_TITLE, title)
                    .putExtra(SpotDetailActivity.EXTRA_ADDR1, addr1)
                    .putExtra(SpotDetailActivity.EXTRA_FIRST_IMAGE, firstImage);
            startActivity(intent);

        } else if (spot.contenttypeid == 15) {
            // 축제 → FestivalDetailActivity (activity_festival_detail.xml 로딩)
            Intent intent = new Intent(requireContext(), FestivalDetailActivity.class)
                    .putExtra(FestivalDetailActivity.EXTRA_CONTENT_ID, contentId)
                    .putExtra(FestivalDetailActivity.EXTRA_CONTENT_TYPE_ID, "15")
                    .putExtra(FestivalDetailActivity.EXTRA_TITLE, title)
                    .putExtra(FestivalDetailActivity.EXTRA_ADDR1, addr1)
                    .putExtra(FestivalDetailActivity.EXTRA_FIRST_IMAGE, firstImage);
            startActivity(intent);

        } else if (spot.contenttypeid == 99) {
            // 뉴스 → 바로 외부 브라우저 열기 (contentid에 링크 저장됨)
            String url = normalizeUrl(contentId);
            if (TextUtils.isEmpty(url)) {
                Toast.makeText(requireContext(), "유효한 뉴스 링크가 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browser);
        }
    }
    // =====================================

    /** http/https 스킴 누락 시 자동 보정 */
    private String normalizeUrl(String link) {
        if (TextUtils.isEmpty(link)) return null;
        String trimmed = link.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed;
        // 네이버 뉴스 링크 등 스킴이 빠진 경우 대비
        return "https://" + trimmed;
    }

    // --- 탭/인디케이터 ---
    private void applyTabState() {
        if (!isAdded() || getView() == null) return;
        if (tabFestival == null || tabTour == null || tabNews == null || indicator == null) return;

        setActive(tabFestival, selectedTab == 0);
        setActive(tabTour, selectedTab == 1);
        setActive(tabNews, selectedTab == 2);

        final TextView target = (selectedTab == 0) ? tabFestival : (selectedTab == 1 ? tabTour : tabNews);
        indicator.post(() -> {
            if (getView() == null) return;
            int width = target.getWidth();
            int left = target.getLeft();

            if (indicator.getLayoutParams() instanceof FrameLayout.LayoutParams) {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) indicator.getLayoutParams();
                params.width = width;
                params.leftMargin = ((View) target.getParent()).getLeft() + left;
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
            if (bottomNav != null) bottomNav.setSelectedItemId(R.id.navigation_home);
        } catch (Exception ignored) {}
    }
}