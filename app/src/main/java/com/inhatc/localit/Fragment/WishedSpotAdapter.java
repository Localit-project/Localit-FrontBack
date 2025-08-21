package com.inhatc.localit.Fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.inhatc.localit.R;
import com.inhatc.localit.db.TouristSpot; // DB의 TouristSpot 모델 사용
import java.util.ArrayList;
import java.util.List;

// ✨ 클래스 이름을 WishedSpotAdapter로 변경
public class WishedSpotAdapter extends RecyclerView.Adapter<WishedSpotAdapter.ViewHolder> {

    private final Context context;
    private List<TouristSpot> items = new ArrayList<>();

    public WishedSpotAdapter(Context context) {
        this.context = context;
    }

    public void setItems(List<TouristSpot> spotList) {
        this.items = spotList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 검색 결과와 동일한 레이아웃 재사용
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_spot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TouristSpot item = items.get(position);
        holder.bind(item, context);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageTourismThumb;
        TextView textTourismTitle, textTourismSub;
        View btnFavorite; // 하트 버튼 뷰

        ViewHolder(@NonNull View v) {
            super(v);
            imageTourismThumb = v.findViewById(R.id.imageTourismThumb);
            textTourismTitle = v.findViewById(R.id.textTourismTitle);
            textTourismSub = v.findViewById(R.id.textTourismSub);
            btnFavorite = v.findViewById(R.id.btnFavorite);
        }

        void bind(TouristSpot item, Context context) {
            textTourismTitle.setText(item.name);
            textTourismSub.setText(item.address);
            Glide.with(context)
                    .load(item.imageUrl)
                    // 이미지가 없을 경우를 대비한 플레이스홀더 이미지
                    .placeholder(R.drawable.ic_image_placeholder)
                    .into(imageTourismThumb);

            // 찜 목록에서는 하트 버튼을 보여줄 필요가 없으므로 숨깁니다.
            btnFavorite.setVisibility(View.GONE);
        }
    }
}