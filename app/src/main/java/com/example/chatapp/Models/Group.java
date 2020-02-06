package com.example.chatapp.Models;

import java.util.Map;

public class Group {
    String groupId;
    String groupName;
    String groupImage;
    Map<String , String> messages ;
    Map<String , User> users ;


    public Group() {
    }

    public Group(String groupName, Map<String , String> messages, Map<String , User> users,String groupId,String groupImage) {
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

    public Map<String , String> getMessages() {
        return messages;
    }

    public void setMessages(Map<String , String> messages) {
        this.messages = messages;
    }

    public Map<String , User> getUsers() {
        return users;
    }

    public void setUsers(Map<String , User> users) {
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
