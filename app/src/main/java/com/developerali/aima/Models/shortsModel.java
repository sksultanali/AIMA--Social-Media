package com.developerali.aima.Models;

public class shortsModel {

    String videoLink, uploader;
    long time;

    public shortsModel(String videoLink, String uploader, long time) {
        this.videoLink = videoLink;
        this.uploader = uploader;
        this.time = time;
    }

    public shortsModel() {
    }

    public String getVideoLink() {
        return videoLink;
    }

    public void setVideoLink(String videoLink) {
        this.videoLink = videoLink;
    }

    public String getUploader() {
        return uploader;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader;
    }


    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
