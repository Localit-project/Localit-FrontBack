package com.inhatc.localit.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.view.ViewGroup.LayoutParams;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider; // ViewModelProvider import
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.inhatc.localit.R;
import com.inhatc.localit.api.SpotApiHelper;
import com.inhatc.localit.api.SpotApiService;
import com.inhatc.localit.api.SpotResponse;
import com.inhatc.localit.db.TouristSpot; // db 모델 import

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";
    private static final String SERVICE_KEY = "wL/Ry8EMiMg43mPRl3wyQhKosVExsJbLLDcZebat4S4eedobtNuBG+eqrj5GPKHvEAxy4NjYPz25Parbyeg8PA==";
    private static final String BASE_URL = "https://apis.data.go.kr/B551011/KorService2/";

    private TextInputLayout til;
    private TextInputEditText etSearch;
    private RecyclerView recyclerResults;
    private View progress;

    private SearchAdapter adapter; // 프로젝트의 기존 어댑터 사용
    private SpotApiService api;

    // ViewModel 변수 선언
    private SearchViewModel searchViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        // 뒤로가기(홈)
        ImageButton back = v.findViewById(R.id.btn_back);
        back.setOnClickListener(view -> goHomeSingleTop());

        // 뷰 초기화
        til = v.findViewById(R.id.searchInputLayout);
        etSearch = v.findViewById(R.id.etSearch);
        recyclerResults = v.findViewById(R.id.recyclerResults);
        progress = v.findViewById(R.id.progress_bar);

        tweakEndIconSize(til);

        // ViewModel 인스턴스 생성
        searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        // RecyclerView 설정
        Context ctx = v.getContext();
        recyclerResults.setLayoutManager(new LinearLayoutManager(ctx));
        adapter = new SearchAdapter(
                ctx,
                this::openHomepageFor, // ✅ 결과 클릭 → 홈페이지(또는 구석구석 검색)
                favItem -> {
                    // 찜 버튼 클릭 시 ViewModel에 이벤트 전달
                    searchViewModel.toggleFavorite(favItem);
                }
        );
        recyclerResults.setAdapter(adapter);

        // 찜 목록 LiveData 구독 (DB의 찜 목록이 변경될 때마다 자동 호출)
        searchViewModel.getAllWishedSpots().observe(getViewLifecycleOwner(), wishedSpots -> {
            if (wishedSpots != null) {
                // 찜된 아이템의 contentId만 Set으로 만듭니다.
                Set<String> wishedIds = new HashSet<>();
                for (TouristSpot spot : wishedSpots) {
                    wishedIds.add(spot.contentId);
                }
                // 어댑터에 찜 목록을 전달하여 하트 아이콘을 업데이트합니다.
                adapter.updateFavorites(wishedIds);
            }
        });

        // Retrofit (Gson) 초기화
        HttpLoggingInterceptor log = new HttpLoggingInterceptor();
        log.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(log).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        api = retrofit.create(SpotApiService.class);

        // 엔터/검색 아이콘 동작 리스너
        etSearch.setOnEditorActionListener((tv, action, event) -> {
            if (action == EditorInfo.IME_ACTION_SEARCH
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                performSearchFromInput();
                return true;
            }
            return false;
        });
        til.setEndIconOnClickListener(view -> performSearchFromInput());

        // 추천 검색어 칩 리스너
        setChipClick(v, R.id.chip_busan, "부산");
        setChipClick(v, R.id.chip_seoul, "서울");
        setChipClick(v, R.id.chip_gangneung, "강릉");
        setChipClick(v, R.id.chip_pohang, "포항");
        setChipClick(v, R.id.chip_incheon, "인천");
    }

    private void setChipClick(View root, int chipId, String keyword) {
        Chip chip = root.findViewById(chipId);
        if (chip != null) chip.setOnClickListener(v -> searchAll(keyword));
    }

    private void performSearchFromInput() {
        String keyword = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";
        if (TextUtils.isEmpty(keyword)) {
            etSearch.setError("검색어를 입력해 주세요");
            return;
        }
        searchAll(keyword);
    }

    private void tweakEndIconSize(TextInputLayout til) {
        if (til == null) return;
        View endView = til.findViewById(com.google.android.material.R.id.text_input_end_icon);
        if (endView instanceof ImageView) {
            ImageView end = (ImageView) endView;
            int sizeDp = 24;
            float density = getResources().getDisplayMetrics().density;
            int px = (int) (sizeDp * density);

            end.setScaleType(ImageView.ScaleType.FIT_CENTER);
            end.setAdjustViewBounds(true);
            LayoutParams lp = end.getLayoutParams();
            if (lp != null) { lp.width = px; lp.height = px; end.setLayoutParams(lp); }
            end.setMinimumWidth(px);
            end.setMinimumHeight(px);

            View parent = (View) end.getParent();
            if (parent != null && parent.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) parent.getLayoutParams();
                mlp.setMarginEnd((int) (13 * density));
                parent.setLayoutParams(mlp);
                parent.requestLayout();
            }
        }
    }

    /** 관광지(12) + 축제(15) 병렬 호출 후 하나의 리스트로 합침 */
    private void searchAll(String keyword) {
        showLoading(true);
        adapter.setItems(Collections.emptyList());

        AtomicInteger done = new AtomicInteger(0);
        List<SpotResponse.Item> merged = Collections.synchronizedList(new ArrayList<>());

        // 관광지 검색 API 호출
        api.searchKeyword(
                SERVICE_KEY, "AND", "Localit", "json",
                keyword, 12, 20, 1
        ).enqueue(new Callback<SpotResponse>() {
            @Override public void onResponse(Call<SpotResponse> call, Response<SpotResponse> res) {
                if (res.isSuccessful() && res.body() != null
                        && res.body().response != null
                        && res.body().response.body != null
                        && res.body().response.body.items != null) {
                    List<SpotResponse.Item> list = res.body().response.body.items.item;
                    if (list != null) {
                        for (SpotResponse.Item it : list) it.setLocalContentType(12);
                        merged.addAll(list);
                    }
                }
                if (done.incrementAndGet() == 2) onBothFinished(merged);
            }
            @Override public void onFailure(Call<SpotResponse> call, Throwable t) {
                Log.e(TAG, "관광지 검색 실패", t);
                if (done.incrementAndGet() == 2) onBothFinished(merged);
            }
        });

        // 축제 검색 API 호출
        api.searchKeyword(
                SERVICE_KEY, "AND", "Localit", "json",
                keyword, 15, 20, 1
        ).enqueue(new Callback<SpotResponse>() {
            @Override public void onResponse(Call<SpotResponse> call, Response<SpotResponse> res) {
                if (res.isSuccessful() && res.body() != null
                        && res.body().response != null
                        && res.body().response.body != null
                        && res.body().response.body.items != null) {
                    List<SpotResponse.Item> list = res.body().response.body.items.item;
                    if (list != null) {
                        for (SpotResponse.Item it : list) it.setLocalContentType(15);
                        merged.addAll(list);
                    }
                }
                if (done.incrementAndGet() == 2) onBothFinished(merged);
            }
            @Override public void onFailure(Call<SpotResponse> call, Throwable t) {
                Log.e(TAG, "축제 검색 실패", t);
                if (done.incrementAndGet() == 2) onBothFinished(merged);
            }
        });
    }

    private void onBothFinished(List<SpotResponse.Item> merged) {
        Collections.sort(merged, (a, b) -> {
            String ta = a.getTitle() == null ? "" : a.getTitle();
            String tb = b.getTitle() == null ? "" : b.getTitle();
            return ta.compareToIgnoreCase(tb);
        });
        adapter.setItems(merged);
        showLoading(false);
    }

    private void showLoading(boolean show) {
        if (progress != null) progress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /** 홈으로 이동(중복 쌓임 방지) */
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
        } catch (Exception ignored) {}
        BottomNavigationView bottom = requireActivity().findViewById(R.id.nav_view);
        if (bottom != null) bottom.setSelectedItemId(R.id.navigation_home);
    }

    // ----------------------- ⬇⬇ 홈페이지 열기 로직 -----------------------

    /** 검색 결과 아이템 클릭 -> 홈페이지(또는 '대한민국 구석구석' 검색)로 이동 */
    private void openHomepageFor(SpotResponse.Item item) {
        if (item == null) return;

        // contentId
        String contentId = null;
        try {
            if (item.contentid != null) contentId = String.valueOf(item.contentid);
            else if (item.getContentid() != null) contentId = String.valueOf(item.getContentid());
        } catch (Throwable ignored) {
        }
        if (TextUtils.isEmpty(contentId)) {
            Toast.makeText(requireContext(), "콘텐츠 ID가 없어 이동할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // contentTypeId (우선 localContentType, 없으면 원본)
        String contentTypeId = null;
        try {
            Integer local = null;
            try {
                local = item.getLocalContentType();
            } catch (Throwable ignored) {
            }
            if (local != null && local > 0) {
                contentTypeId = String.valueOf(local); // 12 or 15
            } else if (item.contenttypeid != null) {
                contentTypeId = String.valueOf(item.contenttypeid);
            } else if (item.getContenttypeid() != null) {
                contentTypeId = String.valueOf(item.getContentid()); // <-- getContentid()가 getContenttypeid()로 수정되었습니다.
            }
        } catch (Throwable ignored) {
        }
        if (TextUtils.isEmpty(contentTypeId)) contentTypeId = "12";

        // 제목 (구석구석 검색 Fallback에 사용)
        String t = null;
        try {
            t = item.getTitle();
        } catch (Throwable ignored) {
        }
        if (TextUtils.isEmpty(t)) t = item.title;
        final String titleFinal = t; //
    }
}
