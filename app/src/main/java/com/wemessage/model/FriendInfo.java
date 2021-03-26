package com.wemessage.model;

public class FriendInfo {
    private String uid;
    private String name;
    private String avatar;
    private String status;

    public FriendInfo(String uid, String name, String avatar, String status) {
        this.uid = uid;
        this.name = name;
        this.avatar = avatar;
        this.status = status;
    }

    public FriendInfo() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
