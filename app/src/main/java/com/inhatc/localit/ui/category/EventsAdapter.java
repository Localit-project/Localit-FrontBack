package com.inhatc.localit.ui.category;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.inhatc.localit.R;
import com.inhatc.localit.api.SpotResponse;

import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> {

    public interface OnItemClick {
        void onClick(SpotResponse.Item item, int position);
    }

    public interface OnFavClick {
        void onClick(SpotResponse.Item item, int position);
    }

    private final List<SpotResponse.Item> items;
    private final OnItemClick itemClick;
    private final OnFavClick favClick;

    public EventsAdapter(List<SpotResponse.Item> items,
                         OnItemClick itemClick,
                         OnFavClick favClick) {
        this.items = items;
        this.itemClick = itemClick;
        this.favClick = favClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_festival, parent, false); // ← 방금 올린 XML 파일명
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        SpotResponse.Item item = items.get(position);

        // 제목
        h.textTitle.setText(item.title == null ? "" : item.title);

        // 날짜
        String start = formatDate(item.eventstartdate);
        String end   = formatDate(item.eventenddate);
        if (!TextUtils.isEmpty(start) && !TextUtils.isEmpty(end)) {
            h.textDate.setText(start + " ~ " + end);
        } else if (!TextUtils.isEmpty(start)) {
            h.textDate.setText(start);
        } else if (!TextUtils.isEmpty(end)) {
            h.textDate.setText(end);
        } else {
            h.textDate.setText("일정 미정");
        }

        // 이미지
        if (!TextUtils.isEmpty(item.firstimage)) {
            Glide.with(h.itemView.getContext())
                    .load(item.firstimage)
                    .placeholder(R.drawable.sample1)
                    .error(R.drawable.sample1)
                    .into(h.imageThumb);
        } else {
            h.imageThumb.setImageResource(R.drawable.sample1);
        }

        // 클릭: 카드 전체
        h.itemView.setOnClickListener(v -> {
            if (itemClick != null) itemClick.onClick(item, h.getBindingAdapterPosition());
        });

        // 클릭: 즐겨찾기 버튼
        h.btnFavorite.setOnClickListener(v -> {
            if (favClick != null) favClick.onClick(item, h.getBindingAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageThumb;
        TextView textTitle, textDate;
        ImageView btnFavorite; // XML에서 ImageView로 정의됨

        ViewHolder(@NonNull View v) {
            super(v);
            imageThumb  = v.findViewById(R.id.imageEvent);
            textTitle   = v.findViewById(R.id.textEventTitle);
            textDate    = v.findViewById(R.id.textEventDate);
            btnFavorite = v.findViewById(R.id.btnFavorite);
        }
    }

    // yyyyMMdd → yyyy.MM.dd
    private static String formatDate(String raw) {
        if (TextUtils.isEmpty(raw) || raw.length() < 8) return "";
        try {
            return raw.substring(0, 4) + "." + raw.substring(4, 6) + "." + raw.substring(6, 8);
        } catch (Exception e) {
            return "";
        }
    }
}