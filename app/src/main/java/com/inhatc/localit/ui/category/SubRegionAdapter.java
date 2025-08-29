package com.inhatc.localit.ui.category;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inhatc.localit.R;

import java.util.List;

public class SubRegionAdapter extends RecyclerView.Adapter<SubRegionAdapter.ViewHolder> {

    private final List<String> subRegions;
    private final OnSubRegionClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public interface OnSubRegionClickListener {
        void onSubRegionClick(String subRegion);
    }


    public SubRegionAdapter(List<String> subRegions, OnSubRegionClickListener listener) {
        this.subRegions = subRegions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SubRegionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_region, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubRegionAdapter.ViewHolder holder, int position) {
        String subRegion = subRegions.get(position);
        holder.textView.setText(subRegion);

        // 배경과 텍스트 스타일 조절
        if (position == selectedPosition) {
            holder.itemView.setBackgroundColor(android.graphics.Color.WHITE);
            holder.textView.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            holder.itemView.setBackgroundColor(android.graphics.Color.parseColor("#F5F5F5"));
            holder.textView.setTypeface(null, android.graphics.Typeface.NORMAL);
        }

        // 클릭 시 리스너 호출 및 UI 업데이트
        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(previousPosition);
            notifyItemChanged(position);
            listener.onSubRegionClick(subRegion);
        });
    }


    @Override
    public int getItemCount() {
        return subRegions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.textRegionName); // item_region.xml 내 TextView ID
        }
    }
}
