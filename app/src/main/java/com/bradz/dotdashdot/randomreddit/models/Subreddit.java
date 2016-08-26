package com.bradz.dotdashdot.randomreddit.models;

/**
 * Created by drewmahrt on 3/6/16.
 */
public class Subreddit {
    private String display_name;
    private String public_description;
    private String description;
    private String title;
    private String header_title;
    private boolean nsfw;

    public Subreddit(String display_name, String public_description, String description, String title, String header_title, boolean nsfw){
        this.display_name = display_name;
        this.public_description = public_description;
        this.description = description;
        this.title = title;
        this.header_title = header_title;
        this.nsfw = nsfw;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHeader_title() {
        return header_title;
    }

    public void setHeader_title(String header_title) {
        this.header_title = header_title;
    }

    public boolean isNsfw() {
        return nsfw;
    }

    public void setNsfw(boolean nsfw) {
        this.nsfw = nsfw;
    }

    public String getPublic_description() {
        return public_description;
    }

    public void setPublic_description(String public_description) {
        this.public_description = public_description;
    }
}
