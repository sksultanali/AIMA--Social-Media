package com.developerali.aima.Models;

import java.util.Date;

public class FollowModel {

    String followBy;
    Long followAt;

    public FollowModel() {
    }

    public FollowModel(String followBy, Long followAt) {
        this.followBy = followBy;
        this.followAt = followAt;
    }

    public String getFollowBy() {
        return followBy;
    }

    public void setFollowBy(String followBy) {
        this.followBy = followBy;
    }

    public Long getFollowAt() {
        return followAt;
    }

    public void setFollowAt(long followAt) {
        this.followAt = followAt;
    }
}
