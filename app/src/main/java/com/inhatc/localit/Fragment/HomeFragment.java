package com.inhatc.localit.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri; // Intent.ACTION_VIEW를 위해 추가
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
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.inhatc.localit.MainActivity;
import com.inhatc.localit.MapActivity;
import com.inhatc.localit.R;
import com.inhatc.localit.api.SpotResponse;
import com.inhatc.localit.api.home.HomeCoursePagerAdapter;
import com.inhatc.localit.api.home.TourApiHelper;
import com.inhatc.localit.api.home.TourItem;
import com.inhatc.localit.databinding.FragmentHomeBinding;
import com.inhatc.localit.ui.category.FestivalActivity;
import com.inhatc.localit.ui.category.FestivalAdapter;
import com.inhatc.localit.ui.category.FestivalDetailActivity;
import com.inhatc.localit.ui.category.SpotActivity;
import com.inhatc.localit.ui.category.SpotDetailActivity;
import com.inhatc.localit.ui.CourseDetailActivity;
import com.inhatc.localit.ui.settings.InterestRegionActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    public HomeCoursePagerAdapter courseAdapter;
    public FestivalAdapter festivalAdapter;

    private final Map<String, String> courseImageOverride = new LinkedHashMap<>();
    private final Map<String, String> courseUrlOverride = new LinkedHashMap<>();

    private ActivityResultLauncher<Intent> interestRegionLauncher;

    private final List<TourItem> spotCardItems = new ArrayList<>();
    private final List<TourItem> festivalCardItems = new ArrayList<>();

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

        setupUserProfile();

        binding.btnNotification.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openNotifications();
            }
        });

        binding.cardOpenMap.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), MapActivity.class)));

        binding.cardInterestRegion.setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), InterestRegionActivity.class);
            interestRegionLauncher.launch(i);
        });

        String pkg = requireContext().getPackageName();
        courseImageOverride.put("3517031", "android.resource://" + pkg + "/drawable/cos1"); // 사용자 요청에 따라 cos1로 변경
        courseImageOverride.put("3516944", "android.resource://" + pkg + "/drawable/travel2");
        courseImageOverride.put("3516594", "android.resource://" + pkg + "/drawable/travel3");
        courseImageOverride.put("2022929", "android.resource://" + pkg + "/drawable/travel4");
        courseImageOverride.put("2987504", "android.resource://" + pkg + "/drawable/travel5");
        courseImageOverride.put("2018433", "android.resource://" + pkg + "/drawable/travel6");
        courseImageOverride.put("2833450", "android.resource://" + pkg + "/drawable/travel7");

        // 여기가 수정된 부분입니다.
        courseUrlOverride.put("3517031", "https://korean.visitkorea.or.kr/detail/cs_detail_cos.do?cotid=e42e60b7-eac0-4540-a1e4-749bed108154&big_category=&mid_category=&big_area=37");

        List<TourItem> fixedSkeleton = Arrays.asList(
                make("3517031", "코스 1"),
                make("3516944", "코스 2"),
                make("3516594", "코스 3"),
                make("2022929", "코스 4"),
                make("2987504", "코스 5"),
                make("2018433", "코스 6"),
                make("2833450", "코스 7")
        );

        courseAdapter = new HomeCoursePagerAdapter(courseImageOverride, this::openCourseDetail);
        binding.pagerCourses.setAdapter(courseAdapter);
        binding.pagerCourses.setOffscreenPageLimit(1);
        courseAdapter.submit(fixedSkeleton);

        ViewPager2 pager = binding.pagerCourses;
        binding.btnPrev.setOnClickListener(v -> {
            int pos = pager.getCurrentItem();
            if (pos > 0) pager.setCurrentItem(pos - 1, true);
        });
        binding.btnNext.setOnClickListener(v -> pager.setCurrentItem(pager.getCurrentItem() + 1, true));

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
                    // TODO: 즐겨찾기 버튼 처리
                }
        );
        binding.recyclerFestivals.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerFestivals.setAdapter(festivalAdapter);
        binding.btnMoreFestivals.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), FestivalActivity.class)));

        wireHomeCards();
        fetchNationwideSpotCards();
        fetchNationwideFestivalCards();
        fetchFestivalsListForRecycler();

        return root;
    }

    private void setupUserProfile() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userName = prefs.getString("USER_NAME", "방문자");
        String profileUrl = prefs.getString("USER_PROFILE_URL", null);

        binding.tvGreeting.setText(userName + "님, 환영합니다");

        if (profileUrl != null && !profileUrl.isEmpty()) {
            Glide.with(this)
                    .load(profileUrl)
                    .circleCrop()
                    .placeholder(R.drawable.bg_avatar_placeholder)
                    .error(R.drawable.bg_avatar_placeholder)
                    .into(binding.ivUserProfile);
        } else {
            binding.ivUserProfile.setImageResource(R.drawable.bg_avatar_placeholder);
        }
    }

    private void wireHomeCards() {
        if (binding.btnMoreMarkets != null) {
            binding.btnMoreMarkets.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), SpotActivity.class)));
        }
        if (binding.cardMarket1 != null) binding.cardMarket1.setOnClickListener(v -> openSpotDetailFromCard(0));
        if (binding.cardMarket2 != null) binding.cardMarket2.setOnClickListener(v -> openSpotDetailFromCard(1));

        if (binding.cardFestival1 != null) binding.cardFestival1.setOnClickListener(v -> openFestivalDetailFromCard(0));
        if (binding.cardFestival2 != null) binding.cardFestival2.setOnClickListener(v -> openFestivalDetailFromCard(1));
    }

    private TourItem make(String id, String title) {
        TourItem t = new TourItem();
        t.contentid = id;
        t.title = title;
        return t;
    }

    private void openCourseDetail(TourItem item) {
        if (getContext() == null || item == null) return;

        if (courseUrlOverride.containsKey(item.contentid)) {
            String url = courseUrlOverride.get(item.contentid);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        } else {
            Intent i = new Intent(requireContext(), CourseDetailActivity.class);
            i.putExtra(CourseDetailActivity.EXTRA_CONTENT_ID, item.contentid);
            i.putExtra(CourseDetailActivity.EXTRA_TITLE, TextUtils.isEmpty(item.title) ? "상세 정보" : item.title);
            if (courseImageOverride.containsKey(item.contentid)) {
                i.putExtra(CourseDetailActivity.EXTRA_FALLBACK_IMAGE_URI, courseImageOverride.get(item.contentid));
            }
            startActivity(i);
        }
    }

    private void fetchNationwideSpotCards() {
        TourApiHelper.fetchHomeSpots(items -> {
            if (getActivity() == null) return;
            spotCardItems.clear();
            if (items != null) spotCardItems.addAll(items);
            getActivity().runOnUiThread(() -> bindSpotCards(spotCardItems));
        });
    }

    private void bindSpotCards(List<TourItem> items) {
        if (items != null && items.size() > 0) {
            TourItem it = items.get(0);
            binding.textMarket1Title.setText(safe(it.title));
            binding.textMarket1Date.setText("");
            binding.textMarket1DateInfo.setText(safe(it.addr1));
            Glide.with(this)
                    .load(!TextUtils.isEmpty(it.firstimage) ? it.firstimage : R.drawable.sample1)
                    .placeholder(R.drawable.sample1).error(R.drawable.sample1)
                    .into(binding.imageMarket1);
        } else {
            binding.textMarket1Title.setText("데이터가 없습니다");
            binding.textMarket1Date.setText("");
            binding.textMarket1DateInfo.setText("");
            binding.imageMarket1.setImageResource(R.drawable.sample1);
        }

        if (items != null && items.size() > 1) {
            TourItem it = items.get(1);
            binding.textMarket2Title.setText(safe(it.title));
            binding.textMarket2Date.setText("");
            binding.textMarket2DateInfo.setText(safe(it.addr1));
            Glide.with(this)
                    .load(!TextUtils.isEmpty(it.firstimage) ? it.firstimage : R.drawable.sample1)
                    .placeholder(R.drawable.sample1).error(R.drawable.sample1)
                    .into(binding.imageMarket2);
        } else {
            binding.textMarket2Title.setText("");
            binding.textMarket2Date.setText("");
            binding.textMarket2DateInfo.setText("");
            binding.imageMarket2.setImageResource(R.drawable.sample1);
        }
    }

    private void openSpotDetailFromCard(int idx) {
        if (spotCardItems == null || spotCardItems.size() <= idx) return;
        TourItem it = spotCardItems.get(idx);
        Intent i = new Intent(requireContext(), SpotDetailActivity.class);
        i.putExtra(SpotDetailActivity.EXTRA_CONTENT_ID, it.contentid);
        i.putExtra(SpotDetailActivity.EXTRA_CONTENT_TYPE_ID, "12");
        i.putExtra(SpotDetailActivity.EXTRA_TITLE, safe(it.title));
        i.putExtra(SpotDetailActivity.EXTRA_ADDR1, safe(it.addr1));
        i.putExtra(SpotDetailActivity.EXTRA_FIRST_IMAGE, safe(it.firstimage));
        startActivity(i);
    }

    private void fetchNationwideFestivalCards() {
        TourApiHelper.fetchHomeFestivals(items -> {
            if (getActivity() == null) return;
            festivalCardItems.clear();
            if (items != null) festivalCardItems.addAll(items);
            getActivity().runOnUiThread(() -> bindFestivalCards(festivalCardItems));
        });
    }

    private void bindFestivalCards(List<TourItem> items) {
        if (items != null && items.size() > 0) {
            TourItem it = items.get(0);
            binding.textFestival1Title.setText(safe(it.title));
            binding.textFestival1Date.setText("시작일: " + formatDate(it.eventstartdate));
            binding.textFestival1DateInfo.setText("종료일: " + formatDate(it.eventenddate));
            Glide.with(this)
                    .load(!TextUtils.isEmpty(it.firstimage) ? it.firstimage : R.drawable.sample1)
                    .placeholder(R.drawable.sample1).error(R.drawable.sample1)
                    .into(binding.imageFestival1);
        } else {
            binding.textFestival1Title.setText("데이터가 없습니다");
            binding.textFestival1Date.setText("");
            binding.textFestival1DateInfo.setText("");
            binding.imageFestival1.setImageResource(R.drawable.sample1);
        }

        if (items != null && items.size() > 1) {
            TourItem it = items.get(1);
            binding.textFestival2Title.setText(safe(it.title));
            binding.textFestival2Date.setText("시작일: " + formatDate(it.eventstartdate));
            binding.textFestival2DateInfo.setText("종료일: " + formatDate(it.eventenddate));
            Glide.with(this)
                    .load(!TextUtils.isEmpty(it.firstimage) ? it.firstimage : R.drawable.sample1)
                    .placeholder(R.drawable.sample1).error(R.drawable.sample1)
                    .into(binding.imageFestival2);
        } else {
            binding.textFestival2Title.setText("");
            binding.textFestival2Date.setText("");
            binding.textFestival2DateInfo.setText("");
            binding.imageFestival2.setImageResource(R.drawable.sample1);
        }
    }

    private void openFestivalDetailFromCard(int idx) {
        if (festivalCardItems == null || festivalCardItems.size() <= idx) return;
        TourItem it = festivalCardItems.get(idx);
        Intent i = new Intent(requireContext(), FestivalDetailActivity.class);
        i.putExtra(FestivalDetailActivity.EXTRA_CONTENT_ID, it.contentid);
        i.putExtra(FestivalDetailActivity.EXTRA_CONTENT_TYPE_ID, "15");
        i.putExtra(FestivalDetailActivity.EXTRA_TITLE, safe(it.title));
        i.putExtra(FestivalDetailActivity.EXTRA_ADDR1, safe(it.addr1));
        i.putExtra(FestivalDetailActivity.EXTRA_FIRST_IMAGE, safe(it.firstimage));
        startActivity(i);
    }

    private void fetchFestivalsListForRecycler() {
        TourApiHelper.fetchHomeFestivals(items -> {
            if (getActivity() != null) {
                List<SpotResponse.Item> convertedList = new ArrayList<>();
                if (items != null) {
                    for (TourItem tourItem : items) {
                        SpotResponse.Item spotItem = new SpotResponse.Item();
                        spotItem.contentid = tourItem.contentid;
                        spotItem.title = tourItem.title;
                        spotItem.addr1 = tourItem.addr1;
                        spotItem.firstimage = tourItem.firstimage;
                        spotItem.eventstartdate = tourItem.eventstartdate;
                        spotItem.eventenddate = tourItem.eventenddate;
                        spotItem.contenttypeid = "15";
                        convertedList.add(spotItem);
                    }
                }
                getActivity().runOnUiThread(() -> festivalAdapter.submitList(convertedList));
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private String formatDate(String raw) {
        if (!TextUtils.isEmpty(raw) && raw.length() == 8) {
            return raw.substring(0, 4) + "." + raw.substring(4, 6) + "." + raw.substring(6);
        }
        return "";
    }
    private String safe(String s) { return s == null ? "" : s; }
}