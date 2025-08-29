package com.inhatc.localit.Fragment;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.inhatc.localit.R;
import com.inhatc.localit.db.TouristSpot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class WishedSpotAdapter extends RecyclerView.Adapter<WishedSpotAdapter.VH> {

    public interface OnSpotClickListener {
        void onClick(@NonNull TouristSpot spot);
    }

    public interface OnHeartClickListener {
        void onHeartClick(@NonNull TouristSpot spot);
    }

    private final Context context;
    private final OnSpotClickListener listener;
    private final List<TouristSpot> items = new ArrayList<>();
    private final OnHeartClickListener heartClickListener;

    private HashSet<String> deactivatedSpotIds = new HashSet<>();

    public WishedSpotAdapter(@NonNull Context context, OnSpotClickListener listener, OnHeartClickListener heartClickListener) {
        this.context = context;
        this.listener = listener;
        this.heartClickListener = heartClickListener;
        setHasStableIds(true);
    }

    public void setDeactivatedSpotIds(HashSet<String> deactivatedSpotIds) {
        this.deactivatedSpotIds = deactivatedSpotIds;
    }

    public void setItems(List<TouristSpot> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        try {
            return Long.parseLong(String.valueOf(items.get(position).contentid));
        } catch (Exception e) {
            return items.get(position).hashCode();
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_wished_spot, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        TouristSpot spot = items.get(position);

        // 공통: 제목(굵게) 설정
        String titleText = safe(spot.title);
        if (spot.contenttypeid == 99) {
            // ===== 뉴스 =====
            // 설명(HTML 제거) + 이미지 숨김
            h.thumb.setVisibility(View.GONE);
            h.title.setText(fromHtmlCompat(titleText));

            String newsDesc = safe(spot.addr1); // 현재 데이터 구조에서 뉴스 설명을 addr1에 담는 것으로 가정
            h.addr.setText(fromHtmlCompat(newsDesc));

        } else {
            // 이미지 보이기
            h.thumb.setVisibility(View.VISIBLE);
            h.title.setText(titleText);

            // 썸네일 로딩
            String img = !TextUtils.isEmpty(spot.firstimage) ? spot.firstimage : null;
            Glide.with(context)
                    .load(img)
                    .placeholder(R.drawable.sample1)
                    .error(R.drawable.sample1)
                    .into(h.thumb);

            if (spot.contenttypeid == 15) {
                // ===== 축제/행사 =====
                String place  = safe(spot.addr1);
                String start  = pickEventDate(spot, true);   // 시작일
                String end    = pickEventDate(spot, false);  // 종료일
                String period = makePeriod(start, end);      // "YYYY.MM.DD ~ YYYY.MM.DD"

                String sub = TextUtils.isEmpty(period) ? place : place + " · " + period;
                h.addr.setText(sub);
            } else {
                // ===== 관광지(그 외) =====
                h.addr.setText(safe(spot.addr1));
            }

        }

            // 아이템 클릭
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(spot);
        });

        // 하트 상태
        if (deactivatedSpotIds.contains(spot.contentid)) {
            h.heartButton.setImageResource(R.drawable.ic_favorite_border_24);
            h.heartButton.setContentDescription("찜 해제됨");
        } else {
            h.heartButton.setImageResource(R.drawable.ic_favorite_full);
            h.heartButton.setContentDescription("찜 됨");
        }

        // 하트 클릭
        h.heartButton.setOnClickListener(v -> {
            if (heartClickListener != null) {
                heartClickListener.onHeartClick(spot);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView thumb;
        TextView title;
        TextView addr;
        ImageButton heartButton;

        VH(@NonNull View itemView) {
            super(itemView);
            thumb = itemView.findViewById(R.id.imageThumb);
            title = itemView.findViewById(R.id.textTitle);
            addr = itemView.findViewById(R.id.textAddr);
            heartButton = itemView.findViewById(R.id.imageButtonHeart);
        }
    }

    // ===== 유틸 =====

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static CharSequence fromHtmlCompat(String html) {
        if (TextUtils.isEmpty(html)) return "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(html);
        }
    }

    // "20250719" -> "2025.07.19"
    private static String formatYmd(String ymd) {
        if (TextUtils.isEmpty(ymd) || ymd.length() != 8) return "";
        try {
            SimpleDateFormat inFmt = new SimpleDateFormat("yyyyMMdd", Locale.KOREA);
            SimpleDateFormat outFmt = new SimpleDateFormat("yyyy.MM.dd", Locale.KOREA);
            return outFmt.format(inFmt.parse(ymd));
        } catch (ParseException e) {
            return "";
        }
    }

    // 여러 후보 필드명에서 문자열을 안전 추출 (spot → spot.item → spot.detail → spot.festival 순서)
    private static String pickEventDate(Object spot, boolean isStart) {
        String[] candidatesStart = {
                "eventstartdate", "eventStartDate", "startdate", "startDate",
                "sdate", "beginDate", "festivalStartDate", "dateStart"
        };
        String[] candidatesEnd = {
                "eventenddate", "eventEndDate", "enddate", "endDate",
                "edate", "finishDate", "festivalEndDate", "dateEnd"
        };
        String[] targets = isStart ? candidatesStart : candidatesEnd;

        // 1) spot 자체에서 탐색
        String v = pickFromObject(spot, targets);
        if (!TextUtils.isEmpty(v)) return v;

        // 2) spot.item에서 탐색 (FestivalAdapter 패턴)
        Object itemObj = getFieldObject(spot, "item");
        v = pickFromObject(itemObj, targets);
        if (!TextUtils.isEmpty(v)) return v;

        // 3) 여유: spot.detail / spot.festival에서도 탐색
        Object detailObj = getFieldObject(spot, "detail");
        v = pickFromObject(detailObj, targets);
        if (!TextUtils.isEmpty(v)) return v;

        Object festivalObj = getFieldObject(spot, "festival");
        v = pickFromObject(festivalObj, targets);
        if (!TextUtils.isEmpty(v)) return v;

        return "";
    }

    // 주어진 객체에서 후보 필드명으로 값을 찾아 문자열로 반환
    private static String pickFromObject(Object obj, String[] fieldNames) {
        if (obj == null) return "";
        for (String name : fieldNames) {
            String v = getFieldString(obj, name);
            if (!TextUtils.isEmpty(v)) return v;
        }
        return "";
    }

    private static Object getFieldObject(Object obj, String fieldName) {
        if (obj == null) return null;
        try {
            java.lang.reflect.Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(obj);
        } catch (Exception ignore) {
            return null;
        }
    }

    private static String getFieldString(Object obj, String fieldName) {
        if (obj == null) return "";
        try {
            java.lang.reflect.Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            Object val = f.get(obj);
            if (val == null) return "";
            if (val instanceof java.util.Date) {
                return new java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.KOREA)
                        .format((java.util.Date) val);
            }
            return String.valueOf(val);
        } catch (Exception ignore) {
            return "";
        }
    }

    // "20250719" / "2025-07-19" / "2025.07.19" 등 허용 → "2025.07.19"
    // 날짜 포맷 유연 처리
    private static String formatYmdFlexible(String input) {
        if (TextUtils.isEmpty(input)) return "";
        String[] inPatterns = {"yyyyMMdd", "yyyy-MM-dd", "yyyy.MM.dd"};
        for (String p : inPatterns) {
            try {
                java.text.SimpleDateFormat inFmt = new java.text.SimpleDateFormat(p, java.util.Locale.KOREA);
                inFmt.setLenient(false);
                java.util.Date d = inFmt.parse(input);
                java.text.SimpleDateFormat outFmt = new java.text.SimpleDateFormat("yyyy.MM.dd", java.util.Locale.KOREA);
                return outFmt.format(d);
            } catch (Exception ignore) {}
        }
        String onlyDigits = input.replaceAll("\\D+", "");
        if (onlyDigits.length() == 8) {
            try {
                java.text.SimpleDateFormat inFmt = new java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.KOREA);
                java.util.Date d = inFmt.parse(onlyDigits);
                java.text.SimpleDateFormat outFmt = new java.text.SimpleDateFormat("yyyy.MM.dd", java.util.Locale.KOREA);
                return outFmt.format(d);
            } catch (Exception ignore) {}
        }
        return "";
    }

    private static String makePeriod(String start, String end) {
        String s = formatYmdFlexible(start);
        String e = formatYmdFlexible(end);
        if (!TextUtils.isEmpty(s) && !TextUtils.isEmpty(e)) return s + " ~ " + e;
        if (!TextUtils.isEmpty(s)) return s;
        return e; // e가 비어있으면 "" 반환
    }

}
