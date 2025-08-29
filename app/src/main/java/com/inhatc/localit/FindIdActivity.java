package com.inhatc.localit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class FindIdActivity extends AppCompatActivity {

    private EditText etName, etPhone;
    private Button btnFindId;
    private TextView btnNewPw;
    private FirebaseFirestore db;  // Firestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_id);

        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_phone);
        btnFindId = findViewById(R.id.btn_findId);
        btnNewPw = findViewById(R.id.btn_newPw);
        db = FirebaseFirestore.getInstance();  // Firestore 초기화

        // 🔙 뒤로가기
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // ✅ 아이디(이메일) 찾기 버튼
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

            db.collection("users")
                    .whereEqualTo("name", name)
                    .whereEqualTo("phone", phone)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {




                        if (!queryDocumentSnapshots.isEmpty()) {
                            String email = queryDocumentSnapshots.getDocuments().get(0).getString("email");
                            Toast.makeText(this, "등록된 이메일: " + email, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "일치하는 회원 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "오류 발생: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("FIND_ID", "Firestore 오류", e);
                    });
        });

        // 🔐 비밀번호 찾기 화면 이동
        btnNewPw.setOnClickListener(v -> {
            Intent intent = new Intent(FindIdActivity.this, ChangePwActivity.class);
            startActivity(intent);
        });
    }
}
