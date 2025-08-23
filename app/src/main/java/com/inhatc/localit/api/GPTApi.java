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

                JSONArray messages = new JSONArray();
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