package com.inhatc.localit.ui.category;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.inhatc.localit.R;

import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.VH> {

    public interface OnClick {
        void onClick(String url);
    }

    private final List<String> data;
    private final OnClick onClick;

    public GalleryAdapter(List<String> data, OnClick onClick) {
        this.data = data;
        this.onClick = onClick;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_spot_gallery, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        String url = data.get(position);
        Glide.with(h.img.getContext())
                .load(url)
                .placeholder(R.drawable.sample1)
                .error(R.drawable.sample1)
                .into(h.img);

        h.itemView.setOnClickListener(v -> {
            if (onClick != null) onClick.onClick(url);
        });
    }

    @Override public int getItemCount() { return data == null ? 0 : data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        VH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgThumb);
        }
    }
}
