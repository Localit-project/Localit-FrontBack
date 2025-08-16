package com.inhatc.localit.ui.category;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.inhatc.localit.R;
import com.inhatc.localit.api.SpotResponse;

import java.util.ArrayList;
import java.util.List;

public class TourismAdapter extends RecyclerView.Adapter<TourismAdapter.VH> {

    public interface OnItemClick {
        void onTourismClick(SpotResponse.Item item, int position);
    }

    public interface OnFavClick {
        void onFavoriteClick(SpotResponse.Item item, int position);
    }

    private final List<SpotResponse.Item> items = new ArrayList<>();
    private final OnItemClick onItemClick;
    private final OnFavClick onFavClick;

    public TourismAdapter(List<SpotResponse.Item> initial,
                          OnItemClick onItemClick,
                          OnFavClick onFavClick) {
        if (initial != null) items.addAll(initial);
        this.onItemClick = onItemClick;
        this.onFavClick  = onFavClick;
    }

    public void submitList(List<SpotResponse.Item> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_spot, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        final SpotResponse.Item it = items.get(position);

        // 제목
        h.title.setText(it != null && it.title != null && !it.title.isEmpty()
                ? it.title : "제목 없음");

        // 주소/부가정보 (addr1이 우선, 없으면 "지역정보 없음")
        if (h.sub != null) {
            String addr = (it != null && it.addr1 != null && !it.addr1.trim().isEmpty())
                    ? it.addr1.trim() : "지역정보 없음";
            // 필요하면 뒤에 카테고리 간단 라벨 추가
            h.sub.setText(addr);
        }

        // 이미지
        if (h.image != null) {
            if (it != null && it.firstimage != null && !it.firstimage.isEmpty()) {
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

        // 아이템 클릭
        h.itemView.setOnClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            if (onItemClick != null) onItemClick.onTourismClick(items.get(pos), pos);
        });

        // 즐겨찾기 클릭
        if (h.btnFavorite != null) {
            h.btnFavorite.setOnClickListener(v -> {
                int pos = h.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                if (onFavClick != null) onFavClick.onFavoriteClick(items.get(pos), pos);
            });
        } else {
            // 즐겨찾기 버튼이 레이아웃에 없을 경우 롱클릭으로 대체
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
        ImageView image;
        TextView title;
        TextView sub;
        ImageButton btnFavorite;

        VH(@NonNull View v) {
            super(v);
            image       = v.findViewById(R.id.imageTourismThumb);
            title       = v.findViewById(R.id.textTourismTitle);
            sub         = v.findViewById(R.id.textTourismSub);
            btnFavorite = v.findViewById(R.id.btnFavorite);
        }
    }
}