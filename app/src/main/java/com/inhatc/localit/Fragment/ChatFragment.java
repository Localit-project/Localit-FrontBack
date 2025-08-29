package com.inhatc.localit.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.inhatc.localit.R;
import com.inhatc.localit.api.GPTApi;
import com.inhatc.localit.api.Message;
import com.inhatc.localit.api.MessageAdapter;
import com.inhatc.localit.databinding.FragmentChatBinding;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    private FragmentChatBinding binding; // ✅ ViewBinding
    private GPTApi gptApi;
    private List<Message> messageList = new ArrayList<>();
    private MessageAdapter adapter;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // ✅ binding 초기화
        binding = FragmentChatBinding.inflate(inflater, container, false);

        // 뒤로가기 버튼 (UI)
        binding.btnBack.setOnClickListener(v -> goHomeSingleTop());

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

        return binding.getRoot(); // ✅ root view 반환
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        gptApi = new GPTApi();

        // ✅ RecyclerView 초기화
        adapter = new MessageAdapter(messageList);
        binding.rvMessages.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMessages.setAdapter(adapter);

        // ✅ 전송 버튼 클릭 이벤트
        binding.btnSend.setOnClickListener(v -> {
            String prompt = binding.etMessage.getText().toString().trim();
            if (!prompt.isEmpty()) {
                showTyping(true);

                // 사용자 메시지 추가
                addMessage("user", prompt);

                // 입력창 초기화
                binding.etMessage.setText("");

                // GPT API 호출
                gptApi.generateLocationRecommendations(prompt, new GPTApi.GPTResponseCallback() {
                    @Override
                    public void onResponse(String result) {
                        requireActivity().runOnUiThread(() -> {
                            showTyping(false);
                            addMessage("bot", result);
                            binding.rvMessages.post(() ->
                                    binding.rvMessages.smoothScrollToPosition(messageList.size() - 1));
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        requireActivity().runOnUiThread(() -> {
                            showTyping(false);
                            addMessage("bot", "오류 발생: " + e.getMessage());
                            binding.rvMessages.post(() ->
                                    binding.rvMessages.smoothScrollToPosition(messageList.size() - 1));
                        });
                    }
                });
            }
        });
    }

    // 🔹 홈으로 이동 메소드
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

    private void showTyping(boolean show) {
        binding.typingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void addMessage(String sender, String message) {
        Message.Sender senderEnum;
        if ("USER".equalsIgnoreCase(sender)) {
            senderEnum = Message.Sender.USER;
        } else {
            senderEnum = Message.Sender.BOT;
        }
        messageList.add(new Message(message, senderEnum));
        adapter.notifyItemInserted(messageList.size() - 1);
        binding.rvMessages.scrollToPosition(messageList.size() - 1);
    }
}
