package com.inhatc.localit.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.inhatc.localit.R;
import com.inhatc.localit.databinding.FragmentCategoryBinding;
import com.inhatc.localit.ui.category.EventsActivity;
import com.inhatc.localit.ui.category.NewsActivity;
import com.inhatc.localit.ui.category.RegionAdapter;
import com.inhatc.localit.ui.category.SpotActivity;
import com.inhatc.localit.ui.category.SubRegionAdapter;

import java.util.Arrays;
import java.util.List;

public class CategoryFragment extends Fragment {

    private FragmentCategoryBinding binding;
    private String selectedRegion = null;
    private String selectedSubRegion = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        hideRightCategorySection(); // 초기 숨김

        List<String> regions = Arrays.asList(getResources().getStringArray(R.array.korea_regions));

        RegionAdapter adapter = new RegionAdapter(regions, region -> {
            selectedRegion = region;
            selectedSubRegion = null;

            Log.d("RegionClick", "선택된 지역: " + region);

            if (region.equals("경기")) {
                binding.subCategoryLayout.setVisibility(View.VISIBLE);

                List<String> subRegions = Arrays.asList(getResources().getStringArray(R.array.textGyeonggi));
                SubRegionAdapter subAdapter = new SubRegionAdapter(subRegions, subRegion -> {
                    selectedSubRegion = subRegion;
                    Log.d("SubRegionClick", "선택된 상세 지역: " + subRegion);
                    showRightCategorySection();
                });

                binding.recyclerViewSubRegions.setLayoutManager(new LinearLayoutManager(getContext()));
                binding.recyclerViewSubRegions.setAdapter(subAdapter);

            } else {
                binding.subCategoryLayout.setVisibility(View.GONE);
                selectedSubRegion = null;
                showRightCategorySection();
            }
        });

        binding.recyclerViewRegions.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewRegions.setAdapter(adapter);

        setupCategoryClickListeners();

        return root;
    }

    private void setupCategoryClickListeners() {
        binding.categoryNews.setOnClickListener(v -> openCategoryActivity(NewsActivity.class));
        binding.categoryEvents.setOnClickListener(v -> openCategoryActivity(EventsActivity.class));
        binding.categoryTourism.setOnClickListener(v -> openCategoryActivity(SpotActivity.class));
    }

    private void openCategoryActivity(Class<?> activityClass) {
        String targetRegion;

        if ("경기".equals(selectedRegion)) {
            if (selectedSubRegion == null) {
                Toast.makeText(getContext(), "경기도의 상세 지역을 선택해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            targetRegion = selectedSubRegion;
        } else {
            if (selectedRegion == null) {
                Toast.makeText(getContext(), "먼저 지역을 선택해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            targetRegion = selectedRegion;
        }

        Intent intent = new Intent(getContext(), activityClass);  // 해당 액티비티로 이동
        intent.putExtra("regionName", selectedRegion);
        if (selectedSubRegion != null) {
            intent.putExtra("subRegionName", selectedSubRegion);
        }
        startActivity(intent);
    }

    private void showRightCategorySection() {
        binding.categoryNews.setVisibility(View.VISIBLE);
        binding.categoryEvents.setVisibility(View.VISIBLE);
        binding.categoryTourism.setVisibility(View.VISIBLE);
    }

    private void hideRightCategorySection() {
        binding.categoryNews.setVisibility(View.GONE);
        binding.categoryEvents.setVisibility(View.GONE);
        binding.categoryTourism.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
