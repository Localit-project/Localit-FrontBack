package com.inhatc.localit.api;

import android.util.Log;
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

public class GeminiApi {

    private static final String API_KEY = "AIzaSyBY3dx-lTq1VBxOQhWOSEqyamkI9zxKLcw"; // 실제 키로 교체
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-preview-05-20:generateContent?key=" + API_KEY;
    private static final String TAG = "GeminiApi";

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    public interface GeminiResponseCallback {
        void onResponse(String result);
        void onFailure(Exception e);
    }

    public void generateLocationRecommendations(String prompt, GeminiResponseCallback callback) {
        executorService.execute(() -> {
            try {
                // 요청 JSON 만들기
                JSONObject requestBody = new JSONObject();
                JSONArray contents = new JSONArray();
                JSONObject content = new JSONObject();
                content.put("role", "user");
                JSONArray parts = new JSONArray();
                JSONObject part = new JSONObject();
                part.put("text", prompt);
                parts.put(part);
                content.put("parts", parts);
                contents.put(content);
                requestBody.put("contents", contents);

                // HTTP 연결 설정
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
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

    private String parseApiResponse(String responseString) {
        try {
            JSONObject jsonObject = new JSONObject(responseString);
            JSONArray candidates = jsonObject.getJSONArray("candidates");
            if (candidates.length() > 0) {
                JSONObject firstCandidate = candidates.getJSONObject(0);
                JSONObject content = firstCandidate.getJSONObject("content");
                JSONArray parts = content.getJSONArray("parts");
                if (parts.length() > 0) {
                    return parts.getJSONObject(0).getString("text");
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse API response", e);
        }
        return "응답을 처리하는 데 실패했습니다.";
    }

    /**
     * ✅ InputStream에서 모든 바이트를 읽는 호환성 있는 유틸리티 메서드
     * @param inputStream 읽을 InputStream
     * @return InputStream의 모든 내용을 담은 문자열
     * @throws IOException
     */
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
