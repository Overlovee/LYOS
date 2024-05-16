package com.example.lyos.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Album implements Parcelable {
    private String id;
    private String title;
    private String userID;
    private String description;
    private String imageFileName;
    private String normalizedTitle;
    private ArrayList<String> songList;

    // Constructor
    public Album() {
        this.id = "";
        this.title = "";
        this.userID = "";
        this.description = "";
        this.imageFileName = "";
        this.normalizedTitle = "";
        this.songList = null;
    }
    public Album(String title, String userID, String description, String imageFileName, String normalizedTitle, ArrayList<String> songList) {
        this.id = "";
        this.title = title;
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

    // Phương thức Parcelable
    protected Album(Parcel in) {
        id = in.readString();
        title = in.readString();
        userID = in.readString();
        description = in.readString();
        imageFileName = in.readString();
        normalizedTitle = in.readString();
        songList = in.createStringArrayList();
    }

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(userID);
        dest.writeString(description);
        dest.writeString(imageFileName);
        dest.writeString(normalizedTitle);
        dest.writeStringList(songList);
    }
}