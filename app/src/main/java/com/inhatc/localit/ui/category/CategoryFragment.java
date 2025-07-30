package com.inhatc.localit.ui.category;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;         // ← 이 부분 반드시 필요
import androidx.lifecycle.ViewModelProvider;

import com.inhatc.localit.databinding.FragmentCategoryBinding;


public class CategoryFragment extends Fragment {

    private FragmentCategoryBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // 1) 뷰모델 인스턴스 생성
        CategoryViewModel dashboardViewModel =
                new ViewModelProvider(this).get(CategoryViewModel.class);

        // 2) 뷰바인딩
        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 3) 뷰모델 인스턴스에서 getText() 호출해서 LiveData 구독
        final TextView textView = binding.textCategory;
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
