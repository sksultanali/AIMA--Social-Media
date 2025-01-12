package com.developerali.aima.Model_Apis;

import java.util.ArrayList;

public class SinglePostData {

    private String status;
    private String message;
    private PostResponse.PostData data;

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

    public PostResponse.PostData getData() {
        return data;
    }

    public void setData(PostResponse.PostData data) {
        this.data = data;
    }
}
