package com.inhatc.localit.ui.category;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inhatc.localit.R;
import com.inhatc.localit.model.Event;   // 뉴스도 임시로 Event 모델 재사용 (API 붙일 때 News 모델로 변경)

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    /** 클릭 리스너 인터페이스 */
    public interface OnNewsClickListener {
        void onNewsClick(Event news, int position);
        void onFavoriteClick(Event news, int position);
    }

    private List<Event> newsList;
    private OnNewsClickListener listener;

    /** 생성자 */
    public NewsAdapter(List<Event> newsList, OnNewsClickListener listener) {
        this.newsList = newsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false); // ✅ item_event 재사용
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        Event news = newsList.get(position);
        holder.bind(news, listener, position);
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    /** ViewHolder 내부 클래스 */
    static class NewsViewHolder extends RecyclerView.ViewHolder {

        TextView title, date;
        ImageView image, btnFavorite;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textEventTitle);
            date = itemView.findViewById(R.id.textEventDate);
            image = itemView.findViewById(R.id.imageEvent);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }

        void bind(Event news, OnNewsClickListener listener, int position) {
            title.setText(news.getTitle());
            date.setText(news.getDate());
            image.setImageResource(news.getImageResId());

            itemView.setOnClickListener(v -> listener.onNewsClick(news, position));

            btnFavorite.setOnClickListener(v -> listener.onFavoriteClick(news, position));

            btnFavorite.setImageResource(
                    news.isFavorite() ? R.drawable.img : R.drawable.ic_favorite_border_24
            );
        }
    }
}
