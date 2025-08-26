package com.inhatc.localit.Fragment;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.inhatc.localit.R;
import com.inhatc.localit.db.TouristSpot;

import java.util.ArrayList;
import java.util.HashSet;
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

    private HashSet<String> deactivatedSpotIds = new HashSet<>();

    public WishedSpotAdapter(@NonNull Context context, OnSpotClickListener listener, OnHeartClickListener heartClickListener) {
        this.context = context;
        this.listener = listener;
        this.heartClickListener = heartClickListener;
        setHasStableIds(true);
    }

    public void setDeactivatedSpotIds(HashSet<String> deactivatedSpotIds) {
        this.deactivatedSpotIds = deactivatedSpotIds;
    }

    public void setItems(List<TouristSpot> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        try {
            return Long.parseLong(String.valueOf(items.get(position).contentid));
        } catch (Exception e) {
            return items.get(position).hashCode();
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_wished_spot, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        TouristSpot spot = items.get(position);

        if (spot.contenttypeid == 99) {
            // 뉴스 항목의 경우, HTML 태그를 제거하고 이미지를 숨깁니다.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                h.title.setText(Html.fromHtml(spot.title, Html.FROM_HTML_MODE_LEGACY));
                h.addr.setText(Html.fromHtml(spot.addr1, Html.FROM_HTML_MODE_LEGACY));
            } else {
                h.title.setText(Html.fromHtml(spot.title));
                h.addr.setText(Html.fromHtml(spot.addr1));
            }
            h.thumb.setVisibility(View.GONE);
        } else {
            // 다른 콘텐츠 항목의 경우, 제목, 주소, 이미지를 정상적으로 표시합니다.
            h.title.setText(spot.title != null ? spot.title : "");
            h.addr.setText(spot.addr1 != null ? spot.addr1 : "");
            h.thumb.setVisibility(View.VISIBLE);

            String img = !TextUtils.isEmpty(spot.firstimage) ? spot.firstimage : null;
            Glide.with(context)
                    .load(img)
                    .placeholder(R.drawable.sample1)
                    .error(R.drawable.sample1)
                    .into(h.thumb);
        }

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(spot);
        });

        if (deactivatedSpotIds.contains(spot.contentid)) {
            h.heartButton.setImageResource(R.drawable.ic_favorite_border_24);
        } else {
            h.heartButton.setImageResource(R.drawable.ic_favorite_full);
        }

        h.heartButton.setOnClickListener(v -> {
            if (heartClickListener != null) {
                heartClickListener.onHeartClick(spot);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView thumb;
        TextView title;
        TextView addr;
        ImageButton heartButton;

        VH(@NonNull View itemView) {
            super(itemView);
            thumb = itemView.findViewById(R.id.imageThumb);
            title = itemView.findViewById(R.id.textTitle);
            addr = itemView.findViewById(R.id.textAddr);
            heartButton = itemView.findViewById(R.id.imageButtonHeart);
        }
    }
}