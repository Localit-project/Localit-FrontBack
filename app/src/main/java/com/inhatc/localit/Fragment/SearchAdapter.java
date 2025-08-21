package com.inhatc.localit.Fragment;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
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

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.VH> {

    public interface OnItemClickListener {
        void onItemClick(SpotResponse.Item item);
    }
    public interface OnFavoriteClickListener {
        void onFavoriteClick(SpotResponse.Item item);
    }

    private final Context context;
    private final List<SpotResponse.Item> data = new ArrayList<>();
    private final OnItemClickListener itemClickListener;
    private final OnFavoriteClickListener favoriteClickListener;

    // ✨ 1. '찜'된 아이템의 contentid를 저장할 Set 추가
    // Set을 사용하면 특정 아이디가 포함되어 있는지 매우 빠르게 확인할 수 있습니다.
    private final Set<String> favoriteIds = new HashSet<>();

    public SearchAdapter(Context context,
                         OnItemClickListener itemClickListener,
                         OnFavoriteClickListener favoriteClickListener) {
        this.context = context;
        this.itemClickListener = itemClickListener;
        this.favoriteClickListener = favoriteClickListener;
    }

    public void setItems(List<SpotResponse.Item> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
    }

    // ✨ 3. 외부(Fragment)에서 찜 목록을 갱신하는 함수 추가
    public void updateFavorites(Set<String> newFavoriteIds) {
        favoriteIds.clear();
        if (newFavoriteIds != null) {
            favoriteIds.addAll(newFavoriteIds);
        }
        notifyDataSetChanged(); // 찜 상태가 변경되었으니 전체 목록을 갱신하여 아이콘을 바꿉니다.
    }


    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_spot, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        SpotResponse.Item item = data.get(position);

        // --- 기존 코드 (수정 없음) ---
        h.textTourismTitle.setText(item.getTitle() == null ? "" : item.getTitle());
        String addr = !TextUtils.isEmpty(item.getAddr1()) ? item.getAddr1()
                : (!TextUtils.isEmpty(item.getAddr2()) ? item.getAddr2() : "");
        h.textTourismSub.setText(addr == null ? "" : addr);

        String imgUrl = item.getFirstimage();
        if (TextUtils.isEmpty(imgUrl)) {
            h.imageTourismThumb.setImageDrawable(new ColorDrawable(0xFFEFEFEF));
        } else {
            Glide.with(context).load(imgUrl)
                    .error(new ColorDrawable(0xFFEFEFEF))
                    .into(h.imageTourismThumb);
        }

        // --- 클릭 리스너 (수정 없음) ---
        h.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) itemClickListener.onItemClick(item);
        });
        h.btnFavorite.setOnClickListener(v -> {
            if (favoriteClickListener != null) favoriteClickListener.onFavoriteClick(item);
        });

        // ✨ 2. '찜' 상태에 따라 하트 아이콘 변경하는 로직 추가
        if (favoriteIds.contains(item.getContentid())) {
            // 이 아이템의 ID가 찜 목록 Set에 포함되어 있다면
            //h.btnFavorite.setImageResource(R.drawable.img); // 채워진 하트
        } else {
            // 포함되어 있지 않다면
            h.btnFavorite.setImageResource(R.drawable.ic_favorite_border_24); // 빈 하트
        }
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imageTourismThumb;
        TextView textTourismTitle, textTourismSub;
        ImageButton btnFavorite;
        VH(@NonNull View v) {
            super(v);
            imageTourismThumb = v.findViewById(R.id.imageTourismThumb);
            textTourismTitle = v.findViewById(R.id.textTourismTitle);
            textTourismSub = v.findViewById(R.id.textTourismSub);
            btnFavorite = v.findViewById(R.id.btnFavorite);
        }
    }
}