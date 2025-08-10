package com.inhatc.localit.ui.category;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.inhatc.localit.R;
import com.inhatc.localit.api.TourResponse;
import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.VH> {

    public interface OnItemClick { void onEventClick(TourResponse.Item item, int position); }
    public interface OnFavClick  { void onFavoriteClick(TourResponse.Item item, int position); }

    private final List<TourResponse.Item> items;
    private final OnItemClick onItemClick;
    private final OnFavClick onFavClick;

    public EventsAdapter(List<TourResponse.Item> items, OnItemClick onItemClick, OnFavClick onFavClick) {
        this.items = items;
        this.onItemClick = onItemClick;
        this.onFavClick = onFavClick;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        TourResponse.Item it = items.get(position);

        h.title.setText(it.title != null ? it.title : "");

        String start = formatDate(it.eventstartdate);
        String end   = formatDate(it.eventenddate);
        String dateText = (!start.isEmpty() && !end.isEmpty()) ? (start + " ~ " + end)
                : (!start.isEmpty() ? start : (!end.isEmpty() ? end : "일정 미정"));
        h.date.setText(dateText);

        if (it.firstimage != null && !it.firstimage.isEmpty()) {
            Glide.with(h.itemView.getContext())
                    .load(it.firstimage)
                    .centerCrop()
                    .placeholder(R.drawable.sample1)
                    .error(R.drawable.sample1)
                    .into(h.image);
        } else {
            h.image.setImageResource(R.drawable.sample1);
        }

        h.itemView.setOnClickListener(v -> { if (onItemClick != null) onItemClick.onEventClick(it, h.getBindingAdapterPosition()); });
        h.btnFavorite.setOnClickListener(v -> { if (onFavClick != null) onFavClick.onFavoriteClick(it, h.getBindingAdapterPosition()); });
    }

    @Override public int getItemCount() { return items == null ? 0 : items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView date;
        ImageView btnFavorite;
        VH(@NonNull View v) {
            super(v);
            image = v.findViewById(R.id.imageEvent);
            title = v.findViewById(R.id.textEventTitle);
            date = v.findViewById(R.id.textEventDate);
            btnFavorite = v.findViewById(R.id.btnFavorite);
        }
    }

    private String formatDate(String raw) {
        return (raw != null && raw.length() >= 8) ? raw.substring(0,4)+"."+raw.substring(4,6)+"."+raw.substring(6,8) : "";
    }
}