package com.inhatc.localit.ui.category;

import android.text.TextUtils;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SpotAdapter extends RecyclerView.Adapter<SpotAdapter.VH> {

    public interface OnItemClick {
        void onTourismClick(SpotResponse.Item item, int position);
    }
    public interface OnFavClick {
        void onFavoriteClick(SpotResponse.Item item, int position);
    }

    private final List<SpotResponse.Item> items = new ArrayList<>();
    private final OnItemClick onItemClick;
    private final OnFavClick onFavClick;

    // 즐겨찾기 상태( contentid 우선, 없으면 title )
    private final Set<String> favoriteKeys = new HashSet<>();

    public SpotAdapter(List<SpotResponse.Item> initial,
                       OnItemClick onItemClick,
                       OnFavClick onFavClick) {
        if (initial != null) items.addAll(initial);
        this.onItemClick = onItemClick;
        this.onFavClick  = onFavClick;
        setHasStableIds(true);
    }

    public void submitList(List<SpotResponse.Item> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    /** (선택) 즐겨찾기 복구/저장 */
    public void setFavoriteKeys(Set<String> keys) {
        favoriteKeys.clear();
        if (keys != null) favoriteKeys.addAll(keys);
        notifyDataSetChanged();
    }
    public Set<String> getFavoriteKeys() { return new HashSet<>(favoriteKeys); }

    @Override public long getItemId(int position) {
        return keyOf(items.get(position)).hashCode();
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
        h.title.setText(it != null && !TextUtils.isEmpty(it.title) ? it.title : "제목 없음");

        // 주소
        if (h.sub != null) {
            String addr = (it != null && !TextUtils.isEmpty(it.addr1)) ? it.addr1.trim() : "지역정보 없음";
            h.sub.setText(addr);
        }

        // 이미지
        if (h.image != null) {
            if (it != null && !TextUtils.isEmpty(it.firstimage)) {
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

        // ★ 즐겨찾기 아이콘: selected 상태로 셀렉터 작동
        boolean isFav = favoriteKeys.contains(keyOf(it));
        if (h.btnFavorite != null) h.btnFavorite.setSelected(isFav);

        // 아이템 클릭
        h.itemView.setOnClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            if (onItemClick != null) onItemClick.onTourismClick(items.get(pos), pos);
        });

        // 즐겨찾기 클릭 → 상태 토글 + selected 변경
        if (h.btnFavorite != null) {
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
                h.btnFavorite.setSelected(newState); // notify 없이 즉시 반영

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

    private String keyOf(SpotResponse.Item it) {
        if (it == null) return "@null";
        if (!TextUtils.isEmpty(it.contentid)) return "id:" + it.contentid;
        if (!TextUtils.isEmpty(it.title))     return "title:" + it.title;
        return "pos@" + System.identityHashCode(it);
    }

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
            if (btnFavorite != null) {
                btnFavorite.setFocusable(false);
                btnFavorite.setFocusableInTouchMode(false);
            }
        }
    }
}
