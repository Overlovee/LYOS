package com.example.lyos.Models;

import java.util.Date;

public class YoutubeVideo {
    private String videoId;
    private Date publishedAt;
    private String channelId;
    private String channelTitle;
    private String title;
    private String description;
    private String imageUrl;

    public YoutubeVideo() {
        this.videoId = "";
        this.publishedAt = new Date();
        this.channelId = "";
        this.channelTitle = "";
        this.title = "";
        this.description = "";
        this.imageUrl = "";
    }
    public YoutubeVideo(String videoId, Date publishedAt, String channelId, String channelTitle, String title, String description, String imageUrl) {
        this.videoId = videoId;
        this.publishedAt = publishedAt;
        this.channelId = channelId;
        this.channelTitle = channelTitle;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public Date getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Date publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelTitle() {
        return channelTitle;
    }

    public void setChannelTitle(String channelTitle) {
        this.channelTitle = channelTitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

