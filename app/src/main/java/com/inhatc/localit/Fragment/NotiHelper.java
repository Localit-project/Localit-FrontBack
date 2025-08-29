package com.inhatc.localit.Fragment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.inhatc.localit.R;
import com.inhatc.localit.util.AlarmPrefs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** 시스템 알림 유틸: 채널 생성 + InboxStyle 4줄 구성 */
public final class NotiHelper {
    private NotiHelper() {}

    // 채널 ID
    public static final String CH_WISH = "wish_channel";
    public static final String CH_FESTIVAL = "festival_channel";

    private static final SimpleDateFormat DF = new SimpleDateFormat("yyyy.MM.dd", Locale.KOREA);

    /** 앱 어디서든 한 번만 호출되면 됨 (알림 발송 전 보장) */
    public static void ensureChannels(@NonNull Context ctx) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;

        NotificationChannel wish = new NotificationChannel(
                CH_WISH, "찜 알림", NotificationManager.IMPORTANCE_DEFAULT);
        wish.setDescription("찜 목록 관련 알림");

        NotificationChannel fest = new NotificationChannel(
                CH_FESTIVAL, "축제·행사 알림", NotificationManager.IMPORTANCE_DEFAULT);
        fest.setDescription("축제 시작/종료 전 안내");

        nm.createNotificationChannel(wish);
        nm.createNotificationChannel(fest);
    }

    private static String fmt(@Nullable Date d) {
        if (d == null) return "";
        return DF.format(d);
    }

    /** 예: "찜 목록 (축제·행사)" */
    private static String badge(@NonNull NotificationsFragment.Source s,
                                @NonNull NotificationsFragment.Type t) {
        String left;
        switch (s) {
            case WISH: left = "찜 목록"; break;
            case FESTIVAL: left = "축제 알림"; break;
            case SPOT_NEW: left = "신규 관광지"; break;
            default: left = "";
        }
        String right = (t == NotificationsFragment.Type.FESTIVAL) ? "축제·행사" : "관광지";
        return left + " (" + right + ")";
    }

    /** 예: "기간 : 2025.08.22 ~ 2025.08.30" (없으면 빈 문자열) */
    private static String periodLine(@Nullable Date s, @Nullable Date e) {
        if (s == null || e == null) return "";
        return "기간 : " + fmt(s) + " ~ " + fmt(e);
    }

    /**
     * 시스템 알림 표시 (InboxStyle 4줄)
     * 줄1: 뱃지(찜 목록/축제·행사)  줄2: 타이틀  줄3: 기간  줄4: 설명
     */
    public static void showSystemNotification(
            @NonNull Context ctx,
            long notiId,
            @NonNull NotificationsFragment.Source source,
            @NonNull NotificationsFragment.Type type,
            @NonNull String title,
            @Nullable Date startDate,
            @Nullable Date endDate,
            @NonNull String description,
            @Nullable PendingIntent contentIntent
    ) {
        // 🔒 스위치 상태에 따라 알림 차단
        if (source == NotificationsFragment.Source.WISH && type == NotificationsFragment.Type.FESTIVAL) {
            if (!AlarmPrefs.isEnabled(ctx, AlarmPrefs.KEY_WISH_FESTIVAL)) return;
        } else if (source == NotificationsFragment.Source.WISH && type == NotificationsFragment.Type.SPOT) {
            if (!AlarmPrefs.isEnabled(ctx, AlarmPrefs.KEY_WISH_SPOT)) return;
        } else if (source == NotificationsFragment.Source.FESTIVAL) {
            if (!AlarmPrefs.allowFestival(ctx)) return;
        } else if (source == NotificationsFragment.Source.SPOT_NEW) {
            if (!AlarmPrefs.allowSpot(ctx)) return;
        }

        ensureChannels(ctx);

        String channelId = (source == NotificationsFragment.Source.FESTIVAL) ? CH_FESTIVAL : CH_WISH;

        String line1 = badge(source, type);
        String line2 = title != null ? title : "";
        String line3 = periodLine(startDate, endDate);
        String line4 = description != null ? description : "";

        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle()
                .addLine(line1)
                .addLine(line2);
        if (!line3.isEmpty()) style.addLine(line3);
        if (!line4.isEmpty()) style.addLine(line4);

        // 상태바에 보일 한 줄 요약
        String statusTitle = line2.isEmpty() ? line1 : line2;
        String statusText = !line3.isEmpty() ? line3 : line1;

        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, channelId)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(statusTitle)
                .setContentText(statusText)
                .setStyle(style)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (contentIntent != null) b.setContentIntent(contentIntent);

        NotificationManagerCompat.from(ctx).notify((int) notiId, b.build());
    }
}