package com.example.lyos.Models;

public class UserInfo {
    private String id;
    private String email;
    private String normalizedUsername;
    private String profileBanner;
    private String profilePhoto;
    private String username;

    public UserInfo() {
        this.id = "";
        this.email = "email";
        this.normalizedUsername = "";
        this.profileBanner = "";
        this.profilePhoto = "";
        this.username = "";
    }

    public UserInfo(String email, String normalizedUsername, String profileBanner, String profilePhoto, String username) {
        this.id = "";
        this.email = email;
        this.normalizedUsername = normalizedUsername;
        this.profileBanner = profileBanner;
        this.profilePhoto = profilePhoto;
        this.username = username;
    }

    // Getters và setters cho các trường

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
}

