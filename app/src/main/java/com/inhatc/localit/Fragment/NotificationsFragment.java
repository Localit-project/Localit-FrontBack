package com.inhatc.localit.Fragment;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.inhatc.localit.MainActivity;
import com.inhatc.localit.R;
import com.inhatc.localit.databinding.FragmentNotificationsBinding;
import com.inhatc.localit.db.AppDatabase;
import com.inhatc.localit.db.TouristSpot;
import com.inhatc.localit.db.TouristSpotDao;
import com.inhatc.localit.util.AlarmPrefs;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 알림 목록 + 시스템 알림 발송
 */
public class NotificationsFragment extends Fragment {

    // ───── Types ─────
    enum Source {WISH, FESTIVAL, SPOT_NEW}

    enum Type {FESTIVAL, SPOT}

    /**
     * 알림 모델 (기간 문자열 포함)
     */
    static class Noti {
        final long id;
        final Source source;
        final Type type;
        final String title;
        final Date timestamp;
        @Nullable
        final Date startDate;
        @Nullable
        final Date endDate;
        final String periodText;        // "2025.08.22 ~ 2025.08.30"
        final String description;

        Noti(long id, Source source, Type type, String title, Date timestamp,
             @Nullable Date startDate, @Nullable Date endDate,
             String periodText, String description) {
            this.id = id;
            this.source = source;
            this.type = type;
            this.title = title;
            this.timestamp = timestamp;
            this.startDate = startDate;
            this.endDate = endDate;
            this.periodText = (periodText == null) ? "" : periodText;
            this.description = (description == null) ? "" : description;
        }
    }

    // ───── View State ─────
    private FragmentNotificationsBinding binding;
    private NotiAdapter adapter;
    private final List<Noti> allNotifications = new ArrayList<>();

    // 뒤로가기 버튼 변수 추가
    private ImageView btnBack;

    // 채널
    private static final String CH_WISH = "wish_channel";
    private static final String CH_FESTIVAL = "festival_channel";

    // 날짜 포맷터
    private static final SimpleDateFormat DF = new SimpleDateFormat("yyyy.MM.dd", Locale.KOREA);

    private long generateId(TouristSpot spot) {
        return spot.contentid.hashCode();
    }

    private static String buildDateRange(@Nullable Date s, @Nullable Date e) {
        if (s == null || e == null) return "";
        return DF.format(s) + " ~ " + DF.format(e);
    }

