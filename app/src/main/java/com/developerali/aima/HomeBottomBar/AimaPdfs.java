package com.developerali.aima.HomeBottomBar;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.developerali.aima.Adapters.pdfAdapter;
import com.developerali.aima.Models.pdfModel;
import com.developerali.aima.databinding.ActivityAimaPdfsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class AimaPdfs extends AppCompatActivity {

    ActivityAimaPdfsBinding binding;
    FirebaseDatabase database;
    ArrayList<pdfModel> pdfModelArrayList;
    pdfAdapter pdfAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAimaPdfsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        pdfModelArrayList = new ArrayList<>();

        binding.goBack.setOnClickListener(v->{
            finish();
        });

        LinearLayoutManager lnm = new LinearLayoutManager(AimaPdfs.this);
        binding.discoverRecyclerView.setLayoutManager(lnm);

        loadPdfs();



    }

    public void loadPdfs(){
        pdfAdapter = new pdfAdapter(AimaPdfs.this, pdfModelArrayList);
        binding.discoverRecyclerView.setAdapter(pdfAdapter);
        binding.spinKit.setVisibility(View.VISIBLE);
        binding.nextBtn.setVisibility(View.GONE);

        database.getReference().child("pdfs")
                .orderByChild("time")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            pdfModelArrayList.clear();
                            for (DataSnapshot snapshot1 : snapshot.getChildren()){
                                pdfModel pdfModel = snapshot1.getValue(pdfModel.class);

                                if (pdfModel != null){
                                    pdfModel.setId(snapshot1.getKey());
                                    pdfModel.setCaption(pdfModel.getCaption());
                                    pdfModel.setTime(pdfModel.getTime());
                                    //pdfModel.setLink(pdfModel.getLink());

                                    //Toast.makeText(AimaPdfs.this, "s", Toast.LENGTH_SHORT).show();
                                    pdfModelArrayList.add(pdfModel);
                                }
                            }

                            //Toast.makeText(AimaPdfs.this, "t", Toast.LENGTH_SHORT).show();
                            Collections.reverse(pdfModelArrayList);
                            binding.spinKit.setVisibility(View.GONE);
                            pdfAdapter.notifyDataSetChanged();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.spinKit.setVisibility(View.GONE);
                        Toast.makeText(AimaPdfs.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}