package com.developerali.aima.Models;

public class MembershipModel {

    String name, fatherName, address, dob, uiNo, image;
    long valid;

    public MembershipModel(String name, String fatherName, String address, String dob, String uiNo, String image, long valid) {
        this.name = name;
        this.fatherName = fatherName;
        this.address = address;
        this.dob = dob;
        this.uiNo = uiNo;
        this.image = image;
        this.valid = valid;
    }

    public MembershipModel() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public long getValid() {
        return valid;
    }

    public void setValid(long valid) {
        this.valid = valid;
    }

    public String getUiNo() {
        return uiNo;
    }

    public void setUiNo(String uiNo) {
        this.uiNo = uiNo;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
