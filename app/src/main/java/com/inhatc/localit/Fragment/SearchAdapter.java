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
import com.inhatc.localit.api.TourResponse;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.VH> {

    public interface OnItemClickListener {
        void onItemClick(TourResponse.Item item);
    }
    public interface OnFavoriteClickListener {
        void onFavoriteClick(TourResponse.Item item);
    }

    private final Context context;
    private final List<TourResponse.Item> data = new ArrayList<>();
    private final OnItemClickListener itemClickListener;
    private final OnFavoriteClickListener favoriteClickListener;

    public SearchAdapter(Context context,
                         OnItemClickListener itemClickListener,
                         OnFavoriteClickListener favoriteClickListener) {
        this.context = context;
        this.itemClickListener = itemClickListener;
        this.favoriteClickListener = favoriteClickListener;
    }

    public void setItems(List<TourResponse.Item> items) {
        data.clear();
        if (items != null) data.addAll(items);
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
        TourResponse.Item item = data.get(position);

        h.textTourismTitle.setText(item.getTitle() == null ? "" : item.getTitle());
        String addr = !TextUtils.isEmpty(item.getAddr1()) ? item.getAddr1()
                : (!TextUtils.isEmpty(item.getAddr2()) ? item.getAddr2() : "");
        h.textTourismSub.setText(addr == null ? "" : addr);

        String imgUrl = item.getFirstimage();
        if (TextUtils.isEmpty(imgUrl)) {
            h.imageTourismThumb.setImageDrawable(new ColorDrawable(0xFFEFEFEF)); // 플레이스홀더 없이 회색
        } else {
            Glide.with(context).load(imgUrl)
                    .error(new ColorDrawable(0xFFEFEFEF))
                    .into(h.imageTourismThumb);
        }

        h.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) itemClickListener.onItemClick(item);
        });
        h.btnFavorite.setOnClickListener(v -> {
            if (favoriteClickListener != null) favoriteClickListener.onFavoriteClick(item);
        });
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