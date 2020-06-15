package com.narminas.chatty.auth;

import android.app.Application;

public class UserApi extends Application {

    private String username;
    private String userId;
    private String imageUrl;

    private static UserApi instance;

    public UserApi() {

    }

    public static UserApi getInstance() {
        if (instance == null) {
            instance = new UserApi();
        }
        return instance;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
