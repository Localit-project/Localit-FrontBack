package com.inhatc.localit.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.inhatc.localit.R;

/**
 * ChatFragment

 * 1) 뷰 바인딩 또는 findViewById로 필요한 뷰 참조
 * 2) RecyclerView, Adapter 구현
 * 3) 전송 버튼/IME 처리
 * 4) API 연동 필요
 */
public class ChatFragment extends Fragment {
    private ImageView btnBack;
    private ImageButton btnNotification;
    private TextView badgeNew;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        // fragment_chat.xml만 inflate
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        // 예) RecyclerView 설정, 버튼 리스너, ViewModel 연결 등 필요
        // RecyclerView rv = view.findViewById(R.id.rvMessages);
        // Button btnSend = view.findViewById(R.id.btnSend);
        // TextInputEditText etMessage = view.findViewById(R.id.etMessage);
    }
}
