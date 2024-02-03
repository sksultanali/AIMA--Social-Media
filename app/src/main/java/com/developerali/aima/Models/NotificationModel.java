package com.developerali.aima.Models;

public class NotificationModel {

    String notifyBy, type, id, notificationId;
    long notifyAt;
    Boolean seen;

    public NotificationModel(String notifyBy, String type, String id, long notifyAt, Boolean seen) {
        this.notifyBy = notifyBy;
        this.type = type;
        this.id = id;
        this.notifyAt = notifyAt;
        this.seen = seen;
    }

    public NotificationModel() {
    }

    public String getNotifyBy() {
        return notifyBy;
    }

    public void setNotifyBy(String notifyBy) {
        this.notifyBy = notifyBy;
    }

    public long getNotifyAt() {
        return notifyAt;
    }

    public void setNotifyAt(long notifyAt) {
        this.notifyAt = notifyAt;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }
}
