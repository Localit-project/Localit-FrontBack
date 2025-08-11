package com.inhatc.localit.Fragment;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputLayout;
import com.inhatc.localit.R;

public class SearchFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 뒤로가기(홈으로)
        ImageButton back = view.findViewById(R.id.btn_back);
        back.setOnClickListener(v -> goHomeSingleTop());

        TextInputLayout til = view.findViewById(R.id.searchInputLayout);
        if (til != null) {
            View endView = til.findViewById(
                    com.google.android.material.R.id.text_input_end_icon
            );
            if (endView instanceof ImageView) {
                ImageView end = (ImageView) endView;

                int sizeDp = 24; // 20~28 등으로 조절 가능
                float density = getResources().getDisplayMetrics().density;
                int px = (int) (sizeDp * density);

                end.setScaleType(ImageView.ScaleType.FIT_CENTER);
                end.setAdjustViewBounds(true);

                LayoutParams lp = end.getLayoutParams();
                if (lp != null) {
                    lp.width = px;
                    lp.height = px;
                    end.setLayoutParams(lp);
                }
                end.setMinimumWidth(px);
                end.setMinimumHeight(px);

                end.setImageTintList(ColorStateList.valueOf(Color.parseColor("#61000000")));

                // marginEnd 값을 늘리면 아이콘이 살짝 "왼쪽"으로, 줄이면 "오른쪽"으로 붙습니다.
                View parent = (View) end.getParent(); // 아이콘이 들어있는 프레임 레이아웃
                if (parent != null && parent.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams mlp =
                            (ViewGroup.MarginLayoutParams) parent.getLayoutParams();

                    int desiredMarginEndDp = 13; // ← 이 값만 바꿔서 좌우 이동 (dp)
                    mlp.setMarginEnd((int) (desiredMarginEndDp * density));
                    parent.setLayoutParams(mlp);
                    parent.requestLayout();
                }
            }
        }
    }

    /** 홈으로 이동(중복 쌓임 방지) */
    private void goHomeSingleTop() {
        try {
            NavController nav = NavHostFragment.findNavController(this);
            int homeId = nav.getGraph().getStartDestinationId(); // 보통 R.id.navigation_home

            NavOptions opts = new NavOptions.Builder()
                    .setPopUpTo(homeId, false)   // 홈은 남기고 위 스택만 제거
                    .setLaunchSingleTop(true)    // 이미 홈이면 중복 네비게이트 방지
                    .build();

            if (nav.getCurrentDestination() == null ||
                    nav.getCurrentDestination().getId() != homeId) {
                nav.navigate(homeId, null, opts);
            }
        } catch (Exception ignored) { }

        // 바텀네비 동기화(프로젝트 id에 맞게)
        BottomNavigationView bottom = requireActivity().findViewById(R.id.nav_view);
        if (bottom != null) {
            bottom.setSelectedItemId(R.id.navigation_home);
        }
    }
}