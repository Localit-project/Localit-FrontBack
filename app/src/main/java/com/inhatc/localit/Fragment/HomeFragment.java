package com.inhatc.localit.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.inhatc.localit.MainActivity;
import com.inhatc.localit.R;
import com.inhatc.localit.databinding.FragmentHomeBinding;
import com.inhatc.localit.MapActivity;
import com.inhatc.localit.ui.settings.InterestRegionActivity;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    // 관심지역 선택 결과 수신용 런처
    private ActivityResultLauncher<Intent> interestRegionLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // registerForActivityResult 로 onActivityResult 대체
        interestRegionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                (ActivityResult result) -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        ArrayList<String> selected =
                                result.getData().getStringArrayListExtra(InterestRegionActivity.EXTRA_SELECTED);
                        // TODO: 선택 목록 UI 반영 (칩/텍스트 등)
                        // 예) binding.textSelectedRegions.setText(TextUtils.join(", ", selected));
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 알림 아이콘 → MainActivity의 알림 화면
        binding.btnNotification.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openNotifications();
            }
        });

        // 지도 카드 → MapActivity
        binding.cardOpenMap.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), MapActivity.class)));

        // 관심지역 카드 → InterestRegionActivity (결과 받기)
        binding.cardInterestRegion.setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), InterestRegionActivity.class);
            interestRegionLauncher.launch(i);
        });

        // 캐러셀 좌/우 버튼
        ViewPager2 pager = binding.pagerCourses;
        binding.btnPrev.setOnClickListener(v -> {
            int pos = pager.getCurrentItem();
            if (pos > 0) pager.setCurrentItem(pos - 1, true);
        });
        binding.btnNext.setOnClickListener(v -> {
            int pos = pager.getCurrentItem();
            pager.setCurrentItem(pos + 1, true);
        });

        // 주의: ViewPager2 어댑터는 프로젝트에서 이미 세팅된 어댑터를 사용하세요.
        // 여기서는 어댑터에 의존하지 않도록 이동 버튼만 연결했습니다.

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
