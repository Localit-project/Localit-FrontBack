package com.inhatc.localit.Fragment;

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
import com.inhatc.localit.util.AlarmPrefs;

import java.util.ArrayList;
import java.util.List;

/** 알림 목록: 설정에서 켠 것만 보이기 (기본 on) */
public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private NotiAdapter adapter;

    // 데모용 전체 리스트 (실제로는 서버/DB 데이터)
    private final List<Noti> all = new ArrayList<>();

    enum Source { WISH, REGION }         // 출처
    enum Type   { FESTIVAL, SPOT, NEWS }  // 타입

    static class Noti {
        final Source source;
        final Type type;
        final String title;
        Noti(Source s, Type t, String title) { this.source = s; this.type = t; this.title = title; }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);

        // 뒤로가기
        binding.topBar.findViewById(R.id.btnBack).setOnClickListener(v -> requireActivity().onBackPressed());

        // RecyclerView
        adapter = new NotiAdapter();
        binding.rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rv.setAdapter(adapter);

        // 데모 데이터 주입
        seedDemo();

        // 알림 허용 설정에 따라 칩 노출 제어
        boolean allowFestival = AlarmPrefs.allowFestival(requireContext());
        boolean allowSpot     = AlarmPrefs.allowSpot(requireContext());
        boolean allowNews     = AlarmPrefs.allowNews(requireContext());
        setVisible(binding.chipFestival, allowFestival);
        setVisible(binding.chipSpot,     allowSpot);
        setVisible(binding.chipNews,     allowNews);

        // 칩 변경 리스너 (한 번만 등록)
        binding.chipGroup.setOnCheckedStateChangeListener((ChipGroup group, List<Integer> checkedIds) -> {
            int id = checkedIds.isEmpty() ? binding.chipAll.getId() : checkedIds.get(0);
            updateHeaderForTab(id);   // 헤더 문구 변경
            applyFilter();            // 리스트 필터링
        });

        // 첫 로딩 시: 남아있는 첫 번째 칩을 선택, 없으면 '전체'
        if (!selectFirstVisibleChip(binding.chipGroup)) {
            binding.chipAll.setChecked(true);
            updateHeaderForTab(binding.chipAll.getId());
        } else {
            // 첫 선택된 칩에 맞게 헤더 세팅
            updateHeaderForTab(getCheckedId(binding.chipGroup));
        }

        // 버튼(읽음 처리 등) 연결
      /*  binding.btnMarkMsgsRead.setOnClickListener(v -> {
            // TODO: 선택/전체 읽음 처리 로직 연동
        }); */

        // 초기 필터 적용
        applyFilter();
        return binding.getRoot();
    }

    /** 탭에 따른 섹션 타이틀 변경 */
    private void updateHeaderForTab(int id) {
        if (id == binding.chipFestival.getId()) {
            binding.tvSectionTitle.setText(R.string.noti_header_festival);
        } else if (id == binding.chipSpot.getId()) {
            binding.tvSectionTitle.setText(R.string.noti_header_spot);
        } else if (id == binding.chipNews.getId()) {
            binding.tvSectionTitle.setText(R.string.noti_header_news);
        } else {
            binding.tvSectionTitle.setText(R.string.noti_header_all);
        }
    }

    /** 현재 체크된 칩의 id (없으면 '전체' 칩 id 반환) */
    private int getCheckedId(ChipGroup group) {
        List<Integer> ids = group.getCheckedChipIds();
        return ids.isEmpty() ? binding.chipAll.getId() : ids.get(0);
    }

    /** 현재 칩/설정 상태에 따라 목록 필터링 */
    private void applyFilter() {
        // 현재 Chip
        int checkedId = getCheckedId(binding.chipGroup);

        // 설정 상태
        boolean wishFest   = AlarmPrefs.isEnabled(requireContext(), AlarmPrefs.KEY_WISH_FESTIVAL);
        boolean wishSpot   = AlarmPrefs.isEnabled(requireContext(), AlarmPrefs.KEY_WISH_SPOT);
        boolean wishNews   = AlarmPrefs.isEnabled(requireContext(), AlarmPrefs.KEY_WISH_NEWS);
        boolean regionFest = AlarmPrefs.isEnabled(requireContext(), AlarmPrefs.KEY_REGION_FESTIVAL);
        boolean regionSpot = AlarmPrefs.isEnabled(requireContext(), AlarmPrefs.KEY_REGION_SPOT);
        boolean regionNews = AlarmPrefs.isEnabled(requireContext(), AlarmPrefs.KEY_REGION_NEWS);

        List<Noti> visible = new ArrayList<>();
        for (Noti n : all) {
            // 1) 설정에서 꺼진 조합 제거
            boolean allowed =
                    (n.source == Source.WISH   && n.type == Type.FESTIVAL && wishFest)   ||
                            (n.source == Source.WISH   && n.type == Type.SPOT     && wishSpot)   ||
                            (n.source == Source.WISH   && n.type == Type.NEWS     && wishNews)   ||
                            (n.source == Source.REGION && n.type == Type.FESTIVAL && regionFest) ||
                            (n.source == Source.REGION && n.type == Type.SPOT     && regionSpot) ||
                            (n.source == Source.REGION && n.type == Type.NEWS     && regionNews);

            if (!allowed) continue;

            // 2) Chip 필터 (전체/축제/관광지/뉴스) — ViewBinding의 id로 비교
            if (checkedId == binding.chipAll.getId()
                    || (checkedId == binding.chipFestival.getId() && n.type == Type.FESTIVAL)
                    || (checkedId == binding.chipSpot.getId()     && n.type == Type.SPOT)
                    || (checkedId == binding.chipNews.getId()     && n.type == Type.NEWS)) {
                visible.add(n);
            }
        }
        adapter.submit(visible);
    }

    private void setVisible(View v, boolean visible) {
        v.setVisibility(visible ? View.VISIBLE : View.GONE);
        v.setEnabled(visible);
    }

    /** 처음 로딩 시: 남아있는 첫 번째 칩 선택 (없으면 false) */
    private boolean selectFirstVisibleChip(ChipGroup group) {
        if (binding.chipFestival.getVisibility() == View.VISIBLE) { binding.chipFestival.setChecked(true); return true; }
        if (binding.chipSpot.getVisibility()     == View.VISIBLE) { binding.chipSpot.setChecked(true);     return true; }
        if (binding.chipNews.getVisibility()     == View.VISIBLE) { binding.chipNews.setChecked(true);     return true; }
        return false;
    }

    private void seedDemo() {
        // 실제 앱에서는 서버/DB에서 가져오세요.
        all.clear();
        all.add(new Noti(Source.WISH,   Type.FESTIVAL, "찜한 축제 소식"));
        all.add(new Noti(Source.WISH,   Type.SPOT,     "찜한 관광지 이벤트"));
        all.add(new Noti(Source.WISH,   Type.NEWS,     "찜한 지역 뉴스"));
        all.add(new Noti(Source.REGION, Type.FESTIVAL, "관심 지역 축제 알림"));
        all.add(new Noti(Source.REGION, Type.SPOT,     "관심 지역 스팟 업데이트"));
        all.add(new Noti(Source.REGION, Type.NEWS,     "관심 지역 뉴스 브리핑"));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ───────────────────────── Adapter (간단 데모) ─────────────────────────
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
            @Override public boolean areItemsTheSame(int i, int j) { return o.get(i) == n.get(j); }
            @Override public boolean areContentsTheSame(int i, int j) {
                Noti a = o.get(i), b = n.get(j);
                return a.source == b.source && a.type == b.type && a.title.equals(b.title);
            }
        }
    }

    private static class NotiVH extends RecyclerView.ViewHolder {
        private final android.widget.TextView t1 = itemView.findViewById(android.R.id.text1);
        private final android.widget.TextView t2 = itemView.findViewById(android.R.id.text2);
        NotiVH(@NonNull View itemView) { super(itemView); }
        void bind(Noti n) {
            t1.setText(n.title);
            t2.setText(n.source + " • " + n.type);
        }
    }
}
