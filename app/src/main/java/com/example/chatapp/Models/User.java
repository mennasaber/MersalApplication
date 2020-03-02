package com.example.chatapp.Models;

public class User {
     String image;
     String phoneNumber;
     String username;
     String userId ;

    public User() {
    }

    public User(String username, String image, String phoneNumber, String userId) {
        this.image = image;
        this.phoneNumber = phoneNumber;
        this.username = username;
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }


}
