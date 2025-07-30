package com.inhatc.localit;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ChangePwActivity extends AppCompatActivity {

    private EditText etId;
    private Button btnChangePw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pw);

        // 뷰 초기화
        etId = findViewById(R.id.et_id);
        btnChangePw = findViewById(R.id.btn_newPw);

        // 뒤로가기 버튼
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // 비밀번호 재설정 메일 전송
        btnChangePw.setOnClickListener(v -> {
            String email = etId.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "올바른 이메일 형식이 아닙니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            sendResetEmail(email);
        });
    }

    // 비밀번호 재설정 메일 전송
    private void sendResetEmail(String email) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "비밀번호 재설정 이메일이 전송되었습니다.", Toast.LENGTH_LONG).show();
                        finish(); // 전송 후 종료 (원하면 다른 화면으로 이동 가능)
                    } else {
                        Toast.makeText(this, "이메일 전송 실패: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
