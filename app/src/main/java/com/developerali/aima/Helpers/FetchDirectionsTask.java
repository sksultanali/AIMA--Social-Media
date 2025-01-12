package com.developerali.aima.Helpers;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FetchDirectionsTask extends AsyncTask<String, Void, List<LatLng>> {

    public interface Callback {
        void onRouteFetched(List<LatLng> routePoints);
    }

    private Callback callback;

    public FetchDirectionsTask(Callback callback) {
        this.callback = callback;
    }

    @Override
    protected List<LatLng> doInBackground(String... urls) {
        String jsonResponse = fetchJsonFromUrl(urls[0]);
        if (jsonResponse == null) return null;

        return parseDirections(jsonResponse);
    }

    @Override
    protected void onPostExecute(List<LatLng> routePoints) {
        if (callback != null && routePoints != null) {
            callback.onRouteFetched(routePoints);
        }
    }

    private String fetchJsonFromUrl(String urlString) {
        StringBuilder jsonResult = new StringBuilder();
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            InputStream inputStream = connection.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                jsonResult.append(line);
            }

            reader.close();
            inputStream.close();
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return jsonResult.toString();
    }

    private List<LatLng> parseDirections(String json) {
        List<LatLng> routePoints = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray routes = jsonObject.getJSONArray("routes");
            if (routes.length() > 0) {
                JSONObject route = routes.getJSONObject(0);
                JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                String encodedPolyline = overviewPolyline.getString("points");
                routePoints = decodePolyline(encodedPolyline);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return routePoints;
    }

    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> polyline = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng point = new LatLng(lat / 1E5, lng / 1E5);
            polyline.add(point);
        }
        return polyline;
    }
}

