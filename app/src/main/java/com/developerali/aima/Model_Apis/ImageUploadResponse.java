package com.developerali.aima.Model_Apis;

public class ImageUploadResponse {
    private String status;
    private String message;
    private Data data;

    // Getters
    public static class Data {
        private String fileName;
        private String filePath;
        private String url;

        public String getFileName() {
            return fileName;
        }

        public String getFilePath() {
            return filePath;
        }

        public String getUrl() {
            return url;
        }
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Data getData() {
        return data;
    }


}
