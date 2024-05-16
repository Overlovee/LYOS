package com.example.lyos.Models;
import java.util.ArrayList;

public class Playlist {
    private String id;
    private String normalizedTitle;
    private String title;
    private String userID;
    private ArrayList<String> songList;

    // Constructors
    public Playlist() {
        // Default constructor
        this.id = "";
        this.normalizedTitle = "";
        this.title = "";
        this.userID = "";
        this.songList = null;
    }

    public Playlist(String normalizedTitle, String title, String userID, ArrayList<String> songList) {
        this.id = "";
        this.normalizedTitle = normalizedTitle;
        this.title = title;
        this.userID = userID;
        this.songList = songList;
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNormalizedTitle() {
        return normalizedTitle;
    }

    public void setNormalizedTitle(String normalizedTitle) {
        this.normalizedTitle = normalizedTitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public ArrayList<String> getSongList() {
        return songList;
    }

    public void setSongList(ArrayList<String> songList) {
        this.songList = songList;
    }
}

