package com.inhatc.localit.ui.category;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inhatc.localit.R;

import java.util.List;

public class RegionAdapter extends RecyclerView.Adapter<RegionAdapter.RegionViewHolder> {
    private int selectedPosition = RecyclerView.NO_POSITION;

    private final List<String> regionList;
    private final OnRegionClickListener listener;

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

        // 선택된 항목에 따라 UI를 업데이트합니다.
        if (position == selectedPosition) {
            holder.itemRegionContainer.setBackgroundColor(Color.WHITE);
            holder.textRegionName.setTypeface(null, Typeface.BOLD);
        } else {
            holder.itemRegionContainer.setBackgroundColor(Color.parseColor("#F5F5F5"));
            holder.textRegionName.setTypeface(null, Typeface.NORMAL);
        }

        // [수정됨] 클릭 이벤트 처리 로직 개선
        holder.itemView.setOnClickListener(v -> {
            // 1. 클릭된 순간의 정확한 위치를 가져옵니다.
            int currentPosition = holder.getAdapterPosition();
            // 2. 유효하지 않은 위치는 무시합니다 (예: 아이템이 빠르게 삭제된 경우).
            if (currentPosition == RecyclerView.NO_POSITION) {
                return;
            }

            // 3. 이전에 선택되었던 아이템의 위치를 저장합니다.
            int previousPosition = selectedPosition;
            // 4. 새로 선택된 아이템의 위치를 업데이트합니다.
            selectedPosition = currentPosition;

            // 5. 변경이 필요한 부분만 효율적으로 새로 고칩니다.
            // 이전에 선택됐던 아이템을 다시 그려서 선택 해제 처리
            if (previousPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(previousPosition);
            }
            // 새로 선택된 아이템을 다시 그려서 선택 처리
            notifyItemChanged(selectedPosition);

            // 6. 리스너에는 정확한 위치의 데이터를 전달합니다.
            listener.onRegionClick(regionList.get(currentPosition));
        });
    }

    @Override
    public int getItemCount() {
        return regionList.size();
    }

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