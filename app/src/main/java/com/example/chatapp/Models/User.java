package com.example.chatapp.Models;

public class User {
    private String image;
    private String phoneNumber;
    private String username;
    public User() {
    }

    public User(String username, String image, String phoneNumber) {
        this.username = username;
        this.image = image;
        this.phoneNumber = phoneNumber;
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
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public void setUsername(String username) {
        this.username = username;
    }




}
