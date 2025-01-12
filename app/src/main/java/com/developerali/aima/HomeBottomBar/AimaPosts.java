package com.developerali.aima.HomeBottomBar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.developerali.aima.Adapters.PostAdapter;
import com.developerali.aima.Helpers.ApiReformOldPost;
import com.developerali.aima.Helpers.Helper;
import com.developerali.aima.Model_Apis.ApiService;
import com.developerali.aima.Model_Apis.PostResponse;
import com.developerali.aima.Model_Apis.RetrofitClient;
import com.developerali.aima.Models.PostModel;
import com.developerali.aima.Models.PostModelOld;
import com.developerali.aima.databinding.ActivityAimaPostsBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AimaPosts extends AppCompatActivity {

    ActivityAimaPostsBinding binding;
    FirebaseFirestore firebaseFirestore;
    ArrayList<PostModel> postModelArrayList;
    CollectionReference collectionReference;
    PostAdapter adapter;
    //private DocumentSnapshot lastVisibleDocument;
    ApiService apiService;
    int nextPageToken, currentPage = 0;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAimaPostsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        apiService = RetrofitClient.getClient().create(ApiService.class);

        firebaseFirestore = FirebaseFirestore.getInstance();
        postModelArrayList = new ArrayList<>();
        collectionReference = firebaseFirestore.collection("post");

        setupRecyclerView();
        getPostData();

//        LinearLayoutManager lnm = new LinearLayoutManager(AimaPosts.this);
//        binding.discoverRecyclerView.setLayoutManager(lnm);
//        adapter = new PostAdapter(postModelArrayList, AimaPosts.this);
//        binding.discoverRecyclerView.setAdapter(adapter);
//        initialPage();

        binding.goBack.setOnClickListener(v->{
            finish();
        });

        binding.nextBtn.setOnClickListener(c->{
            //binding.spinKit.setVisibility(View.VISIBLE);
            //nextStore();
        });

//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        database.getReference().child("users")
//            .addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    for (DataSnapshot snapshot1 : snapshot.getChildren()){
//                        UserModelOld userModelOld = snapshot1.getValue(UserModelOld.class);
//
//                        UserApiReform apiReformOldPost = new UserApiReform(apiService, AimaPosts.this);
//                        Toast.makeText(AimaPosts.this, "doing..." + snapshot1.getKey(), Toast.LENGTH_SHORT).show();
//                        int verified = userModelOld.isVerified() ? 1 : 0;
//
//                        apiReformOldPost.enqueueInsertTask(
//                                snapshot1.getKey(), userModelOld.getName
//                                        (), userModelOld.getType(), verified,
//                                "2025-01-05", userModelOld.getStars(), userModelOld.getEmail(), userModelOld.getPhone(),
//                                userModelOld.getPassword(), userModelOld.getImage(), userModelOld.getCover(), userModelOld.getWhatsapp(),
//                                userModelOld.getFacebook(), userModelOld.getAbout(), userModelOld.getBio(), userModelOld.getFollower(),
//                                userModelOld.getFollowing(), userModelOld.getPosts(), "NA", ()->{
//                                    Toast.makeText(AimaPosts.this, "done", Toast.LENGTH_SHORT).show();
//                                }
//                        );
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//
//                }
//            });

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
        Call<PostResponse> call = apiService.getAllPost(
                "getAllPost", "Approved", nextPageToken
        );

        call.enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                if (response.isSuccessful() && response.body() != null){
                    PostResponse apiResponse = response.body();
                    if (apiResponse.getStatus().equalsIgnoreCase("success") && apiResponse.getData() != null
                            && !apiResponse.getData().isEmpty()){
                        if (adapter == null) {
                            adapter = new PostAdapter(apiResponse.getData(), AimaPosts.this, false);
                            binding.discoverRecyclerView.setAdapter(adapter);
                        } else {
                            adapter.addItems(apiResponse.getData()); // Modify your adapter to support adding new items
                        }

                        nextPageToken = apiResponse.getNextToken();
                        currentPage = (nextPageToken/15);
                        Helper.saveCountsToSharedPref(AimaPosts.this, "PublicPosts", apiResponse.getTotalPost());

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
            public void onFailure(Call<PostResponse> call, Throwable t) {

            }
        });

    }

    public void initialPage(){
        Query query = collectionReference.orderBy("time", Query.Direction.DESCENDING)
                .whereEqualTo("approved", true)
                .limit(18);;

        collectionReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                if (!queryDocumentSnapshots.isEmpty()) {
                    postModelArrayList.clear();
//                    lastVisibleDocument = queryDocumentSnapshots.getDocuments()
//                            .get(queryDocumentSnapshots.size() - 1);
                    // Process the data from the initial page
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()){
                        PostModelOld postModel = snapshot.toObject(PostModelOld.class);
                        Toast.makeText(AimaPosts.this, "fil ", Toast.LENGTH_SHORT).show();

                        if (postModel != null){
                            postModel.setId(snapshot.getId());
                            postModel.setImage(snapshot.getString("image"));
                            postModel.setUploader(snapshot.getString("uploader"));
                            if (snapshot.getString("caption") != null){
                                postModel.setCaption(snapshot.getString("caption"));
                            }else {
                                postModel.setCaption(null);
                            }
                            postModel.setCommentsCount(postModel.getCommentsCount());
                            postModel.setLikesCount(postModel.getLikesCount());

                            ApiReformOldPost apiReformOldPost = new ApiReformOldPost(apiService, AimaPosts.this);
                            String status;
                            if (snapshot.contains("approved")){
                                boolean t = Boolean.TRUE.equals(snapshot.getBoolean("approved"));
                                status = t ? "Approved" : "Pending Approval";
                            }else {
                                status = "Pending Approval";
                            }


//                            Call<ApiResponse> call = postService.insertOldPost(
//                                    currentTask.id,
//                                    currentTask.image,
//                                    currentTask.uploader,
//                                    currentTask.caption,
//                                    currentTask.status,
//                                    currentTask.commentsCount,
//                                    currentTask.likesCount
//                            );
//
//                            call.enqueue(new Callback<ApiResponse>() {
//                                @Override
//                                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
//                                    if (response.isSuccessful() && response.body() != null) {
//                                        ApiResponse apiResponse = response.body();
//                                        if ("success".equals(apiResponse.getStatus())) {
//                                            if (currentTask.actionOnSuccess != null) {
//                                                currentTask.actionOnSuccess.run();
//                                            }
//                                        }else {
//                                            Toast.makeText(activity, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
//                                        }
//                                        taskQueue.poll(); // Remove the task after successful completion
//                                        processNextTask(); // Process the next task
//                                    } else {
//                                        // Handle failure (e.g., log error or retry)
//                                    }
//                                }
//
//                                @Override
//                                public void onFailure(Call<ApiResponse> call, Throwable t) {
//                                    // Handle the failure scenario (e.g., retry or log error)
//                                }
//                            });

                            apiReformOldPost.enqueueInsertTask(
                                    snapshot.getId(), postModel.getImage(), postModel.getUploader(), postModel.getCaption(),
                                    status, postModel.getCommentsCount(), postModel.getLikesCount(), ()->{
                                        Toast.makeText(AimaPosts.this, "done", Toast.LENGTH_SHORT).show();
                                    }
                            );

//                            postModel.setTime(snapshot.getLong("time"));
//                            if (snapshot.contains("approved")){
//                                boolean t = Boolean.TRUE.equals(snapshot.getBoolean("approved"));
//                                postModel.setApproved(t);
//                            }else {
//                                postModel.setApproved(false);
//                            }
//                            postModelArrayList.add(postModel);
                        }
                    }
//                    adapter.notifyDataSetChanged();
//                    binding.spinKit.setVisibility(View.GONE);
//                    binding.nextBtn.setVisibility(View.VISIBLE);
                }else {
                    //binding.spinKit.setVisibility(View.GONE);
                    Toast.makeText(AimaPosts.this, "No Records!", Toast.LENGTH_SHORT).show();
                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AimaPosts.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                //binding.spinKit.setVisibility(View.GONE);
            }
        });

    }
