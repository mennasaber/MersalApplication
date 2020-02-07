package com.example.chatapp.Models;

import java.util.ArrayList;
import java.util.Map;

public class Group {
    String groupId;
    String groupName;
    String groupImage;

    public Group() {
    }

    public Group(String groupName,String groupId,String groupImage) {
        this.groupName = groupName;
        this.groupId = groupId;
        this.groupImage =groupImage;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
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
