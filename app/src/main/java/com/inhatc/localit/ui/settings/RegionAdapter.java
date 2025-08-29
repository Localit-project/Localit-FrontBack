package com.inhatc.localit.ui.settings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inhatc.localit.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class RegionAdapter extends RecyclerView.Adapter<RegionAdapter.VH> {

    interface OnToggleListener {
        void onToggle(String name, boolean checked, int totalChecked);
    }

    private final List<String> original;   // 전체
    private final List<String> display;    // 표시(검색 반영)
    private final Set<String> checkedSet;  // 선택 유지
    private final OnToggleListener listener;

    RegionAdapter(List<String> full, Set<String> preChecked, OnToggleListener l) {
        this.original = new ArrayList<>(full);
        this.display = new ArrayList<>(full);
        this.checkedSet = new HashSet<>(preChecked);
        this.listener = l;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_region_check, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        String name = display.get(pos);
        h.cb.setText(name);

        h.cb.setOnCheckedChangeListener(null);
        h.cb.setChecked(checkedSet.contains(name));
        h.cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkedSet.add(name);
            } else {
                checkedSet.remove(name);
            }
            if (listener != null) listener.onToggle(name, isChecked, checkedSet.size());
        });

        h.itemView.setOnClickListener(v -> h.cb.performClick());
    }

    @Override
    public int getItemCount() {
        return display.size();
    }

    List<String> getCheckedItems() {
        return new ArrayList<>(checkedSet);
    }

    void submitList(List<String> newList) {
        display.clear();
        display.addAll(newList);
        notifyDataSetChanged();
    }

    void forceUncheck(String name) {
        if (checkedSet.remove(name)) {
            int idx = display.indexOf(name);
            if (idx >= 0) {
                notifyItemChanged(idx);
            } else {
                notifyDataSetChanged();
            }
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        CheckBox cb;
        VH(@NonNull View itemView) {
            super(itemView);
            cb = itemView.findViewById(R.id.cbRegion);
        }
    }
}
