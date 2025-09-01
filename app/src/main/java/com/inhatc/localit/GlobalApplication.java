package com.inhatc.localit;

import android.app.Application;

import com.inhatc.localit.Fragment.NotiHelper;
import com.inhatc.localit.api.home.TourApiHelper;
import com.kakao.sdk.common.KakaoSdk;

public class GlobalApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        KakaoSdk.init(this, "47032f657514090a03eb23e26657b152");//카카오 로그인 초기화
        NotiHelper.ensureChannels(this); // 알림
        com.inhatc.localit.api.home.TourApiHelper.AppCtx.init(getApplicationContext());
        TourApiHelper.AppCtx.init(getApplicationContext());


    }
}
