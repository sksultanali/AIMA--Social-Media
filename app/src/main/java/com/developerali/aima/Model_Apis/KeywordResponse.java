package com.developerali.aima.Model_Apis;

import java.util.List;

public class KeywordResponse {
    private String status;
    private String message;
    private List<Data> data;

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // Nested Data class
    public static class Data {
        private String matchKeyword;
        private String type;

        // Getters and Setters
        public String getMatchKeyword() {
            return matchKeyword;
        }

        public void setMatchKeyword(String matchKeyword) {
            this.matchKeyword = matchKeyword;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
