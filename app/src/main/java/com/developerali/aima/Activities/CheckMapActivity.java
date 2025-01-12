package com.developerali.aima.Activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.developerali.aima.Adapters.DialogMapSearchAdapter;
import com.developerali.aima.Helpers.FetchDirectionsTask;
import com.developerali.aima.Helpers.Helper;
import com.developerali.aima.Model_Apis.ApiService;
import com.developerali.aima.Model_Apis.BoundaryData;
import com.developerali.aima.Model_Apis.MapPointerResponse;
import com.developerali.aima.Model_Apis.RetrofitClient;
import com.developerali.aima.R;
import com.developerali.aima.databinding.ActivityCheckMapBinding;
import com.developerali.aima.databinding.DialogForSearchBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CheckMapActivity extends FragmentActivity implements OnMapReadyCallback {

    ActivityCheckMapBinding binding;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    List<MapPointerResponse.Data> mapResponseData;
    private LatLng userLocation;
    private Polyline currentPolyline;
    private Marker currentLocationMarker;
    DialogMapSearchAdapter search_adapter;
    ApiService apiService;
    private static final float STROKE_WIDTH = 15f;
    private List<LatLng> boundaryLatLngs = new ArrayList<>();

    private List<String> textList = Arrays.asList(
            "Search here...",
            "Get confused? See video tutorial!",
            "Search-> Start-> Destination"
    );
    private int listIndex = 0;
    private int charIndex = 0;
    private long typingDelay = 100;
    private long deletingDelay = 50;
    private long showDelay = 2000;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isDeleting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivityCheckMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        apiService = RetrofitClient.getClient().create(ApiService.class);
        startTypingAnimation();

        // Apply window insets to the root view
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkAndAddPointers();


        binding.customLocationButton.setOnClickListener(v -> {
            // Check if location permission is granted
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Handle permission request if not granted
                return;
            }

            // Use FusedLocationProviderClient to get the current location
            getUserLocation();
        });

        binding.closeBtn.setOnClickListener(v->{
            binding.infoBox.setVisibility(View.GONE);
        });

        binding.searchTxt.setOnClickListener(v->{
            showDialogSearch();
        });


        //createBoundary();
        initializeBoundary();
    }

    private void checkAndAddPointers() {
        Call<MapPointerResponse> call = apiService.fetchMapPointers(
                "fetchMapPointers", null);

        call.enqueue(new Callback<MapPointerResponse>() {
            @Override
            public void onResponse(Call<MapPointerResponse> call, Response<MapPointerResponse> response) {
                if (response.isSuccessful() && response.body() != null){
                    MapPointerResponse apiResponse = response.body();
                    if (apiResponse.getStatus().equalsIgnoreCase("success")){
                        mapResponseData = apiResponse.getData();
                        if (mapResponseData != null && !mapResponseData.isEmpty()){

                            for (MapPointerResponse.Data stall : mapResponseData) {
                                // Convert latitude and longitude to LatLng
                                LatLng location = new LatLng(
                                        Double.parseDouble(stall.getLatitude()),
                                        Double.parseDouble(stall.getLongitude())
                                );

                                Glide.with(CheckMapActivity.this)
                                    .asBitmap()
                                    .load(stall.getImage())
                                    .into(new CustomTarget<Bitmap>() {
                                        @Override
                                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                            // Resize the bitmap
                                            int width = 100;  // Desired width in pixels
                                            int height = 100; // Desired height in pixels
                                            Bitmap resizedBitmap = Bitmap.createScaledBitmap(resource, width, height, false);

                                            // Add marker with resized custom icon
                                            mMap.addMarker(new MarkerOptions()
                                                            .position(location)
                                                            .title(stall.getName())
                                                            .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap)));

                                            //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 30));
                                        }

                                        @Override
                                        public void onLoadCleared(@Nullable Drawable placeholder) {
                                            // Handle placeholder if needed
                                        }
                                    });
                            }
                        }else {
                            Toast.makeText(CheckMapActivity.this, "List is empty! ", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(CheckMapActivity.this, apiResponse.getStatus(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<MapPointerResponse> call, Throwable t) {
                Toast.makeText(CheckMapActivity.this, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showDialogSearch() {
        DialogForSearchBinding dialogBinding = DialogForSearchBinding.inflate(getLayoutInflater());

        // Create a new dialog and set the custom layout
        Dialog dialog = new Dialog(this);
        dialog.setContentView(dialogBinding.getRoot());
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialog.getWindow().setStatusBarColor(getColor(R.color.white));
//        dialog.getWindow().setGravity(Gravity.BOTTOM);

        dialogBinding.backBtn.setOnClickListener(v->{
            dialog.dismiss();
        });

        dialogBinding.clodeBtn.setOnClickListener(v->{
            dialogBinding.searchView2.setText("");
        });

        //for opening textView keyboard
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialogBinding.searchView2.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(dialogBinding.searchView2, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 300);

        LinearLayoutManager lnm = new LinearLayoutManager(CheckMapActivity.this);
        lnm.setOrientation(RecyclerView.HORIZONTAL);
        dialogBinding.recViewKeywords.setLayoutManager(lnm);
        //search_adapter = new DialogMapSearchAdapter(CheckMapActivity.this, CheckMapActivity.this);
        dialogBinding.recViewKeywords.setAdapter(search_adapter);


        dialogBinding.searchView2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() == 0){
//                    dialogBinding.noData.setVisibility(View.VISIBLE);
                    dialogBinding.offers.setVisibility(View.VISIBLE);
                    dialogBinding.noData.setVisibility(View.GONE);
                    dialogBinding.recViewKeywords.setVisibility(View.GONE);
                    dialogBinding.clodeBtn.setVisibility(View.GONE);


                }else {
                    dialogBinding.offers.setVisibility(View.GONE);
                    dialogBinding.recViewKeywords.setVisibility(View.VISIBLE);
                    dialogBinding.clodeBtn.setVisibility(View.VISIBLE);

                    Call<MapPointerResponse> call = apiService.fetchMapPointers(
                            "fetchMapPointers", charSequence.toString());

                    call.enqueue(new Callback<MapPointerResponse>() {
                        @Override
                        public void onResponse(Call<MapPointerResponse> call, Response<MapPointerResponse> response) {
                            if (response.isSuccessful() && response.body() != null){
                                MapPointerResponse apiResponse = response.body();
                                if (apiResponse.getStatus().equalsIgnoreCase("success")){
                                    DialogMapSearchAdapter searchKeywordAdapter = new DialogMapSearchAdapter(
                                            CheckMapActivity.this, apiResponse.getData());
                                    dialogBinding.recViewKeywords.setAdapter(searchKeywordAdapter);
                                    dialogBinding.noData.setVisibility(View.GONE);
                                    dialogBinding.recViewKeywords.setVisibility(View.VISIBLE);
                                }else {
                                    dialogBinding.recViewKeywords.setVisibility(View.GONE);
                                    dialogBinding.noData.setVisibility(View.VISIBLE);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<MapPointerResponse> call, Throwable t) {
                            Toast.makeText(CheckMapActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            dialogBinding.noData.setVisibility(View.VISIBLE);
                            //Log.d("API Request", "API URL: " + call.request().url().toString());
                        }
                    });
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        dialog.show();
    }

    private void initializeBoundary() {
        Call<BoundaryData> call = apiService.fetchBoundaryData(
                "fetchBoundaryData");
        call.enqueue(new Callback<BoundaryData>() {
            @Override
            public void onResponse(Call<BoundaryData> call, Response<BoundaryData> response) {
                if (response.isSuccessful() && response.body() != null){
                    BoundaryData apiResponse = response.body();
                    if (apiResponse.getStatus().equalsIgnoreCase("success")){
                        if (apiResponse.getShow().equalsIgnoreCase("yes")){
                            //Toast.makeText(CheckMapActivity.this, "adding...", Toast.LENGTH_SHORT).show();
                            boundaryLatLngs = Helper.convertToLatLngList(apiResponse.getData());
                            if (boundaryLatLngs != null && !boundaryLatLngs.isEmpty()) {
                                addBoundaryAndShadow(mMap);
                            } else {
                                Toast.makeText(CheckMapActivity.this, "Failed to convert data to boundary points.", Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            Toast.makeText(CheckMapActivity.this, "List is empty! ", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(CheckMapActivity.this, apiResponse.getStatus(), Toast.LENGTH_SHORT).show();
                    }

                    binding.tutorialLink.setOnClickListener(v->{
                        try {
                            Helper.openLink(CheckMapActivity.this, apiResponse.getYoutube_link());
                        }catch (Exception e){
                            Toast.makeText(CheckMapActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<BoundaryData> call, Throwable t) {
                Toast.makeText(CheckMapActivity.this, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    // Method to add the boundary and shadow to the map
    private void addBoundaryAndShadow(GoogleMap mMap) {
        if (mMap == null || boundaryLatLngs.isEmpty()) {
            Log.e("Map", "GoogleMap or boundary coordinates are not available.");
            return;
        }

        mMap.addPolygon(createRoundedPolygonOptions(boundaryLatLngs, STROKE_WIDTH));
        moveCameraToBoundary(mMap, boundaryLatLngs);
    }

    private PolygonOptions createRoundedPolygonOptions(List<LatLng> latLngs, float strokeWidth) {
        return new PolygonOptions()
                .addAll(latLngs)
                .strokeWidth(strokeWidth)
                .strokeColor(Color.BLUE) // Boundary stroke color (e.g., blue)
                .fillColor(Color.TRANSPARENT) // Transparent fill for the boundary
                .zIndex(1); // Optional: Set the z-index for layering
    }

    // Method to move the camera to the boundary and apply padding
    private void moveCameraToBoundary(GoogleMap mMap, List<LatLng> boundaryLatLngs) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : boundaryLatLngs) {
            builder.include(point);
        }

        LatLngBounds bounds = builder.build();
        int padding = 100; // Padding around the boundary
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        // Animate the camera to the boundary
        mMap.animateCamera(cameraUpdate);
    }


    private void animateMarkerBounce(final Marker marker) {
        if (marker == null) return;

        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1500;

        final Interpolator interpolator = new DecelerateInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);
                float y = (t * 1f); // The "bounce" height in the Y direction

                marker.setAnchor(0.5f, 1 + y); // Move marker up and down

                if (elapsed < duration) {
                    handler.postDelayed(this, 16);
                } else {
                    marker.setAnchor(0.5f, 1f);
                }
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        //mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        enableUserLocation();

        // Set a listener for marker clicks
        mMap.setOnMarkerClickListener(marker -> {
            String stall = marker.getTitle();
            findDataModelByName(stall, mapResponseData);

            if (stall != null) {
                Helper.LATITUDE = marker.getPosition().latitude;
                Helper.LONGITUDE = marker.getPosition().longitude;
                Helper.NAME = marker.getTitle();

                if (stall.equalsIgnoreCase("You are here")){
                    binding.infoBox.setVisibility(View.GONE);
                }else {
                    binding.infoBox.setVisibility(View.VISIBLE);
                    binding.titleTxt.setText(stall);
                }

                binding.shareBtn.setVisibility(View.VISIBLE);
                marker.showInfoWindow();
            }
            return true;
        });

        binding.startBtn.setOnClickListener(v->{
            String encodedLabel = Uri.encode(Helper.NAME);
            Uri geoLocation = Uri.parse("geo:" + Helper.LATITUDE + "," + Helper.LONGITUDE + "?q=" +
                    Helper.LATITUDE + "," + Helper.LONGITUDE + "(" + encodedLabel + ")");
            Intent intent = new Intent(Intent.ACTION_VIEW, geoLocation);
            Intent chooser = Intent.createChooser(intent, "Open with");
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(chooser);
            } else {
                Toast.makeText(CheckMapActivity.this, "No app available to open the map.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.shareBtn.setOnClickListener(v->{
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/html");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, Helper.generateMapShareMessage(CheckMapActivity.this));
            if (sharingIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(Intent.createChooser(sharingIntent,"Share using"));
            }
        });

        binding.mapType.setOnClickListener(v -> {
            // Show a popup menu for map type selection
            PopupMenu popupMenu = new PopupMenu(this, v);
            popupMenu.getMenuInflater().inflate(R.menu.map_type_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.normal) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                } else if (itemId == R.id.satellite) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                } else if (itemId == R.id.terrain) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                } else if (itemId == R.id.hybrid) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                }
                return true;
            });

            popupMenu.show();
        });
    }

    public void findDataModelByName(String name, List<MapPointerResponse.Data> dataList) {
        if (name == null || dataList == null || dataList.isEmpty()) {

        }

        for (MapPointerResponse.Data data : dataList) {
            if (name.equalsIgnoreCase(data.getName())) { // Case-insensitive match
                Glide.with(CheckMapActivity.this)
                        .load(data.getImage())
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .placeholder(getDrawable(R.drawable.placeholder))
                        .skipMemoryCache(false)
                        .into(binding.img);
                Helper.DESCRIPTION = data.getDescription();
                binding.descriptionTxt.setText(data.getDescription());

            }
        }
    }

    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            mMap.setMyLocationEnabled(true);
            getUserLocation();
        }
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        // Get the current location as LatLng
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                        // Move camera smoothly to the current location with zoom level 16
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLocation, 30);
                        mMap.animateCamera(cameraUpdate, 1000, new GoogleMap.CancelableCallback() {
                            @Override
                            public void onFinish() {
                                // This is called when the camera animation finishes
                                Log.d("Camera", "Animation finished");
                            }

                            @Override
                            public void onCancel() {
                                // This is called when the camera animation is canceled
                                Log.d("Camera", "Animation canceled");
                            }
                        });

                        // Remove the old marker if it exists
                        if (currentLocationMarker != null) {
                            currentLocationMarker.remove();
                        }

                        // Add a new marker at the current location
                        currentLocationMarker = mMap.addMarker(new MarkerOptions()
                                .position(currentLocation)
                                .title("You are here")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));  // Customize the marker icon
                        currentLocationMarker.setSnippet("Current Location");
                        currentLocationMarker.showInfoWindow();

                        // Optional: Add a bounce animation to the marker for more visual impact
                        animateMarkerBounce(currentLocationMarker);

                    } else {
                        Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateTo(LatLng destination) {
        if (userLocation == null) {
            Toast.makeText(this, "Unable to fetch your location", Toast.LENGTH_SHORT).show();
            return;
        }

        // Call Directions API and draw polyline
        String url = getDirectionsUrl(userLocation, destination);

        new FetchDirectionsTask(new FetchDirectionsTask.Callback() {
            @Override
            public void onRouteFetched(List<LatLng> routePoints) {
                if (currentPolyline != null) {
                    currentPolyline.remove();
                }
                currentPolyline = mMap.addPolyline(new PolylineOptions()
                        .addAll(routePoints)
                        .color(getResources().getColor(R.color.purple_500))
                        .width(8));
            }
        }).execute(url);

    }

    private String getDirectionsUrl(LatLng origin, LatLng destination) {
        return "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                origin.latitude + "," + origin.longitude + "&destination=" +
                destination.latitude + "," + destination.longitude + "&key=AIzaSyCPX1Nv9DdjtO4Ls6bCsJTpGwg3_LMoT2o";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableUserLocation();
        }
    }


    //Typing start
    private void startTypingAnimation() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentText = textList.get(listIndex);
                if (!isDeleting) {
                    if (charIndex < currentText.length()) {
                        binding.searchTxt.setText(currentText.substring(0, charIndex + 1));
                        charIndex++;
                        handler.postDelayed(this, typingDelay);
                    } else {
                        handler.postDelayed(() -> isDeleting = true, showDelay);
                        handler.postDelayed(this, showDelay);
                    }
                } else {
                    if (charIndex > 0) {
                        binding.searchTxt.setText(currentText.substring(0, charIndex - 1));
                        charIndex--;
                        handler.postDelayed(this, deletingDelay);
                    } else {
                        isDeleting = false;
                        listIndex = (listIndex + 1) % textList.size();
                        handler.postDelayed(this, 0);
                    }
                }
            }
        }, typingDelay);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}