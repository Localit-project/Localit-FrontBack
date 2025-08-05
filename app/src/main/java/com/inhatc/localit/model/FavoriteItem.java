package com.inhatc.localit.model;

public class FavoriteItem {
    public String title;
    public String category;
    public String date;
    public String imageUrl;
    public boolean liked;

    public FavoriteItem(String title, String category, String date, String imageUrl, boolean liked) {
        this.title = title;
        this.category = category;
        this.date = date;
        this.imageUrl = imageUrl;
        this.liked = liked;
    }
}
