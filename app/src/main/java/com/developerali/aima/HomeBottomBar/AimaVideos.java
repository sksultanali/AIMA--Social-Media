package com.developerali.aima.HomeBottomBar;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.developerali.aima.Adapters.PostAdapter;
import com.developerali.aima.Adapters.VideoPostAdapter;
import com.developerali.aima.Helpers.Helper;
import com.developerali.aima.Model_Apis.ApiService;
import com.developerali.aima.Model_Apis.PostResponse;
import com.developerali.aima.Model_Apis.RetrofitClient;
import com.developerali.aima.Model_Apis.VideoResponse;
import com.developerali.aima.Models.VideoModel;
import com.developerali.aima.databinding.ActivityAimaVideosBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AimaVideos extends AppCompatActivity {

    ActivityAimaVideosBinding binding;
    VideoPostAdapter adapterVideo;
    ArrayList<VideoModel> postVideoArrayList = new ArrayList<>();
    CollectionReference collectionReference;
    FirebaseFirestore firebaseFirestore;
    private DocumentSnapshot lastVisibleDocument;
    ApiService apiService;
    int nextPageToken, currentPage = 0;
    private boolean isLoading = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAimaVideosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        apiService = RetrofitClient.getClient().create(ApiService.class);

        firebaseFirestore = FirebaseFirestore.getInstance();
        collectionReference = firebaseFirestore.collection("video");

        setupRecyclerView();
        getPostData();
//        LinearLayoutManager lnm = new LinearLayoutManager(AimaVideos.this);
//        binding.discoverRecyclerView.setLayoutManager(lnm);
//        adapterVideo = new VideoPostAdapter(postVideoArrayList, getLifecycle(), AimaVideos.this);
        //initialVideoPage();

        binding.goBack.setOnClickListener(v->{
            finish();
        });

        binding.nextBtn.setOnClickListener(v->{
            //nextVideoStore();
        });

        
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
                    getPostData();
                }
            }
        });
    }

    void getPostData() {
        binding.loadMore.setVisibility(View.VISIBLE);
        Call<VideoResponse> call = apiService.getAllVideo(
                "getAllVideo", null, nextPageToken
        );

        call.enqueue(new Callback<VideoResponse>() {
            @Override
            public void onResponse(Call<VideoResponse> call, Response<VideoResponse> response) {
                if (response.isSuccessful() && response.body() != null){
                    VideoResponse apiResponse = response.body();
                    if (apiResponse.getStatus().equalsIgnoreCase("success") && apiResponse.getData() != null
                            && !apiResponse.getData().isEmpty()){
                        if (adapterVideo == null) {
                            adapterVideo = new VideoPostAdapter(apiResponse.getData(), getLifecycle(), AimaVideos.this, false);
                            binding.discoverRecyclerView.setAdapter(adapterVideo);
                        } else {
                            adapterVideo.addItems(apiResponse.getData()); // Modify your adapter to support adding new items
                        }

                        nextPageToken = apiResponse.getNextToken();
                        currentPage = (nextPageToken/15);
                        Helper.saveCountsToSharedPref(AimaVideos.this, "Videos", apiResponse.getVideosCount());

//                        if (apiResponse.getData().size() < 15){
//                            nextPageToken = 0;
//                        }
                        isLoading = false;
                    }
                    //binding.spinKit.setVisibility(View.GONE);
                    binding.loadMore.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<VideoResponse> call, Throwable t) {

            }
        });

    }



//    public void initialVideoPage(){
//        binding.discoverRecyclerView.setAdapter(adapterVideo);
//
//        binding.spinKit.setVisibility(View.VISIBLE);
//        binding.nextBtn.setVisibility(View.GONE);
//
//        collectionReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//
//                        if (!queryDocumentSnapshots.isEmpty()) {
//                            postVideoArrayList.clear();
//                            lastVisibleDocument = queryDocumentSnapshots.getDocuments()
//                                    .get(queryDocumentSnapshots.size() - 1);
//                            // Process the data from the initial page
//                            for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()){
//                                VideoModel postModel = snapshot.toObject(VideoModel.class);
//                                if (postModel != null){
//                                    postModel.setUploader(snapshot.getString("uploader"));
//                                    if (snapshot.getString("caption") != null){
//                                        postModel.setCaption(snapshot.getString("caption"));
//                                    }
//
//                                    VideoApiReform apiReformOldPost = new VideoApiReform(apiService);
//                                    Toast.makeText(AimaVideos.this, "doing..." + snapshot.getId(), Toast.LENGTH_SHORT).show();
//
//                                    String status;
//                                    if (postModel.isApproved()){
//                                        status = "Approved";
//                                    }else {
//                                        status = "Pending Approval";
//                                    }
//
//                                    apiReformOldPost.enqueueInsertTask(
//                                            snapshot.getId(), postModel.getUploader(), postModel.getVideoId(), postModel.getCaption(), status,
//                                            ()->{
//                                                Toast.makeText(AimaVideos.this, "done", Toast.LENGTH_SHORT).show();
//                                            }
//                                    );
//                                    //postModel.setTime(snapshot.getLong("time"));
////                                    postVideoArrayList.add(postModel);
//                                }
//                            }
////                            adapterVideo.notifyDataSetChanged();
////                            binding.spinKit.setVisibility(View.GONE);
////                            binding.nextBtn.setVisibility(View.VISIBLE);
//                        }else {
//                            binding.spinKit.setVisibility(View.GONE);
//                            Toast.makeText(AimaVideos.this, "No Records!", Toast.LENGTH_SHORT).show();
//                        }
//
//
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(AimaVideos.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
//                        binding.spinKit.setVisibility(View.GONE);
//                    }
//                });
//    }
//    public void nextVideoStore(){
//        collectionReference // Order by a suitable field
//                .orderBy("time", Query.Direction.DESCENDING)
//                .startAfter(lastVisibleDocument)
//                .whereEqualTo("approved", true)
//                .limit(5)
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    if (!queryDocumentSnapshots.isEmpty()) {
//                        postVideoArrayList.clear();
//                        lastVisibleDocument = queryDocumentSnapshots.getDocuments()
//                                .get(queryDocumentSnapshots.size() - 1);
//                        // Process the data from the next page
//                        for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()){
//                            VideoModel postModel = snapshot.toObject(VideoModel.class);
//
//                            if (postModel != null){
//                                postModel.setUploader(snapshot.getString("uploader"));
//                                if (snapshot.getString("caption") != null){
//                                    postModel.setCaption(snapshot.getString("caption"));
//                                }
//
//                                //postModel.setTime(snapshot.getLong("time"));
//                                postVideoArrayList.add(postModel);
//                            }
//                        }
//                        adapterVideo.notifyDataSetChanged();
//                        binding.discoverRecyclerView.smoothScrollToPosition(0);
//                        binding.spinKit.setVisibility(View.GONE);
//                    }else {
//                        binding.spinKit.setVisibility(View.GONE);
//                        binding.nextBtn.setVisibility(View.GONE);
//                        Toast.makeText(AimaVideos.this, "No Records!", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    binding.spinKit.setVisibility(View.GONE);
//                });
//    }
}