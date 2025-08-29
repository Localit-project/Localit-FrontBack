package com.inhatc.localit.api.home;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        TourItem tourItem = getItem(position);
        holder.bind(tourItem, imageOverrides.get(tourItem.contentid), clickListener);
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

        void bind(TourItem item, String imageUri, OnCourseClickListener listener) {
            if (imageUri != null) {
                imageView.setImageURI(Uri.parse(imageUri));
            }
            itemView.setOnClickListener(v -> listener.onCourseClick(item));
        }
    }

    private static final DiffUtil.ItemCallback<TourItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<TourItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull TourItem oldItem, @NonNull TourItem newItem) {
            return oldItem.contentid.equals(newItem.contentid);
        }

        @Override
        public boolean areContentsTheSame(@NonNull TourItem oldItem, @NonNull TourItem newItem) {
            // Since there's no title view, we can just check the content ID for changes.
            return oldItem.contentid.equals(newItem.contentid);
        }
    };
}