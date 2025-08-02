package com.inhatc.localit;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.inhatc.localit.Fragment.CategoryFragment;
import com.inhatc.localit.Fragment.FavoriteFragment;
import com.inhatc.localit.Fragment.HomeFragment;
import com.inhatc.localit.Fragment.MypageFragment;
import com.inhatc.localit.Fragment.SearchFragment;
import com.inhatc.localit.databinding.ActivityMainBinding;

import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);

        // ✅ 인텐트로 받은 start_fragment 값 확인 (없으면 기본값 0)
        int startFragmentIndex = getIntent().getIntExtra("start_fragment", 0);

        // ✅ start_fragment 값에 따라 첫 화면 프래그먼트 바로 띄우기
        Fragment initialFragment = null;
        switch (startFragmentIndex) {
            case 0:
                initialFragment = new HomeFragment();
                navView.setSelectedItemId(R.id.navigation_home);
                break;
            case 1:
                initialFragment = new CategoryFragment();
                navView.setSelectedItemId(R.id.navigation_category);
                break;
            case 2:
                initialFragment = new SearchFragment();
                navView.setSelectedItemId(R.id.navigation_search);
                break;
            case 3:
                initialFragment = new FavoriteFragment();
                navView.setSelectedItemId(R.id.navigation_favorite);
                break;
            case 4:
                initialFragment = new MypageFragment();
                navView.setSelectedItemId(R.id.navigation_mypage);
                break;
        }

        // ✅ 첫 화면 프래그먼트 교체
        if (initialFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, initialFragment)
                    .commit();
        }

        // ✅ 네비게이션 클릭 시 프래그먼트 전환
        setupBottomNavigationView();
    }

    private void setupBottomNavigationView() {
        binding.navView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.navigation_home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.navigation_category) {
                selectedFragment = new CategoryFragment();
            } else if (id == R.id.navigation_search) {
                selectedFragment = new SearchFragment();
            } else if (id == R.id.navigation_favorite) {
                selectedFragment = new FavoriteFragment();
            } else if (id == R.id.navigation_mypage) {
                selectedFragment = new MypageFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });
    }
}
