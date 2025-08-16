package com.inhatc.localit.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.inhatc.localit.R;
import com.inhatc.localit.databinding.FragmentAlarmSettingsBinding;
import com.inhatc.localit.util.AlarmPrefs;

public class AlarmSettingsFragment extends Fragment {

    private FragmentAlarmSettingsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAlarmSettingsBinding.inflate(inflater, container, false);

        // 찜목록
        setupRow(binding.rowWishFestival.getRoot(),  getString(R.string.festival), AlarmPrefs.KEY_WISH_FESTIVAL);
        setupRow(binding.rowWishSpot.getRoot(),      getString(R.string.spot),     AlarmPrefs.KEY_WISH_SPOT);
        setupRow(binding.rowWishNews.getRoot(),      getString(R.string.news),     AlarmPrefs.KEY_WISH_NEWS);

        // 관심 지역
        setupRow(binding.rowRegionFestival.getRoot(),getString(R.string.festival), AlarmPrefs.KEY_REGION_FESTIVAL);
        setupRow(binding.rowRegionSpot.getRoot(),    getString(R.string.spot),     AlarmPrefs.KEY_REGION_SPOT);
        setupRow(binding.rowRegionNews.getRoot(),    getString(R.string.news),     AlarmPrefs.KEY_REGION_NEWS);

        binding.btnBack.setOnClickListener(v -> requireActivity()
                .getOnBackPressedDispatcher().onBackPressed());

        return binding.getRoot();
    }

    /** 공통 행 설정 */
    private void setupRow(View row, String title, String prefKey) {
        TextView tv = row.findViewById(R.id.tvTitle);
        SwitchMaterial sw = row.findViewById(R.id.sw);

        tv.setText(title);

        // 기본값 true
        boolean enabled = AlarmPrefs.get(requireContext(), prefKey, true);
        sw.setChecked(enabled);

        sw.setOnCheckedChangeListener((button, isChecked) ->
                AlarmPrefs.put(requireContext(), prefKey, isChecked)
        );
    }
}
