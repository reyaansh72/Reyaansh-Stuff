package com.reyaansh72.reyaanshsstuff;

public class AiMessageItem {
    private String text;
    private boolean isUser;
    private long timestamp;

    public AiMessageItem() {
    }

    public AiMessageItem(String text, boolean isUser, long timestamp) {
        this.text = text;
        this.isUser = isUser;
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public boolean isUser() {
        return isUser;
    }

    public long getTimestamp() {
        return timestamp;
    }
}