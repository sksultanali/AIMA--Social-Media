package com.developerali.aima.Model_Apis;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BoundaryData implements Parcelable {

    @SerializedName("status")
    private String status;
    @SerializedName("show")
    private String show;
    @SerializedName("imgData")
    private String imgData;
    @SerializedName("youtube_link")
    private String youtube_link;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private List<Data> data;

    // Default Constructor
    public BoundaryData() {
    }

    protected BoundaryData(Parcel in) {
        status = in.readString();
        show = in.readString();
        imgData = in.readString();
        youtube_link = in.readString();
        data = in.createTypedArrayList(Data.CREATOR);
    }

    public static final Creator<BoundaryData> CREATOR = new Creator<BoundaryData>() {
        @Override
        public BoundaryData createFromParcel(Parcel in) {
            return new BoundaryData(in);
        }

        @Override
        public BoundaryData[] newArray(int size) {
            return new BoundaryData[size];
        }
    };

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getShow() {
        return show;
    }

    public void setShow(String show) {
        this.show = show;
    }

    public String getImgData() {
        return imgData;
    }

    public void setImgData(String imgData) {
        this.imgData = imgData;
    }

    public String getYoutube_link() {
        return youtube_link;
    }

    public void setYoutube_link(String youtube_link) {
        this.youtube_link = youtube_link;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        parcel.writeString(status);
        parcel.writeString(show);
        parcel.writeString(imgData);
        parcel.writeString(youtube_link);
        parcel.writeTypedList(data);
    }

    public static class Data implements Parcelable {
        @SerializedName("id")
        private int id;
        @SerializedName("latitude")
        private String latitude;
        @SerializedName("longitude")
        private String longitude;
        @SerializedName("time")
        private String time;

        // Default Constructor
        public Data() {
        }

        protected Data(Parcel in) {
            id = in.readInt();
            latitude = in.readString();
            longitude = in.readString();
            time = in.readString();
        }

        public static final Creator<Data> CREATOR = new Creator<Data>() {
            @Override
            public Data createFromParcel(Parcel in) {
                return new Data(in);
            }

            @Override
            public Data[] newArray(int size) {
                return new Data[size];
            }
        };

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getLatitude() {
            return latitude;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        public String getLongitude() {
            return longitude;
        }

        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel parcel, int flags) {
            parcel.writeInt(id);
            parcel.writeString(latitude);
            parcel.writeString(longitude);
            parcel.writeString(time);
        }
    }
}