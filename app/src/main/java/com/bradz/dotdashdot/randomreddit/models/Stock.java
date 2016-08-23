package com.bradz.dotdashdot.randomreddit.models;

/**
 * Created by drewmahrt on 3/6/16.
 */
public class Stock {
    private String title;
    private String url;
    private int votes;
    private String image;

    public Stock(String title, String url, String image, int votes){
        this.title = title;
        this.url = url;
        this.votes = votes;
        this.image = image;
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
}
