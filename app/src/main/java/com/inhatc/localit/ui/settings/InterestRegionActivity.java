package com.inhatc.localit.ui.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.inhatc.localit.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InterestRegionActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "prefs_interest_regions";
    public static final String KEY_SELECTED_SET = "selected_regions";
    public static final String EXTRA_SELECTED = "extra_selected_regions";
    private static final int MAX_SELECTION = 5; // 변경 가능

    private RegionAdapter adapter;
    private ArrayList<String> fullList = new ArrayList<>();
    private EditText etSearch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interest_region);

        // 상단 바
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView btnSave = findViewById(R.id.btnSave);
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("관심지역 설정 (경기도)");

        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // 데이터: 경기도 시/군/구 목록
        fullList.clear();
        fullList.addAll(Arrays.asList(getResources().getStringArray(R.array.textGyeonggi)));

        // 기존 선택 로드
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Set<String> selectedSet = new HashSet<>(sp.getStringSet(KEY_SELECTED_SET, new HashSet<>()));

        // 리사이클러뷰
        RecyclerView rv = findViewById(R.id.rvRegions);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RegionAdapter(fullList, selectedSet, (name, checked, totalChecked) -> {
            if (checked && totalChecked > MAX_SELECTION) {
                adapter.forceUncheck(name);
                Toast.makeText(this, "최대 " + MAX_SELECTION + "개까지 선택할 수 있어요.", Toast.LENGTH_SHORT).show();
            }
        });
        rv.setAdapter(adapter);

        // 저장
        btnSave.setOnClickListener(v -> {
            Set<String> current = new HashSet<>(adapter.getCheckedItems());
            sp.edit().putStringSet(KEY_SELECTED_SET, current).apply();

            Intent data = new Intent();
            data.putStringArrayListExtra(EXTRA_SELECTED, new ArrayList<>(current));
            setResult(Activity.RESULT_OK, data);

            Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show();
            finish();
        });

        // 검색 필터
        etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                String q = s.toString().trim();
                if (q.isEmpty()) {
                    adapter.submitList(fullList);
                } else {
                    List<String> filtered = new ArrayList<>();
                    for (String it : fullList) {
                        if (it.contains(q)) filtered.add(it);
                    }
                    adapter.submitList(filtered);
                }
            }
        });
    }
}
