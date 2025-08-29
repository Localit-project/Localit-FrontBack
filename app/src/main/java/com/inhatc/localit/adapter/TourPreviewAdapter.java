package com.inhatc.localit.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.inhatc.localit.R;
import com.inhatc.localit.api.home.TourItem;

import java.util.ArrayList;
import java.util.List;

public class TourPreviewAdapter extends RecyclerView.Adapter<TourPreviewAdapter.VH> {

    private List<TourItem> fullList = new ArrayList<>();
    private int visibleCount = 2; // 처음 2개만 보이기

    public void setItems(List<TourItem> list) {
        this.fullList = list;
        notifyDataSetChanged();
    }

    public void showAll() {
        visibleCount = fullList.size();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tour, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        TourItem item = fullList.get(position);
        holder.title.setText(item.title);
        Glide.with(holder.image.getContext())
                .load(item.firstimage)
                .placeholder(R.drawable.ic_image_placeholder)
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return Math.min(visibleCount, fullList.size());
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title;
        ImageView image;
        VH(View v) {
            super(v);
            title = v.findViewById(R.id.textTitle);
            image = v.findViewById(R.id.imageThumb);
        }
    }
}