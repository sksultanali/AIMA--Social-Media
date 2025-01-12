package com.developerali.aima.Models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class PostModelOld implements Parcelable{
    String id, image, uploader, caption;
    int likesCount, commentsCount;
    long time;
    boolean approved;

    public PostModelOld(String uploader, long time) {
        this.uploader = uploader;
        this.time = time;
    }

    public PostModelOld() {
    }

    protected PostModelOld(Parcel in) {
        id = in.readString();
        image = in.readString();
        uploader = in.readString();
        caption = in.readString();
        likesCount = in.readInt();
        commentsCount = in.readInt();
        time = in.readLong();
        approved = in.readByte() != 0;
    }

    public static final Parcelable.Creator<PostModel> CREATOR = new Parcelable.Creator<PostModel>() {
        @Override
        public PostModel createFromParcel(Parcel in) {
            return new PostModel(in);
        }

        @Override
        public PostModel[] newArray(int size) {
            return new PostModel[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(image);
        dest.writeString(uploader);
        dest.writeString(caption);
        dest.writeInt(likesCount);
        dest.writeInt(commentsCount);
        dest.writeLong(time);
        dest.writeByte((byte) (approved ? 1 : 0));
    }
}
