package com.inhatc.localit.model;

public class Event {
    private String title;
    private String date;
    private int imageResId;
    private boolean isFavorite;

    public Event(String title, String date, int imageResId, boolean isFavorite) {
        this.title = title;
        this.date = date;
        this.imageResId = imageResId;
        this.isFavorite = isFavorite;
    }

    public String getTitle() { return title; }
    public String getDate() { return date; }
    public int getImageResId() { return imageResId; }
    public boolean isFavorite() { return isFavorite; }

    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}
