package com.example.chatapp.Models;

import java.util.ArrayList;

public class UserGroups {
    ArrayList<String> groupsIds;

    public UserGroups() {
    }

    public UserGroups(ArrayList<String> groupsIds) {
        this.groupsIds = groupsIds;
    }

    public ArrayList<String> getGroupsIds() {
        return groupsIds;
    }

    public void setGroupsIds(ArrayList<String> groupsIds) {
        this.groupsIds = groupsIds;
    }
}
