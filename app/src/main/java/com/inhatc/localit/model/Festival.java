// app/src/main/java/com/inhatc/localit/model/Festival.java
package com.inhatc.localit.model;

public class Festival {
    private String title;
    private String date;

    // 로컬 샘플 이미지용 (기존 유지)
    private int imageResId;

    // API 이미지 URL용
    private String imageUrl;

    private boolean favorite;

    // 리소스 이미지용 (샘플/더미)
    public Festival(String title, String date, int imageResId, boolean favorite) {
        this.title = title;
        this.date = date;
        this.imageResId = imageResId;
        this.favorite = favorite;
    }

    //  API용 생성자 (URL)
    public Festival(String title, String date, String imageUrl, boolean favorite) {
        this.title = title;
        this.date = date;
        this.imageUrl = imageUrl;
        this.favorite = favorite;
    }

    public String getTitle() { return title; }
    public String getDate() { return date; }

    public int getImageResId() { return imageResId; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
}