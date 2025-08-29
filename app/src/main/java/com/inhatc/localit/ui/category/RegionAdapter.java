package com.inhatc.localit.ui.category;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.graphics.Typeface;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inhatc.localit.R;

import java.util.List;

public class RegionAdapter extends RecyclerView.Adapter<RegionAdapter.RegionViewHolder> {
    private int selectedPosition = RecyclerView.NO_POSITION;

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

        // 선택된 항목일 경우
        if (position == selectedPosition) {
            holder.itemRegionContainer.setBackgroundColor(Color.WHITE); // 배경 흰색
            holder.textRegionName.setTypeface(null, Typeface.BOLD);     // Bold 처리
        } else {
            holder.itemRegionContainer.setBackgroundColor(Color.parseColor("#F5F5F5")); // 기본 회색
            holder.textRegionName.setTypeface(null, Typeface.NORMAL);   // 기본 텍스트
        }

        // 클릭 이벤트 처리
        holder.itemView.setOnClickListener(v -> {
            selectedPosition = position;
            notifyDataSetChanged(); // 전체 리프레시
            listener.onRegionClick(region);
        });
    }



    @Override
    public int getItemCount() {
        return regionList.size();
    }

    // ViewHolder 정의
    static class RegionViewHolder extends RecyclerView.ViewHolder {
        TextView textRegionName;
        View itemRegionContainer;

        public RegionViewHolder(@NonNull View itemView) {
            super(itemView);
            textRegionName = itemView.findViewById(R.id.textRegionName);
            itemRegionContainer = itemView.findViewById(R.id.itemRegionContainer);
        }
    }
}
