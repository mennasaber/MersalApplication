package com.example.chatapp.modules;

public class User {
    private String username;
    private String image;
    private String phoneNumber;

    public User(String username, String image, String phoneNumber) {
        this.username = username;
        this.image = image;
        this.phoneNumber = phoneNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
