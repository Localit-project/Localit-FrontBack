package com.inhatc.localit.api;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.inhatc.localit.R;
import com.inhatc.localit.api.GeminiApi;

public class ActivityGemini extends AppCompatActivity {

    private GeminiApi geminiApi;
    private EditText editPrompt;
    private Button btnRequest;
    private TextView textResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gemini);

        geminiApi = new GeminiApi();
        editPrompt = findViewById(R.id.edit_prompt);
        btnRequest = findViewById(R.id.btn_request);
        textResult = findViewById(R.id.text_result);

        btnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String prompt = editPrompt.getText().toString();
                if (!prompt.isEmpty()) {
                    // API 호출
                    geminiApi.generateLocationRecommendations(prompt, new GeminiApi.GeminiResponseCallback() {
                        @Override
                        public void onResponse(String result) {
                            runOnUiThread(() -> textResult.setText(result));
                        }

                        @Override
                        public void onFailure(Exception e) {
                            runOnUiThread(() -> textResult.setText("오류 발생: " + e.getMessage()));
                        }
                    });
                }
            }
        });
    }
}
