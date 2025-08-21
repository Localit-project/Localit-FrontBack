package com.inhatc.localit.ui.category;

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
import com.inhatc.localit.api.naver.NaverNewsResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.VH> {

    public interface OnNewsClickListener {
        void onNewsClick(NaverNewsResponse.Item item, int position);
        void onFavoriteClick(NaverNewsResponse.Item item, int position);
    }

    private final List<NaverNewsResponse.Item> items = new ArrayList<>();
    private final OnNewsClickListener listener;

    // 즐겨찾기 상태 (link 우선, 없으면 title)
    private final Set<String> favoriteKeys = new HashSet<>();

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

    public void setFavoriteKeys(Set<String> keys) {
        favoriteKeys.clear();
        if (keys != null) favoriteKeys.addAll(keys);
        notifyDataSetChanged();
    }

    public Set<String> getFavoriteKeys() {
        return new HashSet<>(favoriteKeys);
    }

    @Override public long getItemId(int position) {
        return keyOf(items.get(position)).hashCode();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ⬇️ 네가 붙여준 XML 파일명으로 맞춰줘 (item_news.xml 가정)
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        NaverNewsResponse.Item it = items.get(position);

        // 네이버 응답은 HTML 태그 포함 → 사람 읽기용 변환
        h.title.setText(htmlToText(it.getTitle()));
        h.description.setText(htmlToText(it.getDescription()));

        // ⭐ 셀렉터용 selected 상태 적용 (스크롤 재활용 대비해서 "항상" 해줘야 함)
        boolean isFav = favoriteKeys.contains(keyOf(it));
        h.btnFavorite.setSelected(isFav);

        // 카드 클릭
        h.itemView.setOnClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            if (listener != null) listener.onNewsClick(items.get(pos), pos);
        });

        // 즐겨찾기 클릭 → 상태 반전 + selected 토글 (notify 불필요)
        h.btnFavorite.setOnClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            String key = keyOf(items.get(pos));
            boolean newState;
            if (favoriteKeys.contains(key)) {
                favoriteKeys.remove(key);
                newState = false;
            } else {
                favoriteKeys.add(key);
                newState = true;
            }
            h.btnFavorite.setSelected(newState); // ⬅️ 아이콘 즉시 변경(셀렉터가 처리)

            if (listener != null) listener.onFavoriteClick(items.get(pos), pos);
        });
    }

    @Override public int getItemCount() { return items.size(); }

    private String keyOf(NaverNewsResponse.Item it) {
        if (it == null) return "@null";
        if (!TextUtils.isEmpty(it.getLink()))  return "link:" + it.getLink();
        if (!TextUtils.isEmpty(it.getTitle())) return "title:" + it.getTitle();
        return "pos@" + System.identityHashCode(it);
    }

    private CharSequence htmlToText(String html) {
        if (html == null) return "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        }
        return Html.fromHtml(html);
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title;        // @id/textNewsTitle
        TextView description;  // @id/textNewsDescription
        ImageButton btnFavorite; // @id/btnFavorite

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