package com.developerali.aima.MyPostSection;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.developerali.aima.Activities.MyPostActivity;
import com.developerali.aima.Adapters.PostAdapter;
import com.developerali.aima.Adapters.VideoPostAdapter;
import com.developerali.aima.Helpers.Helper;
import com.developerali.aima.Model_Apis.ApiService;
import com.developerali.aima.Model_Apis.RetrofitClient;
import com.developerali.aima.Model_Apis.VideoResponse;
import com.developerali.aima.R;
import com.developerali.aima.databinding.FragmentVideoPostBinding;
import com.google.firebase.auth.FirebaseAuth;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoPost extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    FragmentVideoPostBinding binding;
    int nextPageToken, currentPage = 0;
    private boolean isLoading = false, normalPost = false;
    FirebaseAuth auth;
    String profileId;
    VideoPostAdapter adapterVideo;
    ApiService apiService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentVideoPostBinding.inflate(inflater, container, false);
        apiService = RetrofitClient.getClient().create(ApiService.class);
        auth = FirebaseAuth.getInstance();
        profileId = Helper.tempId;

        setupRecyclerView();
        if (profileId.equalsIgnoreCase(auth.getCurrentUser().getUid())){
            getMyVideos();
        }else {
            getVideoPost(profileId);
        }




        return binding.getRoot();
    }
    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        binding.discoverRecyclerView.setLayoutManager(layoutManager);
        binding.discoverRecyclerView.setHasFixedSize(true);
        binding.discoverRecyclerView.setItemViewCacheSize(50);
        binding.discoverRecyclerView.setDrawingCacheEnabled(true);
        binding.discoverRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        binding.discoverRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount) {
                    isLoading = true;
                    if (profileId.equalsIgnoreCase(auth.getCurrentUser().getUid())){
                        getMyVideos();
                    }else {
                        getVideoPost(profileId);
                    }
                }
            }
        });
    }
    void getVideoPost(String uploader) {
        binding.loadMore.setVisibility(View.VISIBLE);
        Call<VideoResponse> call = apiService.getAllVideo(
                "getAllVideo", uploader, nextPageToken
        );

        call.enqueue(new Callback<VideoResponse>() {
            @Override
            public void onResponse(Call<VideoResponse> call, Response<VideoResponse> response) {
                if (response.isSuccessful() && response.body() != null){
                    VideoResponse apiResponse = response.body();
                    if (apiResponse.getStatus().equalsIgnoreCase("success") && apiResponse.getData() != null
                            && !apiResponse.getData().isEmpty()){

                        binding.noData.setVisibility(View.GONE);
                        if (adapterVideo == null) {
                            adapterVideo = new VideoPostAdapter(apiResponse.getData(), getLifecycle(), getActivity(), false);
                            binding.discoverRecyclerView.setAdapter(adapterVideo);
                        } else {
                            adapterVideo.addItems(apiResponse.getData()); // Modify your adapter to support adding new items
                        }

                        nextPageToken = apiResponse.getNextToken();
                        currentPage = (nextPageToken/15);

//                        if (apiResponse.getData().size() < 15){
//                            nextPageToken = 0;
//                        }
                        isLoading = false;

                    }else {
                        binding.noData.setVisibility(View.VISIBLE);
                        binding.message.setText(apiResponse.getMessage());
                    }
                    checkRecData();
                    binding.loadMore.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<VideoResponse> call, Throwable t) {
                binding.noData.setVisibility(View.VISIBLE);
            }
        });

    }
    void getMyVideos() {
        binding.loadMore.setVisibility(View.VISIBLE);
        Call<VideoResponse> call = apiService.getMyVideos(
                "getMyVideos", auth.getCurrentUser().getUid(), nextPageToken
        );

        call.enqueue(new Callback<VideoResponse>() {
            @Override
            public void onResponse(Call<VideoResponse> call, Response<VideoResponse> response) {
                if (response.isSuccessful() && response.body() != null){
                    VideoResponse apiResponse = response.body();
                    if (apiResponse.getStatus().equalsIgnoreCase("success") && apiResponse.getData() != null
                            && !apiResponse.getData().isEmpty()){

                        binding.noData.setVisibility(View.GONE);
                        if (adapterVideo == null) {
                            adapterVideo = new VideoPostAdapter(apiResponse.getData(), getLifecycle(), getActivity(), true);
                            binding.discoverRecyclerView.setAdapter(adapterVideo);
                        } else {
                            adapterVideo.addItems(apiResponse.getData()); // Modify your adapter to support adding new items
                        }

                        nextPageToken = apiResponse.getNextToken();
                        currentPage = (nextPageToken/15);

//                        if (apiResponse.getData().size() < 15){
//                            nextPageToken = 0;
//                        }
                        isLoading = false;
                    }else {
                        binding.noData.setVisibility(View.VISIBLE);
                        binding.message.setText(apiResponse.getMessage());
                    }
                    checkRecData();
                    binding.loadMore.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<VideoResponse> call, Throwable t) {

            }
        });

    }

    void checkRecData(){
        if (binding.discoverRecyclerView.getAdapter() != null) {
            int itemCount = binding.discoverRecyclerView.getAdapter().getItemCount();
            if (itemCount > 0) {
                binding.noData.setVisibility(View.GONE);
            } else {
                binding.noData.setVisibility(View.VISIBLE);
            }
        } else {
            binding.noData.setVisibility(View.VISIBLE);
        }
    }
}