    // ───── Lifecycle ─────
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);

        btnBack = binding.getRoot().findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    getActivity().onBackPressed();
                }
            });
        }

        ensureChannels(requireContext());

        AppDatabase db = AppDatabase.getDatabase(requireContext());
        TouristSpotDao dao = db.touristSpotDao();

        adapter = new NotiAdapter();
        binding.rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rv.setAdapter(adapter);

        dao.getWishedSpots().observe(getViewLifecycleOwner(), spots -> {
            allNotifications.clear();
            Date now = new Date();

            for (TouristSpot spot : spots) {
                boolean isFestival = (spot.contenttypeid == 15);
                Type type = isFestival ? Type.FESTIVAL : Type.SPOT;
                String periodText = buildDateRange(spot.startDate, spot.endDate);

                // 1) 찜 등록
                {
                    String desc = isFestival ? "찜 목록의 축제·행사가 추가되었습니다." : "찜 목록에 장소가 추가되었습니다.";
                    Noti n = new Noti(generateId(spot), Source.WISH, type, spot.title, now, spot.startDate, spot.endDate, periodText, desc);
                    allNotifications.add(n);
                    showSystemNotification(requireContext(), n);
                }

                // 2) 신규 관광지
                if (spot.isNew && !isFestival) {
                    Noti n = new Noti(generateId(spot) + 1000, Source.SPOT_NEW, Type.SPOT, spot.title, now, null, null, "", "새로운 관광지 정보가 업데이트되었습니다.");
                    allNotifications.add(n);
                    showSystemNotification(requireContext(), n);
                }

                // 3) 축제 시작/종료 전
                if (isFestival && spot.startDate != null && spot.endDate != null) {
                    addFestivalPreNotifications(spot);
                }
            }

            applyFilter();
        });

        setVisible(binding.chipFestival, AlarmPrefs.allowFestival(requireContext()));
        setVisible(binding.chipSpot, AlarmPrefs.allowSpot(requireContext()));

        binding.chipGroup.setOnCheckedStateChangeListener((ChipGroup group, List<Integer> checkedIds) -> {
            int id = checkedIds.isEmpty() ? binding.chipAll.getId() : checkedIds.get(0);
            updateHeaderForTab(id);
            applyFilter();
        });

        if (!selectFirstVisibleChip(binding.chipGroup)) {
            binding.chipAll.setChecked(true);
            updateHeaderForTab(binding.chipAll.getId());
        } else {
            updateHeaderForTab(getCheckedId(binding.chipGroup));
        }

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ───── Pre notifications (축제) ─────
    private void addFestivalPreNotifications(TouristSpot spot) {
        String periodText = buildDateRange(spot.startDate, spot.endDate);
        Calendar cal = Calendar.getInstance();

        cal.setTime(spot.startDate);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Noti d1Start = new Noti(generateId(spot) + 2000, Source.FESTIVAL, Type.FESTIVAL, spot.title, cal.getTime(), spot.startDate, spot.endDate, periodText, "찜 목록의 축제·행사가 1일 뒤에 시작됩니다.");
        allNotifications.add(d1Start);
        showSystemNotification(requireContext(), d1Start);

        cal.setTime(spot.startDate);
        cal.add(Calendar.DAY_OF_MONTH, -7);
        Noti d7Start = new Noti(generateId(spot) + 3000, Source.FESTIVAL, Type.FESTIVAL, spot.title, cal.getTime(), spot.startDate, spot.endDate, periodText, "찜 목록의 축제·행사가 1주 뒤에 시작됩니다.");
        allNotifications.add(d7Start);
        showSystemNotification(requireContext(), d7Start);

        cal.setTime(spot.endDate);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Noti d1End = new Noti(generateId(spot) + 4000, Source.FESTIVAL, Type.FESTIVAL, spot.title, cal.getTime(), spot.startDate, spot.endDate, periodText, "축제가 1일 뒤에 종료됩니다.");
        allNotifications.add(d1End);
        showSystemNotification(requireContext(), d1End);

        cal.setTime(spot.endDate);
        cal.add(Calendar.DAY_OF_MONTH, -7);
        Noti d7End = new Noti(generateId(spot) + 5000, Source.FESTIVAL, Type.FESTIVAL, spot.title, cal.getTime(), spot.startDate, spot.endDate, periodText, "축제가 1주 뒤에 종료됩니다.");
        allNotifications.add(d7End);
        showSystemNotification(requireContext(), d7End);
    }

    // ───── Header / Filter ─────
    private void updateHeaderForTab(int id) {
        if (binding == null) return;
        if (id == binding.chipFestival.getId())
            binding.tvSectionTitle.setText(R.string.noti_header_festival);
        else if (id == binding.chipSpot.getId())
            binding.tvSectionTitle.setText(R.string.noti_header_spot);
        else binding.tvSectionTitle.setText(R.string.noti_header_all);
    }

    private int getCheckedId(ChipGroup group) {
        if (binding == null) return -1;
        List<Integer> ids = group.getCheckedChipIds();
        return ids.isEmpty() ? binding.chipAll.getId() : ids.get(0);
    }

    private void applyFilter() {
        if (getContext() == null || binding == null) return;
        int checkedId = getCheckedId(binding.chipGroup);
        boolean wishFest = AlarmPrefs.isEnabled(requireContext(), AlarmPrefs.KEY_WISH_FESTIVAL);
        boolean wishSpot = AlarmPrefs.isEnabled(requireContext(), AlarmPrefs.KEY_WISH_SPOT);

        List<Noti> visible = new ArrayList<>();
        for (Noti n : allNotifications) {
            boolean allowed =
                    (n.source == Source.WISH && n.type == Type.FESTIVAL && wishFest) ||
                            (n.source == Source.WISH && n.type == Type.SPOT && wishSpot) ||
                            n.source == Source.FESTIVAL ||
                            n.source == Source.SPOT_NEW;

            if (!allowed) continue;

            if (checkedId == binding.chipAll.getId()
                    || (checkedId == binding.chipFestival.getId() && n.type == Type.FESTIVAL)
                    || (checkedId == binding.chipSpot.getId() && n.type == Type.SPOT)) {
                visible.add(n);
            }
        }
        adapter.submit(visible);
    }

    private void setVisible(View v, boolean visible) {
        v.setVisibility(visible ? View.VISIBLE : View.GONE);
        v.setEnabled(visible);
    }

    private boolean selectFirstVisibleChip(ChipGroup group) {
        if (binding == null) return false;
        if (binding.chipFestival.getVisibility() == View.VISIBLE) {
            binding.chipFestival.setChecked(true);
            return true;
        }
        if (binding.chipSpot.getVisibility() == View.VISIBLE) {
            binding.chipSpot.setChecked(true);
            return true;
        }
        return false;
    }

    // ───── System Notification ─────
    private static void ensureChannels(Context ctx) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;

        NotificationChannel wish = new NotificationChannel(CH_WISH, "찜 알림", NotificationManager.IMPORTANCE_DEFAULT);
        wish.setDescription("찜 목록 관련 알림");

        NotificationChannel fest = new NotificationChannel(CH_FESTIVAL, "축제·행사 알림", NotificationManager.IMPORTANCE_DEFAULT);
        fest.setDescription("축제 시작/종료 전 안내");

        nm.createNotificationChannel(wish);
        nm.createNotificationChannel(fest);
    }

    private static String badge(Source s, Type t) {
        String left;
        switch (s) {
            case WISH: left = "찜 목록"; break;
            case FESTIVAL: left = "축제 알림"; break;
            case SPOT_NEW: left = "신규 관광지"; break;
            default: left = "";
        }
        String right = (t == Type.FESTIVAL) ? "축제·행사" : "관광지";
        return left + " (" + right + ")";
    }

    private void showSystemNotification(@NonNull Context ctx, @NonNull Noti n) {
        if (n.source == Source.WISH && n.type == Type.FESTIVAL && !AlarmPrefs.isEnabled(ctx, AlarmPrefs.KEY_WISH_FESTIVAL)) return;
        if (n.source == Source.WISH && n.type == Type.SPOT && !AlarmPrefs.isEnabled(ctx, AlarmPrefs.KEY_WISH_SPOT)) return;

        final String channelId = (n.source == Source.FESTIVAL) ? CH_FESTIVAL : CH_WISH;

        Intent intent = new Intent(ctx, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, (int) n.id, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        RemoteViews expanded = new RemoteViews(ctx.getPackageName(), R.layout.item_notification);
        String badge = badge(n.source, n.type);
        CharSequence when = DateUtils.getRelativeTimeSpanString((n.timestamp != null ? n.timestamp.getTime() : System.currentTimeMillis()), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
        String period = !TextUtils.isEmpty(n.periodText) ? "기간 : " + n.periodText : "";
        String desc = n.description;

        expanded.setTextViewText(R.id.tvBadge, badge);
        expanded.setTextViewText(R.id.tvWhen, when);
        expanded.setTextViewText(R.id.tvTitle, n.title != null ? n.title : "");
        expanded.setTextViewText(R.id.tvPeriod, period);
        expanded.setTextViewText(R.id.tvDesc, desc);
        expanded.setImageViewResource(R.id.ivLogo, R.drawable.logo);

        String collapsedTitle = n.title;
        String collapsedText = !TextUtils.isEmpty(period) ? period : badge;

        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, channelId)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(collapsedTitle)
                .setContentText(collapsedText)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomBigContentView(expanded)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setWhen(n.timestamp != null ? n.timestamp.getTime() : System.currentTimeMillis())
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        NotificationManagerCompat.from(ctx).notify((int) n.id, b.build());
    }

    // ───── Adapter ─────
    // [수정됨] 컴파일 오류를 해결하기 위해 Adapter와 ViewHolder 구조를 수정했습니다.
    public static class NotiAdapter extends RecyclerView.Adapter<NotiAdapter.NotiVH> {
        private final List<Noti> items = new ArrayList<>();

        void submit(List<Noti> newItems) {
            DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCb(items, newItems));
            items.clear();
            items.addAll(newItems);
            diff.dispatchUpdatesTo(this);
        }

        @NonNull
        @Override
        public NotiVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification, parent, false);
            return new NotiVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull NotiVH h, int pos) {
            h.bind(items.get(pos));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        // ViewHolder를 Adapter의 non-static inner class로 변경하여 안정성 확보
        public class NotiVH extends RecyclerView.ViewHolder {
            private final ImageView ivLogo = itemView.findViewById(R.id.ivLogo);
            private final TextView tvBadge = itemView.findViewById(R.id.tvBadge);
            private final TextView tvWhen = itemView.findViewById(R.id.tvWhen);
            private final TextView tvTitle = itemView.findViewById(R.id.tvTitle);
            private final TextView tvPeriod = itemView.findViewById(R.id.tvPeriod);
            private final TextView tvDesc = itemView.findViewById(R.id.tvDesc);

            NotiVH(@NonNull View itemView) {
                super(itemView);
            }

            void bind(final Noti n) {
                ivLogo.setImageResource(R.drawable.logo);
                tvBadge.setText(badge(n.source, n.type));
                tvTitle.setText(n.title != null ? n.title : "");

                CharSequence when = DateUtils.getRelativeTimeSpanString((n.timestamp != null ? n.timestamp.getTime() : System.currentTimeMillis()), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
                tvWhen.setText(when);

                String p = n.periodText;
                if (TextUtils.isEmpty(p)) {
                    tvPeriod.setVisibility(View.GONE);
                } else {
                    tvPeriod.setVisibility(View.VISIBLE);
                    tvPeriod.setText("기간 : " + p);
                }

                if (TextUtils.isEmpty(n.description)) {
                    tvDesc.setVisibility(View.GONE);
                } else {
                    tvDesc.setVisibility(View.VISIBLE);
                    tvDesc.setText(n.description);
                }

                itemView.setOnClickListener(v -> {
                    Context context = v.getContext();
                    if (context instanceof NotificationsFragmentHost) {
                        ((NotificationsFragmentHost) context).removeNotification(n.id);
                    }
                    NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (nm != null) nm.cancel((int) n.id);
                });
            }
        }

        static class DiffCb extends DiffUtil.Callback {
            final List<Noti> o, n;
            DiffCb(List<Noti> o, List<Noti> n) { this.o = o; this.n = n; }
            @Override public int getOldListSize() { return o.size(); }
            @Override public int getNewListSize() { return n.size(); }
            @Override public boolean areItemsTheSame(int i, int j) { return o.get(i).id == n.get(j).id; }
            @Override public boolean areContentsTheSame(int i, int j) {
                Noti a = o.get(i), b = n.get(j);
                return a.source == b.source && a.type == b.type &&
                        eq(a.title, b.title) && eq(a.timestamp, b.timestamp) &&
                        eq(a.startDate, b.startDate) && eq(a.endDate, b.endDate) &&
                        eq(a.periodText, b.periodText) && eq(a.description, b.description);
            }
            private static boolean eq(Object x, Object y) {
                return (x == y) || (x != null && x.equals(y));
            }
        }
    }

    /**
     * Activity에서 RecyclerView에서도 알림 제거를 위해 구현해야 함
     */
    public interface NotificationsFragmentHost {
        void removeNotification(long notiId);
    }
}