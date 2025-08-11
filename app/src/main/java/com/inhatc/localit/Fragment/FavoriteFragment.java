package com.inhatc.localit.Fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.inhatc.localit.R;

public class FavoriteFragment extends Fragment {

    // 상단바
    private ImageButton btnBack;
    private TextView titleText;

    // 탭/인디케이터
    private TextView tabFestival;
    private TextView tabTour;
    private TextView tabNews;
    private View indicator;

    private int selectedTab = 0;     // 0: 축제·행사, 1: 관광지, 2: 뉴스

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnBack = view.findViewById(R.id.btn_back);
        titleText = view.findViewById(R.id.text_search);

        if (titleText != null) {
            try {
                titleText.setText(getString(R.string.favorite_title));
            } catch (Exception ignore) {
                titleText.setText("찜 목록");
            }
        }

        // 뒤로가기(홈으로 singleTop)
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> goHomeSingleTop());
        } else {
            Log.e("FavoriteFragment", "btn_back is null. Check fragment_favorite.xml id.");
        }

        tabFestival = view.findViewById(R.id.tabFestival);
        tabTour = view.findViewById(R.id.tabTour);
        tabNews = view.findViewById(R.id.tabNews);
        indicator = view.findViewById(R.id.indicator);

        if (savedInstanceState != null) {
            selectedTab = savedInstanceState.getInt("selectedTab", 0);
        }

        View.OnClickListener tabClick = v1 -> {
            int id = v1.getId();
            if (id == R.id.tabFestival) selectedTab = 0;
            else if (id == R.id.tabTour) selectedTab = 1;
            else if (id == R.id.tabNews) selectedTab = 2;
            applyTabState();
        };

        if (tabFestival != null) tabFestival.setOnClickListener(tabClick);
        if (tabTour != null) tabTour.setOnClickListener(tabClick);
        if (tabNews != null) tabNews.setOnClickListener(tabClick);

        View tabsRow = view.findViewById(R.id.tabsRow);
        if (tabsRow != null) {
            tabsRow.post(this::applyTabState);
            tabsRow.addOnLayoutChangeListener((v12, l, t, r, b, ol, ot, orr, ob) -> applyTabState());
        } else {
            applyTabState();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selectedTab", selectedTab);
    }

    // ✅ 최소 수정: 프래그먼트/뷰 살아있을 때만 Resources 접근
    private int dpToPx(int dp) {
        if (!isAdded() || getView() == null) return dp; // 가드
        float density = requireView().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void applyTabState() {
        // ✅ 최소 수정: 생명주기 가드
        if (!isAdded() || getView() == null) return;
        if (tabFestival == null || tabTour == null || tabNews == null || indicator == null) return;

        setActive(tabFestival, selectedTab == 0);
        setActive(tabTour,    selectedTab == 1);
        setActive(tabNews,    selectedTab == 2);

        final TextView target = (selectedTab == 0) ? tabFestival : (selectedTab == 1 ? tabTour : tabNews);

        indicator.post(() -> {
            // ✅ 최소 수정: 지연 실행 가드
            if (!isAdded() || getView() == null) return;

            int width = Math.max(target.getWidth(), 1);
            int left  = target.getLeft();

            int defaultHeight = dpToPx(2);

            ViewGroup parent = (ViewGroup) indicator.getParent();
            ViewGroup.LayoutParams baseLp = indicator.getLayoutParams();
            int currentH = (baseLp != null && baseLp.height > 0) ? baseLp.height : defaultHeight;

            if (parent instanceof FrameLayout) {
                FrameLayout.LayoutParams flp;
                if (baseLp instanceof FrameLayout.LayoutParams) {
                    flp = (FrameLayout.LayoutParams) baseLp;
                } else {
                    flp = new FrameLayout.LayoutParams(width, currentH);
                }
                flp.width = width;
                flp.height = (flp.height > 0) ? flp.height : defaultHeight;
                flp.leftMargin = left;
                flp.gravity = Gravity.BOTTOM | Gravity.START;
                indicator.setLayoutParams(flp);
            } else {
                ViewGroup.LayoutParams lp = (baseLp != null)
                        ? baseLp
                        : new ViewGroup.LayoutParams(width, currentH);
                lp.width = width;
                if (lp.height <= 0) lp.height = defaultHeight;
                indicator.setLayoutParams(lp);
                indicator.setX(target.getX());
            }
        });
    }

    private void setActive(@NonNull TextView tv, boolean active) {
        tv.setTextColor(Color.parseColor(active ? "#222222" : "#BBBBBB"));
    }

    /** 홈으로 이동(중복 쌓임 방지) — SearchFragment와 동일한 방식 */
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