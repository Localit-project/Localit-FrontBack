package com.inhatc.localit;

import static java.lang.System.getProperties;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.navercorp.nid.NaverIdLoginSDK;
import com.navercorp.nid.oauth.OAuthLoginCallback;
import android.view.View;

import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_KAKAO_SIGN_IN = 64206;
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

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.googleSignInButton).setOnClickListener(view -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        NaverIdLoginSDK.INSTANCE.initialize(this, "XjKX66OIxZPMPqnulSoy", "BS64ivicIw", "Localit");

        ImageButton btnNaver = findViewById(R.id.btn_naver);
        btnNaver.setOnClickListener(v -> {
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

        setupKakaoLogin();
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
                                                    Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                    finish();
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

                    JSONObject json = new JSONObject(response.toString());
                    JSONObject responseJson = json.getJSONObject("response");
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

    private static final String TAG = "KAKAO_LOGIN";

    private void setupKakaoLogin() {
        ImageButton kakaoLoginBtn = findViewById(R.id.btn_kakao);
        kakaoLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(LoginActivity.this)) {
                    UserApiClient.getInstance().loginWithKakaoTalk(LoginActivity.this, new Function2<OAuthToken, Throwable, Unit>() {
                        @Override
                        public Unit invoke(OAuthToken token, Throwable error) {
                            if (error != null) {
                                Log.e(TAG, "카카오톡 로그인 실패", error);
                                runOnUiThread(() ->
                                        Toast.makeText(LoginActivity.this, "카카오톡 로그인 실패: " + error.getMessage(), Toast.LENGTH_SHORT).show()
                                );
                            } else if (token != null) {
                                Log.i(TAG, "카카오톡 로그인 성공: " + token.getAccessToken());
                                runOnUiThread(() ->
                                        Toast.makeText(LoginActivity.this, "카카오톡 로그인 성공", Toast.LENGTH_SHORT).show()
                                );
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            return null;
                        }
                    });
                } else {
                    UserApiClient.getInstance().loginWithKakaoAccount(LoginActivity.this, new Function2<OAuthToken, Throwable, Unit>() {
                        @Override
                        public Unit invoke(OAuthToken token, Throwable error) {
                            if (error != null) {
                                Log.e(TAG, "카카오계정 로그인 실패", error);
                                runOnUiThread(() ->
                                        Toast.makeText(LoginActivity.this, "카카오계정 로그인 실패: " + error.getMessage(), Toast.LENGTH_SHORT).show()
                                );
                            } else if (token != null) {
                                Log.i(TAG, "카카오계정 로그인 성공: " + token.getAccessToken());
                                runOnUiThread(() ->
                                        Toast.makeText(LoginActivity.this, "카카오계정 로그인 성공", Toast.LENGTH_SHORT).show()
                                );
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            return null;
                        }
                    });
                }
            }
        });
    }
}