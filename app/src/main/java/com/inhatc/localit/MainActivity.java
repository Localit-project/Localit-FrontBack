package com.inhatc.localit;

import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.inhatc.localit.Fragment.CategoryFragment;
import com.inhatc.localit.Fragment.FavoriteFragment;
import com.inhatc.localit.Fragment.HomeFragment;
import com.inhatc.localit.Fragment.MypageFragment;
import com.inhatc.localit.Fragment.NotificationsFragment;
import com.inhatc.localit.Fragment.AlarmSettingsFragment;
import com.inhatc.localit.Fragment.SearchFragment;
import com.inhatc.localit.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = binding.navView;

        // 인텐트로 받은 start_fragment 값 확인 (없으면 기본값 0)
        int startFragmentIndex = getIntent().getIntExtra("start_fragment", 0);

        // start_fragment 값에 따라 첫 화면 프래그먼트 선택
        Fragment initialFragment = null;
        @IdRes int initialMenuId = R.id.navigation_home;

        switch (startFragmentIndex) {
            case 0:
                initialFragment = new HomeFragment();
                initialMenuId = R.id.navigation_home;
                break;
            case 1:
                initialFragment = new CategoryFragment();
                initialMenuId = R.id.navigation_category;
                break;
            case 2:
                initialFragment = new SearchFragment();
                initialMenuId = R.id.navigation_search;
                break;
            case 3:
                initialFragment = new FavoriteFragment();
                initialMenuId = R.id.navigation_favorite;
                break;
            case 4:
                initialFragment = new MypageFragment();
                initialMenuId = R.id.navigation_mypage;
                break;
        }

        if (initialFragment != null) {
            replaceFragment(initialFragment, false);
            navView.setSelectedItemId(initialMenuId);
        }

        // 하단 네비게이션 클릭 시 프래그먼트 전환
        setupBottomNavigationView();
    }

    /** 하단 네비게이션 클릭 핸들러 */
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
                replaceFragment(selectedFragment, false);
            }
            return true;
        });
    }

    /** 공통 프래그먼트 교체 유틸리티 */
    public void replaceFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction tx = getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, fragment);
        if (addToBackStack) {
            tx.addToBackStack(fragment.getClass().getSimpleName());
        }
        tx.commit();
    }

    /** 마이페이지의 알림 버튼에서 호출할 메서드 */
    public void openNotifications() {
        replaceFragment(new NotificationsFragment(), true);
    }

    /** 마이페이지의 '알림 설정' 메뉴에서 호출할 메서드 */
    public void openAlarmSettings() {
        replaceFragment(new AlarmSettingsFragment(), true);
    }
}
