package com.inhatc.localit.api;

import android.util.Log;

import com.inhatc.localit.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GPTApi {

    // 🔑 OpenAI API Key (gradle.properties → BuildConfig 에서 안전하게 관리)
    private static final String API_KEY = BuildConfig.GPT_API_KEY;
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String TAG = "GPTApi";

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    public interface GPTResponseCallback {
        void onResponse(String result);
        void onFailure(Exception e);
    }

    // ✅ GPT 호출 메서드
    public void generateLocationRecommendations(String prompt, GPTResponseCallback callback) {
        executorService.execute(() -> {
            try {
                // 요청 JSON 생성
                JSONObject requestBody = new JSONObject();
                requestBody.put("model", "gpt-4o-mini");
                requestBody.put("temperature", 0.7); // 창의성 조정 가능

                // ✅ 메시지 배열 생성
                JSONArray messages = new JSONArray();

                // 🔹 System 역할 메시지 추가 (한국 여행 가이드 제한)
                JSONObject systemMessage = new JSONObject();
                systemMessage.put("role", "system");
                systemMessage.put("content",
                        "너는 여행 가이드이며 앱 localit의 챗 봇 '컬이'야.존댓말을 기본으로 사용하고 귀엽고 깍듯한 게 너의 컨셉이야.(직접 컨셉 언급하진 말고)너에 대한 정보를 요구할 땐 간단하게만 답변해" +
                                "오직 한국 여행과 관련된 정보만 제공해." +
                                "지역 축제, 명소, 음식, 여행 코스, 숙박 정보 외에 다른 정보를 요구하면 상황에 맞게 대응해." +
                                "주제에 많이 벗어나는 질문에는 '국내 여행 관련 정보만 제공합니다.'라고 대답해줘." +
                                "능동적으로 제공하는 정보 범위를 확장해서 답변해줘.예를 들어서 인사를 나누거나 날씨에 대해 이야기 하는 등에는 답변해도 돼 "
                        );
                messages.put(systemMessage);

                // 🔹 User 메시지 추가
                JSONObject userMessage = new JSONObject();
                userMessage.put("role", "user");
                userMessage.put("content", prompt);
                messages.put(userMessage);

                requestBody.put("messages", messages);

                // HTTP 연결
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = requestBody.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    String responseString = readStream(conn.getInputStream());
                    String result = parseApiResponse(responseString);
                    callback.onResponse(result);
                } else {
                    String errorResponse = readStream(conn.getErrorStream());
                    Log.e(TAG, "API Response Code: " + responseCode);
                    Log.e(TAG, "API Error: " + errorResponse);
                    callback.onFailure(new IOException("API Error: " + errorResponse));
                }
            } catch (Exception e) {
                Log.e(TAG, "API call failed", e);
                callback.onFailure(e);
            }
        });
    }

    // ✅ OpenAI 응답 파싱
    private String parseApiResponse(String responseString) {
        try {
            Log.d(TAG, "API Raw Response: " + responseString);
            JSONObject jsonObject = new JSONObject(responseString);
            JSONArray choices = jsonObject.getJSONArray("choices");
            if (choices.length() > 0) {
                JSONObject firstChoice = choices.getJSONObject(0);
                JSONObject message = firstChoice.getJSONObject("message");
                return message.getString("content");
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse API response", e);
            return "API 응답 처리 중 오류 발생";
        }
        return "응답을 처리하는 데 실패했습니다.";
    }

    // ✅ InputStream → String 변환
    private String readStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString("UTF-8");
    }
}
