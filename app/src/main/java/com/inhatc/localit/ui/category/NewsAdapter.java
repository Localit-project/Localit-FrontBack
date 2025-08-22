package com.inhatc.localit.ui.category; // 1. 패키지 주소를 올바르게 수정

import android.os.Build;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inhatc.localit.R;
// 2. NaverNewsResponse의 정확한 주소를 알려주는 import 문 추가
import com.inhatc.localit.api.naver.NaverNewsResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// ▼▼▼▼▼ 클래스 이름을 파일 이름과 일치하도록 수정 ▼▼▼▼▼
public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.VH> {

    public interface OnNewsClickListener {
        void onNewsClick(NaverNewsResponse.Item item, int position);
        void onFavoriteClick(NaverNewsResponse.Item item, int position);
    }

    private final List<NaverNewsResponse.Item> items = new ArrayList<>();
    private final OnNewsClickListener listener;

    // 찜한 뉴스의 ID(링크)를 저장하는 Set
    private final Set<String> favoriteKeys = new HashSet<>();

    // ▼▼▼▼▼ 생성자 이름을 클래스 이름과 일치하도록 수정 ▼▼▼▼▼
    public NewsAdapter(List<NaverNewsResponse.Item> initial, OnNewsClickListener listener) {
        if (initial != null) items.addAll(initial);
        this.listener = listener;
        setHasStableIds(true);
    }

    public void updateData(List<NaverNewsResponse.Item> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    // 3. NewsActivity와 연동하기 위한 updateFavorites 메서드
    public void updateFavorites(Set<String> keys) {
        favoriteKeys.clear();
        if (keys != null) favoriteKeys.addAll(keys);
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        String link = items.get(position).getLink();
        return link != null ? link.hashCode() : RecyclerView.NO_ID;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        NaverNewsResponse.Item it = items.get(position);

        h.title.setText(htmlToText(it.getTitle()));
        h.description.setText(htmlToText(it.getDescription()));

        // 4. 찜 상태에 따라 아이콘을 직접 변경하는 로직
        boolean isFav = favoriteKeys.contains(it.getLink());
        if (isFav) {
            h.btnFavorite.setImageResource(R.drawable.ic_favorite_full);
        } else {
            h.btnFavorite.setImageResource(R.drawable.ic_favorite_border_24);
        }

        // 카드 클릭
        h.itemView.setOnClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && listener != null) {
                listener.onNewsClick(items.get(pos), pos);
            }
        });

        // 찜 버튼 클릭
        h.btnFavorite.setOnClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            // 5. 아이콘 즉시 변경 로직
            String key = items.get(pos).getLink();
            if (favoriteKeys.contains(key)) {
                favoriteKeys.remove(key);
                h.btnFavorite.setImageResource(R.drawable.ic_favorite_border_24);
            } else {
                favoriteKeys.add(key);
                h.btnFavorite.setImageResource(R.drawable.ic_favorite_full);
            }

            // Activity에 실제 DB 저장을 요청
            if (listener != null) {
                listener.onFavoriteClick(items.get(pos), pos);
            }
        });
    }

    @Override public int getItemCount() { return items.size(); }

    private CharSequence htmlToText(String html) {
        if (html == null) return "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        }
        //noinspection deprecation
        return Html.fromHtml(html);
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title;
        TextView description;
        ImageButton btnFavorite;

        VH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.textNewsTitle);
            description = v.findViewById(R.id.textNewsDescription);
            btnFavorite = v.findViewById(R.id.btnFavorite);
            btnFavorite.setFocusable(false);
            btnFavorite.setFocusableInTouchMode(false);
        }
    }
}
