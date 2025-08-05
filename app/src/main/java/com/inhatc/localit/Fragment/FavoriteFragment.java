package com.inhatc.localit.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.inhatc.localit.R;

public class FavoriteFragment extends Fragment {

    private TextView tabFestival, tabTour, tabNews;
    private View indicator;
    private LinearLayout tabsRow;
    private FrameLayout favoriteContent;
    private int currentTab = 0; // 0: 축제, 1: 관광지, 2: 뉴스

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        tabFestival = view.findViewById(R.id.tabFestival);
        tabTour = view.findViewById(R.id.tabTour);
        tabNews = view.findViewById(R.id.tabNews);
        indicator = view.findViewById(R.id.indicator);
        tabsRow = view.findViewById(R.id.tabsRow);
        favoriteContent = view.findViewById(R.id.favoriteContent);

        // 1. 뒤로가기 버튼 - 홈탭으로 이동!
        ImageView btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            BottomNavigationView nav = requireActivity().findViewById(R.id.nav_view);
            nav.setSelectedItemId(R.id.navigation_home);
        });

        // 2. 탭 클릭 리스너
        View.OnClickListener tabClickListener = v -> {
            if (v == tabFestival) selectTab(0);
            else if (v == tabTour) selectTab(1);
            else if (v == tabNews) selectTab(2);
        };

        tabFestival.setOnClickListener(tabClickListener);
        tabTour.setOnClickListener(tabClickListener);
        tabNews.setOnClickListener(tabClickListener);

        // 처음엔 축제 탭 선택
        tabsRow.getViewTreeObserver().addOnGlobalLayoutListener(() -> selectTab(currentTab));

        return view;
    }

    private void selectTab(int idx) {
        currentTab = idx;
        tabFestival.setTextColor(idx == 0 ? 0xFF222222 : 0xFFBBBBBB);
        tabTour.setTextColor(idx == 1 ? 0xFF222222 : 0xFFBBBBBB);
        tabNews.setTextColor(idx == 2 ? 0xFF222222 : 0xFFBBBBBB);
        tabFestival.setTypeface(null, idx == 0 ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        tabTour.setTypeface(null, idx == 1 ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        tabNews.setTypeface(null, idx == 2 ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);

        // 인디케이터 이동
        tabsRow.post(() -> {
            int tabWidth = tabsRow.getWidth() / 3;
            ViewGroup.LayoutParams params = indicator.getLayoutParams();
            params.width = tabWidth;
            indicator.setLayoutParams(params);
            indicator.setX(tabWidth * idx);
        });

        // 여기에 각 탭별 내용 교체 로직(프래그먼트/리사이클러뷰 등) 추가하면 됨!
    }
}
