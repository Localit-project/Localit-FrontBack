package com.inhatc.localit.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.inhatc.localit.MainActivity;
import com.inhatc.localit.MapActivity;
import com.inhatc.localit.databinding.FragmentHomeBinding;
import com.inhatc.localit.ui.settings.InterestRegionActivity;

import com.inhatc.localit.api.home.HomeCoursePagerAdapter;
import com.inhatc.localit.api.home.TourItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private HomeCoursePagerAdapter courseAdapter;

    // 대표이미지 수동 오버라이드 (contentid -> 리소스 URI)
    private final Map<String, String> courseImageOverride = new LinkedHashMap<>();

    // 상세 API 호출 시 쓸 상수 (상세 화면에서 사용)
    private static final String SERVICE_KEY = "URL_ENCODED_YOUR_KEY";
    private static final String OS = "ETC";
    private static final String APP = "Localit";

    private ActivityResultLauncher<Intent> interestRegionLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        interestRegionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                (ActivityResult result) -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        ArrayList<String> selected =
                                result.getData().getStringArrayListExtra(InterestRegionActivity.EXTRA_SELECTED);
                        // TODO: 관심지역 선택 결과 UI 반영
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 알림 아이콘
        binding.btnNotification.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openNotifications();
            }
        });

        // 지도 카드
        binding.cardOpenMap.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), MapActivity.class)));

        // 관심지역 카드
        binding.cardInterestRegion.setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), InterestRegionActivity.class);
            interestRegionLauncher.launch(i);
        });

        // 고정 7장 매핑 (contentId -> drawable 리소스 URI)
        String pkg = requireContext().getPackageName();
        courseImageOverride.put("3517031", "android.resource://" + pkg + "/drawable/travel1");
        courseImageOverride.put("3516944", "android.resource://" + pkg + "/drawable/travel2");
        courseImageOverride.put("3516594", "android.resource://" + pkg + "/drawable/travel3");
        courseImageOverride.put("2022929", "android.resource://" + pkg + "/drawable/travel4");
        courseImageOverride.put("2987504", "android.resource://" + pkg + "/drawable/travel5");
        courseImageOverride.put("2018433", "android.resource://" + pkg + "/drawable/travel6");
        courseImageOverride.put("2833450", "android.resource://" + pkg + "/drawable/travel7");

        // 화면에 보여줄 7개 아이템 (contentId 꼭 채워야 클릭 시 상세 조회 가능)
        List<TourItem> fixed = Arrays.asList(
                make("3517031", "코스 1"),
                make("3516944", "코스 2"),
                make("3516594", "코스 3"),
                make("2022929", "코스 4"),
                make("2987504", "코스 5"),
                make("2018433", "코스 6"),
                make("2833450", "코스 7")
        );

        // 어댑터 (클릭 -> 상세로 이동은 openCourseDetail()에서 처리)
        courseAdapter = new HomeCoursePagerAdapter(courseImageOverride, this::openCourseDetail);
        binding.pagerCourses.setAdapter(courseAdapter);
        binding.pagerCourses.setOffscreenPageLimit(1);
        courseAdapter.submit(fixed);

        // 좌우 버튼
        ViewPager2 pager = binding.pagerCourses;
        binding.btnPrev.setOnClickListener(v -> {
            int pos = pager.getCurrentItem();
            if (pos > 0) pager.setCurrentItem(pos - 1, true);
        });
        binding.btnNext.setOnClickListener(v -> {
            int pos = pager.getCurrentItem();
            pager.setCurrentItem(pos + 1, true);
        });

        return root;
    }

    private TourItem make(String id, String title) {
        TourItem t = new TourItem();
        t.contentid = id;
        t.title = title;
        return t;
    }

    private void openCourseDetail(TourItem item) {
        // TODO: 너의 상세 Activity 로 교체
        // 예시)
        // Intent i = new Intent(requireContext(), CourseDetailActivity.class);
        // i.putExtra("contentId", item.contentid);
        // i.putExtra("imageUri", courseImageOverride.get(item.contentid));
        // i.putExtra("SERVICE_KEY", SERVICE_KEY);
        // i.putExtra("OS", OS);
        // i.putExtra("APP", APP);
        // startActivity(i);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
