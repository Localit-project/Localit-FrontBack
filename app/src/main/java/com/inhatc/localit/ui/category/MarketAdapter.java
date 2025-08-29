package com.inhatc.localit.ui.category;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inhatc.localit.R;
import com.inhatc.localit.api.home.TourItem;

import java.util.ArrayList;
import java.util.List;

public class MarketAdapter extends RecyclerView.Adapter<MarketAdapter.VH> {

    private final List<TourItem> allItems = new ArrayList<>();
    private final List<TourItem> displayedItems = new ArrayList<>();
    private boolean showingAll = false;

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_market, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        TourItem item = displayedItems.get(position);
        holder.tvTitle.setText(item.title);
        // 필요 시 이미지 처리 가능
    }

    @Override
    public int getItemCount() {
        return displayedItems.size();
    }

    public void setItems(List<TourItem> items) {
        allItems.clear();
        allItems.addAll(items);

        if (!showingAll && items.size() > 2) {
            displayedItems.clear();
            displayedItems.addAll(items.subList(0, 2)); // 처음엔 2개만 표시
        } else {
            displayedItems.clear();
            displayedItems.addAll(items);
        }
        notifyDataSetChanged();
    }

    public void showAll() {
        showingAll = true;
        displayedItems.clear();
        displayedItems.addAll(allItems);
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle;

        VH(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvMarketTitle); // item_market.xml 에서 TextView ID
        }
    }
}