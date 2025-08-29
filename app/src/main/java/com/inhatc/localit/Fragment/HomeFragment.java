package com.inhatc.localit.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.inhatc.localit.MainActivity;
import com.inhatc.localit.MapActivity;
import com.inhatc.localit.api.SpotResponse;
import com.inhatc.localit.api.home.HomeCoursePagerAdapter;
import com.inhatc.localit.api.home.TourApiHelper;
import com.inhatc.localit.api.home.TourItem;
import com.inhatc.localit.databinding.FragmentHomeBinding;
import com.inhatc.localit.ui.category.FestivalActivity;
import com.inhatc.localit.ui.category.FestivalAdapter;
import com.inhatc.localit.ui.category.FestivalDetailActivity;
import com.inhatc.localit.ui.category.MarketActivity;
import com.inhatc.localit.ui.category.MarketAdapter;
import com.inhatc.localit.ui.settings.InterestRegionActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    public HomeCoursePagerAdapter courseAdapter;
    public MarketAdapter marketAdapter;
    public FestivalAdapter festivalAdapter;

    // 대표이미지 수동 오버라이드
    private final Map<String, String> courseImageOverride = new LinkedHashMap<>();

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

        // 고정 7장 매핑
        String pkg = requireContext().getPackageName();
        courseImageOverride.put("3517031", "android.resource://" + pkg + "/drawable/travel1");
        courseImageOverride.put("3516944", "android.resource://" + pkg + "/drawable/travel2");
        courseImageOverride.put("3516594", "android.resource://" + pkg + "/drawable/travel3");
        courseImageOverride.put("2022929", "android.resource://" + pkg + "/drawable/travel4");
        courseImageOverride.put("2987504", "android.resource://" + pkg + "/drawable/travel5");
        courseImageOverride.put("2018433", "android.resource://" + pkg + "/drawable/travel6");
        courseImageOverride.put("2833450", "android.resource://" + pkg + "/drawable/travel7");

        // 화면에 보여줄 7개 아이템
        List<TourItem> fixed = Arrays.asList(
                make("3517031", "코스 1"),
                make("3516944", "코스 2"),
                make("3516594", "코스 3"),
                make("2022929", "코스 4"),
                make("2987504", "코스 5"),
                make("2018433", "코스 6"),
                make("2833450", "코스 7")
        );

        // 어댑터
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

        // RecyclerView 설정 (시장)
        marketAdapter = new MarketAdapter();
        binding.recyclerMarkets.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerMarkets.setAdapter(marketAdapter);
        binding.btnMoreMarkets.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), MarketActivity.class);
            startActivity(intent);
        });

        // RecyclerView 설정 (축제)
        festivalAdapter = new FestivalAdapter(
                new ArrayList<>(),
                (item, position) -> {
                    if (item == null || getContext() == null) return;

                    Intent i = new Intent(requireContext(), FestivalDetailActivity.class);

                    i.putExtra(FestivalDetailActivity.EXTRA_CONTENT_ID, item.contentid);
                    i.putExtra(FestivalDetailActivity.EXTRA_CONTENT_TYPE_ID,
                            TextUtils.isEmpty(item.contenttypeid) ? "15" : item.contenttypeid);
                    i.putExtra(FestivalDetailActivity.EXTRA_TITLE,       item.title != null ? item.title : "");
                    i.putExtra(FestivalDetailActivity.EXTRA_ADDR1,       item.addr1 != null ? item.addr1 : "");
                    i.putExtra(FestivalDetailActivity.EXTRA_FIRST_IMAGE, item.firstimage != null ? item.firstimage : "");

                    startActivity(i);
                },
                (item, position) -> {
                    // TODO: 즐겨찾기 버튼 클릭 시 처리할 로직을 여기에 구현하세요.
                }
        );
        binding.recyclerFestivals.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerFestivals.setAdapter(festivalAdapter);
        binding.btnMoreFestivals.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), FestivalActivity.class);
            startActivity(intent);
        });

        // 데이터 로드
        fetchMarkets();
        fetchFestivals();

        return root;
    }

    private TourItem make(String id, String title) {
        TourItem t = new TourItem();
        t.contentid = id;
        t.title = title;
        return t;
    }

    private void openCourseDetail(TourItem item) {
        // TODO: 상세 Activity로 교체
    }

    private void fetchMarkets() {
        TourApiHelper.fetchHomeMarkets(items -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> marketAdapter.setItems(items));
            }
        });
    }

    private void fetchFestivals() {
        TourApiHelper.fetchHomeFestivals(items -> { // 'items' is a List<TourItem>
            if (getActivity() != null) {

                // Create a new list for the converted data
                List<SpotResponse.Item> convertedList = new ArrayList<>();

                // Loop through the List<TourItem> from the API
                for (TourItem tourItem : items) {
                    // Create a new SpotResponse.Item and copy the data over
                    SpotResponse.Item spotItem = new SpotResponse.Item();
                    spotItem.contentid = tourItem.contentid;
                    spotItem.title = tourItem.title;
                    spotItem.addr1 = tourItem.addr1;
                    spotItem.firstimage = tourItem.firstimage;
                    spotItem.eventstartdate = tourItem.eventstartdate;
                    spotItem.eventenddate = tourItem.eventenddate;
                    spotItem.contenttypeid = "15"; // Set festival content type ID
                    convertedList.add(spotItem);
                }

                // Pass the newly converted list to the adapter
                getActivity().runOnUiThread(() -> festivalAdapter.submitList(convertedList));
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}