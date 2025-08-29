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

    // Activity와 통신하기 위한 인터페이스 정의
    public interface OnItemClickListener {
        void onItemClick(SpotResponse.Item item, int position);
    }
    public interface OnFavoriteClickListener {
        void onFavoriteClick(SpotResponse.Item item, int position);
    }

    private final List<SpotResponse.Item> items = new ArrayList<>();
    private final OnItemClickListener onItemClick;
    private final OnFavoriteClickListener onFavClick;

    // 찜한 아이템의 ID를 저장하는 Set (UI 표시용)
    private final Set<String> favoriteKeys = new HashSet<>();

    // 생성자
    public SpotAdapter(List<SpotResponse.Item> initial,
                       OnItemClickListener onItemClick,
                       OnFavoriteClickListener onFavClick) {
        if (initial != null) items.addAll(initial);
        this.onItemClick = onItemClick;
        this.onFavClick  = onFavClick;
        setHasStableIds(true);
    }

    // RecyclerView에 표시할 목록을 갱신하는 메서드
    public void submitList(List<SpotResponse.Item> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    // Activity로부터 찜 목록을 받아와 UI를 갱신하는 메서드
    public void updateFavorites(Set<String> keys) {
        favoriteKeys.clear();
        if (keys != null) favoriteKeys.addAll(keys);
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        // contentid를 고유 ID로 사용
        SpotResponse.Item item = items.get(position);
        if (item != null && !TextUtils.isEmpty(item.getContentid())) {
            return item.getContentid().hashCode();
        }
        return RecyclerView.NO_ID;
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

        // 데이터 바인딩
        h.title.setText(it != null && !TextUtils.isEmpty(it.getTitle()) ? it.getTitle() : "제목 없음");
        h.sub.setText(it != null && !TextUtils.isEmpty(it.getAddr1()) ? it.getAddr1().trim() : "지역정보 없음");

        if (h.image != null) {
            if (it != null && !TextUtils.isEmpty(it.getFirstimage())) {
                Glide.with(h.itemView.getContext())
                        .load(it.getFirstimage())
                        .centerCrop()
                        .placeholder(R.drawable.sample1)
                        .error(R.drawable.sample1)
                        .into(h.image);
            } else {
                h.image.setImageResource(R.drawable.sample1);
            }
        }

        // 찜 상태에 따라 하트 아이콘 설정
        boolean isFav = favoriteKeys.contains(it.getContentid());
        if (h.btnFavorite != null) {
            if (isFav) {
                h.btnFavorite.setImageResource(R.drawable.ic_favorite_full);
            } else {
                h.btnFavorite.setImageResource(R.drawable.ic_favorite_border_24);
            }
        }

        // 아이템 전체 클릭 리스너
        h.itemView.setOnClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && onItemClick != null) {
                onItemClick.onItemClick(items.get(pos), pos);
            }
        });

        // 찜 버튼 클릭 리스너
        if (h.btnFavorite != null) {
            h.btnFavorite.setOnClickListener(v -> {
                int pos = h.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;

                // UI 즉시 반응을 위해 로컬 상태를 먼저 변경하고 아이콘을 업데이트
                String key = items.get(pos).getContentid();
                if (favoriteKeys.contains(key)) {
                    favoriteKeys.remove(key);
                    h.btnFavorite.setImageResource(R.drawable.ic_favorite_border_24);
                } else {
                    favoriteKeys.add(key);
                    h.btnFavorite.setImageResource(R.drawable.ic_favorite_full);
                }

                // Activity에 실제 DB 저장을 요청
                if (onFavClick != null) {
                    onFavClick.onFavoriteClick(items.get(pos), pos);
                }
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
