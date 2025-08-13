package com.example.shopbepoly.DTO;

public class Message {
    private String _id; // Thêm trường id
    private User from;
    private User to;
    private String content;
    private String timestamp;

    public Message(User from, User to, String content) {
        this.from = from;
        this.to = to;
        this.content = content;
    }

    // Constructor mới để dễ dàng tạo message với id
    public Message() {
        this._id = _id;
        this.from = from;
        this.to = to;
        this.content = content;
        this.timestamp = timestamp;
    }

    // Thêm getter và setter cho id
    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public User getFrom() {
        return from;
    }

    public void setFrom(User from) {
        this.from = from;
    }

    public User getTo() {
        return to;
    }

    public void setTo(User to) {
        this.to = to;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}