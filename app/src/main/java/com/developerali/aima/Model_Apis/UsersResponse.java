package com.developerali.aima.Model_Apis;

import com.developerali.aima.Models.UserModel;

import java.util.ArrayList;

public class UsersResponse {
    private String status;
    private String message;
    private int nextToken;
    private ArrayList<UserModel> data;

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

    public int getNextToken() {
        return nextToken;
    }

    public void setNextToken(int nextToken) {
        this.nextToken = nextToken;
    }

    public ArrayList<UserModel> getData() {
        return data;
    }

    public void setData(ArrayList<UserModel> data) {
        this.data = data;
    }
}
