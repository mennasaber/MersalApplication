package com.example.chatapp.Models;

public class Chat {
    User user;
    Message lastMessage;

    public Chat() {}

    public Chat(User user, Message lastMessage) {
        this.user = user;
        this.lastMessage = lastMessage;

    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }
}
