package com.inhatc.localit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ChangePwActivity extends AppCompatActivity {

    private EditText etId, etPassword, etPasswordConfirm;
    private Button btnChangePw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pw);

        // 뷰 초기화
        etId = findViewById(R.id.et_id);
        etPassword = findViewById(R.id.et_password);
        etPasswordConfirm = findViewById(R.id.et_password_confirm);
        btnChangePw = findViewById(R.id.btn_newPw);

        // 뒤로가기 버튼
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // 비밀번호 변경 버튼 클릭
        btnChangePw.setOnClickListener(v -> {
            String id = etId.getText().toString().trim();
            String newPw = etPassword.getText().toString();
            String confirmPw = etPasswordConfirm.getText().toString();

            if (id.isEmpty()) {
                Toast.makeText(this, "아이디(이메일)를 입력하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(id).matches()) {
                Toast.makeText(this, "올바른 이메일 형식이 아닙니다", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPw.isEmpty() || confirmPw.isEmpty()) {
                Toast.makeText(this, "비밀번호를 모두 입력하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPw.equals(confirmPw)) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPw.length() < 8) {
                Toast.makeText(this, "비밀번호는 최소 8자리 이상이어야 합니다", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: 서버 요청 또는 로컬 처리
            changePassword(id, newPw);
        });
    }

    // 비밀번호 변경 처리 (예시)
    private void changePassword(String email, String newPassword) {
        // TODO: 서버에 비밀번호 변경 요청
        Toast.makeText(this, email + "의 비밀번호를 변경합니다", Toast.LENGTH_SHORT).show();

        // 예: 성공 시 처리
        // showSuccessDialog();
    }


    }

