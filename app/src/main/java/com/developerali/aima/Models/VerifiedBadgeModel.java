package com.developerali.aima.Models;

public class VerifiedBadgeModel {

    String profId, validity, identityLink, paymentScreenshot, paymentMode;
    long time;

    public VerifiedBadgeModel(String validity, long time) {
        this.validity = validity;
        this.time = time;
    }

    public VerifiedBadgeModel() {
    }

    public String getProfId() {
        return profId;
    }

    public void setProfId(String profId) {
        this.profId = profId;
    }

    public String getValidity() {
        return validity;
    }

    public void setValidity(String validity) {
        this.validity = validity;
    }

    public String getIdentityLink() {
        return identityLink;
    }

    public void setIdentityLink(String identityLink) {
        this.identityLink = identityLink;
    }

    public String getPaymentScreenshot() {
        return paymentScreenshot;
    }

    public void setPaymentScreenshot(String paymentScreenshot) {
        this.paymentScreenshot = paymentScreenshot;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
