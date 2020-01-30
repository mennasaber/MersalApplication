package com.example.chatapp.Models;

public class Message {
    String message ;
    String time ;
    String senderPhone ;
    String recieverPhone ;

    public Message() {
    }

    public Message(String message, String time, String senderPhone, String recieverPhone) {
        this.message = message;
        this.time = time;
        this.senderPhone = senderPhone;
        this.recieverPhone = recieverPhone;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSenderPhone() {
        return senderPhone;
    }

    public void setSenderPhone(String senderPhone) {
        this.senderPhone = senderPhone;
    }

    public String getRecieverPhone() {
        return recieverPhone;
    }

    public void setRecieverPhone(String recieverPhone) {
        this.recieverPhone = recieverPhone;
    }
}
