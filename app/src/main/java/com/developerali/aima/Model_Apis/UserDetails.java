package com.developerali.aima.Model_Apis;

import com.developerali.aima.Models.UserModel;

public class UserDetails {
    private String status;
    private String message;
    private UserModel data;

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserModel getData() {
        return data;
    }

    public void setData(UserModel data) {
        this.data = data;
    }
}

