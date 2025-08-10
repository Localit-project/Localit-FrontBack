package com.inhatc.localit.api.naver;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inhatc.localit.R;

import java.util.List;

public class NaverNewsAdapter extends RecyclerView.Adapter<NaverNewsAdapter.NewsViewHolder> {

    public interface OnNewsClickListener {
        void onNewsClick(NaverNewsResponse.Item item, int position);
        void onFavoriteClick(NaverNewsResponse.Item item, int position);
    }

    private List<NaverNewsResponse.Item> newsList;
    private OnNewsClickListener listener;

    public NaverNewsAdapter(List<NaverNewsResponse.Item> newsList, OnNewsClickListener listener) {
        this.newsList = newsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NaverNewsResponse.Item news = newsList.get(position);
        holder.bind(news, listener);
    }

    @Override
    public int getItemCount() {
        return newsList != null ? newsList.size() : 0;
    }

    // ✅ 데이터 갱신을 위한 헬퍼 메서드
    public void updateData(List<NaverNewsResponse.Item> newItems) {
        if (newItems != null) {
            newsList.clear();
            newsList.addAll(newItems);
            notifyDataSetChanged();
        }
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView newsTitle;
        TextView newsDescription;
        // ImageView btnFavorite; // 즐겨찾기 버튼은 레이아웃에 따라 추가/삭제

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            newsTitle = itemView.findViewById(R.id.textNewsTitle);
            newsDescription = itemView.findViewById(R.id.textNewsDescription);
            // btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }

        void bind(final NaverNewsResponse.Item item, final OnNewsClickListener listener) {
            if (item.getTitle() != null) {
                newsTitle.setText(Html.fromHtml(item.getTitle(), Html.FROM_HTML_MODE_COMPACT));
            }
            if (item.getDescription() != null) {
                newsDescription.setText(Html.fromHtml(item.getDescription(), Html.FROM_HTML_MODE_COMPACT));
            }

            itemView.setOnClickListener(v -> listener.onNewsClick(item, getAdapterPosition()));
            // btnFavorite.setOnClickListener(v -> listener.onFavoriteClick(item, getAdapterPosition()));
        }
    }
}