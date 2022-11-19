package com.firebase.uidemo.models;

import java.util.HashMap;
import java.util.Map;

public class MessageModel {
    // MessageModel(String message, String senderId, long messageTime, String receiverId, String attachmentId, boolean messageRead, boolean messageEncrypted, int pubKeySender, int pubKeyReceiver

    String message;
    long messageTime;
    String senderId;
    String receiverId;
    String attachmentId;
    boolean messageRead;
    boolean messageEncrypted;
    int pubKeySender;
    int pubKeyReceiver;

    public MessageModel() {}

    // full message constructor
    public MessageModel(String message, long messageTime, String senderId, String receiverId, String attachmentId, boolean messageRead, boolean messageEncrypted, int pubKeySender, int pubKeyReceiver) {
        this.message = message;
        this.messageTime = messageTime;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.attachmentId = attachmentId;
        this.messageRead = messageRead;
        this.messageEncrypted = messageEncrypted;
        this.pubKeySender = pubKeySender;
        this.pubKeyReceiver = pubKeyReceiver;
    }

    // constructor for beginner chats (unencrypted, no attachment)
    public MessageModel(String message, long messageTime, String senderId, String receiverId) {
        this.message = message;
        this.messageTime = messageTime;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.attachmentId = "";
        this.messageRead = false;
        this.messageEncrypted = false;
        this.pubKeySender = 0;
        this.pubKeyReceiver = 0;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("sender", senderId);
        result.put("message", message);
        result.put("messageTime", messageTime);
        result.put("messageEncrypted", messageEncrypted);
        return result;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    public boolean isMessageEncrypted() {
        return messageEncrypted;
    }

    public void setMessageEncrypted(boolean messageEncrypted) {
        this.messageEncrypted = messageEncrypted;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public boolean isMessageRead() {
        return messageRead;
    }

    public void setMessageRead(boolean messageRead) {
        this.messageRead = messageRead;
    }

    public int getPubKeySender() {
        return pubKeySender;
    }

    public void setPubKeySender(int pubKeySender) {
        this.pubKeySender = pubKeySender;
    }

    public int getPubKeyReceiver() {
        return pubKeyReceiver;
    }

    public void setPubKeyReceiver(int pubKeyReceiver) {
        this.pubKeyReceiver = pubKeyReceiver;
    }

    public String getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(String attachmentId) {
        this.attachmentId = attachmentId;
    }
}
