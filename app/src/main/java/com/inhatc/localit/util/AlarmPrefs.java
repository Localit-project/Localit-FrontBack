package com.inhatc.localit.util;

import android.content.Context;
import android.content.SharedPreferences;

public final class AlarmPrefs {

    private static final String PREF_NAME = "alarm_prefs";

    //  토글 키 (알림 설정 화면과 알림 목록 화면에서 공통 사용)
    public static final String KEY_WISH_FESTIVAL   = "wish_festival";
    public static final String KEY_WISH_SPOT       = "wish_spot";
    public static final String KEY_WISH_NEWS       = "wish_news";

    public static final String KEY_REGION_FESTIVAL = "region_festival";
    public static final String KEY_REGION_SPOT     = "region_spot";
    public static final String KEY_REGION_NEWS     = "region_news";

    private AlarmPrefs() {}

    private static SharedPreferences sp(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /** 기본값 포함 boolean 읽기 */
    public static boolean get(Context ctx, String key, boolean defValue) {
        return sp(ctx).getBoolean(key, defValue);
    }

    /** boolean 저장 */
    public static void put(Context ctx, String key, boolean value) {
        sp(ctx).edit().putBoolean(key, value).apply();
    }

    // ========= 편의 메서드들 (NotificationsFragment에서 사용) =========

    /** 기본값 true로 읽기 (설정이 없으면 on) */
    public static boolean isEnabled(Context ctx, String key) {
        return get(ctx, key, true);
    }

    /** 축제/행사 알림 허용 여부 (찜목록 or 관심지역 중 하나라도 켜져 있으면 true) */
    public static boolean allowFestival(Context ctx) {
        return isEnabled(ctx, KEY_WISH_FESTIVAL) || isEnabled(ctx, KEY_REGION_FESTIVAL);
    }

    /** 관광지 알림 허용 여부 */
    public static boolean allowSpot(Context ctx) {
        return isEnabled(ctx, KEY_WISH_SPOT) || isEnabled(ctx, KEY_REGION_SPOT);
    }

    /** 뉴스 알림 허용 여부 */
    public static boolean allowNews(Context ctx) {
        return isEnabled(ctx, KEY_WISH_NEWS) || isEnabled(ctx, KEY_REGION_NEWS);
    }
}
