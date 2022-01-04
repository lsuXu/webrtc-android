package com.example.webrtctest.bean;

public class WSMessage {

    private String userId;

    private String toUserId;

    private Object message;

    public WSMessage() {
    }

    public WSMessage(String userId, String toUserId) {
        this.userId = userId;
        this.toUserId = toUserId;
    }

    public WSMessage(String userId, String toUserId, Object message) {
        this.userId = userId;
        this.toUserId = toUserId;
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }
}
