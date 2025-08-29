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

public class CoursePagerAdapter extends RecyclerView.Adapter<CoursePagerAdapter.VH> {

    public interface OnClick {
        void onClick(int pos);
    }

    private final List<String> imageUrls;
    private final OnClick onClick;

    public CoursePagerAdapter(List<String> imageUrls, OnClick onClick) {
        this.imageUrls = imageUrls;
        this.onClick = onClick;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course_page, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        String url = imageUrls.get(position);
        Glide.with(h.img.getContext())
                .load(url)
                .placeholder(R.drawable.sample1)
                .error(R.drawable.sample1)
                .into(h.img);

        h.itemView.setOnClickListener(v -> {
            if (onClick != null) onClick.onClick(position);
        });
    }

    @Override public int getItemCount() { return imageUrls == null ? 0 : imageUrls.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        VH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgHero);
        }
    }
}
