package com.inhatc.localit.api.home;

import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.inhatc.localit.R;

import java.util.List;
import java.util.Map;

public class HomeCoursePagerAdapter
        extends ListAdapter<TourItem, HomeCoursePagerAdapter.VH> {

    public interface OnCourseClick { void onClick(TourItem item); }

    private final Map<String, String> imageOverride; // contentId -> resource/URL
    private final OnCourseClick click;

    public HomeCoursePagerAdapter(@NonNull Map<String, String> imageOverride,
                                  @NonNull OnCourseClick click) {
        super(DIFF);
        this.imageOverride = imageOverride;
        this.click = click;
    }

    public void submit(List<TourItem> list) { submitList(list); }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_course, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        TourItem item = getItem(position);
        if (item == null) return;

        String override = imageOverride == null ? null : imageOverride.get(item.contentid);
        if (!TextUtils.isEmpty(override)) {
            try {
                Glide.with(h.image)
                        .load(Uri.parse(override))
                        .placeholder(R.drawable.sample1)
                        .error(R.drawable.sample1)
                        .into(h.image);
            } catch (Exception e) {
                loadFallback(h, item);
            }
        } else {
            loadFallback(h, item);
        }
        h.card.setOnClickListener(v -> click.onClick(item));
    }

    private void loadFallback(@NonNull VH h, @NonNull TourItem item) {
        if (!TextUtils.isEmpty(item.firstimage)) {
            Glide.with(h.image)
                    .load(item.firstimage)
                    .placeholder(R.drawable.sample1)
                    .error(R.drawable.sample1)
                    .into(h.image);
        } else {
            h.image.setImageResource(R.drawable.sample1);
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        final CardView card;
        final ImageView image;
        VH(@NonNull View itemView) {
            super(itemView);
            card = (CardView) itemView;
            image = itemView.findViewById(R.id.imageCourse);
        }
    }

    private static final DiffUtil.ItemCallback<TourItem> DIFF =
            new DiffUtil.ItemCallback<TourItem>() {
                @Override public boolean areItemsTheSame(@NonNull TourItem a, @NonNull TourItem b) {
                    return TextUtils.equals(a.contentid, b.contentid);
                }
                @Override public boolean areContentsTheSame(@NonNull TourItem a, @NonNull TourItem b) {
                    return TextUtils.equals(a.title, b.title)
                            && TextUtils.equals(a.firstimage, b.firstimage);
                }
            };
}
