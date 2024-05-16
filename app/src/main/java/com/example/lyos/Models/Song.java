package com.example.lyos.Models;

import java.util.ArrayList;
import java.util.Date;

public class Song {
    private String id;
    private String title;
    private String mp3FileName;
    private String imageFileName;
    private String description;
    private int duration;
    private ArrayList<String> likedBy;
    private int listens;
    private String type;
    private Date uploadDate;
    private String userID;
    private String normalizedTitle;

    public Song() {
        // Khởi tạo giá trị mặc định
        this.id = "";
        this.title = "";
        this.mp3FileName = "";
        this.imageFileName = "";
        this.description = "";
        this.duration = 0;
        this.likedBy = null;
        this.listens = 0;
        this.type = "";
        this.uploadDate = new Date();
        this.userID = "";
        this.normalizedTitle = "";
    }

    public Song(String title, String mp3FileName, String imageFileName, String description, int duration, ArrayList<String> likedBy, int listens, String type, Date uploadDate, String userID) {
        this.id = "";
        this.title = title;
        this.mp3FileName = mp3FileName;
        this.imageFileName = imageFileName;
        this.description = description;
        this.duration = duration;
        this.likedBy = likedBy;
        this.listens = listens;
        this.type = type;
        this.uploadDate = uploadDate;
        this.userID = userID;
        this.normalizedTitle = normalizeTitle(title);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Thêm phương thức chuẩn hóa tiêu đề
    private String normalizeTitle(String title) {
        // Chuẩn hóa tiêu đề ở đây, ví dụ: loại bỏ dấu và chuyển thành chữ thường
        // Bạn có thể sử dụng các thư viện như ICU4J để chuẩn hóa chuỗi
        // Ở đây, một ví dụ đơn giản:
        return title.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
    }

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.normalizedTitle = normalizeTitle(title); // Cập nhật normalizedTitle khi tiêu đề thay đổi
    }

    public String getMp3FileName() {
        return mp3FileName;
    }

    public void setMp3FileName(String mp3FileName) {
        this.mp3FileName = mp3FileName;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public ArrayList<String> getLikedBy() {
        return likedBy;
    }

    public void setLikedBy(ArrayList<String> likedBy) {
        this.likedBy = likedBy;
    }

    public int getListens() {
        return listens;
    }

    public void setListens(int listens) {
        this.listens = listens;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getNormalizedTitle() {
        return normalizedTitle;
    }

    public void setNormalizedTitle(String normalizedTitle) {
        this.normalizedTitle = normalizedTitle;
    }
}


