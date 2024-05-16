package com.example.lyos.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class UserInfo implements Parcelable {
    private String id;
    private String email;
    private String normalizedUsername;
    private String profileBanner;
    private String profilePhoto;
    private String username;
    private ArrayList<String> followers;
    private ArrayList<String> following;
    private ArrayList<String> likes;

    // Default constructor required for calls to DataSnapshot.getValue(UserInfo.class)
    public UserInfo() {
        // Initialize arrays to avoid null pointer exceptions
        this.id = "";
        this.email = "";
        this.normalizedUsername = "";
        this.profileBanner = "";
        this.profilePhoto = "";
        this.username = "";
        followers = new ArrayList<>();
        following = new ArrayList<>();
        likes = new ArrayList<>();
    }

    // Constructor with parameters
    public UserInfo(String email, String normalizedUsername, String profileBanner, String profilePhoto, String username, ArrayList<String> followers, ArrayList<String> following, ArrayList<String> likes) {
        this.id = "";
        this.email = email;
        this.normalizedUsername = normalizedUsername;
        this.profileBanner = profileBanner;
        this.profilePhoto = profilePhoto;
        this.username = username;
        this.followers = followers;
        this.following = following;
        this.likes = likes;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNormalizedUsername() {
        return normalizedUsername;
    }

    public void setNormalizedUsername(String normalizedUsername) {
        this.normalizedUsername = normalizedUsername;
    }

    public String getProfileBanner() {
        return profileBanner;
    }

    public void setProfileBanner(String profileBanner) {
        this.profileBanner = profileBanner;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ArrayList<String> getFollowers() {
        return followers;
    }

    public void setFollowers(ArrayList<String> followers) {
        this.followers = followers;
    }

    public ArrayList<String> getFollowing() {
        return following;
    }

    public void setFollowing(ArrayList<String> following) {
        this.following = following;
    }

    public ArrayList<String> getLikes() {
        return likes;
    }

    public void setLikes(ArrayList<String> likes) {
        this.likes = likes;
    }


    // Phương thức Parcelable
    protected UserInfo(Parcel in) {
        id = in.readString();
        email = in.readString();
        normalizedUsername = in.readString();
        profileBanner = in.readString();
        profilePhoto = in.readString();
        username = in.readString();
        followers = in.createStringArrayList();
        following = in.createStringArrayList();
        likes = in.createStringArrayList();
    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel in) {
            return new UserInfo(in);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(email);
        dest.writeString(normalizedUsername);
        dest.writeString(profileBanner);
        dest.writeString(profilePhoto);
        dest.writeString(username);
        dest.writeStringList(followers);
        dest.writeStringList(following);
        dest.writeStringList(likes);
    }
}