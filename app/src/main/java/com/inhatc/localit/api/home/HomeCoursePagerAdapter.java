package com.inhatc.localit.api.home;

import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.inhatc.localit.R;

import java.util.List;
import java.util.Map;

public class HomeCoursePagerAdapter extends ListAdapter<TourItem, HomeCoursePagerAdapter.CourseViewHolder> {

    private final Map<String, String> imageOverrides;
    private final OnCourseClickListener clickListener;

    public interface OnCourseClickListener {
        void onCourseClick(TourItem item);
    }

    public HomeCoursePagerAdapter(Map<String, String> imageOverrides, OnCourseClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.imageOverrides = imageOverrides;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        TourItem tourItem = getItem(position);
        String override = imageOverrides.get(tourItem.contentid);
        holder.bind(tourItem, override, clickListener);
    }

    public void submit(List<TourItem> list) {
        submitList(list);
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;

        CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageCourse);
        }

        void bind(TourItem item, String fallbackImageUri, OnCourseClickListener listener) {
            // 1순위: API로 받은 firstimage, 2순위: 오버라이드 리소스
            String imageToLoad = !TextUtils.isEmpty(item.firstimage) ? item.firstimage : fallbackImageUri;

            if (!TextUtils.isEmpty(imageToLoad)) {
                if (imageToLoad.startsWith("http")) {
                    Glide.with(imageView.getContext())
                            .load(imageToLoad)
                            .placeholder(R.drawable.bg_image_round) // 필요시 다른 플레이스홀더
                            .error(R.drawable.bg_image_round)
                            .into(imageView);
                } else {
                    imageView.setImageURI(Uri.parse(imageToLoad));
                }
            } else {
                imageView.setImageResource(R.drawable.bg_image_round);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onCourseClick(item);
            });
        }
    }

    private static final DiffUtil.ItemCallback<TourItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<TourItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull TourItem oldItem, @NonNull TourItem newItem) {
                    return oldItem != null && newItem != null &&
                            TextUtils.equals(oldItem.contentid, newItem.contentid);
                }

                @Override
                public boolean areContentsTheSame(@NonNull TourItem oldItem, @NonNull TourItem newItem) {
                    // 간단 비교 (id / title / firstimage)
                    return TextUtils.equals(oldItem.contentid, newItem.contentid) &&
                            TextUtils.equals(oldItem.title, newItem.title) &&
                            TextUtils.equals(oldItem.firstimage, newItem.firstimage);
                }
            };
}
