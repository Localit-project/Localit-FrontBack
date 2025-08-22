package com.inhatc.localit.ui.category;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.inhatc.localit.R;
import com.inhatc.localit.api.SpotResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FestivalAdapter extends RecyclerView.Adapter<FestivalAdapter.ViewHolder> {

    public interface OnItemClick { void onClick(SpotResponse.Item item, int position); }
    public interface OnFavClick { void onClick(SpotResponse.Item item, int position); }

    private final List<SpotResponse.Item> items = new ArrayList<>();
    private final OnItemClick itemClick;
    private final OnFavClick favClick;

    // 즐겨찾기 상태
    private final Set<String> favoriteKeys = new HashSet<>();

    public FestivalAdapter(List<SpotResponse.Item> initial,
                           OnItemClick itemClick,
                           OnFavClick favClick) {
        if (initial != null) items.addAll(initial);
        this.itemClick = itemClick;
        this.favClick  = favClick;
        setHasStableIds(true);
    }

    public void submitList(List<SpotResponse.Item> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void updateFavorites(Set<String> keys) {
        favoriteKeys.clear();
        if (keys != null) favoriteKeys.addAll(keys);
        notifyDataSetChanged();
    }

    public Set<String> getFavoriteKeys() { return new HashSet<>(favoriteKeys); }

    // ▼▼▼▼▼ 'keyOf'를 사용하던 부분을 안정적인 방식으로 수정 ▼▼▼▼▼
    @Override
    public long getItemId(int position) {
        SpotResponse.Item item = items.get(position);
        if (item != null && item.getContentid() != null) {
            // contentid가 있으면 그것의 해시코드를 고유 ID로 사용
            return item.getContentid().hashCode();
        }
        // 없으면 기본값 사용
        return RecyclerView.NO_ID;
    }
    // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_festival, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        SpotResponse.Item item = items.get(position);

        h.textTitle.setText(item.getTitle() == null ? "" : item.getTitle());

        String start = formatDate(item.getEventstartdate());
        String end   = formatDate(item.getEventenddate());
        h.textDate.setText(buildDateText(start, end));

        if (!TextUtils.isEmpty(item.getFirstimage())) {
            Glide.with(h.itemView.getContext())
                    .load(item.getFirstimage())
                    .placeholder(R.drawable.sample1)
                    .error(R.drawable.sample1)
                    .into(h.imageThumb);
        } else {
            h.imageThumb.setImageResource(R.drawable.sample1);
        }

        boolean fav = favoriteKeys.contains(item.getContentid());

        if (fav) {
            h.btnFavorite.setImageResource(R.drawable.ic_favorite_full);
        } else {
            h.btnFavorite.setImageResource(R.drawable.ic_favorite_border_24);
        }

        h.itemView.setOnClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            if (itemClick != null) itemClick.onClick(items.get(pos), pos);
        });

        h.btnFavorite.setOnClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            if (favClick != null) favClick.onClick(items.get(pos), pos);
        });
    }

    @Override public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageThumb;
        TextView textTitle, textDate;
        ImageView btnFavorite;
        ViewHolder(@NonNull View v) {
            super(v);
            imageThumb  = v.findViewById(R.id.imageEvent);
            textTitle   = v.findViewById(R.id.textEventTitle);
            textDate    = v.findViewById(R.id.textEventDate);
            btnFavorite = v.findViewById(R.id.btnFavorite);
        }
    }

    private static String formatDate(String raw) {
        if (TextUtils.isEmpty(raw) || raw.length() < 8) return "";
        return raw.substring(0,4)+"."+raw.substring(4,6)+"."+raw.substring(6,8);
    }
    private static String buildDateText(String s, String e) {
        if (!TextUtils.isEmpty(s) && !TextUtils.isEmpty(e)) return s+" ~ "+e;
        if (!TextUtils.isEmpty(s)) return s;
        if (!TextUtils.isEmpty(e)) return e;
        return "일정 미정";
    }
}
