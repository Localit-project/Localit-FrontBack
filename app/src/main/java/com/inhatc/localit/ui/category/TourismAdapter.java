package com.inhatc.localit.ui.category;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.inhatc.localit.R;
import com.inhatc.localit.api.TourResponse;

import java.util.ArrayList;
import java.util.List;

public class TourismAdapter extends RecyclerView.Adapter<TourismAdapter.VH> {

    public interface OnItemClick {
        void onTourismClick(TourResponse.Item item, int position);
    }
    public interface OnFavClick {
        void onFavoriteClick(TourResponse.Item item, int position);
    }

    private final List<TourResponse.Item> items = new ArrayList<>();
    private final OnItemClick onItemClick;
    private final OnFavClick onFavClick;

    public TourismAdapter(List<TourResponse.Item> initial,
                          OnItemClick onItemClick,
                          OnFavClick onFavClick) {
        if (initial != null) items.addAll(initial);
        this.onItemClick = onItemClick;
        this.onFavClick  = onFavClick;
    }

    public void submitList(List<TourResponse.Item> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tourism, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        final TourResponse.Item it = items.get(position);

        // 제목 표시
        h.title.setText(it != null && it.title != null && !it.title.isEmpty()
                ? it.title : "제목 없음");

        // ✅ 이미지 표시 로직 추가
        if (h.image != null) {
            if (it.firstimage != null && !it.firstimage.isEmpty()) {
                Glide.with(h.itemView.getContext())
                        .load(it.firstimage)
                        .centerCrop()
                        .placeholder(R.drawable.sample1)
                        .error(R.drawable.sample1)
                        .into(h.image);
            } else {
                h.image.setImageResource(R.drawable.sample1);
            }
        }

        // 클릭 이벤트
        h.itemView.setOnClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            if (onItemClick != null) onItemClick.onTourismClick(items.get(pos), pos);
        });

        // 즐겨찾기 클릭 이벤트
        if (h.btnFavorite != null) {
            h.btnFavorite.setOnClickListener(v -> {
                int pos = h.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                if (onFavClick != null) onFavClick.onFavoriteClick(items.get(pos), pos);
            });
        } else {
            h.itemView.setOnLongClickListener(v -> {
                int pos = h.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return true;
                if (onFavClick != null) onFavClick.onFavoriteClick(items.get(pos), pos);
                return true;
            });
        }
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView image;         // @id/imageTourismThumb
        TextView title;          // @id/textTourismTitle
        ImageView btnFavorite;   // @id/btnFavorite (선택)

        VH(@NonNull View v) {
            super(v);
            image = v.findViewById(R.id.imageTourismThumb);
            title = v.findViewById(R.id.textTourismTitle);
            btnFavorite = v.findViewById(R.id.btnFavorite);
        }
    }
}