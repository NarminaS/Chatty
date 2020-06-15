package com.narminas.chatty.models;

import com.google.firebase.Timestamp;

public class Message {

    private String chatId;
    private String sender;
    private String receiver;
    private String message;
    private boolean isseen;
    private Timestamp timeAdded;

    public Message() {
    }

    public Message(String chatId, String sender, String receiver, String message, boolean isseen, Timestamp timeAdded) {
        this.chatId = chatId;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isseen = isseen;
        this.timeAdded = timeAdded;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    public Timestamp getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Timestamp timeAdded) {
        this.timeAdded = timeAdded;
    }
}
