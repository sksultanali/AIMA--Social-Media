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
import com.developerali.aima.Helpers.Helper;
import com.developerali.aima.Model_Apis.ApiService;
import com.developerali.aima.Model_Apis.PostResponse;
import com.developerali.aima.Model_Apis.RetrofitClient;
import com.developerali.aima.R;
import com.developerali.aima.databinding.FragmentNormalPostBinding;
import com.google.firebase.auth.FirebaseAuth;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NormalPost extends Fragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    FragmentNormalPostBinding binding;
    int nextPageToken, currentPage = 0;
    private boolean isLoading = false, normalPost = false;
    FirebaseAuth auth;
    String profileId;
    PostAdapter adapter;
    ApiService apiService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentNormalPostBinding.inflate(inflater, container, false);
        apiService = RetrofitClient.getClient().create(ApiService.class);
        auth = FirebaseAuth.getInstance();
        profileId = Helper.tempId;

        setupRecyclerView();
        if (profileId.equalsIgnoreCase(auth.getCurrentUser().getUid())){
            getMyPostData();
        }else {
            getPostData(profileId);
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
                        getMyPostData();
                    }else {
                        getPostData(profileId);
                    }
                }
            }
        });
    }

    void getPostData(String uploaderId) {
        binding.loadMore.setVisibility(View.VISIBLE);
        Call<PostResponse> call = apiService.getAllAdminPost(
                "getAllPost", "Approved", uploaderId, nextPageToken
        );

        call.enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                if (response.isSuccessful() && response.body() != null){
                    PostResponse apiResponse = response.body();
                    if (apiResponse.getStatus().equalsIgnoreCase("success") && apiResponse.getData() != null
                            && !apiResponse.getData().isEmpty()){
                        binding.noData.setVisibility(View.GONE);
                        if (adapter == null) {
                            adapter = new PostAdapter(apiResponse.getData(), getActivity(), false);
                            binding.discoverRecyclerView.setAdapter(adapter);
                        } else {
                            adapter.addItems(apiResponse.getData()); // Modify your adapter to support adding new items
                        }

                        nextPageToken = apiResponse.getNextToken();
                        currentPage = (nextPageToken/15);
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
            public void onFailure(Call<PostResponse> call, Throwable t) {

            }
        });
    }
    void getMyPostData() {
        binding.loadMore.setVisibility(View.VISIBLE);
        Call<PostResponse> call = apiService.getMyPosts(
                "getMyPosts", auth.getCurrentUser().getUid(), nextPageToken
        );

        call.enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                if (response.isSuccessful() && response.body() != null){
                    PostResponse apiResponse = response.body();
                    if (apiResponse.getStatus().equalsIgnoreCase("success") && apiResponse.getData() != null
                            && !apiResponse.getData().isEmpty()){

                        binding.noData.setVisibility(View.GONE);
                        if (adapter == null) {
                            adapter = new PostAdapter(apiResponse.getData(), getActivity(), true);
                            binding.discoverRecyclerView.setAdapter(adapter);
                        } else {
                            adapter.addItems(apiResponse.getData()); // Modify your adapter to support adding new items
                        }

                        nextPageToken = apiResponse.getNextToken();
                        currentPage = (nextPageToken/15);
                        isLoading = false;

                    }else {
                        //binding.noData.setVisibility(View.VISIBLE);
                        binding.message.setText(apiResponse.getMessage());
                    }
                    checkRecData();
                    binding.loadMore.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<PostResponse> call, Throwable t) {

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