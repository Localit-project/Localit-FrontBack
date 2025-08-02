package com.inhatc.localit.ui.category;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inhatc.localit.R;

import java.util.List;

public class RegionAdapter extends RecyclerView.Adapter<RegionAdapter.RegionViewHolder> {

    private final List<String> regionList;
    private final OnRegionClickListener listener;

    // 클릭 이벤트를 위한 인터페이스
    public interface OnRegionClickListener {
        void onRegionClick(String region);
    }

    public RegionAdapter(List<String> regionList, OnRegionClickListener listener) {
        this.regionList = regionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RegionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_region, parent, false);
        return new RegionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RegionViewHolder holder, int position) {
        String region = regionList.get(position);
        holder.textRegionName.setText(region);

        // 클릭 이벤트 연결
        holder.itemView.setOnClickListener(v -> listener.onRegionClick(region));
    }

    @Override
    public int getItemCount() {
        return regionList.size();
    }

    // ViewHolder 정의
    static class RegionViewHolder extends RecyclerView.ViewHolder {
        TextView textRegionName;

        public RegionViewHolder(@NonNull View itemView) {
            super(itemView);
            textRegionName = itemView.findViewById(R.id.textRegionName);
        }
    }
}
