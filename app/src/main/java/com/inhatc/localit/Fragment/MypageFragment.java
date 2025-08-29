package com.inhatc.localit.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.inhatc.localit.LoginActivity;
import com.inhatc.localit.MainActivity;
import com.inhatc.localit.R;
import com.inhatc.localit.databinding.FragmentMypageBinding;
import com.kakao.sdk.user.UserApiClient;
import com.navercorp.nid.NaverIdLoginSDK;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class MypageFragment extends Fragment {

    private FragmentMypageBinding binding;

    private ActivityResultLauncher<String>  galleryLauncher;
    private ActivityResultLauncher<Uri>     cameraLauncher;
    private ActivityResultLauncher<String[]> permissionLauncher;
    private ImageButton btnBack;

    private enum Pending { NONE, GALLERY, CAMERA }
    private Pending pending = Pending.NONE;

    private Uri cameraPhotoUri;
    private boolean isValidNickname(@NonNull String s) {
        // 영문/숫자/한글만 허용, 2~10자
        return s.matches("^[A-Za-z0-9가-힣]{2,10}$");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMypageBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initActivityResultLaunchers();

        // 뒤로가기 버튼 (UI)
        binding.btnBack.setOnClickListener(v -> goHomeSingleTop());
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> goHomeSingleTop());
        }

        // 물리/소프트 뒤로가기 키 처리
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        goHomeSingleTop();
                    }
                }
        );

        // 알림 화면
        binding.btnNotification.setOnClickListener(v ->
                ((MainActivity) requireActivity()).openNotifications()
        );

        // 알림 설정
        binding.rowAlarm.setOnClickListener(v ->
                ((MainActivity) requireActivity()).openAlarmSettings()
        );

        // 프로필 사진 추가(갤러리/카메라)
        binding.btnAddPhoto.setOnClickListener(v -> showImagePickDialog());

        // 닉네임 저장: 버튼 클릭
        binding.btnSaveNickname.setOnClickListener(v -> {
            String nick = binding.etNickname.getText().toString().trim();
            if (!isValidNickname(nick)) {
                Toast.makeText(requireContext(),
                        "닉네임은 2~10자, 공백/특수기호/이모지 불가", Toast.LENGTH_SHORT).show();
                return;
            }
            saveDisplayName(nick);
        });

        // 로그아웃
        binding.rowLogout.setOnClickListener(v -> confirmLogout());

        // 닉네임 저장: 키보드 완료
        binding.etNickname.setOnEditorActionListener((tv, actionId, event) -> {
            boolean done = (actionId == EditorInfo.IME_ACTION_DONE) ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            if (done) {
                saveDisplayName(binding.etNickname.getText().toString().trim());
                return true;
            }
            return false;
        });

        // 유저 프로필 로딩
        loadUserProfile();

        return root;
    }

    /** 홈으로 이동(중복 쌓임 방지) */
    private void goHomeSingleTop() {
        try {
            NavController nav = NavHostFragment.findNavController(this);
            int homeId = nav.getGraph().getStartDestinationId();

            NavOptions opts = new NavOptions.Builder()
                    .setPopUpTo(homeId, false)
                    .setLaunchSingleTop(true)
                    .build();

            if (nav.getCurrentDestination() == null ||
                    nav.getCurrentDestination().getId() != homeId) {
                nav.navigate(homeId, null, opts);
            }
        } catch (Exception ignored) { }

        BottomNavigationView bottom = requireActivity().findViewById(R.id.nav_view);
        if (bottom != null) {
            bottom.setSelectedItemId(R.id.navigation_home);
        }
    }

    /* ───────────── 공통: 현재 프로필 문서키 결정 ─────────────
       - 일반/구글(실제 Firebase 로그인)  : currentUser.getUid()
       - 네이버/카카오(익명세션 사용 가능): prefs("auth").social_uid  (예: naver_email, kakao_12345)
    */
    private String getProfileDocId() {
        FirebaseUser fb = FirebaseAuth.getInstance().getCurrentUser();
        if (fb != null && !fb.isAnonymous()) {
            return fb.getUid();
        }
        // 익명/소셜일 수 있으므로 prefs에서 키 사용
        return requireContext()
                .getSharedPreferences("auth", Context.MODE_PRIVATE)
                .getString("social_uid", null);
    }

    private String getProfileEmailFallback() {
        // Firestore 초기 생성 시 email 채우는 용도(없어도 동작은 함)
        String email = requireContext()
                .getSharedPreferences("auth", Context.MODE_PRIVATE)
                .getString("social_email", null);
        if (email != null) return email;

        FirebaseUser fb = FirebaseAuth.getInstance().getCurrentUser();
        return (fb != null) ? fb.getEmail() : null;
    }

    /* ───────────── 프로필 로딩/저장 ───────────── */

    private void loadUserProfile() {
        String docId = getProfileDocId();
        if (docId == null) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            goLoginAndFinish();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users").document(docId)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.exists()) {
                        upsertUserProfileToFirestore(docId);
                        return;
                    }

                    String displayName = snap.getString("displayName");
                    String photoUrl = snap.getString("photoUrl");

                    if (displayName != null && !displayName.isEmpty()) {
                        binding.etNickname.setText(displayName);
                    }

                    if (photoUrl != null && !photoUrl.isEmpty()) {
                        Glide.with(this).load(photoUrl).into(binding.imgAvatar);
                    } else {
                        // FirebaseUser의 사진도 참고(있으면)
                        FirebaseUser authUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (authUser != null && authUser.getPhotoUrl() != null) {
                            Glide.with(this).load(authUser.getPhotoUrl()).into(binding.imgAvatar);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "프로필 불러오기 실패", Toast.LENGTH_SHORT).show());
    }

    private void upsertUserProfileToFirestore(@NonNull String docId) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("uid", docId);
        doc.put("email", getProfileEmailFallback());
        doc.put("displayName", binding.etNickname.getText() != null ? binding.etNickname.getText().toString() : null);
        doc.put("photoUrl", null);
        doc.put("updatedAt", FieldValue.serverTimestamp());
        doc.put("createdAt", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(docId)
                .set(doc, SetOptions.merge())
                .addOnSuccessListener(v -> {
                    if (doc.get("displayName") != null) {
                        binding.etNickname.setText(String.valueOf(doc.get("displayName")));
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "프로필 초기화 실패", Toast.LENGTH_SHORT).show());
    }

    private void saveDisplayName(@NonNull String name) {
        String docId = getProfileDocId();
        if (docId == null) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> patch = new HashMap<>();
        patch.put("displayName", name);
        patch.put("updatedAt", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
                .collection("users").document(docId)
                .set(patch, SetOptions.merge())
                .addOnSuccessListener(v -> {
                    // FirebaseUser 프로필은 실계정일 때만 갱신(익명은 생략)
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null && !user.isAnonymous()) {
                        UserProfileChangeRequest req = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();
                        user.updateProfile(req);
                    }
                    Toast.makeText(requireContext(), "닉네임이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "닉네임 저장 실패", Toast.LENGTH_SHORT).show());
    }

    private StorageReference getUserAvatarRef(@NonNull String docId) {
        return FirebaseStorage.getInstance().getReference()
                .child("avatars/" + docId + "/avatar.jpg");
    }

    private void uploadAvatarAndSave(Uri localUri) {
        String docId = getProfileDocId();
        if (docId == null || localUri == null) return;

        StorageReference ref = getUserAvatarRef(docId);
        ref.putFile(localUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    return ref.getDownloadUrl();
                })
                .addOnSuccessListener(downloadUri -> {
                    String photoUrl = downloadUri.toString();

                    Map<String, Object> patch = new HashMap<>();
                    patch.put("photoUrl", photoUrl);
                    patch.put("updatedAt", FieldValue.serverTimestamp());

                    FirebaseFirestore.getInstance()
                            .collection("users").document(docId)
                            .set(patch, SetOptions.merge());

                    // FirebaseUser의 photoUrl은 실계정만 갱신(익명은 생략 가능)
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null && !user.isAnonymous()) {
                        UserProfileChangeRequest req = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(downloadUri)
                                .build();
                        user.updateProfile(req);
                    }

                    Glide.with(this).load(photoUrl).into(binding.imgAvatar);
                    Toast.makeText(requireContext(), "프로필 사진이 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(requireContext(), "사진 업로드 실패", Toast.LENGTH_SHORT).show();
                });
    }

    /* ───────────── 이미지 선택/권한/카메라 ───────────── */

    private void initActivityResultLaunchers() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        binding.imgAvatar.setImageURI(uri);
                        uploadAvatarAndSave(uri);
                    }
                });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && cameraPhotoUri != null) {
                        binding.imgAvatar.setImageURI(cameraPhotoUri);
                        uploadAvatarAndSave(cameraPhotoUri);
                    }
                });

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean grantedAll = true;
                    for (Boolean ok : result.values()) grantedAll &= (ok != null && ok);
                    if (!grantedAll) {
                        Toast.makeText(requireContext(), getString(R.string.permission_needed), Toast.LENGTH_SHORT).show();
                        pending = Pending.NONE; return;
                    }
                    if (pending == Pending.GALLERY) openGallery();
                    else if (pending == Pending.CAMERA) openCamera();
                    pending = Pending.NONE;
                });
    }

    private void showImagePickDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.choose_profile_photo))
                .setItems(new CharSequence[]{ getString(R.string.pick_from_gallery), getString(R.string.take_with_camera) },
                        (dialog, which) -> {
                            if (which == 0) {
                                if (hasGalleryPermission()) openGallery();
                                else { pending = Pending.GALLERY; requestGalleryPermission(); }
                            } else {
                                if (hasCameraPermission()) openCamera();
                                else { pending = Pending.CAMERA; requestCameraPermission(); }
                            }
                        })
                .show();
    }

    private boolean hasGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(new String[]{ Manifest.permission.READ_MEDIA_IMAGES });
        } else {
            permissionLauncher.launch(new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE });
        }
    }

    private void openGallery() { galleryLauncher.launch("image/*"); }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        permissionLauncher.launch(new String[]{ Manifest.permission.CAMERA });
    }

    private void openCamera() {
        try {
            cameraPhotoUri = createImageUri();
            if (cameraPhotoUri != null) cameraLauncher.launch(cameraPhotoUri);
            else Toast.makeText(requireContext(), "사진 파일을 만들 수 없습니다.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "사진 파일 생성 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private Uri createImageUri() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new java.util.Date());
        String fileName = "PROFILE_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(fileName, ".jpg", storageDir);
        String authority = requireContext().getPackageName() + ".fileprovider";
        return FileProvider.getUriForFile(requireContext(), authority, image);
    }

    /* ───────────── 로그아웃 ───────────── */

    private void confirmLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("로그아웃")
                .setMessage("정말 로그아웃 하시겠어요?")
                .setPositiveButton("로그아웃", (d, w) -> performLogout())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void performLogout() {
        // Firebase 세션 종료
        try { FirebaseAuth.getInstance().signOut(); } catch (Throwable ignore) {}

        // Google 세션 종료
        try {
            GoogleSignInClient gsc = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(
                    requireContext(),
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            );
            gsc.signOut();
        } catch (Throwable ignore) {}

        // Naver/Kakao 세션 종료
        try { NaverIdLoginSDK.INSTANCE.logout(); } catch (Throwable ignore) {}
        try {
            UserApiClient.getInstance().logout(new Function1<Throwable, Unit>() {
                @Override public Unit invoke(Throwable throwable) { return Unit.INSTANCE; }
            });
        } catch (Throwable ignore) {}

        // 로컬 세션 정리
        requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
                .edit().clear().apply();

        Toast.makeText(requireContext(), "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
        goLoginAndFinish();
    }

    private void goLoginAndFinish() {
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}