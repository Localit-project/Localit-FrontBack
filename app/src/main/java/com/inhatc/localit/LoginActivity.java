package com.inhatc.localit;

import static java.lang.System.getProperties;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;
import com.navercorp.nid.NaverIdLoginSDK;
import com.navercorp.nid.oauth.OAuthLoginCallback;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_KAKAO_SIGN_IN = 64206;
    private static final String TAG = "LOGIN";

    private EditText etId, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvSignUp;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setClickListeners();
        // Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.googleSignInButton).setOnClickListener(view -> {
            signOutAllSessions(); // 기존 세션 정리
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        // Naver
        NaverIdLoginSDK.INSTANCE.initialize(this, "XjKX66OIxZPMPqnulSoy", "BS64ivicIw", "Localit");
        ImageButton btnNaver = findViewById(R.id.btn_naver);
        btnNaver.setOnClickListener(v -> {
            signOutAllSessions(); // 기존 세션 정리
            NaverIdLoginSDK.INSTANCE.authenticate(LoginActivity.this, new OAuthLoginCallback() {
                @Override
                public void onSuccess() {
                    String accessToken = NaverIdLoginSDK.INSTANCE.getAccessToken();
                    fetchNaverUserProfile(accessToken); // 성공 시 → Firebase 세션 보장 후 메인
                }
                @Override
                public void onFailure(int httpStatus, @NonNull String message) {
                    Toast.makeText(LoginActivity.this, "네이버 로그인 실패", Toast.LENGTH_SHORT).show();
                    Log.e("NaverLogin", "실패: " + httpStatus + " / " + message);
                }
                @Override
                public void onError(int errorCode, @NonNull String message) { onFailure(errorCode, message); }
            });
        });

        // Kakao
        setupKakaoLogin();
    }

    /* ========== 공통 유틸 ========== */

    /** 모든 로그인 세션 정리(이메일/Firebase/구글/네이버/카카오) */
    private void signOutAllSessions() {
        try { FirebaseAuth.getInstance().signOut(); } catch (Throwable ignore) {}
        try {
            if (mGoogleSignInClient == null) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
            }
            mGoogleSignInClient.signOut();
        } catch (Throwable ignore) {}
        try { NaverIdLoginSDK.INSTANCE.logout(); } catch (Throwable ignore) {}
        try { UserApiClient.getInstance().logout(throwable -> null); } catch (Throwable ignore) {}
    }
    /** 소셜 로그인 후 Firebase 세션 없으면 익명 로그인으로 세션 보장 */
    private void ensureFirebaseSessionForSocial(Runnable onReady) {
        FirebaseUser cur = FirebaseAuth.getInstance().getCurrentUser();
        if (cur != null) { onReady.run(); return; }
        FirebaseAuth.getInstance().signInAnonymously()
                .addOnSuccessListener(res -> onReady.run())
                .addOnFailureListener(e -> {
                    String code = (e instanceof FirebaseAuthException) ? ((FirebaseAuthException) e).getErrorCode() : "unknown";
                    Log.e("LOGIN", "Anonymous sign-in failed, code=" + code, e);
                    Toast.makeText(this, "세션 생성 실패: " + code, Toast.LENGTH_SHORT).show();
                });
    }

    /** 메인 이동(+스택 정리, 소셜 여부 전달) */
    private void goMainWithSocial(boolean isSocial) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("isSocial", isSocial);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /* ========== Google ========== */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w("GoogleSignIn", "Google sign in failed", e);
                Toast.makeText(this, "Google 로그인 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        // ★ 소셜 플래그 & 고정키 저장 (구글은 Firebase UID가 고정)
                        if (user != null) {
                            getSharedPreferences("auth", MODE_PRIVATE)
                                    .edit()
                                    .putBoolean("is_social", true)
                                    .putString("provider", "google")
                                    .putString("social_uid", user.getUid())
                                    .putString("social_email", user.getEmail())
                                    .apply();
                        }
                        Log.d("FirebaseAuth", "Google 로그인 성공");
                        goMainWithSocial(true); // 소셜
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

    /* ========== 이메일/비번 ========== */

    private void performLogin() {
        String email = etId.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) { etId.setError("이메일을 입력해주세요"); etId.requestFocus(); return; }
        if (password.isEmpty()) { etPassword.setError("비밀번호를 입력해주세요"); etPassword.requestFocus(); return; }

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
                                                    Map<String, Object> userMap = new HashMap<>();
                                                    userMap.put("uid", user.getUid());
                                                    userMap.put("provider", "password");
                                                    userMap.put("providerUid", user.getUid());
                                                    userMap.put("email", user.getEmail());
                                                    userMap.put("displayName", "");
                                                    userMap.put("photoUrl", null);
                                                    userMap.put("createdAt", FieldValue.serverTimestamp());
                                                    userMap.put("updatedAt", FieldValue.serverTimestamp());

                                                    db.collection("users").document(user.getUid())
                                                            .set(userMap, SetOptions.merge())
                                                            .addOnSuccessListener(aVoid -> goMainWithSocial(false))
                                                            .addOnFailureListener(e ->
                                                                    Toast.makeText(this, "유저 정보 저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                                } else {
                                                    goMainWithSocial(false);
                                                }
                                            });
                                } else {
                                    Toast.makeText(this, "이메일 인증을 완료해주세요", Toast.LENGTH_LONG).show();
                                    mAuth.signOut();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(this, "로그인 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /* ========== View ========== */

    private void initViews() {
        etId = findViewById(R.id.et_id);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        tvSignUp = findViewById(R.id.tv_sign_up);
    }

    private void setClickListeners() {
        btnLogin.setOnClickListener(v -> performLogin());
        tvForgotPassword.setOnClickListener(v -> startActivity(new Intent(this, FindIdActivity.class)));
        tvSignUp.setOnClickListener(v -> startActivity(new Intent(this, SignUpActivity.class)));
    }

    /* ========== Naver ========== */

    private void fetchNaverUserProfile(String token) {
        new Thread(() -> {
            try {
                URL url = new URL("https://openapi.naver.com/v1/nid/me");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                int responseCode = conn.getResponseCode();

                if (responseCode == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) response.append(line);
                    br.close();

                    JSONObject json = new JSONObject(response.toString());
                    JSONObject responseJson = json.getJSONObject("response");
                    String email = responseJson.optString("email", null);
                    String name  = responseJson.optString("name", "네이버사용자");

                    runOnUiThread(() -> {
                        // ★ 소셜 고정키 저장
                        if (email != null) {
                            getSharedPreferences("auth", MODE_PRIVATE)
                                    .edit()
                                    .putBoolean("is_social", true)
                                    .putString("provider", "naver")
                                    .putString("social_uid", "naver_" + email)
                                    .putString("social_email", email)
                                    .apply();
                        } else {
                            getSharedPreferences("auth", MODE_PRIVATE)
                                    .edit()
                                    .putBoolean("is_social", true)
                                    .putString("provider", "naver")
                                    .apply();
                        }

                        // (선택) Firestore에도 저장하고 싶으면 사용
                        // saveNaverUserToFirestore(email, name);

                        // ★ Firebase 세션 보장 후 메인 이동
                        ensureFirebaseSessionForSocial(() -> goMainWithSocial(true));
                    });
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "네이버 로그인 실패(" + responseCode + ")", Toast.LENGTH_SHORT).show()
                    );
                }
            } catch (Exception e) {
                Log.e("NaverAPI", "프로필 요청 실패", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "네이버 로그인 실패", Toast.LENGTH_SHORT).show()
                );
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

    /* ========== Kakao ========== */

    private void setupKakaoLogin() {
        ImageButton kakaoLoginBtn = findViewById(R.id.btn_kakao);
        kakaoLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutAllSessions(); // 기존 세션 정리

                if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(LoginActivity.this)) {
                    UserApiClient.getInstance().loginWithKakaoTalk(LoginActivity.this, (OAuthToken token, Throwable error) -> {
                        if (error != null) {
                            Log.e(TAG, "카카오톡 로그인 실패", error);
                            runOnUiThread(() ->
                                    Toast.makeText(LoginActivity.this, "카카오톡 로그인 실패: " + error.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                        } else if (token != null) {
                            Log.i(TAG, "카카오톡 로그인 성공: " + token.getAccessToken());

                            // ★ 사용자 정보 조회 → 고정키 저장
                            UserApiClient.getInstance().me((User user, Throwable err) -> {
                                if (err != null || user == null) {
                                    Toast.makeText(LoginActivity.this, "카카오 사용자 정보 조회 실패", Toast.LENGTH_SHORT).show();
                                    return null;
                                }
                                String kakaoKey = "kakao_" + user.getId();
                                String email = (user.getKakaoAccount() != null) ? user.getKakaoAccount().getEmail() : null;

                                getSharedPreferences("auth", MODE_PRIVATE)
                                        .edit()
                                        .putBoolean("is_social", true)
                                        .putString("provider", "kakao")
                                        .putString("social_uid", kakaoKey)
                                        .putString("social_email", email)
                                        .apply();

                                ensureFirebaseSessionForSocial(() -> goMainWithSocial(true));
                                return null;
                            });
                        }
                        return null;
                    });
                } else {
                    UserApiClient.getInstance().loginWithKakaoAccount(LoginActivity.this, (OAuthToken token, Throwable error) -> {
                        if (error != null) {
                            Log.e(TAG, "카카오계정 로그인 실패", error);
                            runOnUiThread(() ->
                                    Toast.makeText(LoginActivity.this, "카카오계정 로그인 실패: " + error.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                        } else if (token != null) {
                            Log.i(TAG, "카카오계정 로그인 성공: " + token.getAccessToken());

                            // ★ 사용자 정보 조회 → 고정키 저장
                            UserApiClient.getInstance().me((User user, Throwable err) -> {
                                if (err != null || user == null) {
                                    Toast.makeText(LoginActivity.this, "카카오 사용자 정보 조회 실패", Toast.LENGTH_SHORT).show();
                                    return null;
                                }
                                String kakaoKey = "kakao_" + user.getId();
                                String email = (user.getKakaoAccount() != null) ? user.getKakaoAccount().getEmail() : null;

                                getSharedPreferences("auth", MODE_PRIVATE)
                                        .edit()
                                        .putBoolean("is_social", true)
                                        .putString("provider", "kakao")
                                        .putString("social_uid", kakaoKey)
                                        .putString("social_email", email)
                                        .apply();

                                ensureFirebaseSessionForSocial(() -> goMainWithSocial(true));
                                return null;
                            });
                        }
                        return null;
                    });
                }
            }
        });
    }
}