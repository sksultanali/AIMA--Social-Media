package com.developerali.aima.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.developerali.aima.Adapters.FollowAdapter;
import com.developerali.aima.Adapters.PostAdapter;
import com.developerali.aima.Adapters.VideoPostAdapter;
import com.developerali.aima.Helpers.Helper;
import com.developerali.aima.HomeBottomBar.AimaPosts;
import com.developerali.aima.HomeBottomBar.AimaVideos;
import com.developerali.aima.Model_Apis.ApiService;
import com.developerali.aima.Model_Apis.PostResponse;
import com.developerali.aima.Model_Apis.RetrofitClient;
import com.developerali.aima.Model_Apis.UserDetails;
import com.developerali.aima.Model_Apis.UsersResponse;
import com.developerali.aima.Model_Apis.VideoResponse;
import com.developerali.aima.R;
import com.developerali.aima.databinding.ActivitySearchResultsBinding;
import com.google.firebase.auth.FirebaseAuth;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchResults extends AppCompatActivity {

    ActivitySearchResultsBinding binding;
    ApiService apiService;
    String type, keyword;
    PostAdapter adapter;
    FollowAdapter followAdapter;
    VideoPostAdapter adapterVideo;
    FirebaseAuth auth;
    int nextPageToken, currentPage = 0;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchResultsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        apiService = RetrofitClient.getClient().create(ApiService.class);
        auth = FirebaseAuth.getInstance();

        binding.goBack.setOnClickListener(v->{
            onBackPressed();
        });

        type = getIntent().getStringExtra("type");
        keyword = getIntent().getStringExtra("keyword");

        binding.titleTxt.setText(keyword);
        binding.typeTxt.setText(type);

        if (type.equalsIgnoreCase("post")){
            setupRecyclerView1();
            getPostData();
        }else if (type.equalsIgnoreCase("video")){
            setupRecyclerView();
            getVideoPostData();
        }else{
            setupRecyclerView2();
            getPeopleData();
        }





    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
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
                    // Load more data here
                    isLoading = true;
                    //binding.spinKit.setVisibility(View.VISIBLE);
                    getVideoPostData();
                }
            }
        });
    }
    void getVideoPostData() {
        binding.loadMore.setVisibility(View.VISIBLE);
        Call<VideoResponse> call = apiService.searchAllVideos(
                "searchAllVideos", keyword, nextPageToken
        );

        call.enqueue(new Callback<VideoResponse>() {
            @Override
            public void onResponse(Call<VideoResponse> call, Response<VideoResponse> response) {
                if (response.isSuccessful() && response.body() != null){
                    VideoResponse apiResponse = response.body();
                    if (apiResponse.getStatus().equalsIgnoreCase("success") && apiResponse.getData() != null
                            && !apiResponse.getData().isEmpty()){
                        //binding.noData.setVisibility(View.GONE);
                        if (adapterVideo == null) {
                            adapterVideo = new VideoPostAdapter(apiResponse.getData(), getLifecycle(), SearchResults.this, false);
                            binding.discoverRecyclerView.setAdapter(adapterVideo);
                        } else {
                            adapterVideo.addItems(apiResponse.getData()); // Modify your adapter to support adding new items
                        }

                        nextPageToken = apiResponse.getNextToken();
                        currentPage = (nextPageToken/15);

                        isLoading = false;
                    }else{
                        //binding.noData.setVisibility(View.VISIBLE);
                        binding.message.setText(apiResponse.getMessage());
                    }
                    checkRecData();
                    binding.loadMore.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<VideoResponse> call, Throwable t) {
                binding.noData.setVisibility(View.VISIBLE);
                Toast.makeText(SearchResults.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void setupRecyclerView1() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
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
                    // Load more data here
                    isLoading = true;
                    //binding.spinKit.setVisibility(View.VISIBLE);
                    getPostData();
                }
            }
        });
    }
    void getPostData() {
        binding.loadMore.setVisibility(View.VISIBLE);
        Call<PostResponse> call = apiService.searchAllPosts(
                "searchAllPosts", keyword, nextPageToken
        );

        call.enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                if (response.isSuccessful() && response.body() != null){
                    PostResponse apiResponse = response.body();
                    if (apiResponse.getStatus().equalsIgnoreCase("success") && apiResponse.getData() != null
                            && !apiResponse.getData().isEmpty()){
                        //binding.noData.setVisibility(View.GONE);
                        if (adapter == null) {
                            adapter = new PostAdapter(apiResponse.getData(), SearchResults.this, false);
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
                binding.noData.setVisibility(View.VISIBLE);
                Toast.makeText(SearchResults.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void setupRecyclerView2() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
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
                    // Load more data here
                    isLoading = true;
                    //binding.spinKit.setVisibility(View.VISIBLE);
                    getPeopleData();
                }
            }
        });
    }
    void getPeopleData() {
        binding.loadMore.setVisibility(View.VISIBLE);
        Call<UsersResponse> call = apiService.searchPeople(
                "searchPeople", keyword, auth.getCurrentUser().getUid(), nextPageToken
        );

        call.enqueue(new Callback<UsersResponse>() {
            @Override
            public void onResponse(Call<UsersResponse> call, Response<UsersResponse> response) {
                binding.loadMore.setVisibility(View.GONE);
                if (!response.isSuccessful() || response.body() == null) {
                    binding.noData.setVisibility(View.VISIBLE);
                    binding.message.setText(response.body().getMessage());
                    return;
                }
                UsersResponse apiResponse = response.body();
                if (apiResponse.getStatus().equalsIgnoreCase("success")) {
                    if (followAdapter == null) {
                        followAdapter = new FollowAdapter(SearchResults.this, apiResponse.getData());
                        binding.discoverRecyclerView.setAdapter(followAdapter);
                    } else {
                        followAdapter.addItems(apiResponse.getData()); // Ensure this method handles updates
                    }
                    nextPageToken = apiResponse.getNextToken();
                    currentPage = (nextPageToken / 15);
                    isLoading = false;
                }else {
                    //binding.noData.setVisibility(View.VISIBLE);
                    binding.message.setText(apiResponse.getMessage());
                }
                checkRecData();
            }

            @Override
            public void onFailure(Call<UsersResponse> call, Throwable t) {
                binding.noData.setVisibility(View.VISIBLE);
                Toast.makeText(SearchResults.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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