package com.developerali.aima.BottomBar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.developerali.aima.Activities.DonationPage;
import com.developerali.aima.Activities.MemberShip_Act;
import com.developerali.aima.Activities.ProfileActivity;
import com.developerali.aima.Activities.WebViewActivity;
import com.developerali.aima.Adapters.AdminActivityAdapter;
import com.developerali.aima.Models.PostModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.FragmentMenuBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import me.ibrahimsn.lib.SmoothBottomBar;


public class MenuFragment extends Fragment {

    FragmentMenuBinding binding;
    private SmoothBottomBar bottomBar;
    FirebaseDatabase database;
    ArrayList<PostModel> postModelArrayList;
    private DocumentSnapshot lastVisibleDocument;
    AdminActivityAdapter adapter;
    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;
    CollectionReference collectionReference;

    public MenuFragment(){}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMenuBinding.inflate(inflater, container, false);
        bottomBar = (SmoothBottomBar) getActivity().findViewById(R.id.bottomBar);
        bottomBar.setItemActiveIndex(4);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();


        //ImageSlider
        final ArrayList<SlideModel> slideModels = new ArrayList<>();

        database.getReference().child("banners")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && !getActivity().isDestroyed()){
                            slideModels.clear();
                            for (DataSnapshot ds:snapshot.getChildren()){
                                slideModels.add(new SlideModel(ds.child("imageUrl").getValue(String.class), ScaleTypes.CENTER_CROP));
                                binding.imageSlider.setImageList(slideModels, ScaleTypes.CENTER_CROP);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        if (slideModels == null){
            slideModels.add(new SlideModel(R.drawable.donationpng, ScaleTypes.CENTER_CROP));
            binding.imageSlider.setImageList(slideModels, ScaleTypes.FIT);
        }

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

        binding.donations.setOnClickListener(v->{
            Intent intent = new Intent(getActivity().getApplicationContext(), DonationPage.class);
            intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
            getActivity().startActivity(intent);
        });
        binding.memberShip.setOnClickListener(c->{
            Intent i = new Intent(getActivity().getApplicationContext(), MemberShip_Act.class);
            i.setFlags(i.FLAG_ACTIVITY_NEW_TASK);
            getActivity().startActivity(i);
        });
        binding.profile.setOnClickListener(v->{
            Intent intent = new Intent(getActivity().getApplicationContext(), ProfileActivity.class);
            intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
            getActivity().startActivity(intent);
        });
        binding.meetings.setOnClickListener(c->{
            transaction.replace(R.id.content, new MeetingFragment()).addToBackStack(null);
            transaction.commit();
        });

        binding.terms.setOnClickListener(v->{
            Intent i = new Intent(getActivity().getApplicationContext(), WebViewActivity.class);
            i.putExtra("provide", "https://www.visitaima.org/terms");
            startActivity(i);
        });
        binding.privacyPolicy.setOnClickListener(v->{
            Intent i = new Intent(getActivity().getApplicationContext(), WebViewActivity.class);
            i.putExtra("provide", "https://www.visitaima.org/privacy");
            startActivity(i);
        });
        binding.refundPolicy.setOnClickListener(v->{
            Intent i = new Intent(getActivity().getApplicationContext(), WebViewActivity.class);
            i.putExtra("provide", "https://www.visitaima.org/refund");
            startActivity(i);
        });

        binding.helpCentre.setOnClickListener(v->{
            String message = "Hi, I'm using AIMA app. I wanted to know about something. Can we talk now?";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("http://api.whatsapp.com/send?phone="+"+91"+"8967254087" + "&text="+ message));
            startActivity(intent);
        });

//        postModelArrayList = new ArrayList<>();
//
//        binding.nestedScroll.setNestedScrollingEnabled(false);
//        adapter = new AdminActivityAdapter(getActivity(), postModelArrayList);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
//        binding.discoverRecyclerView.setLayoutManager(layoutManager);
//        binding.discoverRecyclerView.setHasFixedSize(true);
//        binding.discoverRecyclerView.setAdapter(adapter);
//
//
//        binding.nextBtn.setOnClickListener(c->{
//            nextStore();
//        });
//
//        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
//                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
//                .build();
//        FirebaseFirestore.getInstance().setFirestoreSettings(settings);
//
//        collectionReference = firebaseFirestore.collection("post");
//        initialPage();



















        return binding.getRoot();
    }

//    public void initialPage(){
//
//        postModelArrayList.clear();
//        collectionReference.whereEqualTo("uploader", "Admin")
//                .orderBy("time", Query.Direction.DESCENDING)
//                .limit(5).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//
//                        if (!queryDocumentSnapshots.isEmpty()) {
//                            lastVisibleDocument = queryDocumentSnapshots.getDocuments()
//                                    .get(queryDocumentSnapshots.size() - 1);
//                            // Process the data from the initial page
//                            for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()){
//                                PostModel postModel = snapshot.toObject(PostModel.class);
//
//                                postModel.setId(snapshot.getId());
//                                postModel.setImage(snapshot.getString("image"));
//                                postModel.setUploader(snapshot.getString("uploader"));
//                                if (snapshot.getString("caption") != null){
//                                    postModel.setCaption(snapshot.getString("caption"));
//                                }
//
//                                postModel.setTime(snapshot.getLong("time"));
////                                postModel.setLikesCount(postModel.getLikesCount());
////                                postModel.setCommentsCount(postModel.getCommentsCount());
//                                postModelArrayList.add(postModel);
//                            }
//                            adapter.notifyDataSetChanged();
//                            binding.spinKit.setVisibility(View.GONE);
//                            binding.nextBtn.setVisibility(View.VISIBLE);
//                        }else {
//                            binding.spinKit.setVisibility(View.GONE);
//                            Toast.makeText(getActivity(), "No Records!", Toast.LENGTH_SHORT).show();
//                        }
//
//
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
//                        binding.spinKit.setVisibility(View.GONE);
//                    }
//                });
//
//    }
//
//    public void nextStore(){
//        postModelArrayList.clear();
//
//        collectionReference // Order by a suitable field
//                .whereEqualTo("uploader", "Admin")
//                .orderBy("time", Query.Direction.DESCENDING)
//                .startAfter(lastVisibleDocument)
//                .limit(5)
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    if (!queryDocumentSnapshots.isEmpty()) {
//                        lastVisibleDocument = queryDocumentSnapshots.getDocuments()
//                                .get(queryDocumentSnapshots.size() - 1);
//                        // Process the data from the next page
//                        for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()){
//                            PostModel postModel = snapshot.toObject(PostModel.class);
//
//                            postModel.setId(snapshot.getId());
//                            postModel.setImage(snapshot.getString("image"));
//                            postModel.setUploader(snapshot.getString("uploader"));
//                            if (snapshot.getString("caption") != null){
//                                postModel.setCaption(snapshot.getString("caption"));
//                            }
//
//                            postModel.setTime(snapshot.getLong("time"));
////                            postModel.setLikesCount(snapshot.getLong("likesCount"));
//                            postModel.setCommentsCount(postModel.getCommentsCount());
//                            postModelArrayList.add(postModel);
//                        }
//                        adapter.notifyDataSetChanged();
//                        binding.nestedScroll.smoothScrollTo(0, 0);
//                        binding.spinKit.setVisibility(View.GONE);
//                    }else {
//                        binding.spinKit.setVisibility(View.GONE);
//                        Toast.makeText(getActivity(), "No Records!", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    binding.spinKit.setVisibility(View.GONE);
//                });
//
//    }
}