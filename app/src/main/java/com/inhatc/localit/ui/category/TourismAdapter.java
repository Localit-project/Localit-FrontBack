package com.inhatc.localit.ui.category;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inhatc.localit.R;
import com.inhatc.localit.model.Event;   // 관광지도 API 붙기 전까지 Event 모델 임시 재사용

import java.util.List;

public class TourismAdapter extends RecyclerView.Adapter<TourismAdapter.TourismViewHolder> {

    /** 클릭 리스너 인터페이스 */
    public interface OnTourismClickListener {
        void onTourismClick(Event tourism, int position);
        void onFavoriteClick(Event tourism, int position);
    }

    private List<Event> tourismList;
    private OnTourismClickListener listener;

    /** 생성자 */
    public TourismAdapter(List<Event> tourismList, OnTourismClickListener listener) {
        this.tourismList = tourismList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TourismViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false); // ✅ item_event 재사용
        return new TourismViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TourismViewHolder holder, int position) {
        Event tourism = tourismList.get(position);
        holder.bind(tourism, listener, position);
    }

    @Override
    public int getItemCount() {
        return tourismList.size();
    }

    /** ViewHolder 내부 클래스 */
    static class TourismViewHolder extends RecyclerView.ViewHolder {

        TextView title, date;
        ImageView image, btnFavorite;

        public TourismViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textEventTitle);
            date = itemView.findViewById(R.id.textEventDate);
            image = itemView.findViewById(R.id.imageEvent);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }

        void bind(Event tourism, OnTourismClickListener listener, int position) {
            title.setText(tourism.getTitle());
            date.setText(tourism.getDate());
            image.setImageResource(tourism.getImageResId());

            itemView.setOnClickListener(v -> listener.onTourismClick(tourism, position));

            btnFavorite.setOnClickListener(v -> listener.onFavoriteClick(tourism, position));

            btnFavorite.setImageResource(
                    tourism.isFavorite() ? R.drawable.img : R.drawable.ic_favorite_border_24
            );
        }
    }
}
