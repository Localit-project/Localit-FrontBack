package com.inhatc.localit.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.inhatc.localit.MainActivity; // MainActivity import
import com.inhatc.localit.R;
import com.inhatc.localit.RegionDetailActivity;
import com.inhatc.localit.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private TextView[] regionTexts;
    private String[] regions;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // strings.xml에서 지역 배열 불러오기
        regions = getResources().getStringArray(R.array.regions_array);

        // fragment_home.xml 안의 TextView 연결
        regionTexts = new TextView[]{
                binding.textSeoul,
                binding.textIncheon,
                binding.textGyeonggi,
                binding.textGangwon,
                binding.textChungbuk,
                binding.textSejong,
                binding.textChungnam,
                binding.textDaejeon,
                binding.textGyeongbuk,
                binding.textDaegu,
                binding.textUlsan,
                binding.textBusan,
                binding.textGyeongnam,
                binding.textJeonbuk,
                binding.textGwangju,
                binding.textJeonnam,
                binding.textJeju
        };

        setRegionNames();
        setupRegionInteractions();

        // 알림 버튼 클릭 리스너 추가
        ImageView btnNotifications = binding.getRoot().findViewById(R.id.btnNotifications);
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> {
                // MainActivity의 openNotifications() 메서드를 호출하여 알림 화면으로 이동
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).openNotifications();
                }
            });
        }

        return root;
    }

    // TextView에 지역 이름 넣기
    private void setRegionNames() {
        for (int i = 0; i < regionTexts.length; i++) {
            regionTexts[i].setText(regions[i]);
        }
    }

    // 각 지역 TextView 클릭 & 터치 이벤트
    private void setupRegionInteractions() {
        for (TextView tv : regionTexts) {

            // 터치 시 확대/복원 애니메이션
            tv.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).start();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                        break;
                }
                return false;
            });

            // 클릭 시 상세 페이지로 이동
            tv.setOnClickListener(v -> {
                String regionName = ((TextView) v).getText().toString().replace("\n", "");
                Toast.makeText(getContext(), regionName + " 선택됨", Toast.LENGTH_SHORT).show();
                onRegionSelected(regionName);
            });
        }
    }

    // 상세 페이지로 이동하는 함수
    private void onRegionSelected(String regionName) {
        Intent intent = new Intent(getActivity(), RegionDetailActivity.class);
        intent.putExtra("regionName", regionName);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}