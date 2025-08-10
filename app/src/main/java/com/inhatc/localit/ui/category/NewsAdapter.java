
package com.inhatc.localit.ui.category;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inhatc.localit.R;
import com.inhatc.localit.model.Event;   // 뉴스도 임시로 Event 모델 재사용

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.VH> {

    /** TourismAdapter 패턴과 동일한 콜백 2종 */
    public interface OnItemClick {
        void onNewsClick(Event news, int position);
    }
    public interface OnFavClick {
        void onFavoriteClick(Event news, int position);
    }

    private final List<Event> items = new ArrayList<>();
    private final OnItemClick onItemClick;
    private final OnFavClick onFavClick;

    /** TourismAdapter와 동일하게: 초기 리스트 + 콜백 2개 */
    public NewsAdapter(List<Event> initial,
                       OnItemClick onItemClick,
                       OnFavClick onFavClick) {
        if (initial != null) items.addAll(initial);
        this.onItemClick = onItemClick;
        this.onFavClick  = onFavClick;
    }

    /** 리스트 갱신 (TourismAdapter의 submitList와 동일) */
    public void submitList(List<Event> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                // item_event 레이아웃 재사용 (imageEvent, textEventTitle, textEventDate, btnFavorite)
                .inflate(R.layout.item_event, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        final Event it = items.get(position);

        // 제목/날짜 바인딩
        h.title.setText(it != null && it.getTitle() != null ? it.getTitle() : "");
        h.date.setText(it != null && it.getDate()  != null ? it.getDate()  : "");

        // 썸네일(임시 리소스 사용)
        if (it != null && it.getImageResId() != 0) {
            h.image.setImageResource(it.getImageResId());
        } else {
            h.image.setImageResource(R.drawable.sample1);
        }

        // 아이템 클릭 → 외부 콜백 위임
        h.itemView.setOnClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            if (onItemClick != null) onItemClick.onNewsClick(items.get(pos), pos);
        });

        // 즐겨찾기 클릭 → 외부 콜백 위임 (아이콘 토글은 외부에서 처리)
        if (h.btnFavorite != null) {
            h.btnFavorite.setOnClickListener(v -> {
                int pos = h.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                if (onFavClick != null) onFavClick.onFavoriteClick(items.get(pos), pos);
            });
        } else {
            // 버튼 없으면 롱클릭으로 대체 (TourismAdapter와 동일한 fallback)
            h.itemView.setOnLongClickListener(v -> {
                int pos = h.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return true;
                if (onFavClick != null) onFavClick.onFavoriteClick(items.get(pos), pos);
                return true;
            });
        }
    }

    @Override public int getItemCount() { return items.size(); }

    /** ViewHolder (TourismAdapter.VH와 유사) */
    static class VH extends RecyclerView.ViewHolder {
        ImageView image;       // @id/imageEvent
        TextView title;        // @id/textEventTitle
        TextView date;         // @id/textEventDate
        ImageView btnFavorite; // @id/btnFavorite (선택)

        VH(@NonNull View v) {
            super(v);
            image = v.findViewById(R.id.imageEvent);
            title = v.findViewById(R.id.textEventTitle);
            date  = v.findViewById(R.id.textEventDate);
            btnFavorite = v.findViewById(R.id.btnFavorite);

            // 버튼이 루트 클릭 포커스를 뺏지 않도록
            if (btnFavorite != null) {
                btnFavorite.setFocusable(false);
                btnFavorite.setFocusableInTouchMode(false);
            }
        }
    }
}