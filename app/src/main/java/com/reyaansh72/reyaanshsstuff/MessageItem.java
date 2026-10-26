package com.reyaansh72.reyaanshsstuff;

import java.util.HashMap;
import java.util.Map;

public class MessageItem {
    private String messageId;
    private String senderId;
    private String senderUsername;
    private String text;
    private String mediaUrl;
    private String mediaType; // "text", "image", "video", "file"
    private String replyToText;
    private Map<String, String> reactions; // userId -> reaction emoji
    private long timestamp;

    public MessageItem() {
        this.reactions = new HashMap<>();
        this.mediaType = "text";
    }

    public MessageItem(String messageId, String senderId, String senderUsername, String text,
                       String mediaUrl, String mediaType, String replyToText, long timestamp) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.text = text;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType != null ? mediaType : "text";
        this.replyToText = replyToText;
        this.reactions = new HashMap<>();
        this.timestamp = timestamp;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getReplyToText() {
        return replyToText;
    }

    public void setReplyToText(String replyToText) {
        this.replyToText = replyToText;
    }

    public Map<String, String> getReactions() {
        return reactions;
    }

    public void setReactions(Map<String, String> reactions) {
        this.reactions = reactions;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}