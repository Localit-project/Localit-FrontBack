package com.inhatc.localit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2초 (2000ms)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start); // 시작화면 레이아웃 표시

        // 2초 후 LoginActivity로 이동
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(StartActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // StartActivity 종료
        }, SPLASH_DELAY);
    }
}
