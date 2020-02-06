package com.example.chatapp.Models;

import java.util.ArrayList;

public class Group {
    String groupId;
    String groupName;
    String groupImage;
    ArrayList<Message> messages ;
    ArrayList<User> users;

    public Group() {
    }

    public Group(String groupName, ArrayList<Message> messages, ArrayList<User> users,String groupId,String groupImage) {
        this.groupName = groupName;
        this.messages = messages;
        this.users = users;
        this.groupId = groupId;
        this.groupImage =groupImage;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupImage() {
        return groupImage;
    }

    public void setGroupImage(String groupImage) {
        this.groupImage = groupImage;
    }
}
