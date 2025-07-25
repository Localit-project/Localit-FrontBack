package com.inhatc.localit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import android.util.Log;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {

    private EditText etId, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvSignUp;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    GoogleSignInClient mGoogleSignInClient;//구글 로그인 intent를 만들고 실행하기 위해 사용
    private int RC_SIGN_IN = 9001;//로그인 요청 코드. 로그인 결과를 구분하는 데 필요


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Firebase 인증 및 Firestore 초기화
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        //뷰 및 클릭 리스너 초기화
        initViews();
        setClickListeners();
        //Google 로그인 옵션 설정
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))  // google-services.json 안에 있음
                .requestEmail()
                .build();
        //Google 로그인 클라이언트 초기화
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        //구글 로그인 버튼 클릭 처리
        findViewById(R.id.googleSignInButton).setOnClickListener(view -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override//구글 로그인
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w("Google Sign In", "Google sign in failed", e);
            }
        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {//성공시 MainActivity로 넘어가기,실패시 오류 메세지 출력
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d("FirebaseAuth", "signInWithCredential:success, user: " + user.getEmail());

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Exception e = task.getException();
                        if (e != null) {
                            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(this, "유효하지 않은 인증 정보입니다. 다시 시도해 주세요.", Toast.LENGTH_LONG).show();
                            } else if (e instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(this, "이미 다른 계정으로 가입된 이메일입니다.", Toast.LENGTH_LONG).show();
                            } else if (e instanceof FirebaseNetworkException) {
                                Toast.makeText(this, "네트워크 오류입니다. 인터넷 연결을 확인하세요.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, "로그인에 실패했습니다: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                            Log.w("FirebaseAuth", "signInWithCredential:failure", e);
                        }
                    }
                });
    }
    private void initViews() {
        etId = findViewById(R.id.et_id);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        tvSignUp = findViewById(R.id.tv_sign_up);
    }

    private void setClickListeners() {
        btnLogin.setOnClickListener(v -> performLogin());

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, FindIdActivity.class);
            startActivity(intent);
        });

        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    private void performLogin() {
        String email = etId.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etId.setError("이메일을 입력해주세요");
            etId.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("비밀번호를 입력해주세요");
            etPassword.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.reload().addOnSuccessListener(unused -> {
                                if (user.isEmailVerified()) {
                                    db.collection("users").document(user.getUid())
                                            .get()
                                            .addOnSuccessListener(document -> {
                                                if (!document.exists()) {
                                                    // 최초 로그인 시 Firestore에 기본 정보 저장
                                                    Map<String, Object> userMap = new HashMap<>();
                                                    userMap.put("email", user.getEmail());
                                                    userMap.put("uid", user.getUid());
                                                    userMap.put("name", "");      
                                                    userMap.put("gender", "");
                                                    userMap.put("phone", "");

                                                    db.collection("users").document(user.getUid())
                                                            .set(userMap)
                                                            .addOnSuccessListener(aVoid -> {
                                                                Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show();
                                                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                                finish();
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Toast.makeText(this, "유저 정보 저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            });
                                                } else {
                                                    // 이미 Firestore에 정보 있음
                                                    Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                    finish();
                                                }
                                            });
                                } else {
                                    Toast.makeText(this, "이메일 인증을 완료해주세요", Toast.LENGTH_LONG).show();
                                    mAuth.signOut(); // 인증되지 않은 경우 로그인불가
                                }
                            });
                        }
                    } else {
                        Toast.makeText(this, "로그인 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}