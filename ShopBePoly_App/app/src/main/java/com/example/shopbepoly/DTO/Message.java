package com.example.shopbepoly.DTO;


public class Message {
    private String _id;
    private String from;
    private String to;
    private String content;
    private String timestamp;

    public Message(String from, String to, String content) {
        this.from = from;
        this.to = to;
        this.content = content;
    }

    // Getters và Setters
    public String getId() { return _id; }
    public String getFrom() { return from; }
    public String getTo() { return to; }
    public String getContent() { return content; }
    public String getTimestamp() { return timestamp; }

    public void setId(String _id) { this._id = _id; }
    public void setFrom(String from) { this.from = from; }
    public void setTo(String to) { this.to = to; }
    public void setContent(String content) { this.content = content; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
