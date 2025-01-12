package com.developerali.aima.Model_Apis;

public class CountResponse {
    private String status;
    private Data data;

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    // Nested Data Class
    public static class Data {
        private int adminPosts;
        private int publicPosts;
        private int videos;

        // Getters and Setters
        public int getAdminPosts() {
            return adminPosts;
        }

        public void setAdminPosts(int adminPosts) {
            this.adminPosts = adminPosts;
        }

        public int getPublicPosts() {
            return publicPosts;
        }

        public void setPublicPosts(int publicPosts) {
            this.publicPosts = publicPosts;
        }

        public int getVideos() {
            return videos;
        }

        public void setVideos(int videos) {
            this.videos = videos;
        }
    }
}
