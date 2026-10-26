package com.reyaansh72.reyaanshsstuff;

public class CommentItem {
    private String commentId;
    private String shortId;
    private String username;
    private String userId;
    private String text;
    private long timestamp;

    public CommentItem() {
    }

    public CommentItem(String commentId, String shortId, String username, String userId, String text, long timestamp) {
        this.commentId = commentId;
        this.shortId = shortId;
        this.username = username;
        this.userId = userId;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getShortId() {
        return shortId;
    }

    public void setShortId(String shortId) {
        this.shortId = shortId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}