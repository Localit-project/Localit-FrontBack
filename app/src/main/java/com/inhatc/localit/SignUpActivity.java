package com.inhatc.localit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etPasswordConfirm, etPhone, etName;
    private RadioGroup rgGender;
    private Button btnSignUp;
    private ImageButton btnBack;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup); //

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setClickListeners();
        addPhoneHyphenWatcher();
    }
    //각 버튼과 연결
    private void initViews() {
        etEmail = findViewById(R.id.et_id);
        etPassword = findViewById(R.id.et_password);
        etPasswordConfirm = findViewById(R.id.et_password_confirm);
        etPhone = findViewById(R.id.et_phone);
        etName = findViewById(R.id.et_name);
        rgGender = findViewById(R.id.rg_gender);
        btnSignUp = findViewById(R.id.btn_signup);
        btnBack = findViewById(R.id.btn_back);
    }

    private void setClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        //각 항목에 조건을 만족하지 않으면  배너 띄움

        btnSignUp.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etPasswordConfirm.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String name = etName.getText().toString().trim();
            int selectedGenderId = rgGender.getCheckedRadioButtonId();

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()
                    || phone.isEmpty() || name.isEmpty() || selectedGenderId == -1) {
                Toast.makeText(this, "모든 항목을 입력해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "유효한 이메일 형식이 아닙니다", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidPassword(password)) {
                Toast.makeText(this, "비밀번호는 영문자와 특수문자를 포함해야 하며 최소 6글자 이상 입력해야 합니다", Toast.LENGTH_SHORT).show();
                return;
            }

            String gender = ((RadioButton) findViewById(selectedGenderId)).getText().toString();

            // 회원가입 정보넣기
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                user.sendEmailVerification()
                                        .addOnCompleteListener(mailTask -> {
                                            if (mailTask.isSuccessful()) {
                                                // Firestore 저장
                                                Map<String, Object> userMap = new HashMap<>();
                                                userMap.put("email", email);
                                                userMap.put("uid", user.getUid());
                                                userMap.put("name", name);
                                                userMap.put("gender", gender);
                                                userMap.put("phone", phone);

                                                db.collection("users").document(user.getUid())
                                                        .set(userMap)
                                                        .addOnSuccessListener(aVoid -> {
                                                            Toast.makeText(this, "이메일 인증을 위해 메일을 확인해주세요.", Toast.LENGTH_LONG).show();
                                                            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                                                            finish();
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(this, "회원 정보 저장 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                        });

                                            } else {
                                                Toast.makeText(this, "인증 이메일 전송 실패: " + mailTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(this, "회원가입 실패: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    // 비번 유효성 검사 (영문 + 특수문자 포함, 최소 6자)
    private boolean isValidPassword(String password) {
        return password.matches("^(?=.*[A-Za-z])(?=.*[^A-Za-z0-9]).{6,}$");
    }

    // 전화번호 자동 - 설정
    private void addPhoneHyphenWatcher() {
        etPhone.addTextChangedListener(new android.text.TextWatcher() {
            private String current = "";
            private final String dash = "-";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                String input = s.toString().replaceAll("[^\\d]", "");
                if (input.equals(current)) return;

                StringBuilder formatted = new StringBuilder();
                int len = input.length();

                if (len >= 3) {
                    formatted.append(input.substring(0, 3));
                    if (len >= 7) {
                        formatted.append(dash).append(input.substring(3, 7));
                        if (len >= 11) {
                            formatted.append(dash).append(input.substring(7, 11));
                        } else if (len > 7) {
                            formatted.append(dash).append(input.substring(7));
                        }
                    } else if (len > 3) {
                        formatted.append(dash).append(input.substring(3));
                    }
                } else {
                    formatted.append(input);
                }

                current = formatted.toString();
                etPhone.removeTextChangedListener(this);
                etPhone.setText(current);
                etPhone.setSelection(current.length());
                etPhone.addTextChangedListener(this);
            }
        });
    }
}