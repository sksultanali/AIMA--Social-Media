package com.developerali.aima.Notifications;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FcmNotificationsSender {

    public static final String PROJECT_ID = "bong-stay"; // Replace with your Firebase Project ID
    public static final String FCM_ENDPOINT = "https://fcm.googleapis.com/v1/projects/" + PROJECT_ID + "/messages:send";

    // Path to your service account JSON key file
    public static final String SERVICE_ACCOUNT_FILE = "bong-stay-firebase-adminsdk-ql1hb-3c4f72146d.json";

    public static void main(String[] args) {
        try {
            String token = getAccessToken();
            sendMessage(token, "Sample Title", "Sample Body", "your-device-token");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getAccessToken() throws IOException {
        FileInputStream serviceAccountStream = new FileInputStream(SERVICE_ACCOUNT_FILE);
        GoogleCredentials credentials = ServiceAccountCredentials.fromStream(serviceAccountStream)
                .createScoped(Arrays.asList("https://www.googleapis.com/auth/firebase.messaging"));
        credentials.refreshIfExpired();
        return credentials.getAccessToken().getTokenValue();
    }

    public static void sendMessage(String accessToken, String title, String body, String deviceToken) throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient();

        JSONObject message = new JSONObject();
        message.put("title", title);
        message.put("body", body);

        JSONObject notification = new JSONObject();
        notification.put("notification", message);
        notification.put("token", deviceToken);

        JSONObject mainObject = new JSONObject();
        mainObject.put("message", notification);

        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                mainObject.toString()
        );


        Request request = new Request.Builder()
                .url(FCM_ENDPOINT)
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json; UTF-8")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            System.out.println("Message sent: " + response.body().string());
        }
    }
}
