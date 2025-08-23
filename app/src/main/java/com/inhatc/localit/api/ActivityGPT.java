package com.inhatc.localit.api;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.inhatc.localit.R;
import java.util.ArrayList;
import java.util.List;

public class ActivityGPT extends AppCompatActivity {

    private GPTApi gptApi;
    private EditText etMessage;
    private Button btnSend;
    private RecyclerView rvMessages;
    private View typingIndicator;

    private List<Message> messageList = new ArrayList<>();
    private MessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_chat);

        gptApi = new GPTApi();

        // XML 연결
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        rvMessages = findViewById(R.id.rvMessages);
        typingIndicator = findViewById(R.id.typingIndicator);

        // ✅ RecyclerView 초기화
        adapter = new MessageAdapter(messageList);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(adapter);

        // 메시지 전송 버튼 클릭 이벤트
        btnSend.setOnClickListener(v -> {
            String prompt = etMessage.getText().toString().trim();
            if (!prompt.isEmpty()) {
                showTyping(true);

                // ✅ 사용자 메시지 추가
                addMessage("user", prompt);

                // ✅ IME 관련 스팬 제거 후 안전하게 텍스트 초기화
                etMessage.clearComposingText();
                etMessage.setText("", TextView.BufferType.NORMAL);

                // ✅ GPT API 호출
                gptApi.generateLocationRecommendations(prompt, new GPTApi.GPTResponseCallback() {
                    @Override
                    public void onResponse(String result) {
                        runOnUiThread(() -> {
                            showTyping(false);
                            addMessage("bot", result);

                            // ✅ RecyclerView 갱신 + 스크롤
                            rvMessages.post(() -> rvMessages.smoothScrollToPosition(messageList.size() - 1));
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        runOnUiThread(() -> {
                            showTyping(false);
                            addMessage("bot", "오류 발생: " + e.getMessage());

                            // ✅ 스크롤 보장
                            rvMessages.post(() -> rvMessages.smoothScrollToPosition(messageList.size() - 1));
                        });
                    }
                });
            }
        });
    }

    private void showTyping(boolean show) {
        typingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
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
        rvMessages.scrollToPosition(messageList.size() - 1);
    }
}