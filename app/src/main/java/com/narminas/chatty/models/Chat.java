package com.narminas.chatty.models;

public class Chat {

    private String creatorUserId;
    private String replyUserId;

    public Chat() {
    }

    public String getCreatorUserId() {
        return creatorUserId;
    }

    public void setCreatorUserId(String creatorUserId) {
        this.creatorUserId = creatorUserId;
    }

    public String getReplyUserId() {
        return replyUserId;
    }

    public void setReplyUserId(String replyUserId) {
        this.replyUserId = replyUserId;
    }

    public Chat(String creatorUserId, String replyUserId) {
        this.creatorUserId = creatorUserId;
        this.replyUserId = replyUserId;
    }
}
