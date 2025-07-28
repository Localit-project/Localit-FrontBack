package com.inhatc.localit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;
import com.navercorp.nid.NaverIdLoginSDK;
import com.navercorp.nid.oauth.OAuthLoginCallback;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText etId, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvSignUp;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private GoogleSignInClient mGoogleSignInClient;
    private final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setClickListeners();


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.googleSignInButton).setOnClickListener(view -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });


        NaverIdLoginSDK.INSTANCE.initialize(
                this,
                "XjKX66OIxZPMPqnulSoy",  // 네이버 Client ID
                "BS64ivicIw",            // 네이버 Client Secret
                "Localit"                // 앱 이름
        );

        ImageButton btnNaver = findViewById(R.id.btn_naver);
        btnNaver.setOnClickListener(v -> {
            NaverIdLoginSDK.INSTANCE.authenticate(
                    LoginActivity.this,
                    new OAuthLoginCallback() {
                        @Override
                        public void onSuccess() {
                            String accessToken = NaverIdLoginSDK.INSTANCE.getAccessToken();
                            fetchNaverUserProfile(accessToken);
                        }

                        @Override
                        public void onFailure(int httpStatus, @NonNull String message) {
                            Toast.makeText(LoginActivity.this, "네이버 로그인 실패", Toast.LENGTH_SHORT).show();
                            Log.e("NaverLogin", "실패: " + httpStatus + " / " + message);
                        }

                        @Override
                        public void onError(int errorCode, @NonNull String message) {
                            onFailure(errorCode, message);
                        }
                    }
            );
        });
    }

    private void fetchNaverUserProfile(String token) {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL("https://openapi.naver.com/v1/nid/me");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                int responseCode = conn.getResponseCode();

                if (responseCode == 200) {
                    java.io.BufferedReader br = new java.io.BufferedReader(
                            new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();

                    org.json.JSONObject json = new org.json.JSONObject(response.toString());
                    org.json.JSONObject responseJson = json.getJSONObject("response");
                    String email = responseJson.getString("email");
                    String name = responseJson.optString("name", "네이버사용자");

                    runOnUiThread(() -> {
                        saveNaverUserToFirestore(email, name);
                        Toast.makeText(this, "네이버 로그인 성공", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    });
                }
            } catch (Exception e) {
                Log.e("NaverAPI", "프로필 요청 실패", e);
            }
        }).start();
    }

    private void saveNaverUserToFirestore(String email, String name) {
        String uid = "naver_" + email;
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", email);
        userMap.put("uid", uid);
        userMap.put("name", name);
        userMap.put("gender", "");
        userMap.put("phone", "");

        db.collection("users").document(uid)
                .set(userMap)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "네이버 유저 저장 성공"))
                .addOnFailureListener(e -> Log.e("Firestore", "저장 실패: " + e.getMessage()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN && data != null) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, "구글 로그인 실패", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(this, "구글 로그인 성공", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "로그인 실패: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
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

        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, FindIdActivity.class)));

        tvSignUp.setOnClickListener(v ->
                startActivity(new Intent(this, SignUpActivity.class)));
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
                        if (user != null && user.isEmailVerified()) {
                            Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "이메일 인증을 완료해주세요", Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                        }
                    } else {
                        Toast.makeText(this, "로그인 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}