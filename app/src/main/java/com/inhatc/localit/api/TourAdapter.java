package com.inhatc.localit.api;

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

public class TourAdapter extends RecyclerView.Adapter<TourAdapter.TourViewHolder> {

    private List<TourResponse.Item> itemList;

    public TourAdapter(List<TourResponse.Item> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public TourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_region, parent, false);
        return new TourViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TourViewHolder holder, int position) {
        TourResponse.Item item = itemList.get(position);

        holder.textTitle.setText(item.title);
        holder.textAddr.setText(item.addr1);

        if (item.createdtime != null && item.createdtime.length() >= 8) {
            String date = item.createdtime.substring(0, 4) + "." +
                    item.createdtime.substring(4, 6) + "." +
                    item.createdtime.substring(6, 8);
            holder.textDate.setText(date);
        } else {
            holder.textDate.setText("날짜 없음");
        }

        if (item.firstimage != null && !item.firstimage.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(item.firstimage)
                    .into(holder.imageTour);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class TourViewHolder extends RecyclerView.ViewHolder {
        ImageView imageTour;
        TextView textTitle, textAddr, textDate;

        public TourViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textAddr = itemView.findViewById(R.id.textAddr);
            textDate = itemView.findViewById(R.id.textDate);
        }
    }
}
