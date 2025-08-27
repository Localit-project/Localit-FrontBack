package com.inhatc.localit.Fragment;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.inhatc.localit.R;
import com.inhatc.localit.databinding.FragmentNotificationsBinding;
import com.inhatc.localit.db.AppDatabase;
import com.inhatc.localit.db.TouristSpot;
import com.inhatc.localit.db.TouristSpotDao;
import com.inhatc.localit.util.AlarmPrefs;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 알림 목록: 설정에서 켠 것만 보이기 (기본 on)
 * 찜한 목록, 축제, 신규 관광지 알림을 표시하는 Fragment
 */
public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private NotiAdapter adapter;
    private final List<Noti> allNotifications = new ArrayList<>();

    enum Source { WISH, FESTIVAL, SPOT_NEW }
    enum Type { FESTIVAL, SPOT }

    /** 알림 데이터 모델 */
    static class Noti {
        final long id;
        final Source source;
        final Type type;
        final String title;
        final Date timestamp;

        Noti(long id, Source s, Type t, String title, Date timestamp) {
            this.id = id;
            this.source = s;
            this.type = t;
            this.title = title;
            this.timestamp = timestamp;
        }
    }

    private long generateId(TouristSpot spot) {
        return spot.contentid.hashCode();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);

        AppDatabase db = AppDatabase.getDatabase(requireContext());
        TouristSpotDao dao = db.touristSpotDao();

        adapter = new NotiAdapter();
        binding.rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rv.setAdapter(adapter);

        // 찜 목록 + 신규 관광지 + 축제 알림 처리
        dao.getWishedSpots().observe(getViewLifecycleOwner(), spots -> {
            allNotifications.clear();
            Date now = new Date();

            for (TouristSpot spot : spots) {
                // 1️⃣ 찜한 장소 알림
                allNotifications.add(new Noti(
                        generateId(spot),
                        Source.WISH,
                        spot.contenttypeid == 15 ? Type.FESTIVAL : Type.SPOT,
                        "찜 등록: " + spot.title,
                        now
                ));

                // 2️⃣ 새로운 관광지 알림
                if (spot.isNew && spot.contenttypeid != 15) {
                    allNotifications.add(new Noti(
                            generateId(spot) + 1000,
                            Source.SPOT_NEW,
                            Type.SPOT,
                            "새로운 관광지 업데이트: " + spot.title,
                            now
                    ));
                }

                // 3️⃣ 축제 시작/종료 전 알림
                if (spot.isFestival && spot.startDate != null && spot.endDate != null) {
                    addFestivalPreNotifications(spot);
                }
            }

            applyFilter(); // UI 갱신
        });

        // 칩 가시성
        setVisible(binding.chipFestival, AlarmPrefs.allowFestival(requireContext()));
        setVisible(binding.chipSpot, AlarmPrefs.allowSpot(requireContext()));

        // 칩 클릭
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

        applyFilter();
        return binding.getRoot();
    }

    private void addFestivalPreNotifications(TouristSpot spot) {
        Calendar cal = Calendar.getInstance();

        // 시작 1일 전
        cal.setTime(spot.startDate);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        allNotifications.add(new Noti(generateId(spot) + 2000,
                Source.FESTIVAL, Type.FESTIVAL,
                "축제 시작 1일 전: " + spot.title, cal.getTime()));

        // 시작 1주일 전
        cal.setTime(spot.startDate);
        cal.add(Calendar.DAY_OF_MONTH, -7);
        allNotifications.add(new Noti(generateId(spot) + 3000,
                Source.FESTIVAL, Type.FESTIVAL,
                "축제 시작 1주일 전: " + spot.title, cal.getTime()));

        // 종료 1일 전
        cal.setTime(spot.endDate);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        allNotifications.add(new Noti(generateId(spot) + 4000,
                Source.FESTIVAL, Type.FESTIVAL,
                "축제 종료 1일 전: " + spot.title, cal.getTime()));

        // 종료 1주일 전
        cal.setTime(spot.endDate);
        cal.add(Calendar.DAY_OF_MONTH, -7);
        allNotifications.add(new Noti(generateId(spot) + 5000,
                Source.FESTIVAL, Type.FESTIVAL,
                "축제 종료 1주일 전: " + spot.title, cal.getTime()));
    }

    private void updateHeaderForTab(int id) {
        if (id == binding.chipFestival.getId()) binding.tvSectionTitle.setText(R.string.noti_header_festival);
        else if (id == binding.chipSpot.getId()) binding.tvSectionTitle.setText(R.string.noti_header_spot);
        else binding.tvSectionTitle.setText(R.string.noti_header_all);
    }

    private int getCheckedId(ChipGroup group) {
        List<Integer> ids = group.getCheckedChipIds();
        return ids.isEmpty() ? binding.chipAll.getId() : ids.get(0);
    }

    private void applyFilter() {
        int checkedId = getCheckedId(binding.chipGroup);

        boolean wishFest = AlarmPrefs.isEnabled(requireContext(), AlarmPrefs.KEY_WISH_FESTIVAL);
        boolean wishSpot = AlarmPrefs.isEnabled(requireContext(), AlarmPrefs.KEY_WISH_SPOT);

        List<Noti> visible = new ArrayList<>();
        for (Noti n : allNotifications) {
            boolean allowed = (n.source == Source.WISH && n.type == Type.FESTIVAL && wishFest)
                    || (n.source == Source.WISH && n.type == Type.SPOT && wishSpot)
                    || n.source == Source.FESTIVAL
                    || n.source == Source.SPOT_NEW;

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
        if (binding.chipFestival.getVisibility() == View.VISIBLE) { binding.chipFestival.setChecked(true); return true; }
        if (binding.chipSpot.getVisibility() == View.VISIBLE) { binding.chipSpot.setChecked(true); return true; }
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ───────────────────────── Adapter ─────────────────────────
    private static class NotiAdapter extends RecyclerView.Adapter<NotiVH> {
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
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
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

        static class DiffCb extends DiffUtil.Callback {
            final List<Noti> o, n;
            DiffCb(List<Noti> o, List<Noti> n) { this.o = o; this.n = n; }
            @Override public int getOldListSize() { return o.size(); }
            @Override public int getNewListSize() { return n.size(); }
            @Override public boolean areItemsTheSame(int i, int j) { return o.get(i).id == n.get(j).id; }
            @Override public boolean areContentsTheSame(int i, int j) {
                Noti a = o.get(i), b = n.get(j);
                return a.source == b.source && a.type == b.type && a.title.equals(b.title) && a.timestamp.equals(b.timestamp);
            }
        }
    }

    private static class NotiVH extends RecyclerView.ViewHolder {
        private final android.widget.TextView t1 = itemView.findViewById(android.R.id.text1);
        private final android.widget.TextView t2 = itemView.findViewById(android.R.id.text2);

        NotiVH(@NonNull View itemView) {
            super(itemView);
        }

        void bind(Noti n) {
            t1.setText(n.title);
            t2.setText(n.source + " • " + n.type);

            // 클릭 시 알림 삭제
            itemView.setOnClickListener(v -> {
                Context context = v.getContext();

                // RecyclerView에서 제거
                if (context instanceof NotificationsFragmentHost) {
                    ((NotificationsFragmentHost) context).removeNotification(n.id);
                }

                // NotificationManager에서 제거
                NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (nm != null) {
                    nm.cancel((int) n.id);
                }
            });
        }
    }

    /** Activity에서 RecyclerView에서도 알림 제거를 위해 구현해야 함 */ //
    public interface NotificationsFragmentHost {
        void removeNotification(long notiId);
    }
}