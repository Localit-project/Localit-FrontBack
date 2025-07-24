package com.inhatc.localit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class FindIdActivity extends AppCompatActivity {

    private EditText etName, etPhone;
    private Button btnFindId;
    private TextView btnNewPw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_id); // XML 파일명에 맞게 수정

        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_phone);
        btnFindId = findViewById(R.id.btn_findId);
        btnNewPw = findViewById(R.id.btn_newPw);

        // 뒤로가기 버튼 처리
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // 아이디 찾기 버튼 클릭 시
        btnFindId.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "이름을 입력하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            if (phone.isEmpty()) {
                Toast.makeText(this, "전화번호를 입력하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: 서버에 요청 보내기

        });

        // "비밀번호 찾기" 텍스트 클릭 시 비밀번호 변경 화면으로 이동
        btnNewPw.setOnClickListener(v -> {
            Intent intent = new Intent(FindIdActivity.this, ChangePwActivity.class);
            startActivity(intent);
        });

    }}

