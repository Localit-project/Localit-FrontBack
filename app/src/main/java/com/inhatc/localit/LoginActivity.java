package com.inhatc.localit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kakao.sdk.user.UserApiClient;
import com.navercorp.nid.NaverIdLoginSDK;
import com.navercorp.nid.oauth.OAuthLoginCallback;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

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

        // Google Sign-In setup
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.googleSignInButton).setOnClickListener(view -> {
            signOutAllSessions();
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        // Naver Sign-In setup
        NaverIdLoginSDK.INSTANCE.initialize(this, "XjKX66OIxZPMPqnulSoy", "BS64ivicIw", "Localit");
        ImageButton btnNaver = findViewById(R.id.btn_naver);
        btnNaver.setOnClickListener(v -> {
            signOutAllSessions();
            NaverIdLoginSDK.INSTANCE.authenticate(LoginActivity.this, new OAuthLoginCallback() {
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
            });
        });

        // Kakao Sign-In setup
        setupKakaoLogin();
    }

    private void signOutAllSessions() {
        try { FirebaseAuth.getInstance().signOut(); } catch (Throwable ignore) {}
        try {
            if (mGoogleSignInClient != null) {
                mGoogleSignInClient.signOut();
            }
        } catch (Throwable ignore) {}
        try { NaverIdLoginSDK.INSTANCE.logout(); } catch (Throwable ignore) {}
        try { UserApiClient.getInstance().logout(throwable -> null); } catch (Throwable ignore) {}
    }

    private void ensureFirebaseSessionForSocial(Runnable onReady) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            onReady.run();
            return;
        }
        FirebaseAuth.getInstance().signInAnonymously()
                .addOnSuccessListener(res -> onReady.run())
                .addOnFailureListener(e -> {
                    String code = (e instanceof FirebaseAuthException) ? ((FirebaseAuthException) e).getErrorCode() : "unknown";
                    Log.e(TAG, "Anonymous sign-in failed, code=" + code, e);
                    Toast.makeText(this, "세션 생성 실패: " + code, Toast.LENGTH_SHORT).show();
                });
    }

    private void goMainWithSocial(boolean isSocial) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("isSocial", isSocial);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

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
                        if (user != null) {
                            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("USER_NAME", user.getDisplayName());
                            if (user.getPhotoUrl() != null) {
                                editor.putString("USER_PROFILE_URL", user.getPhotoUrl().toString());
                            }
                            editor.apply();

                            getSharedPreferences("auth", MODE_PRIVATE)
                                    .edit()
                                    .putBoolean("is_social", true)
                                    .putString("provider", "google")
                                    .putString("social_uid", user.getUid())
                                    .putString("social_email", user.getEmail())
                                    .apply();
                        }
                        Log.d("FirebaseAuth", "Google 로그인 성공");
                        goMainWithSocial(true);
                    } else {
                        handleFirebaseAuthFailure(task.getException());
                    }
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
                            db.collection("users").document(user.getUid())
                                    .get()
                                    .addOnSuccessListener(document -> {
                                        if (document.exists()) {
                                            String userName = document.getString("displayName");
                                            String photoUrl = document.getString("photoUrl");

                                            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                                            SharedPreferences.Editor editor = prefs.edit();
                                            editor.putString("USER_NAME", userName);
                                            editor.putString("USER_PROFILE_URL", photoUrl);
                                            editor.apply();
                                        }
                                        goMainWithSocial(false);
                                    });
                        }
                    } else {
                        handleFirebaseAuthFailure(task.getException());
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
        tvForgotPassword.setOnClickListener(v -> startActivity(new Intent(this, FindIdActivity.class)));
        tvSignUp.setOnClickListener(v -> startActivity(new Intent(this, SignUpActivity.class)));
    }

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
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();

                    JSONObject responseJson = new JSONObject(response.toString()).getJSONObject("response");
                    String email = responseJson.optString("email", null);
                    String name = responseJson.optString("name", "네이버사용자");
                    String profileImage = responseJson.optString("profile_image", null);

                    runOnUiThread(() -> {
                        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("USER_NAME", name);
                        editor.putString("USER_PROFILE_URL", profileImage);
                        editor.apply();

                        if (email != null) {
                            getSharedPreferences("auth", MODE_PRIVATE)
                                    .edit()
                                    .putBoolean("is_social", true)
                                    .putString("provider", "naver")
                                    .putString("social_uid", "naver_" + email)
                                    .putString("social_email", email)
                                    .apply();
                        }
                        ensureFirebaseSessionForSocial(() -> goMainWithSocial(true));
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "네이버 프로필 조회 실패(" + responseCode + ")", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                Log.e("NaverAPI", "프로필 요청 실패", e);
                runOnUiThread(() -> Toast.makeText(this, "네이버 로그인 중 오류 발생", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void setupKakaoLogin() {
        ImageButton kakaoLoginBtn = findViewById(R.id.btn_kakao);
        kakaoLoginBtn.setOnClickListener(v -> {
            signOutAllSessions();

            if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(LoginActivity.this)) {
                UserApiClient.getInstance().loginWithKakaoTalk(LoginActivity.this, (token, error) -> {
                    if (token != null) handleKakaoLoginSuccess();
                    else if (error != null) Log.e(TAG, "카카오톡 로그인 실패", error);
                    return null;
                });
            } else {
                UserApiClient.getInstance().loginWithKakaoAccount(LoginActivity.this, (token, error) -> {
                    if (token != null) handleKakaoLoginSuccess();
                    else if (error != null) Log.e(TAG, "카카오계정 로그인 실패", error);
                    return null;
                });
            }
        });
    }

    private void handleKakaoLoginSuccess() {
        UserApiClient.getInstance().me((user, err) -> {
            if (user != null) {
                String kakaoKey = "kakao_" + user.getId();
                String email = (user.getKakaoAccount() != null) ? user.getKakaoAccount().getEmail() : null;
                String nickname = (user.getKakaoAccount() != null && user.getKakaoAccount().getProfile() != null) ? user.getKakaoAccount().getProfile().getNickname() : "카카오사용자";
                String profileUrl = (user.getKakaoAccount() != null && user.getKakaoAccount().getProfile() != null) ? user.getKakaoAccount().getProfile().getProfileImageUrl() : null;

                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("USER_NAME", nickname);
                editor.putString("USER_PROFILE_URL", profileUrl);
                editor.apply();

                getSharedPreferences("auth", MODE_PRIVATE)
                        .edit()
                        .putBoolean("is_social", true)
                        .putString("provider", "kakao")
                        .putString("social_uid", kakaoKey)
                        .putString("social_email", email)
                        .apply();

                ensureFirebaseSessionForSocial(() -> goMainWithSocial(true));

            } else if (err != null) {
                Toast.makeText(LoginActivity.this, "카카오 사용자 정보 조회 실패", Toast.LENGTH_SHORT).show();
            }
            return null;
        });
    }

    private void handleFirebaseAuthFailure(Exception e) {
        if (e == null) {
            Toast.makeText(this, "알 수 없는 로그인 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            return;
        }
        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            Toast.makeText(this, "이메일 또는 비밀번호가 올바르지 않습니다.", Toast.LENGTH_LONG).show();
        } else if (e instanceof FirebaseAuthUserCollisionException) {
            Toast.makeText(this, "이미 다른 계정으로 가입된 이메일입니다.", Toast.LENGTH_LONG).show();
        } else if (e instanceof FirebaseNetworkException) {
            Toast.makeText(this, "네트워크 오류입니다. 인터넷 연결을 확인하세요.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "로그인에 실패했습니다: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        Log.w(TAG, "FirebaseAuth:failure", e);
    }
}