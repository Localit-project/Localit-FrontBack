package com.inhatc.localit.api.naver;

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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Naver 뉴스 카드 어댑터
 * - 제목/설명: HTML 제거 후 표시
 * - 즐겨찾기: selector_favorite_heart + setSelected(...) 토글 방식
 */
public class NaverNewsAdapter extends RecyclerView.Adapter<NaverNewsAdapter.NewsViewHolder> {

    public interface OnNewsClickListener {
        void onNewsClick(NaverNewsResponse.Item item, int position);
        void onFavoriteClick(NaverNewsResponse.Item item, int position);
    }

    private final List<NaverNewsResponse.Item> newsList = new ArrayList<>();
    private final OnNewsClickListener listener;

    /** 즐겨찾기 상태 (link 우선, 없으면 title 기준) */
    private final Set<String> favoriteKeys = new HashSet<>();

    public NaverNewsAdapter(List<NaverNewsResponse.Item> newsList, OnNewsClickListener listener) {
        if (newsList != null) this.newsList.addAll(newsList);
        this.listener = listener;
        setHasStableIds(true);
    }

    // 데이터 갱신
    public void updateData(List<NaverNewsResponse.Item> newItems) {
        this.newsList.clear();
        if (newItems != null) this.newsList.addAll(newItems);
        notifyDataSetChanged();
    }

    // (선택) 즐겨찾기 복구/저장용
    public void setFavoriteKeys(Set<String> keys) {
        favoriteKeys.clear();
        if (keys != null) favoriteKeys.addAll(keys);
        notifyDataSetChanged();
    }
    public Set<String> getFavoriteKeys() {
        return new HashSet<>(favoriteKeys);
    }

    @Override
    public long getItemId(int position) {
        return keyOf(newsList.get(position)).hashCode();
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news, parent, false); // textNewsTitle, textNewsDescription, btnFavorite
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder h, int position) {
        NaverNewsResponse.Item item = newsList.get(position);

        // HTML 제거 후 표시
        h.newsTitle.setText(htmlToText(item.getTitle()));
        h.newsDescription.setText(htmlToText(item.getDescription()));

        // ★ 셀렉터 selected 상태 반영(스크롤 재활용 대비: 매 바인딩마다)
        boolean isFav = favoriteKeys.contains(keyOf(item));
        h.btnFavorite.setSelected(isFav);

        // 카드 클릭
        h.itemView.setOnClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            if (listener != null) listener.onNewsClick(newsList.get(pos), pos);
        });

        // 즐겨찾기 클릭 → 상태 반전 + selected 토글(즉시 아이콘 변경)
        h.btnFavorite.setOnClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            String key = keyOf(newsList.get(pos));
            boolean newState;
            if (favoriteKeys.contains(key)) {
                favoriteKeys.remove(key);
                newState = false;
            } else {
                favoriteKeys.add(key);
                newState = true;
            }
            h.btnFavorite.setSelected(newState); // notify 없이 아이콘 즉시 반영

            if (listener != null) listener.onFavoriteClick(newsList.get(pos), pos);
        });
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    /** link > title > fallback 순으로 키 생성 */
    private String keyOf(NaverNewsResponse.Item it) {
        if (it == null) return "@null";
        if (!TextUtils.isEmpty(it.getLink()))  return "link:" + it.getLink();
        if (!TextUtils.isEmpty(it.getTitle())) return "title:" + it.getTitle();
        return "pos@" + System.identityHashCode(it);
    }

    /** HTML → 텍스트 */
    private CharSequence htmlToText(String html) {
        if (html == null) return "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        }
        return Html.fromHtml(html);
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView newsTitle;
        TextView newsDescription;
        ImageButton btnFavorite;

        NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            newsTitle = itemView.findViewById(R.id.textNewsTitle);
            newsDescription = itemView.findViewById(R.id.textNewsDescription);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            if (btnFavorite != null) {
                btnFavorite.setFocusable(false);
                btnFavorite.setFocusableInTouchMode(false);
            }
        }
    }
}
