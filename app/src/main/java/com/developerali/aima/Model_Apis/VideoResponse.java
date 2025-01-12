package com.developerali.aima.Model_Apis;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class VideoResponse implements Parcelable{
    private String status;
    private String message;
    private ArrayList<PostData> data;
    private int nextToken;
    private int videosCount;

    protected VideoResponse(Parcel in) {
        status = in.readString();
        data = in.createTypedArrayList(PostData.CREATOR);
        nextToken = in.readInt();
    }

    public static final Creator<VideoResponse> CREATOR = new Creator<VideoResponse>() {
        @Override
        public VideoResponse createFromParcel(Parcel in) {
            return new VideoResponse(in);
        }

        @Override
        public VideoResponse[] newArray(int size) {
            return new VideoResponse[size];
        }
    };

    // Getters and setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getVideosCount() {
        return videosCount;
    }

    public void setVideosCount(int videosCount) {
        this.videosCount = videosCount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(status);
        parcel.writeTypedList(data);
        parcel.writeInt(nextToken);
    }

    // Inner class for PostData
    public static class PostData implements Parcelable {
        private String videoId;
        private String uploader;
        private String caption;
        private String status;
        private String time;
        private String youTubeId;
        private String name;
        private int verified;
        private int verified_valid;
        private String type;
        private String user_image;
        private String token;

        protected PostData(Parcel in) {
            videoId = in.readString();
            uploader = in.readString();
            caption = in.readString();
            status = in.readString();
            time = in.readString();
            youTubeId = in.readString();
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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getYouTubeId() {
            return youTubeId;
        }

        public void setYouTubeId(String youTubeId) {
            this.youTubeId = youTubeId;
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

        public int getVerified_valid() {
            return verified_valid;
        }

        public void setVerified_valid(int verified_valid) {
            this.verified_valid = verified_valid;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
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
            parcel.writeString(status);
            parcel.writeString(time);
            parcel.writeString(youTubeId);
            parcel.writeString(name);
            parcel.writeInt(verified);
            parcel.writeInt(verified_valid);
            parcel.writeString(type);
            parcel.writeString(user_image);
            parcel.writeString(token);
        }
    }
}
