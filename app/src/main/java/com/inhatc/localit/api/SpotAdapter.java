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

import java.util.List;

public class SpotAdapter extends RecyclerView.Adapter<SpotAdapter.TourViewHolder> {
    private List<SpotResponse.Item> itemList;

    public SpotAdapter(List<SpotResponse.Item> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public TourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_spot_sub, parent, false);
        return new TourViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TourViewHolder holder, int position) {
        SpotResponse.Item item = itemList.get(position);

        holder.textTitle.setText(item.title);
        holder.textAddr.setText(item.addr1 != null ? item.addr1 : "주소 정보 없음");

        // 날짜 처리
        if (item.eventstartdate != null && item.eventenddate != null) {
            // 축제일 경우
            String start = formatDate(item.eventstartdate);
            String end = formatDate(item.eventenddate);
            holder.textDate.setText("기간: " + start + " ~ " + end);
        } else if (item.createdtime != null) {
            // 관광지일 경우
            String date = formatDate(item.createdtime);
            holder.textDate.setText("등록일: " + date);
        } else {
            holder.textDate.setText("날짜 정보 없음");
        }

        // 이미지 처리
        if (item.firstimage != null && !item.firstimage.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(item.firstimage)
                    .placeholder(R.drawable.sample1)
                    .into(holder.imageTour);
        } else {
            holder.imageTour.setImageResource(R.drawable.sample1);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    private String formatDate(String raw) {
        if (raw != null && raw.length() == 8) {
            return raw.substring(0, 4) + "." + raw.substring(4, 6) + "." + raw.substring(6);
        }
        return "";
    }

    public static class TourViewHolder extends RecyclerView.ViewHolder {
        ImageView imageTour;
        TextView textTitle, textAddr, textDate;

        public TourViewHolder(@NonNull View itemView) {
            super(itemView);
            imageTour = itemView.findViewById(R.id.imageTour);
            textTitle = itemView.findViewById(R.id.textTitle);
            textAddr = itemView.findViewById(R.id.textAddr);
            textDate = itemView.findViewById(R.id.textDate);
        }
    }
}