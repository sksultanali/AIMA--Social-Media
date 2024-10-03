package com.developerali.aima;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

import org.jitsi.meet.sdk.JitsiMeetActivity;

public class JitsiMeetOngoingConferenceService extends Service{

    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Create and configure your notification
        Notification notification = buildNotification();

        // Start the service in the foreground state and display the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
        }

        // Handle your service logic here

        // Return the appropriate value for your use case
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification buildNotification() {
        // Build and configure your notification here
        // ...

        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle("My Service")
                .setContentText("Service is running")
                .setSmallIcon(R.drawable.notifications_icon)
                .setContentIntent(getPendingIntent());

        return builder.build();
    }

    private PendingIntent getPendingIntent() {
        // Create and configure the PendingIntent for your notification
        // ...

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_IMMUTABLE);
        return pendingIntent;
    }
}