//    public void nextStore(){
//        Query query = collectionReference.orderBy("time", Query.Direction.DESCENDING)
//                .startAfter(lastVisibleDocument)
//                .whereEqualTo("approved", true)
//                .limit(18);
//
//        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
//                    if (!queryDocumentSnapshots.isEmpty()) {
//                        postModelArrayList.clear();
//                        lastVisibleDocument = queryDocumentSnapshots.getDocuments()
//                                .get(queryDocumentSnapshots.size() - 1);
//                        // Process the data from the next page
//                        for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()){
//                            PostModel postModel = snapshot.toObject(PostModel.class);
//
//                            if (postModel != null){
//                                postModel.setId(snapshot.getId());
//                                postModel.setImage(snapshot.getString("image"));
//                                postModel.setUploader(snapshot.getString("uploader"));
//                                if (snapshot.getString("caption") != null){
//                                    postModel.setCaption(snapshot.getString("caption"));
//                                }
//
//                                //postModel.setTime(snapshot.getLong("time"));
//                                postModel.setCommentsCount(postModel.getCommentsCount());
//                                postModel.setLikesCount(postModel.getLikesCount());
//                                if (snapshot.contains("approved")){
//                                    boolean t = Boolean.TRUE.equals(snapshot.getBoolean("approved"));
//
//                                }
//                                postModelArrayList.add(postModel);
//                            }
//                        }
//                        adapter.notifyDataSetChanged();
//                        binding.discoverRecyclerView.smoothScrollToPosition(0);
//                        binding.spinKit.setVisibility(View.GONE);
//                    }else {
//                        binding.spinKit.setVisibility(View.GONE);
//                        binding.nextBtn.setVisibility(View.GONE);
//                        Toast.makeText(AimaPosts.this, "No Records!", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    binding.spinKit.setVisibility(View.GONE);
//                });
//    }
}