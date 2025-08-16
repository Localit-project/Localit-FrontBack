package com.inhatc.localit.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.inhatc.localit.LoginActivity;
import com.inhatc.localit.MainActivity;
import com.inhatc.localit.R;
import com.inhatc.localit.databinding.FragmentMypageBinding;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// 소셜 로그아웃을 같이 하려면 아래 import 들도 사용
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.navercorp.nid.NaverIdLoginSDK;
import com.kakao.sdk.user.UserApiClient;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class MypageFragment extends Fragment {

    private FragmentMypageBinding binding;

    private ActivityResultLauncher<String>  galleryLauncher;
    private ActivityResultLauncher<Uri>     cameraLauncher;
    private ActivityResultLauncher<String[]> permissionLauncher;

    private enum Pending { NONE, GALLERY, CAMERA }
    private Pending pending = Pending.NONE;

    private Uri cameraPhotoUri;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMypageBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initActivityResultLaunchers();

        // 프로필 사진 추가
        binding.btnAddPhoto.setOnClickListener(v -> showImagePickDialog());

        // 종 아이콘 → 알림 화면
        binding.btnNotification.setOnClickListener(v ->
                ((MainActivity) requireActivity()).openNotifications()
        );

        // 알림 설정 화면
        binding.rowAlarm.setOnClickListener(v ->
                ((MainActivity) requireActivity()).openAlarmSettings()
        );

        // 로그아웃
        binding.rowLogout.setOnClickListener(v -> confirmLogout());   // ← 추가

        return root;
    }

    // 로그아웃 확인 다이얼로그
    private void confirmLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("로그아웃")
                .setMessage("정말 로그아웃 하시겠어요?")
                .setPositiveButton("로그아웃", (d, w) -> performLogout())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    // 실제 로그아웃 처리
    private void performLogout() {
        // (1) 소셜 계정들 로그아웃 (있는 것만 시도; 없으면 그냥 넘어가요)
        try {
            GoogleSignInClient gsc = GoogleSignIn.getClient(
                    requireContext(),
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            );
            gsc.signOut();
        } catch (Throwable ignore) {}

        try { NaverIdLoginSDK.INSTANCE.logout(); } catch (Throwable ignore) {}

        try {
            UserApiClient.getInstance().logout(new Function1<Throwable, Unit>() {
                @Override public Unit invoke(Throwable throwable) { return Unit.INSTANCE; }
            });
        } catch (Throwable ignore) {}

        // (2) 앱 로컬 세션/토큰 등 정리 (사용 중인 prefs 이름이 있다면 거기에 맞춰 바꿔주세요)
        requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
                .edit().clear().apply();

        // (3) 로그인 화면으로 이동 + 백스택 완전 제거
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();

        Toast.makeText(requireContext(), "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
    }

    /* ========= 이하 기존 코드 그대로 ========= */

    private void initActivityResultLaunchers() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> { if (uri != null) binding.imgAvatar.setImageURI(uri); });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> { if (success && cameraPhotoUri != null) binding.imgAvatar.setImageURI(cameraPhotoUri); });

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
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "PROFILE_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(fileName, ".jpg", storageDir);
        String authority = requireContext().getPackageName() + ".fileprovider";
        return FileProvider.getUriForFile(requireContext(), authority, image);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
