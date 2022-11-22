package com.firebase.uidemo.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.firebase.uidemo.AbstractChat;
import com.firebase.uidemo.AbstractMessage;

import java.util.HashMap;
import java.util.Map;

public class MessageModel extends AbstractMessage {
    // MessageModel(String message, String senderId, long messageTime, String receiverId, String attachmentId, boolean messageRead, boolean messageEncrypted, int pubKeySender, int pubKeyReceiver

    String message;
    long messageTime;
    String senderId;
    String receiverId;
    String attachmentId;
    boolean messageRead;
    boolean messageEncrypted;
    int pubKeyIdSender;
    int pubKeyIdReceiver;
    String pubKeySender;

    public MessageModel() {}

    // full message constructor
    public MessageModel(String message, long messageTime, String senderId, String receiverId, String attachmentId, boolean messageRead, boolean messageEncrypted, int pubKeyIdSender, int pubKeyIdReceiver, String pubKeySender) {
        this.message = message;
        this.messageTime = messageTime;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.attachmentId = attachmentId;
        this.messageRead = messageRead;
        this.messageEncrypted = messageEncrypted;
        this.pubKeyIdSender = pubKeyIdSender;
        this.pubKeyIdReceiver = pubKeyIdReceiver;
        this.pubKeySender = pubKeySender;
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
        this.pubKeyIdSender = 0;
        this.pubKeyIdReceiver = 0;
        this.pubKeySender = "";
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

    @Nullable
    @Override
    public String getName() {
        return null;
    }

    @Override
    public void setName(@Nullable String name) {

    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @NonNull
    @Override
    public String getUid() {
        // todo delete this getter/setter and variable
        return this.senderId;
    }

    @Override
    public void setUid(@NonNull String uid) {
        // todo delete this getter/setter and variable
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
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

    public int getPubKeyIdSender() {
        return pubKeyIdSender;
    }

    public void setPubKeyIdSender(int pubKeyIdSender) {
        this.pubKeyIdSender = pubKeyIdSender;
    }

    public int getPubKeyIdReceiver() {
        return pubKeyIdReceiver;
    }

    public void setPubKeyIdReceiver(int pubKeyReceiver) {
        this.pubKeyIdReceiver = pubKeyIdReceiver;
    }

    public String getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(String attachmentId) {
        this.attachmentId = attachmentId;
    }

    public String getPubKeySender() {
        return pubKeySender;
    }

    public void setPubKeySender(String pubKeySender) {
        this.pubKeySender = pubKeySender;
    }
}
