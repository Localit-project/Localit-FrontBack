package com.inhatc.localit.Fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedDispatcherOwner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.inhatc.localit.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * ChatFragment
 * - fragment_chat.xml과 메시지 아이템 레이아웃(item_message_user.xml, item_message_bot.xml) 사용
 * - OpenAI API 샘플 연동 (비동기). 실제 키는 BuildConfig 등 안전한 경로로 주입하세요.
 */
public class ChatFragment extends Fragment {

    // ====== OpenAI 설정 ======
    // ★ 실제 프로젝트에서는 local.properties -> buildConfigField 로 주입해 사용 필요
    //   예) build.gradle: buildConfigField "String","OPENAI_API_KEY","\"${localProps.getProperty("OPENAI_API_KEY","")}\""
   // private static final String OPENAI_API_KEY = BuildConfig.OPENAI_API_KEY; // 없으면 하드코드 금지
    private static final String OPENAI_ENDPOINT = "https://api.openai.com/v1/chat/completions";
    private static final String OPENAI_MODEL = "gpt-4o-mini"; // 경량 모델 예시

    private RecyclerView rvMessages;
    private MessagesAdapter adapter;
    private TextInputEditText etMessage;
    private ImageButton btnSend, btnAttach;
    private LinearLayout typingIndicator;
    private ChipGroup chipGroupSuggestions;
    private Toolbar toolbar;

    private final OkHttpClient http = new OkHttpClient();

    // ====== Fragment ======
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        toolbar = v.findViewById(R.id.toolbar);
        rvMessages = v.findViewById(R.id.rvMessages);
        etMessage = v.findViewById(R.id.etMessage);
        btnSend = v.findViewById(R.id.btnSend);
        btnAttach = v.findViewById(R.id.btnAttach);
        typingIndicator = v.findViewById(R.id.typingIndicator);
       // chipGroupSuggestions = v.findViewById(R.id.chipGroupSuggestions);

