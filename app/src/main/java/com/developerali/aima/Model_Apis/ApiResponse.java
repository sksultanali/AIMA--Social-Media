package com.developerali.aima.Model_Apis;

public class ApiResponse {
    private String status;
    private String homeId;
    private String roomId;
    private String message;
    private boolean valid;

    // Getters
    public String getStatus() {
        return status;
    }

    public String getHomeId() {
        return homeId;
    }

    public String getMessage() {
        return message;
    }

    public String getRoomId() {
        return roomId;
    }

    public boolean isValid() {
        return valid;
    }
}

