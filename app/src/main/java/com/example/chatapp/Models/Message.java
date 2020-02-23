package com.example.chatapp.Models;

public class Message  {
     String username;
     String messageId;
     String message;
     String time;
     String senderPhone;
     String receiverPhone;
     String seeners ;

    public Message() {
    }

    public Message(String message, String time, String senderPhone, String receiverPhone, String  seeners) {
        this.message = message;
        this.time = time;
        this.senderPhone = senderPhone;
        this.receiverPhone = receiverPhone;
        this.seeners = seeners;
    }

    public String getSeeners() {
        return seeners;
    }

    public void setSeeners(String seeners) {
        this.seeners = seeners;
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
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
