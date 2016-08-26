package com.bradz.dotdashdot.randomreddit.models;

/**
 * Created by drewmahrt on 3/6/16.
 */
public class Stock {
    private String title;
    private String url;
    private int votes;
    private String image;
    private String full_image;
    private boolean nsfw;

    public Stock(String title, String url, String image, int votes, String full_image, boolean nsfw){
        this.title = title;
        this.url = url;
        this.votes = votes;
        this.image = image;
        this.full_image = full_image;
        this.nsfw = nsfw;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFull_image() {
        return full_image;
    }

    public void setFull_image(String full_image) {
        this.full_image = full_image;
    }

    public boolean isNsfw() {
        return nsfw;
    }

    public void setNsfw(boolean nsfw) {
        this.nsfw = nsfw;
    }
}
