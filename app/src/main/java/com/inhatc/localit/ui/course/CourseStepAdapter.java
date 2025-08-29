package com.inhatc.localit.ui.course;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.inhatc.localit.R;
import com.inhatc.localit.api.home.CourseInfoResponse;

import java.util.ArrayList;
import java.util.List;

public class CourseStepAdapter extends RecyclerView.Adapter<CourseStepAdapter.VH> {

    private final List<CourseInfoResponse.CourseInfoItem> data = new ArrayList<>();

    public void submit(List<CourseInfoResponse.CourseInfoItem> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course_step, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        CourseInfoResponse.CourseInfoItem it = data.get(pos);
        h.title.setText((pos + 1) + ". " + (it.subname != null ? it.subname : ""));
        h.desc.setText(!TextUtils.isEmpty(it.subdetailoverview) ? it.subdetailoverview : "");
        if (!TextUtils.isEmpty(it.subdetailimg)) {
            h.image.setVisibility(View.VISIBLE);
            Glide.with(h.image.getContext()).load(it.subdetailimg)
                    .placeholder(R.drawable.bg_image_round)
                    .into(h.image);
        } else {
            h.image.setVisibility(View.GONE);
        }
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, desc;
        ImageView image;
        VH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.tvStepTitle);
            desc  = v.findViewById(R.id.tvStepDesc);
            image = v.findViewById(R.id.ivStepImage);
        }
    }
}
