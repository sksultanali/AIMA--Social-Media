package com.developerali.aima.HomeBottomBar;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.developerali.aima.Adapters.PostAdapter;
import com.developerali.aima.Helpers.Helper;
import com.developerali.aima.Model_Apis.ApiService;
import com.developerali.aima.Model_Apis.PostResponse;
import com.developerali.aima.Model_Apis.RetrofitClient;
import com.developerali.aima.Models.PostModel;
import com.developerali.aima.databinding.ActivityAimaAdminBinding;
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

public class AimaAdmin extends AppCompatActivity {

    ActivityAimaAdminBinding binding;
    FirebaseFirestore firebaseFirestore;
    ArrayList<PostModel> postModelArrayList;
    CollectionReference collectionReference;
    PostAdapter adapter;
    private DocumentSnapshot lastVisibleDocument;
    ApiService apiService;
    int nextPageToken, currentPage = 0;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAimaAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseFirestore = FirebaseFirestore.getInstance();
        postModelArrayList = new ArrayList<>();
        apiService = RetrofitClient.getClient().create(ApiService.class);

        collectionReference = firebaseFirestore.collection("post");

        setupRecyclerView();
        getPostData();

//        LinearLayoutManager lnm = new LinearLayoutManager(AimaAdmin.this);
//        binding.discoverRecyclerView.setLayoutManager(lnm);
//        adapter = new PostAdapter(postModelArrayList, AimaAdmin.this);
//        binding.discoverRecyclerView.setAdapter(adapter);

        //initialPage();

        binding.goBack.setOnClickListener(v->{
            finish();
        });

        binding.nextBtn.setOnClickListener(c->{
            binding.loadMore.setVisibility(View.VISIBLE);
            nextStore();
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
        Call<PostResponse> call = apiService.getAllAdminPost(
                "getAllPost", "Approved", "Admin", nextPageToken
        );

        call.enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                if (response.isSuccessful() && response.body() != null){
                    PostResponse apiResponse = response.body();
                    if (apiResponse.getStatus().equalsIgnoreCase("success") && apiResponse.getData() != null
                            && !apiResponse.getData().isEmpty()){
                        if (adapter == null) {
                            adapter = new PostAdapter(apiResponse.getData(), AimaAdmin.this, false);
                            binding.discoverRecyclerView.setAdapter(adapter);
                        } else {
                            adapter.addItems(apiResponse.getData()); // Modify your adapter to support adding new items
                        }

                        nextPageToken = apiResponse.getNextToken();
                        currentPage = (nextPageToken/15);
                        Helper.saveCountsToSharedPref(AimaAdmin.this, "AdminPosts", apiResponse.getTotalAdminPost());

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
                .whereEqualTo("uploader", "Admin")
                .limit(18);

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                if (!queryDocumentSnapshots.isEmpty()) {

                    postModelArrayList.clear();

                    lastVisibleDocument = queryDocumentSnapshots.getDocuments()
                            .get(queryDocumentSnapshots.size() - 1);
                    // Process the data from the initial page
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()){
                        PostModel postModel = snapshot.toObject(PostModel.class);

                        if (postModel != null){
                            postModel.setId(snapshot.getId());
                            postModel.setImage(snapshot.getString("image"));
                            postModel.setUploader(snapshot.getString("uploader"));
                            if (snapshot.getString("caption") != null){
                                postModel.setCaption(snapshot.getString("caption"));
                            }

                            //postModel.setTime(snapshot.getLong("time"));
//                            postModel.setCommentsCount(postModel.getCommentsCount());
//                            postModel.setLikesCount(postModel.getLikesCount());
//                            if (snapshot.contains("approved")){
//                                boolean t = Boolean.TRUE.equals(snapshot.getBoolean("approved"));
//                                postModel.setApproved(t);
//                            }else {
//                                postModel.setApproved(false);
//                            }
                            postModelArrayList.add(postModel);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    binding.loadMore.setVisibility(View.GONE);
                    binding.nextBtn.setVisibility(View.VISIBLE);
                }else {
                    binding.loadMore.setVisibility(View.GONE);
                    Toast.makeText(AimaAdmin.this, "No Records!", Toast.LENGTH_SHORT).show();
                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AimaAdmin.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                binding.loadMore.setVisibility(View.GONE);
            }
        });

    }
    public void nextStore(){
        Query query = collectionReference.orderBy("time", Query.Direction.DESCENDING)
                .startAfter(lastVisibleDocument)
                .whereEqualTo("uploader", "Admin")
                .limit(18);

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        postModelArrayList.clear();
                        lastVisibleDocument = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);
                        // Process the data from the next page
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()){
                            PostModel postModel = snapshot.toObject(PostModel.class);

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
//                                    postModel.setApproved(t);
//                                }else {
//                                    postModel.setApproved(false);
//                                }
//                                postModelArrayList.add(postModel);
//                            }
                        }
                        adapter.notifyDataSetChanged();
                        binding.discoverRecyclerView.smoothScrollToPosition(0);
                        binding.loadMore.setVisibility(View.GONE);
                    }else {
                        binding.loadMore.setVisibility(View.GONE);
                        binding.nextBtn.setVisibility(View.GONE);
                        Toast.makeText(AimaAdmin.this, "No Records!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    binding.loadMore.setVisibility(View.GONE);
                });
    }
}