package com.developerali.aima.HomeBottomBar;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.developerali.aima.Adapters.VideoPostAdapter;
import com.developerali.aima.Models.VideoModel;
import com.developerali.aima.databinding.ActivityAimaVideosBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class AimaVideos extends AppCompatActivity {

    ActivityAimaVideosBinding binding;
    VideoPostAdapter adapterVideo;
    ArrayList<VideoModel> postVideoArrayList = new ArrayList<>();
    CollectionReference collectionReference;
    FirebaseFirestore firebaseFirestore;
    private DocumentSnapshot lastVisibleDocument;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAimaVideosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseFirestore = FirebaseFirestore.getInstance();
        collectionReference = firebaseFirestore.collection("video");

        LinearLayoutManager lnm = new LinearLayoutManager(AimaVideos.this);
        binding.discoverRecyclerView.setLayoutManager(lnm);
        adapterVideo = new VideoPostAdapter(postVideoArrayList, getLifecycle(), AimaVideos.this);
        initialVideoPage();

        binding.goBack.setOnClickListener(v->{
            finish();
        });

        binding.nextBtn.setOnClickListener(v->{
            nextVideoStore();
        });

        
    }

    public void initialVideoPage(){
        binding.discoverRecyclerView.setAdapter(adapterVideo);

        binding.spinKit.setVisibility(View.VISIBLE);
        binding.nextBtn.setVisibility(View.GONE);

        collectionReference.orderBy("time", Query.Direction.DESCENDING)
                .whereEqualTo("approved", true)
                .limit(5).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {
                            postVideoArrayList.clear();
                            lastVisibleDocument = queryDocumentSnapshots.getDocuments()
                                    .get(queryDocumentSnapshots.size() - 1);
                            // Process the data from the initial page
                            for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()){
                                VideoModel postModel = snapshot.toObject(VideoModel.class);
                                if (postModel != null){
                                    postModel.setUploader(snapshot.getString("uploader"));
                                    if (snapshot.getString("caption") != null){
                                        postModel.setCaption(snapshot.getString("caption"));
                                    }
                                    //postModel.setTime(snapshot.getLong("time"));

                                    postVideoArrayList.add(postModel);
                                }
                            }
                            adapterVideo.notifyDataSetChanged();
                            binding.spinKit.setVisibility(View.GONE);
                            binding.nextBtn.setVisibility(View.VISIBLE);
                        }else {
                            binding.spinKit.setVisibility(View.GONE);
                            Toast.makeText(AimaVideos.this, "No Records!", Toast.LENGTH_SHORT).show();
                        }


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AimaVideos.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        binding.spinKit.setVisibility(View.GONE);
                    }
                });
    }
    public void nextVideoStore(){
        collectionReference // Order by a suitable field
                .orderBy("time", Query.Direction.DESCENDING)
                .startAfter(lastVisibleDocument)
                .whereEqualTo("approved", true)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        postVideoArrayList.clear();
                        lastVisibleDocument = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);
                        // Process the data from the next page
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()){
                            VideoModel postModel = snapshot.toObject(VideoModel.class);

                            if (postModel != null){
                                postModel.setUploader(snapshot.getString("uploader"));
                                if (snapshot.getString("caption") != null){
                                    postModel.setCaption(snapshot.getString("caption"));
                                }

                                //postModel.setTime(snapshot.getLong("time"));
                                postVideoArrayList.add(postModel);
                            }
                        }
                        adapterVideo.notifyDataSetChanged();
                        binding.discoverRecyclerView.smoothScrollToPosition(0);
                        binding.spinKit.setVisibility(View.GONE);
                    }else {
                        binding.spinKit.setVisibility(View.GONE);
                        binding.nextBtn.setVisibility(View.GONE);
                        Toast.makeText(AimaVideos.this, "No Records!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    binding.spinKit.setVisibility(View.GONE);
                });
    }
}