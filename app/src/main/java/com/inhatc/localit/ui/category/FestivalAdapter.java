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

    public void setFavoriteKeys(Set<String> keys) {
        favoriteKeys.clear();
        if (keys != null) favoriteKeys.addAll(keys);
        notifyDataSetChanged();
    }

    public Set<String> getFavoriteKeys() { return new HashSet<>(favoriteKeys); }

    @Override public long getItemId(int position) { return keyOf(items.get(position)).hashCode(); }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_festival, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        SpotResponse.Item item = items.get(position);

        h.textTitle.setText(item.title == null ? "" : item.title);

        String start = formatDate(item.eventstartdate);
        String end   = formatDate(item.eventenddate);
        h.textDate.setText(buildDateText(start, end));

        if (!TextUtils.isEmpty(item.firstimage)) {
            Glide.with(h.itemView.getContext())
                    .load(item.firstimage)
                    .placeholder(R.drawable.sample1)
                    .error(R.drawable.sample1)
                    .into(h.imageThumb);
        } else {
            h.imageThumb.setImageResource(R.drawable.sample1);
        }

        // 즐겨찾기 아이콘 상태
        boolean fav = favoriteKeys.contains(keyOf(item));
        h.btnFavorite.setSelected(fav);

        h.itemView.setOnClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            if (itemClick != null) itemClick.onClick(items.get(pos), pos);
        });

        h.btnFavorite.setOnClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            String key = keyOf(items.get(pos));
            boolean newState;
            if (favoriteKeys.contains(key)) {
                favoriteKeys.remove(key);
                newState = false;
            } else {
                favoriteKeys.add(key);
                newState = true;
            }
            h.btnFavorite.setSelected(newState); // 즉시 토글
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
    private static String keyOf(SpotResponse.Item it) {
        if (it == null) return "@null";
        if (!TextUtils.isEmpty(it.contentid)) return "id:"+it.contentid;
        if (!TextUtils.isEmpty(it.title))     return "title:"+it.title;
        return "pos@"+System.identityHashCode(it);
    }
}
