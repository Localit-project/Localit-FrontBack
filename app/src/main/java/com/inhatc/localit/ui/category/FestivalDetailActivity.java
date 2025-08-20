//package com.inhatc.localit.ui.category;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.bumptech.glide.Glide;
//import com.inhatc.localit.R;
//import com.inhatc.localit.api.SpotApiHelper;
//import com.inhatc.localit.api.SpotDetailCommonResponse;
//import com.inhatc.localit.api.SpotDetailIntroResponse;
//
//import java.lang.reflect.Field;
//
///**
// * 축제( contentTypeId = 15 ) 전용 상세 화면
// * 표시 항목:
// *  1) 우편번호 (zipcode)
// *  2) 전화명 (telname - 공통상세에 있을 수 있음 / 없으면 공백)
// *  3) 전화번호 (tel)
// *  4) 주소 (addr1/addr2)
// *  5) 개요 (overview)
// *  6) 주최자 정보 (sponsor1)
// *  7) 주최자 연락처 (sponsor1tel)
// *  8) 주관사 정보 (sponsor2)
// *  9) 행사시작일 (eventstartdate)
// * 10) 행사종료일 (eventenddate)
// * 11) 공연시간 (playtime)
// * 12) 진행형태 (subevent)
// * 13) 축제형태 (festivalgrade)
// * 14) 행사소개 (program)
// * 15) 행사내용 (placeinfo 등)
// *
// * 주의: 일부 필드는 API/모델에 없을 수 있어 리플렉션으로 안전 접근(rf)합니다.
// */
//public class FestivalDetailActivity extends AppCompatActivity {
//
//    // SpotDetailActivity와 호환되는 키(그대로 사용 가능)
//    public static final String EXTRA_CONTENT_ID       = SpotDetailActivity.EXTRA_CONTENT_ID;
//    public static final String EXTRA_CONTENT_TYPE_ID  = SpotDetailActivity.EXTRA_CONTENT_TYPE_ID;
//    public static final String EXTRA_TITLE            = SpotDetailActivity.EXTRA_TITLE;
//    public static final String EXTRA_ADDR1            = SpotDetailActivity.EXTRA_ADDR1;
//    public static final String EXTRA_FIRST_IMAGE      = SpotDetailActivity.EXTRA_FIRST_IMAGE;
//
//    private ImageView imageMain;
//    private TextView textTitle;
//
//    // 공통
//    private TextView tvZipcode;     // 1
//    private TextView tvTelName;     // 2 (공통 상세의 telname 이 있으면 표시)
//    private TextView tvTel;         // 3
//    private TextView tvAddr;        // 4
//    private TextView tvOverview;    // 5
//
//    // 인트로(축제)
//    private TextView tvSponsor1;    // 6
//    private TextView tvSponsor1Tel; // 7
//    private TextView tvSponsor2;    // 8
//    private TextView tvStartDate;   // 9
//    private TextView tvEndDate;     // 10
//    private TextView tvPlaytime;    // 11
//    private TextView tvProgress;    // 12 (subevent)
//    private TextView tvFestivalType;// 13 (festivalgrade)
//    private TextView tvProgram;     // 14 (program)
//    private TextView tvContent;     // 15 (placeinfo 등을 묶어서 소개)
//
//    private String contentId;
//    private String contentTypeId; // 기본 15
//    private String passedTitle;
//    private String passedAddr1;
//    private String passedFirstImage;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_festival_detail);
//
//        bindViews();
//
//        // 인텐트 읽기 (onCreate에서만)
//        Intent intent = getIntent();
//        if (intent == null) {
//            finishWithError("잘못된 접근입니다.");
//            return;
//        }
//        contentId        = safe(intent.getStringExtra(EXTRA_CONTENT_ID));
//        contentTypeId    = safe(intent.getStringExtra(EXTRA_CONTENT_TYPE_ID));
//        passedTitle      = safe(intent.getStringExtra(EXTRA_TITLE));
//        passedAddr1      = safe(intent.getStringExtra(EXTRA_ADDR1));
//        passedFirstImage = safe(intent.getStringExtra(EXTRA_FIRST_IMAGE));
//
//        if (TextUtils.isEmpty(contentId)) {
//            finishWithError("상세 조회에 필요한 contentId 가 없습니다.");
//            return;
//        }
//        if (TextUtils.isEmpty(contentTypeId)) contentTypeId = "15"; // 축제 기본값
//
//        // 전달값 선표시
//        if (!TextUtils.isEmpty(passedTitle)) textTitle.setText(passedTitle);
//        if (!TextUtils.isEmpty(passedAddr1)) tvAddr.setText(passedAddr1);
//        if (!TextUtils.isEmpty(passedFirstImage)) {
//            Glide.with(this).load(passedFirstImage)
//                    .placeholder(R.drawable.sample1)
//                    .error(R.drawable.sample1)
//                    .into(imageMain);
//        }
//
//        // 공통 상세 호출(대표사진/개요/주소/우편/연락처/전화명 등)
//        SpotApiHelper.fetchDetailCommon(contentId, contentTypeId, item -> {
//            if (item == null) return;
//            runOnUiThread(() -> bindCommon(item));
//        });
//
//        // 축제 인트로 호출
//        int ctid = 15;
//        try { ctid = Integer.parseInt(contentTypeId); } catch (Exception ignore) {}
//        SpotApiHelper.fetchDetailIntro(contentId, ctid, item -> {
//            if (item == null) return;
//            runOnUiThread(() -> bindIntroFestival(item));
//        });
//    }
//
//    private void bindViews() {
//        imageMain     = findViewById(R.id.imageMain);
//        textTitle     = findViewById(R.id.textTitle);
//
//        tvZipcode     = findViewById(R.id.tvZipcode);
//        tvTelName     = findViewById(R.id.tvTelName);
//        tvTel         = findViewById(R.id.tvTel);
//        tvAddr        = findViewById(R.id.tvAddr);
//        tvOverview    = findViewById(R.id.tvOverview);
//
//        tvSponsor1    = findViewById(R.id.tvSponsor1);
//        tvSponsor1Tel = findViewById(R.id.tvSponsor1Tel);
//        tvSponsor2    = findViewById(R.id.tvSponsor2);
//        tvStartDate   = findViewById(R.id.tvStartDate);
//        tvEndDate     = findViewById(R.id.tvEndDate);
//        tvPlaytime    = findViewById(R.id.tvPlaytime);
//        tvProgress    = findViewById(R.id.tvProgressType);
//        tvFestivalType= findViewById(R.id.tvFestivalType);
//        tvProgram     = findViewById(R.id.tvProgram);
//        tvContent     = findViewById(R.id.tvContent);
//    }
//
//    // ---------- 공통 상세 바인딩 ----------
//    private void bindCommon(SpotDetailCommonResponse.Item it) {
//        if (!TextUtils.isEmpty(it.title)) textTitle.setText(it.title);
//        tvOverview.setText(nl(it.overview));
//        tvAddr.setText(joinAddr(it.addr1, it.addr2));
//        tvZipcode.setText(safe(it.zipcode));
//        tvTel.setText(safe(it.tel));
//
//        // '전화명'은 공통 상세에 있을 수도( telname ). 리플렉션으로 안전 접근.
//        String telname = rf(it, "telname");
//        tvTelName.setText(safe(telname));
//
//        String img = !TextUtils.isEmpty(it.firstimage) ? it.firstimage : it.firstimage2;
//        if (!TextUtils.isEmpty(img)) {
//            Glide.with(this).load(img).placeholder(R.drawable.sample1).error(R.drawable.sample1).into(imageMain);
//        } else if (TextUtils.isEmpty(passedFirstImage)) {
//            imageMain.setImageResource(R.drawable.sample1);
//        }
//    }
//
//    // ---------- 축제 인트로 바인딩 ----------
//    private void bindIntroFestival(SpotDetailIntroResponse.Item it) {
//        // 축제 인트로(15)에서 기대하는 필드들(없을 수도 있으니 rf 로 안전 접근)
//        String sponsor1     = rf(it, "sponsor1");
//        String sponsor1tel  = rf(it, "sponsor1tel");
//        String sponsor2     = rf(it, "sponsor2");
//        String startDate    = rf(it, "eventstartdate");
//        String endDate      = rf(it, "eventenddate");
//        String playtime     = rf(it, "playtime");
//        String subevent     = rf(it, "subevent");       // 진행형태(세부 프로그램 나열 등)
//        String festivalType = rf(it, "festivalgrade");  // 축제형태(지역대표/유명축제 등)
//        String program      = rf(it, "program");        // 행사소개
//        String placeinfo    = rf(it, "placeinfo");      // 행사내용(장소/부스/부대행사 안내 등)
//
//        // 바인딩 + 포맷
//        tvSponsor1.setText(safe(sponsor1));
//        tvSponsor1Tel.setText(safe(sponsor1tel));
//        tvSponsor2.setText(safe(sponsor2));
//        tvStartDate.setText(fmtDate(startDate));
//        tvEndDate.setText(fmtDate(endDate));
//        tvPlaytime.setText(safe(playtime));
//        tvProgress.setText(nl(subevent));
//        tvFestivalType.setText(safe(festivalType));
//        tvProgram.setText(nl(program));
//        tvContent.setText(nl(placeinfo));
//    }
//
//    // ---------- 유틸 ----------
//    private void finishWithError(String msg) {
//        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
//        finish();
//    }
//
//    private String safe(String s) { return s == null ? "" : s; }
//
//    /** <br> 등 단순 HTML 개행 처리 */
//    private String nl(String v) {
//        if (v == null) return "";
//        return v.replaceAll("(?i)<br\\s*/?>", "\n")
//                .replaceAll("(?i)</p>", "\n")
//                .replaceAll("<[^>]*>", "")
//                .trim();
//    }
//
//    private String joinAddr(String a1, String a2) {
//        if (TextUtils.isEmpty(a2)) return safe(a1);
//        if (TextUtils.isEmpty(a1)) return safe(a2);
//        return a1 + " " + a2;
//    }
//
//    /** yyyyMMdd → yyyy.MM.dd */
//    private String fmtDate(String raw) {
//        if (TextUtils.isEmpty(raw) || raw.length() < 8) return safe(raw);
//        try {
//            return raw.substring(0,4) + "." + raw.substring(4,6) + "." + raw.substring(6,8);
//        } catch (Exception e) {
//            return safe(raw);
//        }
//    }
//
//    /**
//     * 리플렉션으로 public 필드 값을 안전하게 문자열로 가져온다.
//     * DTO에 필드가 없어도 예외 없이 빈 문자열 반환.
//     */
//    private String rf(Object obj, String field) {
//        if (obj == null || TextUtils.isEmpty(field)) return "";
//        try {
//            Field f = obj.getClass().getField(field);
//            f.setAccessible(true);
//            Object v = f.get(obj);
//            return v == null ? "" : String.valueOf(v);
//        } catch (Exception ignore) {
//            return "";
//        }
//    }
//}
