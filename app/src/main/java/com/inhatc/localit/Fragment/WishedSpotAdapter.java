package com.inhatc.localit.Fragment;

import android.content.Context;
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
import com.inhatc.localit.db.TouristSpot;

import java.util.ArrayList;
import java.util.HashSet; // [추가]
import java.util.List;

public class WishedSpotAdapter extends RecyclerView.Adapter<WishedSpotAdapter.VH> {

    public interface OnSpotClickListener {
        void onClick(@NonNull TouristSpot spot);
    }

    public interface OnHeartClickListener {
        void onHeartClick(@NonNull TouristSpot spot);
    }

    private final Context context;
    private final OnSpotClickListener listener;
    private final List<TouristSpot> items = new ArrayList<>();
    private final OnHeartClickListener heartClickListener;

    // [추가] 비활성화된 아이템들의 ID를 저장할 Set
    private HashSet<String> deactivatedSpotIds = new HashSet<>();

    public WishedSpotAdapter(@NonNull Context context, OnSpotClickListener listener, OnHeartClickListener heartClickListener) {
        this.context = context;
        this.listener = listener;
        this.heartClickListener = heartClickListener;
        setHasStableIds(true);
    }

    // [추가] Fragment로부터 비활성화 목록을 전달받는 메서드
    public void setDeactivatedSpotIds(HashSet<String> deactivatedSpotIds) {
        this.deactivatedSpotIds = deactivatedSpotIds;
    }

    public void setItems(List<TouristSpot> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @Override public long getItemId(int position) {
        try {
            return Long.parseLong(String.valueOf(items.get(position).contentid));
        } catch (Exception e) {
            return items.get(position).hashCode();
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_wished_spot, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        TouristSpot spot = items.get(position);

        h.title.setText(spot.title != null ? spot.title : "");
        h.addr.setText(spot.addr1 != null ? spot.addr1 : "");

        String img = !TextUtils.isEmpty(spot.firstimage) ? spot.firstimage : null;
        Glide.with(context)
                .load(img)
                .placeholder(R.drawable.sample1)
                .error(R.drawable.sample1)
                .into(h.thumb);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(spot);
        });

        // [수정] 하트 버튼 상태를 비활성화 목록에 따라 다르게 표시
        if (deactivatedSpotIds.contains(spot.contentid)) {
            // 비활성화 상태이면 '빈 하트'
            h.heartButton.setImageResource(R.drawable.ic_favorite_border_24);
        } else {
            // 활성화 상태이면 '채워진 하트'
            h.heartButton.setImageResource(R.drawable.ic_favorite_full);
        }

        h.heartButton.setOnClickListener(v -> {
            if (heartClickListener != null) {
                heartClickListener.onHeartClick(spot);
            }
        });
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView thumb;
        TextView title;
        TextView addr;
        ImageButton heartButton;

        VH(@NonNull View itemView) {
            super(itemView);
            thumb = itemView.findViewById(R.id.imageThumb);
            title = itemView.findViewById(R.id.textTitle);
            addr  = itemView.findViewById(R.id.textAddr);
            heartButton = itemView.findViewById(R.id.imageButtonHeart);
        }
    }
}