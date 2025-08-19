package com.inhatc.localit.ui.category;

public class NewsItem {
    private final String title;
    private final String description; // XML의 textNewsDescription에 매핑
    private boolean favorite;

    public NewsItem(String title, String description) {
        this.title = title;
        this.description = description;
        this.favorite = false;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }

    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
}
