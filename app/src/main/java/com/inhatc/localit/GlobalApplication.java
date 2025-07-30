package com.inhatc.localit;

import android.app.Application;
import com.kakao.sdk.common.KakaoSdk;

public class GlobalApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        KakaoSdk.init(this, "47032f657514090a03eb23e26657b152");//카카오 로그인 초기화
    }
}