package com.developerali.aima.Models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class UserModelOld implements Parcelable {
    String name, email, password, image, cover, whatsapp, facebook, phone, about, bio, type, userId;
    int follower, following, posts, stars;
    long verifiedValid;
    boolean verified;

    public UserModelOld() {
    }

    public UserModelOld(String name, String email) {
        this.name = name;
        this.email = email;
    }

    protected UserModelOld(Parcel in) {
        name = in.readString();
        email = in.readString();
        password = in.readString();
        image = in.readString();
        cover = in.readString();
        whatsapp = in.readString();
        facebook = in.readString();
        phone = in.readString();
        about = in.readString();
        bio = in.readString();
        type = in.readString();
        userId = in.readString();
        follower = in.readInt();
        following = in.readInt();
        posts = in.readInt();
        verified = in.readByte() != 0;
    }

    public static final Creator<UserModel> CREATOR = new Creator<UserModel>() {
        @Override
        public UserModel createFromParcel(Parcel in) {
            return new UserModel(in);
        }

        @Override
        public UserModel[] newArray(int size) {
            return new UserModel[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }

    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getFollower() {
        return follower;
    }

    public void setFollower(int follower) {
        this.follower = follower;
    }

    public int getFollowing() {
        return following;
    }

    public void setFollowing(int following) {
        this.following = following;
    }

    public int getPosts() {
        return posts;
    }

    public void setPosts(int posts) {
        this.posts = posts;
    }

    public boolean isVerified() {
        return verified;
    }

    public long getVerifiedValid() {
        return verifiedValid;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(password);
        dest.writeString(image);
        dest.writeString(cover);
        dest.writeString(whatsapp);
        dest.writeString(facebook);
        dest.writeString(phone);
        dest.writeString(about);
        dest.writeString(bio);
        dest.writeString(type);
        dest.writeString(userId);
        dest.writeInt(follower);
        dest.writeInt(following);
        dest.writeInt(posts);
        dest.writeByte((byte) (verified ? 1 : 0));
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }
}