        // Toolbar back
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(view -> {
                if (requireActivity() instanceof OnBackPressedDispatcherOwner) {
                   // requireActivity().onBackPressedDispatcher().onBackPressed();
                } else {
                    requireActivity().onBackPressed();
                }
            });
        }

        // RecyclerView
        LinearLayoutManager lm = new LinearLayoutManager(requireContext());
        lm.setStackFromEnd(true); // 최신 메시지가 아래
        rvMessages.setLayoutManager(lm);
        adapter = new MessagesAdapter();
        rvMessages.setAdapter(adapter);

        // 초기 웰컴 메시지
        adapter.append(new Message(Role.BOT, "안녕하세요! 무엇을 도와드릴까요?"));

        // 전송 버튼
        btnSend.setOnClickListener(v1 -> submitMessage());

        // 키보드 전송
        etMessage.setOnEditorActionListener((tv, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                            event.getAction() == KeyEvent.ACTION_DOWN)) {
                submitMessage();
                return true;
            }
            return false;
        });

        // 첨부(선택): 파일 선택 로직 연결 지점
        btnAttach.setOnClickListener(v12 -> {
            // TODO: 파일 피커/이미지 선택 등 필요 시 구현
        });

        // 추천 칩 클릭 시 프롬프트 자동 입력
        setupSuggestionChips();
    }

    private void setupSuggestionChips() {
        if (chipGroupSuggestions == null) return;
        for (int i = 0; i < chipGroupSuggestions.getChildCount(); i++) {
            View child = chipGroupSuggestions.getChildAt(i);
            if (child instanceof Chip) {
                child.setOnClickListener(v -> {
                    CharSequence t = ((Chip) v).getText();
                    if (t != null) {
                        etMessage.setText(t);
                        etMessage.setSelection(t.length());
                    }
                });
            }
        }
    }

    // ====== 메시지 전송 ======
    private void submitMessage() {
        String text = Objects.toString(etMessage.getText(), "").trim();
        if (TextUtils.isEmpty(text)) return;

        // 1) UI에 사용자 메시지 추가
        adapter.append(new Message(Role.USER, text));
        scrollToBottom();

        // 2) 입력창 비우기
        etMessage.setText("");

        // 3) GPT 호출
        callOpenAi(text);
    }

    private void showTyping(boolean show) {
        if (typingIndicator != null) {
            typingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void scrollToBottom() {
        rvMessages.post(() -> rvMessages.smoothScrollToPosition(Math.max(0, adapter.getItemCount() - 1)));
    }

    // ====== OpenAI 호출 ======
    private void callOpenAi(String userContent) {
        showTyping(true);

        try {
            JSONObject payload = new JSONObject();
            payload.put("model", OPENAI_MODEL);

            JSONArray messages = new JSONArray();
            messages.put(new JSONObject()
                    .put("role", "system")
                    .put("content", "You are a helpful assistant for an Android app chat UI."));
            messages.put(new JSONObject()
                    .put("role", "user")
                    .put("content", userContent));
            payload.put("messages", messages);

            // temperature 등 옵션 필요 시 추가
            payload.put("temperature", 0.7);

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(payload.toString().getBytes(StandardCharsets.UTF_8), JSON);

            Request req = new Request.Builder()
                    .url(OPENAI_ENDPOINT)
                    //.addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            http.newCall(req).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    postBot("네트워크 오류가 발생했어요: " + e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String botText;
                    if (!response.isSuccessful()) {
                        botText = "API 오류: " + response.code() + " " + response.message();
                    } else {
                        String resBody = Objects.requireNonNull(response.body()).string();
                        botText = parseOpenAiReply(resBody);
                    }
                    postBot(botText);
                }
            });
        } catch (JSONException e) {
            postBot("요청 생성 중 오류가 발생했어요: " + e.getMessage());
        }
    }

    private void postBot(String text) {
        requireActivity().runOnUiThread(() -> {
            showTyping(false);
            adapter.append(new Message(Role.BOT, TextUtils.isEmpty(text) ? "(empty)" : text));
            scrollToBottom();
        });
    }

    /** Chat Completions 응답에서 assistant 메시지 추출 */
    private String parseOpenAiReply(String json) {
        try {
            JSONObject root = new JSONObject(json);
            JSONArray choices = root.optJSONArray("choices");
            if (choices != null && choices.length() > 0) {
                JSONObject first = choices.getJSONObject(0);
                JSONObject message = first.optJSONObject("message");
                if (message != null) {
                    return message.optString("content", "(no content)");
                }
            }
            return "(no choices)";
        } catch (JSONException e) {
            return "응답 파싱 오류: " + e.getMessage();
        }
    }

    // ====== 데이터/어댑터 ======

    enum Role { USER, BOT }

    static class Message {
        final Role role;
        final String text;
        Message(Role role, String text) {
            this.role = role;
            this.text = text;
        }
    }

    static class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int VT_USER = 1;
        private static final int VT_BOT = 2;

        private final List<Message> items = new ArrayList<>();

        void append(Message m) {
            int oldSize = items.size();
            items.add(m);
            notifyItemInserted(oldSize);
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position).role == Role.USER ? VT_USER : VT_BOT;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inf = LayoutInflater.from(parent.getContext());
            if (viewType == VT_USER) {
                View v = inf.inflate(R.layout.item_message_user, parent, false);
                return new UserVH(v);
            } else {
                View v = inf.inflate(R.layout.item_message_bot, parent, false);
                return new BotVH(v);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Message m = items.get(position);
            if (holder instanceof UserVH) {
                ((UserVH) holder).bind(m.text);
            } else if (holder instanceof BotVH) {
                ((BotVH) holder).bind(m.text);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class UserVH extends RecyclerView.ViewHolder {
            final TextView tv;
            UserVH(@NonNull View itemView) {
                super(itemView);
                tv = itemView.findViewById(R.id.tvMessage);
            }
            void bind(String t) { tv.setText(t); }
        }

        static class BotVH extends RecyclerView.ViewHolder {
            final TextView tv;
         //   final ImageView avatar;
            BotVH(@NonNull View itemView) {
                super(itemView);
                tv = itemView.findViewById(R.id.tvMessage);
             //   avatar = itemView.findViewById(R.id.ivAvatar); // 없으면 null, 선택사항
            }
            void bind(String t) { tv.setText(t); }
        }
    }
}
