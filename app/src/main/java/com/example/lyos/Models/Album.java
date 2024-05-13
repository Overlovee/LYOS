package com.example.lyos.Models;

import java.util.ArrayList;

public class Album {
    private String id;
    private String title;
    private int trackQuantity;
    private String userID;
    private String description;
    private String imageFileName;
    private String normalizedTitle;
    private ArrayList<String> songList;

    // Constructor
    public Album() {
        this.id = "";
        this.title = "";
        this.trackQuantity = 0;
        this.userID = "";
        this.description = "";
        this.imageFileName = "";
        this.normalizedTitle = "";
        this.songList = null;
    }
    public Album(String title, int trackQuantity, String userID, String description, String imageFileName, String normalizedTitle, ArrayList<String> songList) {
        this.id = "";
        this.title = title;
        this.trackQuantity = trackQuantity;
        this.userID = userID;
        this.description = description;
        this.imageFileName = imageFileName;
        this.normalizedTitle = normalizedTitle;
        this.songList = songList;
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getTrackQuantity() {
        return trackQuantity;
    }

    public void setTrackQuantity(int trackQuantity) {
        this.trackQuantity = trackQuantity;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public String getNormalizedTitle() {
        return normalizedTitle;
    }

    public void setNormalizedTitle(String normalizedTitle) {
        this.normalizedTitle = normalizedTitle;
    }

    public ArrayList<String> getSongList() {
        return songList;
    }

    public void setSongList(ArrayList<String> songList) {
        this.songList = songList;
    }
}