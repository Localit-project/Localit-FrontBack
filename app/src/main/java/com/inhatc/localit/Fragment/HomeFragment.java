package com.inhatc.localit.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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

    private final Map<String, String> courseImageOverride = new LinkedHashMap<>();
    private final Map<String, String> courseUrlOverride = new LinkedHashMap<>();

    private ActivityResultLauncher<Intent> interestRegionLauncher;

    private final List<SpotResponse.Item> spotCardItems = new ArrayList<>();
    private final List<SpotResponse.Item> festivalCardItems = new ArrayList<>();

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
        courseImageOverride.put("3517031", "android.resource://" + pkg + "/drawable/travel1");
        courseImageOverride.put("3516944", "android.resource://" + pkg + "/drawable/travel2");
        courseImageOverride.put("3516594", "android.resource://" + pkg + "/drawable/travel3");
        courseImageOverride.put("2022929", "android.resource://" + pkg + "/drawable/travel4");
        courseImageOverride.put("2987504", "android.resource://" + pkg + "/drawable/travel5");
        courseImageOverride.put("2018433", "android.resource://" + pkg + "/drawable/travel6");
        courseImageOverride.put("2833450", "android.resource://" + pkg + "/drawable/travel7");

        courseUrlOverride.put("3517031", "https://korean.visitkorea.or.kr/detail/cs_detail_cos.do?cotid=e42e60b7-eac0-4540-a1e4-749bed108154&big_category=&mid_category=&big_area=37");
        courseUrlOverride.put("3516944", "https://korean.visitkorea.or.kr/detail/cs_detail_cos.do?cotid=223fa941-e1ee-4853-aa8d-3faa620263d3&big_category=&mid_category=&big_area=33");
        courseUrlOverride.put("3516594", "https://korean.visitkorea.or.kr/detail/cs_detail_cos.do?cotid=9f4873a0-ba10-4bdb-bb05-97f00a029c67&big_category=&mid_category=&big_area=38");
        courseUrlOverride.put("2022929", "https://korean.visitkorea.or.kr/detail/cs_detail_cos.do?cotid=cf46676f-41d0-495f-972c-bfb32d59ff0b&big_category=C01&mid_category=C0115&big_area=1");
        courseUrlOverride.put("2987504", "https://korean.visitkorea.or.kr/detail/cs_detail_cos.do?cotid=2d54823f-a765-4b64-8693-72bc59e6798b&big_category=C01&mid_category=C0114&big_area=2");
        courseUrlOverride.put("2018433", "https://korean.visitkorea.or.kr/detail/cs_detail_cos.do?cotid=5064fda9-ac0a-40a4-8abf-b50dc5fdb797&big_category=C01&mid_category=C0112&big_area=31");
        courseUrlOverride.put("2833450", "https://korean.visitkorea.or.kr/detail/cs_detail_cos.do?cotid=36c7d072-de3d-4c4b-9c7e-e1ddaade2a7d&big_category=C01&mid_category=C0114&big_area=32");

        // ----- 코스 ViewPager (API만 사용) -----
        courseAdapter = new HomeCoursePagerAdapter(courseImageOverride, this::openCourseDetail);
        binding.pagerCourses.setAdapter(courseAdapter);
        binding.pagerCourses.setOffscreenPageLimit(1);

        ViewPager2 pager = binding.pagerCourses;
        binding.btnPrev.setOnClickListener(v -> {
            int pos = pager.getCurrentItem();
            if (pos > 0) pager.setCurrentItem(pos - 1, true);
        });
        binding.btnNext.setOnClickListener(v -> pager.setCurrentItem(pager.getCurrentItem() + 1, true));

        List<String> courseIds = Arrays.asList(
                "3517031","3516944","3516594","2022929","2987504","2018433","2833450"
        );
        TourApiHelper.fetchCourseSummaries(courseIds, courseItems -> {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> courseAdapter.submit(courseItems)); // List<TourItem>
        });

        wireHomeCards();

        // 레이아웃에 리사이클러뷰가 남아 있다면 숨김
        if (binding.recyclerFestivals != null) binding.recyclerFestivals.setVisibility(View.GONE);

        // 관광/축제 카드: API로 최신 2개 바인딩
        fetchNationwideSpotCards();
        fetchLatestFestivalsForCards();   // ← 목록 없이 카드만 채움

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

        // 더보기 누르면 목록 화면에서 전체 리스트 표시
        if (binding.btnMoreFestivals != null) {
            binding.btnMoreFestivals.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), FestivalActivity.class)));
        }
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

    // ================= 관광 카드 2개 =================
    private void fetchNationwideSpotCards() {
        SpotApiService api = SpotApiHelper.getApiService();
        Call<SpotResponse> call = api.getTourList(
                12, 1, "AND", "localit", "C",
                12,
                1,         // areaCode: 서울(기본) — 필요 시 관심지역으로 교체
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
        // ▼▼▼ [수정됨] 뷰가 파괴된 경우를 대비하여 NullPointerException 방지 코드를 추가합니다. ▼▼▼
        if (binding == null) {
            return;
        }

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
                1,              // areaCode: 서울(기본) — 필요 시 관심지역으로 교체
                null,
                startDate,      // 오늘 이후 축제
                "A",            // 목록에서 최신 잘 나오던 정렬값
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
        // ▼▼▼ [수정됨] 뷰가 파괴된 경우를 대비하여 NullPointerException 방지 코드를 추가합니다. ▼▼▼
        if (binding == null) {
            return;
        }

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
