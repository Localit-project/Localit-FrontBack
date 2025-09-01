package com.inhatc.localit.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.inhatc.localit.BuildConfig;
import com.inhatc.localit.MainActivity;
import com.inhatc.localit.MapActivity;
import com.inhatc.localit.R;
import com.inhatc.localit.api.SpotApiHelper;
import com.inhatc.localit.api.SpotApiService;
import com.inhatc.localit.api.SpotResponse;
import com.inhatc.localit.api.home.HomeCoursePagerAdapter;
import com.inhatc.localit.api.home.TourApiHelper;
import com.inhatc.localit.api.home.TourItem;
import com.inhatc.localit.databinding.FragmentHomeBinding;
import com.inhatc.localit.ui.CourseDetailActivity;
import com.inhatc.localit.ui.category.FestivalActivity;
import com.inhatc.localit.ui.category.FestivalDetailActivity;
import com.inhatc.localit.ui.category.SpotActivity;
import com.inhatc.localit.ui.category.SpotDetailActivity;
import com.inhatc.localit.ui.settings.InterestRegionActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Date;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    public HomeCoursePagerAdapter courseAdapter;

    // 코스 커버 이미지만 앱 번들 리소스로 오버라이드 (클릭 시엔 내부 상세로 이동)
    private final Map<String, String> courseImageOverride = new LinkedHashMap<>();

    private ActivityResultLauncher<Intent> interestRegionLauncher;

    private final List<SpotResponse.Item> spotCardItems = new ArrayList<>();
    private final List<SpotResponse.Item> festivalCardItems = new ArrayList<>();

    /** 무한 캐러셀용 데이터로 감싸기 (size < 2면 원본 반환) */
    private List<TourItem> makeLoopData(List<TourItem> src) {
        if (src == null || src.size() < 2) return src;
        List<TourItem> loop = new ArrayList<>(src.size() + 2);
        loop.add(src.get(src.size() - 1)); // 앞에 마지막 복제
        loop.addAll(src);                   // 본체
        loop.add(src.get(0));               // 뒤에 첫 번째 복제
        return loop;
    }

    private void onCourseClicked(TourItem item) {
        Intent i = new Intent(requireContext(), CourseDetailActivity.class);
        i.putExtra("contentId", item.contentid);
        // 비어있으면 25
        i.putExtra("contentTypeId", TextUtils.isEmpty(item.contenttypeid) ? "25" : item.contenttypeid);
        startActivity(i);
    }

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

        // 코스 커버 이미지 오버라이드 맵 (contentId -> drawable 리소스 URI)
        String pkg = requireContext().getPackageName();
        courseImageOverride.put("3517031", "android.resource://" + pkg + "/drawable/travel1");
        courseImageOverride.put("3516944", "android.resource://" + pkg + "/drawable/travel2");
        courseImageOverride.put("3516594", "android.resource://" + pkg + "/drawable/travel3");
        courseImageOverride.put("2022929", "android.resource://" + pkg + "/drawable/travel4");
        courseImageOverride.put("2987504", "android.resource://" + pkg + "/drawable/travel5");
        courseImageOverride.put("2018433", "android.resource://" + pkg + "/drawable/travel6");
        courseImageOverride.put("2833450", "android.resource://" + pkg + "/drawable/travel7");

        // ----- 코스 ViewPager (무한 캐러셀) -----
        courseAdapter = new HomeCoursePagerAdapter(courseImageOverride, this::openCourseDetail);
        binding.pagerCourses.setAdapter(courseAdapter);
        binding.pagerCourses.setOffscreenPageLimit(1);

        final ViewPager2 pager = binding.pagerCourses;

        // 경계 보정 콜백: 센티널 위치에서 애니메이션 없이 실제 위치로 점프
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            private int currentPos = 0;

            @Override
            public void onPageSelected(int position) {
                currentPos = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    int count = courseAdapter.getItemCount();
                    if (count > 1) {
                        if (currentPos == 0) {
                            // 맨 앞 센티널 -> 마지막 실제 항목
                            pager.setCurrentItem(count - 2, false);
                        } else if (currentPos == count - 1) {
                            // 맨 뒤 센티널 -> 첫 실제 항목
                            pager.setCurrentItem(1, false);
                        }
                    }
                }
            }
        });

        // Prev/Next 버튼: 단순 -1 / +1 (경계 처리는 콜백이 담당)
        binding.btnPrev.setOnClickListener(v -> {
            int count = courseAdapter.getItemCount();
            if (count <= 1) return;
            pager.setCurrentItem(pager.getCurrentItem() - 1, true);
        });

        binding.btnNext.setOnClickListener(v -> {
            int count = courseAdapter.getItemCount();
            if (count <= 1) return;
            pager.setCurrentItem(pager.getCurrentItem() + 1, true);
        });

        // 폴백 리스트를 먼저 표시 (루프 데이터로 감싸고 시작 인덱스를 1로)
        List<String> courseIds = Arrays.asList(
                "3517031","3516944","3516594","2022929","2987504","2018433","2833450"
        );
        List<TourItem> fallback = new ArrayList<>();
        for (String id : courseIds) {
            TourItem t = new TourItem();
            t.contentid = id;
            t.title = "";          // 제목은 API 성공 시 채워짐
            t.contenttypeid = "25";// 코스
            t.firstimage = null;   // 이미지는 오버라이드 우선
            fallback.add(t);
        }
        List<TourItem> loopFallback = makeLoopData(fallback);
        courseAdapter.submit(loopFallback);
        if (loopFallback != null && loopFallback.size() > 1) {
            pager.setCurrentItem(1, false); // 첫 실제 항목으로
        }

        // API 성공 시 최신 데이터로 교체 (루프 데이터로 감싸기 + 시작 인덱스 보정)
        TourApiHelper.fetchCourseSummaries(courseIds, courseItems -> {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                if (courseItems != null && !courseItems.isEmpty()) {
                    // contenttypeid 누락 보정
                    for (TourItem it : courseItems) {
                        if (TextUtils.isEmpty(it.contenttypeid)) it.contenttypeid = "25";
                    }
                    List<TourItem> loopItems = makeLoopData(courseItems);
                    courseAdapter.submit(loopItems);
                    if (loopItems != null && loopItems.size() > 1) {
                        pager.setCurrentItem(1, false);
                    }
                }
            });
        });

        wireHomeCards();

        if (binding.recyclerFestivals != null) binding.recyclerFestivals.setVisibility(View.GONE);

        fetchNationwideSpotCards();
        fetchLatestFestivalsForCards();

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

        if (binding.btnMoreFestivals != null) {
            binding.btnMoreFestivals.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), FestivalActivity.class)));
        }
    }

    // 코스 카드를 클릭하면 항상 내부 상세로 이동
    private void openCourseDetail(TourItem item) {
        if (getContext() == null || item == null) return;

        Intent i = new Intent(requireContext(), CourseDetailActivity.class);
        i.putExtra(CourseDetailActivity.EXTRA_CONTENT_ID, item.contentid);
        i.putExtra(CourseDetailActivity.EXTRA_TITLE,
                TextUtils.isEmpty(item.title) ? "상세 정보" : item.title);
        // 코스 타입 보장(없으면 25로 세팅)
        i.putExtra(CourseDetailActivity.EXTRA_CONTENT_TYPE_ID,
                TextUtils.isEmpty(item.contenttypeid) ? "25" : item.contenttypeid);

        // 커버 이미지 폴백 전달(상세에서 썸네일/헤더로 활용)
        if (courseImageOverride.containsKey(item.contentid)) {
            i.putExtra(CourseDetailActivity.EXTRA_FALLBACK_IMAGE_URI,
                    courseImageOverride.get(item.contentid));
        }
        startActivity(i);
    }

    private void openFestivalDetailFromCard(int idx) {
        if (festivalCardItems == null || festivalCardItems.size() <= idx) return;

        SpotResponse.Item it = festivalCardItems.get(idx);
        Intent i = new Intent(requireContext(), FestivalDetailActivity.class);
        i.putExtra(FestivalDetailActivity.EXTRA_CONTENT_ID, it.contentid);
        i.putExtra(FestivalDetailActivity.EXTRA_CONTENT_TYPE_ID,
                TextUtils.isEmpty(it.contenttypeid) ? "15" : it.contenttypeid);
        i.putExtra(FestivalDetailActivity.EXTRA_TITLE, safe(it.title));
        i.putExtra(FestivalDetailActivity.EXTRA_ADDR1, safe(it.addr1));
        i.putExtra(FestivalDetailActivity.EXTRA_FIRST_IMAGE, safe(it.firstimage));
        startActivity(i);
    }

    // ================= 관광 카드 2개 =================
    private void fetchNationwideSpotCards() {
        SpotApiService api = SpotApiHelper.getApiService();
        Call<SpotResponse> call = api.getTourList(
                12, 1, "AND", "localit", "C",
                12,
                1,
                null,
                "json",
                BuildConfig.TOUR_API_KEY
        );

        call.enqueue(new Callback<SpotResponse>() {
            @Override public void onResponse(Call<SpotResponse> call, Response<SpotResponse> response) {
                spotCardItems.clear();
                if (response.isSuccessful()
                        && response.body()!=null
                        && response.body().response!=null
                        && response.body().response.body!=null
                        && response.body().response.body.items!=null
                        && response.body().response.body.items.item!=null) {
                    List<SpotResponse.Item> list = response.body().response.body.items.item;
                    for (int i = 0; i < list.size() && i < 2; i++) spotCardItems.add(list.get(i));
                }
                bindSpotCards(spotCardItems);
            }
            @Override public void onFailure(Call<SpotResponse> call, Throwable t) {
                spotCardItems.clear();
                bindSpotCards(spotCardItems);
            }
        });
    }

    private void bindSpotCards(List<SpotResponse.Item> items) {
        if (items != null && items.size() > 0) {
            SpotResponse.Item it = items.get(0);
            binding.textMarket1Title.setText(safe(it.title));
            binding.textMarket1Date.setText(formatDate(it.createdtime));
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
            SpotResponse.Item it = items.get(1);
            binding.textMarket2Title.setText(safe(it.title));
            binding.textMarket2Date.setText(formatDate(it.createdtime));
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
        SpotResponse.Item it = spotCardItems.get(idx);
        Intent i = new Intent(requireContext(), SpotDetailActivity.class);
        i.putExtra(SpotDetailActivity.EXTRA_CONTENT_ID, it.contentid);
        i.putExtra(SpotDetailActivity.EXTRA_CONTENT_TYPE_ID,
                TextUtils.isEmpty(it.contenttypeid) ? "12" : it.contenttypeid);
        i.putExtra(SpotDetailActivity.EXTRA_TITLE, safe(it.title));
        i.putExtra(SpotDetailActivity.EXTRA_ADDR1, safe(it.addr1));
        i.putExtra(SpotDetailActivity.EXTRA_FIRST_IMAGE, safe(it.firstimage));
        startActivity(i);
    }

    // ================= 최신 축제 카드 2개만 =================
    private void fetchLatestFestivalsForCards() {
        String startDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        SpotApiService api = SpotApiHelper.getApiService();
        Call<SpotResponse> call = api.getFestivalList(
                30, 1, "AND", "localit", "json",
                1, null, startDate, "A",
                BuildConfig.TOUR_API_KEY
        );

        call.enqueue(new Callback<SpotResponse>() {
            @Override public void onResponse(Call<SpotResponse> call, Response<SpotResponse> response) {
                festivalCardItems.clear();
                if (response.isSuccessful()
                        && response.body()!=null
                        && response.body().response!=null
                        && response.body().response.body!=null
                        && response.body().response.body.items!=null
                        && response.body().response.body.items.item!=null) {
                    List<SpotResponse.Item> list = response.body().response.body.items.item;
                    for (int i = 0; i < list.size() && i < 2; i++) {
                        festivalCardItems.add(list.get(i));
                    }
                }
                bindFestivalCards(festivalCardItems);
            }
            @Override public void onFailure(Call<SpotResponse> call, Throwable t) {
                festivalCardItems.clear();
                bindFestivalCards(festivalCardItems);
            }
        });
    }

    private void bindFestivalCards(List<SpotResponse.Item> items) {
        if (items != null && items.size() > 0) {
            SpotResponse.Item it = items.get(0);
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
            SpotResponse.Item it = items.get(1);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private String formatDate(String raw) {
        if (!TextUtils.isEmpty(raw) && raw.length() >= 8) {
            return raw.substring(0, 4) + "." + raw.substring(4, 6) + "." + raw.substring(6, 8);
        }
        return "";
    }
    private String safe(String s) { return s == null ? "" : s; }
}
