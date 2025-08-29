package com.inhatc.localit.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.inhatc.localit.R;
import com.inhatc.localit.model.FavoriteItem;

import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {

    private final List<FavoriteItem> items;

    public FavoriteAdapter(List<FavoriteItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FavoriteItem item = items.get(position);
        holder.title.setText(item.title);
        holder.date.setText(item.date);
        holder.likeIcon.setImageResource(item.liked ? R.drawable.sample1: R.drawable.ic_favorite_border_24);

        Glide.with(holder.itemView.getContext())
                .load(item.imageUrl)
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, date;
        ImageView image, likeIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.favorite_title);
            date = itemView.findViewById(R.id.favorite_date);
            image = itemView.findViewById(R.id.favorite_image);
            likeIcon = itemView.findViewById(R.id.favorite_like_icon);
        }
    }
}