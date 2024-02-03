package com.developerali.aima.Models;

import java.util.ArrayList;

public class GalleryModel {

    String id, caption;
    ArrayList<String> images;
    long time;

    public GalleryModel(ArrayList<String> images, long time) {
        this.images = images;
        this.time = time;
    }

    public GalleryModel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public void setImages(ArrayList<String> images) {
        this.images = images;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
