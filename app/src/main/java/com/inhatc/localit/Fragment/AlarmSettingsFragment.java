package com.inhatc.localit.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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

        // 관심 지역

        binding.btnBack.setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());

        return binding.getRoot();
    }

    /** 각 행 공통 바인딩 */
    private void setupRow(View row, String title, String prefKey) {
        TextView tv = row.findViewById(R.id.tvTitle);
        Switch sw   = row.findViewById(R.id.sw);   // ← 플랫폼 Switch 로 캐스팅

        tv.setText(title);

        boolean enabled = AlarmPrefs.get(requireContext(), prefKey, true);
        sw.setChecked(enabled);

        // setOnCheckedChangeListener 적용
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AlarmPrefs.put(requireContext(), prefKey, isChecked);
            }
        });
        // 혹은 람다로:
        // sw.setOnCheckedChangeListener((buttonView, isChecked) -> AlarmPrefs.put(requireContext(), prefKey, isChecked));
    }
}
