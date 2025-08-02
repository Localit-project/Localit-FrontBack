package com.inhatc.localit.ui.category;

public class NewsItem {
    private String title;
    private String date;

    public NewsItem(String title, String date) {
        this.title = title;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }
}
