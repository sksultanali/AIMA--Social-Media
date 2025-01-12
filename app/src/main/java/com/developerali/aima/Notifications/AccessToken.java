package com.developerali.aima.Notifications;

import android.os.AsyncTask;
import android.util.Log;

import com.google.auth.oauth2.GoogleCredentials;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class AccessToken {
    private static final String firebaseMessagingScope = "https://www.googleapis.com/auth/firebase.messaging";

    public void getAccessToken(AccessTokenCallback callback) {
        new GetTokenTask(callback).execute();
    }

    private static class GetTokenTask extends AsyncTask<Void, Void, String> {
        private final AccessTokenCallback callback;

        GetTokenTask(AccessTokenCallback callback) {
            this.callback = callback;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                String jsonString = "{\n" +
                        "  \"type\": \"service_account\",\n" +
                        "  \"project_id\": \"aima-6a424\",\n" +
                        "  \"private_key_id\": \"abb3539c6dfd491c2e649f208959a8a5b7cde6de\",\n" +
                        "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC2Ds0CznldNXL6\\nRK4m5JYYFfG3ypUjZmKIauaqfGN3adChKA5UZi0vVf2ertLDFvCoMJFPximvHEsI\\nVoSyI3jc4f4D18yGyjzNklHK6EEpAQ22wXjw5rruYvrM0Qctug9yg2JFrfLsRvKD\\nFtpox7OEeEqDjfiLnYshdTkS21gJxGRRisyWdDo2Z5VhljfZjcABF7FFZAYJEnfD\\nw4QlWuBzV6IxcoQ18QXPC/2jslt6F0UgThbAht/8JAEVkL5GIUV85yGDElWYPvjI\\nEYp8XhQVww8lKbidNcnmi1C0iCdMnCmFTAo3E19GR+qcxtspJMw1dqr9f4xTUIbH\\n/trojHvRAgMBAAECggEAAzdK12/JqKwJ+VorHCJltRxWoDM0vBrJ9uxSAa0Xtfdd\\nXUprieHB7rQReksO662xH7x3ZaoLyJVKrUWXe5zZLMg0dDGMwi0Kgsxg0+FzTLfR\\nO+MqKXG+97jxgpqW6o+uyAxieRYB/Hrim+6Uej08WmTEVrN0YnV8jKFRKjhME2SQ\\nQi5O99lc7lzCNHjpPEYOEPUg2XvsHV4yGHBImnoupUzD6gc5ebEygKnU1pgRiu2U\\nd7yVgyxpSOPEC+1hRWusk3MEiEePzHhSpOo7Up7aMGwGcTGdeEdG4o++8TaH5ADj\\n3gnVmsT229Y/061vxVB4EaEUbXRJg4/dZtD9bHpaYQKBgQDtwoP0r8wdXTIJeixr\\nhfy+lFJ2UhKJjMiwpokRjFv26v8OSDyWH18RGntWVY7thqKBI7BCHrW8btcn3+Bu\\nGwwcdnWLYzewjrGYYbN2TmvPsvvTsz+oLEA4sdhlcOFhSYSYlOxKaWOwQ4zjo3A1\\ndJ0w+D5n0lAGIYdTZVdSiFrg4QKBgQDEBlNc7nKJgwz7WBhVsMcfBqXmaYyW1SRD\\nRuIDiE5sRCITuoV+qCV518Z8ECVOd+LbN+jV41uIynIGu9Wc2RG0snLcqx4rPiB1\\nV0LldPWq6noywjamVgJ7JHHLlSTdMg/JO4OG5QqxJDxPlw6K5JYdxG+S65AqToEi\\nUXjAgi/I8QKBgQDBiTvmmo85O9p2MngkEk4ZSB0T6VzjkLDwcq9SlIFrrgBuPMjs\\nnp7BCmBpd32/xX9URZu9RshCmasT7z3+ApBKPvcRvnLfilfwJV9zJwTFbfllaxyf\\nJV4liEcQ0+DyU2jW1yq8q6sVGPd/eVPmf4VRDlGmK2EujG5eqTh6mzn2oQKBgCgk\\nl3vHhQMuKEFsqAccXZq39M97vAPDgFqasHxCtLStO0FJCD+I1avfCVGsyMcJ/BYi\\nm/b5SWx48OL6ImOtfFfB143jKRl37pO5HAT7b7fhrsaoSSLFKFrperhTQaperybp\\nniTI0Oi33/X4nEzLRQaxjJvMy8guLXnGr3DU+E4xAoGBALkIIF+i/bqjsF+dCGZe\\nOAy0ejLMCsvxnIhYRl2P0/oSiMKzhhAUcLSc/j3OBckjScoO3KKF8eU7qKbmD3cf\\nEy0t9zJQH4ufoSSqoXO4CaP6IialNrstG7GE1zqsrmJm2RVD2+KGDeynvlPtzG6k\\nUClr9e2P5bzwE/EqfG3IHW8Q\\n-----END PRIVATE KEY-----\\n\",\n" +
                        "  \"client_email\": \"firebase-adminsdk-yk5ze@aima-6a424.iam.gserviceaccount.com\",\n" +
                        "  \"client_id\": \"103210672371626902580\",\n" +
                        "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                        "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                        "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                        "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-yk5ze%40aima-6a424.iam.gserviceaccount.com\",\n" +
                        "  \"universe_domain\": \"googleapis.com\"\n" +
                        "}";
                InputStream stream = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));
                GoogleCredentials googleCredentials = GoogleCredentials.fromStream(stream)
                        .createScoped(firebaseMessagingScope);
                googleCredentials.refresh();
                return googleCredentials.getAccessToken().getTokenValue();
            } catch (Exception e) {
                Log.e("AccessToken", "getAccessToken: " + e.getLocalizedMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String token) {
            if (callback != null) {
                callback.onTokenReceived(token);
            }
        }
    }

    public interface AccessTokenCallback {
        void onTokenReceived(String token);
    }
}