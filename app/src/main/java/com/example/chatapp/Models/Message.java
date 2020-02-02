package com.example.chatapp.Models;

public class Message {
    String message;
    String time;
    String senderPhone;
    String receiverPhone;
    int seen;

    public Message() {
    }

    public Message(String message, String time, String senderPhone, String receiverPhone, int seen) {
        this.message = message;
        this.time = time;
        this.senderPhone = senderPhone;
        this.receiverPhone = receiverPhone;
        this.seen = seen;
    }

    public int getSeen() {
        return seen;
    }

    public void setSeen(int seen) {
        this.seen = seen;
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

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }
}
