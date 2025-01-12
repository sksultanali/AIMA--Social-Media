package com.developerali.aima.Notifications;

import com.developerali.aima.Models.NotificationRequest;

public class NotificationRequestTopic {
    private Message message;

    public NotificationRequestTopic(Message message) {
        this.message = message;
    }

    public static class Message {
        private String topic;
        private Notification notification;

        public Message(String topic, Notification notification) {
            this.topic = topic;
            this.notification = notification;
        }
    }

    public static class Notification {
        private String title;
        private String body;

        public Notification(String title, String body) {
            this.title = title;
            this.body = body;
        }
    }
}
