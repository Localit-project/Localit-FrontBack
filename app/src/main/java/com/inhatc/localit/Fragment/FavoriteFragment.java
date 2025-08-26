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

import androidx.activity.OnBackPressedCallback;
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
import com.inhatc.localit.Fragment.WishedSpotAdapter;
import com.inhatc.localit.databinding.FragmentChatBinding;
import com.inhatc.localit.db.TouristSpot;
import com.inhatc.localit.ui.category.FestivalDetailActivity;
import com.inhatc.localit.ui.category.SpotDetailActivity;

import java.util.ArrayList;
import java.util.HashSet; // [추가]
import java.util.List;

public class FavoriteFragment extends Fragment {

    // 상단바/탭
    private FragmentChatBinding binding;
    private ImageButton btnBack;
    private TextView titleText;
    private TextView tabFestival, tabTour, tabNews;
    private View indicator;
    private int selectedTab = 0;

    // 콘텐츠 영역
    private FrameLayout contentFrame;
    private TextView emptyTextView;
    private RecyclerView recyclerView;
    private WishedSpotAdapter adapter;
    private FavoriteViewModel viewModel;
    private List<TouristSpot> allWishedSpots = new ArrayList<>();

    // [추가] 비활성화된 아이템의 ID를 임시로 저장하는 Set
    private HashSet<String> deactivatedSpotIds = new HashSet<>();
    private boolean isInitialLoad = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_favorite, container, false);

        // 뒤로가기 버튼 (UI)
        View btnBack = root.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> goHomeSingleTop());
        }

        // 물리/소프트 뒤로가기 키 처리
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        goHomeSingleTop();
                    }
                }
        );

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // [추가] 뷰가 생성될 때마다 비활성화 목록을 새로 초기화
        isInitialLoad = true;
        deactivatedSpotIds = new HashSet<>();

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

    /** [수정됨] 활성화/비활성화 로직을 처리하도록 RecyclerView 설정 변경 */
    private void setupRecyclerView() {
        // 1. 아이템 전체 클릭 리스너
        WishedSpotAdapter.OnSpotClickListener itemClickListener = this::openDetailFor;

        // 2. 하트 버튼 클릭 리스너 (활성화/비활성화 로직)
        WishedSpotAdapter.OnHeartClickListener heartClickListener = spot -> {
            String contentId = spot.contentid;

            if (deactivatedSpotIds.contains(contentId)) {
                // 비활성화 상태(빈 하트)를 클릭한 경우 -> 다시 활성화 (Undo)
                deactivatedSpotIds.remove(contentId);
                viewModel.addWishedSpot(spot); // DB에 다시 추가
                Toast.makeText(getContext(), "찜 목록에 다시 추가했습니다.", Toast.LENGTH_SHORT).show();
            } else {
                // 활성화 상태(채워진 하트)를 클릭한 경우 -> 비활성화
                deactivatedSpotIds.add(contentId);
                viewModel.removeWishedSpot(spot); // DB에서 삭제
                Toast.makeText(getContext(), "찜을 취소했습니다.", Toast.LENGTH_SHORT).show();
            }

            // 어댑터에 변경된 비활성화 목록을 알려주고, UI를 새로고침
            adapter.setDeactivatedSpotIds(deactivatedSpotIds);
            adapter.notifyDataSetChanged();
        };

        // 어댑터 생성 및 설정
        adapter = new WishedSpotAdapter(requireContext(), itemClickListener, heartClickListener);
        recyclerView = new RecyclerView(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
    }

    /** [수정됨] LiveData 구독 시 어댑터에 비활성화 목록 전달 */
    private void observeViewModel() {
        viewModel.getWishedSpots().observe(getViewLifecycleOwner(), wishedSpots -> {
            // ▼▼▼▼▼ [수정된 로직] ▼▼▼▼▼
            // isInitialLoad 플래그가 true일 때 (즉, 화면에 처음 진입했을 때)만 목록을 새로고침합니다.
            if (isInitialLoad) {
                this.allWishedSpots = (wishedSpots != null) ? wishedSpots : new ArrayList<>();
                adapter.setDeactivatedSpotIds(deactivatedSpotIds);
                filterAndDisplayList();
                isInitialLoad = false; // 플래그를 false로 바꿔서 다음부터는 이 코드가 실행되지 않도록 함
            }
            // ▲▲▲▲▲ [수정된 로직] ▲▲▲▲▲
        });
    }

    /** 현재 탭에 맞게 목록 필터링 후 표시 */
    private void filterAndDisplayList() {
        List<TouristSpot> filteredList = new ArrayList<>();
        for (TouristSpot spot : allWishedSpots) {
            switch (selectedTab) {
                case 0:
                    if (spot.contenttypeid == 15) filteredList.add(spot);
                    break;
                case 1:
                    if (spot.contenttypeid == 12) filteredList.add(spot);
                    break;
                case 2:
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
            // 관광지 → SpotDetailActivity
            Intent intent = new Intent(requireContext(), SpotDetailActivity.class)
                    .putExtra(SpotDetailActivity.EXTRA_CONTENT_ID, contentId)
                    .putExtra(SpotDetailActivity.EXTRA_CONTENT_TYPE_ID, "12")
                    .putExtra(SpotDetailActivity.EXTRA_TITLE, title)
                    .putExtra(SpotDetailActivity.EXTRA_ADDR1, addr1)
                    .putExtra(SpotDetailActivity.EXTRA_FIRST_IMAGE, firstImage);
            startActivity(intent);

        } else if (spot.contenttypeid == 15) {
            // 축제 → FestivalDetailActivity
            Intent intent = new Intent(requireContext(), FestivalDetailActivity.class)
                    .putExtra(FestivalDetailActivity.EXTRA_CONTENT_ID, contentId)
                    .putExtra(FestivalDetailActivity.EXTRA_CONTENT_TYPE_ID, "15")
                    .putExtra(FestivalDetailActivity.EXTRA_TITLE, title)
                    .putExtra(FestivalDetailActivity.EXTRA_ADDR1, addr1)
                    .putExtra(FestivalDetailActivity.EXTRA_FIRST_IMAGE, firstImage);
            startActivity(intent);

        } else if (spot.contenttypeid == 99) {
            // 뉴스 → 외부 브라우저
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

            if (nav.getCurrentDestination() == null ||
                    nav.getCurrentDestination().getId() != homeId) {
                nav.navigate(homeId, null, opts);
            }
        } catch (Exception ignored) { }

        BottomNavigationView bottom = requireActivity().findViewById(R.id.nav_view);
        if (bottom != null) {
            bottom.setSelectedItemId(R.id.navigation_home);
        }
    }
}