package com.developerali.aima.Models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class pdfModel implements Parcelable {

    String id, link, caption;
    long time;

    public pdfModel(String link, long time) {
        this.link = link;
        this.time = time;
    }

    public pdfModel() {
    }

    protected pdfModel(Parcel in) {
        id = in.readString();
        link = in.readString();
        caption = in.readString();
        time = in.readLong();
    }

    public static final Creator<pdfModel> CREATOR = new Creator<pdfModel>() {
        @Override
        public pdfModel createFromParcel(Parcel in) {
            return new pdfModel(in);
        }

        @Override
        public pdfModel[] newArray(int size) {
            return new pdfModel[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
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
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(link);
        dest.writeString(caption);
        dest.writeLong(time);
    }
}
