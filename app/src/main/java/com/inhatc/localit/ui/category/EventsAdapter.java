package com.inhatc.localit.ui.category;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inhatc.localit.R;
import com.inhatc.localit.model.Event;   // model.Event 사용

import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {

    /** 클릭 리스너 인터페이스 */
    public interface OnEventClickListener {
        void onEventClick(Event event, int position);
        void onFavoriteClick(Event event, int position);
    }

    private List<Event> eventList;
    private OnEventClickListener listener;

    /** 생성자 */
    public EventsAdapter(List<Event> eventList, OnEventClickListener listener) {
        this.eventList = eventList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.bind(event, listener, position);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /** ViewHolder 내부 클래스 */
    static class EventViewHolder extends RecyclerView.ViewHolder {

        TextView title, date;
        ImageView image, btnFavorite;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textEventTitle);
            date = itemView.findViewById(R.id.textEventDate);
            image = itemView.findViewById(R.id.imageEvent);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }

        void bind(Event event, OnEventClickListener listener, int position) {
            title.setText(event.getTitle());
            date.setText(event.getDate());
            image.setImageResource(event.getImageResId());

            // 전체 클릭 → 상세 페이지 이동
            itemView.setOnClickListener(v -> listener.onEventClick(event, position));

            // 좋아요 버튼 클릭 → 즐겨찾기 토글
            btnFavorite.setOnClickListener(v -> listener.onFavoriteClick(event, position));

            // 좋아요 상태에 따라 버튼 이미지 변경
            btnFavorite.setImageResource(
                    event.isFavorite() ? R.drawable.img : R.drawable.ic_favorite_border_24
            );
        }
    }
}
