package com.developerali.aima.Model_Apis;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class PostResponse {
    private String status;
    private String message;
    private ArrayList<PostData> data;
    private int nextToken;
    private int totalAdminPost;
    private int totalPost;

    // Getters and setters
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

    public ArrayList<PostData> getData() {
        return data;
    }

    public void setData(ArrayList<PostData> data) {
        this.data = data;
    }

    public int getNextToken() {
        return nextToken;
    }

    public void setNextToken(int nextToken) {
        this.nextToken = nextToken;
    }

    public int getTotalAdminPost() {
        return totalAdminPost;
    }

    public void setTotalAdminPost(int totalAdminPost) {
        this.totalAdminPost = totalAdminPost;
    }

    public int getTotalPost() {
        return totalPost;
    }

    public void setTotalPost(int totalPost) {
        this.totalPost = totalPost;
    }

    // Inner class to represent the individual post data
    public static class PostData implements Parcelable {
        private int tbl_id;
        private String id;
        private String image;
        private String uploader;
        private String caption;
        private String status;
        private int commentsCount;
        private String time;
        private int likesCount;
        private String name;
        private int verified;
        private int verified_valid;
        private String type;
        private String user_image;
        private String token;

        protected PostData(Parcel in) {
            tbl_id = in.readInt();
            id = in.readString();
            image = in.readString();
            uploader = in.readString();
            caption = in.readString();
            status = in.readString();
            commentsCount = in.readInt();
            time = in.readString();
            likesCount = in.readInt();
            name = in.readString();
            verified = in.readInt();
            verified_valid = in.readInt();
            type = in.readString();
            user_image = in.readString();
            token = in.readString();
        }

        public static final Creator<PostData> CREATOR = new Creator<PostData>() {
            @Override
            public PostData createFromParcel(Parcel in) {
                return new PostData(in);
            }

            @Override
            public PostData[] newArray(int size) {
                return new PostData[size];
            }
        };

        // Getters and setters
        public int getTbl_id() {
            return tbl_id;
        }

        public void setTbl_id(int tbl_id) {
            this.tbl_id = tbl_id;
        }

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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public int getCommentsCount() {
            return commentsCount;
        }

        public void setCommentsCount(int commentsCount) {
            this.commentsCount = commentsCount;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public int getLikesCount() {
            return likesCount;
        }

        public void setLikesCount(int likesCount) {
            this.likesCount = likesCount;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getVerified() {
            return verified;
        }

        public void setVerified(int verified) {
            this.verified = verified;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUser_image() {
            return user_image;
        }

        public void setUser_image(String user_image) {
            this.user_image = user_image;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public int getVerified_valid() {
            return verified_valid;
        }

        public void setVerified_valid(int verified_valid) {
            this.verified_valid = verified_valid;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel parcel, int i) {
            parcel.writeInt(tbl_id);
            parcel.writeString(id);
            parcel.writeString(image);
            parcel.writeString(uploader);
            parcel.writeString(caption);
            parcel.writeString(status);
            parcel.writeInt(commentsCount);
            parcel.writeString(time);
            parcel.writeInt(likesCount);
            parcel.writeString(name);
            parcel.writeInt(verified);
            parcel.writeInt(verified_valid);
            parcel.writeString(type);
            parcel.writeString(user_image);
            parcel.writeString(token);
        }
    }
}
