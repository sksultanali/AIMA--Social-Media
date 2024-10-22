package com.developerali.aima.HomeBottomBar;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.developerali.aima.Adapters.GalleryAdapter;
import com.developerali.aima.Models.GalleryModel;
import com.developerali.aima.databinding.ActivityAimaGalleryBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class AimaGallery extends AppCompatActivity {

    ActivityAimaGalleryBinding binding;
    FirebaseDatabase database;
    GalleryAdapter galleryAdapter;
    ArrayList<GalleryModel> galleryModelArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAimaGalleryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        galleryModelArrayList = new ArrayList<>();

        binding.goBack.setOnClickListener(v->{
            finish();
        });

        LinearLayoutManager lnm = new LinearLayoutManager(AimaGallery.this);
        binding.discoverRecyclerView.setLayoutManager(lnm);
        loadGallery();



    }

    private void loadGallery() {
        galleryAdapter = new GalleryAdapter(AimaGallery.this, galleryModelArrayList);
        binding.discoverRecyclerView.setAdapter(galleryAdapter);

        binding.spinKit.setVisibility(View.VISIBLE);
        binding.nextBtn.setVisibility(View.GONE);

        database.getReference().child("gallery")
                .orderByChild("time")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            galleryModelArrayList.clear();
                            for (DataSnapshot snapshot1 : snapshot.getChildren()){
                                GalleryModel galleryModel = snapshot1.getValue(GalleryModel.class);

                                if (galleryModel != null){
                                    galleryModel.setId(snapshot1.getKey());
                                    galleryModel.setCaption(galleryModel.getCaption());
                                    //galleryModel.setTime(galleryModel.getTime());
                                    //galleryModel.setImages(galleryModel.getImages());

                                    galleryModelArrayList.add(galleryModel);
                                }
                            }

                            Collections.reverse(galleryModelArrayList);
                            binding.spinKit.setVisibility(View.GONE);
                            galleryAdapter.notifyDataSetChanged();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.spinKit.setVisibility(View.GONE);
                    }
                });
    }
}