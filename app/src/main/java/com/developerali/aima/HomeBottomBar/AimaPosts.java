package com.developerali.aima.HomeBottomBar;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.developerali.aima.Adapters.PostAdapter;
import com.developerali.aima.Models.PostModel;
import com.developerali.aima.databinding.ActivityAimaPostsBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class AimaPosts extends AppCompatActivity {

    ActivityAimaPostsBinding binding;
    FirebaseFirestore firebaseFirestore;
    ArrayList<PostModel> postModelArrayList;
    CollectionReference collectionReference;
    PostAdapter adapter;
    private DocumentSnapshot lastVisibleDocument;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAimaPostsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseFirestore = FirebaseFirestore.getInstance();
        postModelArrayList = new ArrayList<>();
        collectionReference = firebaseFirestore.collection("post");

        LinearLayoutManager lnm = new LinearLayoutManager(AimaPosts.this);
        binding.discoverRecyclerView.setLayoutManager(lnm);
        adapter = new PostAdapter(postModelArrayList, AimaPosts.this);
        binding.discoverRecyclerView.setAdapter(adapter);

        initialPage();

        binding.goBack.setOnClickListener(v->{
            finish();
        });

        binding.nextBtn.setOnClickListener(c->{
            binding.spinKit.setVisibility(View.VISIBLE);
            nextStore();
        });


    }

    public void initialPage(){
        Query query = collectionReference.orderBy("time", Query.Direction.DESCENDING)
                .whereEqualTo("approved", true)
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
                            postModel.setCommentsCount(postModel.getCommentsCount());
                            postModel.setLikesCount(postModel.getLikesCount());
                            if (snapshot.contains("approved")){
                                boolean t = Boolean.TRUE.equals(snapshot.getBoolean("approved"));
                                postModel.setApproved(t);
                            }else {
                                postModel.setApproved(false);
                            }
                            postModelArrayList.add(postModel);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    binding.spinKit.setVisibility(View.GONE);
                    binding.nextBtn.setVisibility(View.VISIBLE);
                }else {
                    binding.spinKit.setVisibility(View.GONE);
                    Toast.makeText(AimaPosts.this, "No Records!", Toast.LENGTH_SHORT).show();
                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AimaPosts.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                binding.spinKit.setVisibility(View.GONE);
            }
        });

    }
    public void nextStore(){
        Query query = collectionReference.orderBy("time", Query.Direction.DESCENDING)
                .startAfter(lastVisibleDocument)
                .whereEqualTo("approved", true)
                .limit(18);

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        postModelArrayList.clear();
                        lastVisibleDocument = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);
                        // Process the data from the next page
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
                                postModel.setCommentsCount(postModel.getCommentsCount());
                                postModel.setLikesCount(postModel.getLikesCount());
                                if (snapshot.contains("approved")){
                                    boolean t = Boolean.TRUE.equals(snapshot.getBoolean("approved"));
                                    postModel.setApproved(t);
                                }else {
                                    postModel.setApproved(false);
                                }
                                postModelArrayList.add(postModel);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        binding.discoverRecyclerView.smoothScrollToPosition(0);
                        binding.spinKit.setVisibility(View.GONE);
                    }else {
                        binding.spinKit.setVisibility(View.GONE);
                        binding.nextBtn.setVisibility(View.GONE);
                        Toast.makeText(AimaPosts.this, "No Records!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    binding.spinKit.setVisibility(View.GONE);
                });
    }
}