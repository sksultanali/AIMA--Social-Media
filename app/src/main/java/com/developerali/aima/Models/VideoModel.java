package com.developerali.aima.Models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class VideoModel implements Parcelable {

    String videoId, uploader, caption;
    long time;
    boolean approved;

    public VideoModel(String videoId, String uploader, long time) {
        this.videoId = videoId;
        this.uploader = uploader;
        this.time = time;
    }

    public VideoModel() {
    }

    protected VideoModel(Parcel in) {
        videoId = in.readString();
        uploader = in.readString();
        caption = in.readString();
        time = in.readLong();
    }

    public static final Creator<VideoModel> CREATOR = new Creator<VideoModel>() {
        @Override
        public VideoModel createFromParcel(Parcel in) {
            return new VideoModel(in);
        }

        @Override
        public VideoModel[] newArray(int size) {
            return new VideoModel[size];
        }
    };

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getUploader() {
        return uploader;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(videoId);
        parcel.writeString(uploader);
        parcel.writeString(caption);
        parcel.writeLong(time);
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }
}
