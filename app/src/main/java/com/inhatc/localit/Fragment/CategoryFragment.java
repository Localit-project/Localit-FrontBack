package com.inhatc.localit.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.inhatc.localit.R;
import com.inhatc.localit.databinding.FragmentCategoryBinding;
import com.inhatc.localit.ui.category.EventsActivity;
import com.inhatc.localit.ui.category.NewsActivity;
import com.inhatc.localit.ui.category.TourismActivity;
import com.inhatc.localit.ui.category.RegionAdapter;

import java.util.Arrays;
import java.util.List;

public class CategoryFragment extends Fragment {

    private FragmentCategoryBinding binding;
    private String selectedRegion = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 지역 목록 불러오기
        List<String> regions = Arrays.asList(getResources().getStringArray(R.array.regions_array));

        // RecyclerView 설정
        RegionAdapter adapter = new RegionAdapter(regions, region -> {
            selectedRegion = region;
            Log.d("RegionClick", "클릭된 지역: " + region);
        });


        binding.recyclerViewRegions.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewRegions.setAdapter(adapter);

        // 오른쪽 메뉴 클릭 리스너
        setupCategoryClickListeners();

        return root;
    }

    private void setupCategoryClickListeners() {
        binding.categoryNews.setOnClickListener(v -> openCategoryActivity(NewsActivity.class));
        binding.categoryEvents.setOnClickListener(v -> openCategoryActivity(EventsActivity.class));
        binding.categoryTourism.setOnClickListener(v -> openCategoryActivity(TourismActivity.class));
    }

    private void openCategoryActivity(Class<?> activityClass) {
        if (selectedRegion == null) {
            Toast.makeText(getContext(), "먼저 지역을 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(getContext(), activityClass);
        intent.putExtra("region_name", selectedRegion); // 지역명 선택!
        startActivity(intent);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
