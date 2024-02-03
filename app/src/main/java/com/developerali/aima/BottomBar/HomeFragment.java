package com.developerali.aima.BottomBar;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.developerali.aima.Activities.DonationPage;
import com.developerali.aima.Activities.Login;
import com.developerali.aima.Activities.MemberShip_Act;
import com.developerali.aima.Activities.NotificationAct;
import com.developerali.aima.Activities.PostActivity;
import com.developerali.aima.Activities.ProfileActivity;
import com.developerali.aima.Activities.SearchActivity;
import com.developerali.aima.Adapters.GalleryAdapter;
import com.developerali.aima.Adapters.PostAdapter;
import com.developerali.aima.Adapters.VideoPostAdapter;
import com.developerali.aima.Adapters.pdfAdapter;
import com.developerali.aima.Models.GalleryModel;
import com.developerali.aima.Models.PostModel;
import com.developerali.aima.Models.UserModel;
import com.developerali.aima.Models.VideoModel;
import com.developerali.aima.Models.pdfModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.DialogNotLoginBinding;
import com.developerali.aima.databinding.FragmentHomeBinding;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import me.ibrahimsn.lib.SmoothBottomBar;

public class HomeFragment extends Fragment {

    FragmentHomeBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    String who;
    CollectionReference collectionReference;
    private int currentTabPosition = 0;
    ArrayList<PostModel> postModelArrayList;
    ArrayList<pdfModel> pdfModelArrayList;
    ArrayList<VideoModel> postVideoArrayList;
    ArrayList<GalleryModel> galleryModelArrayList;
    FirebaseFirestore firebaseFirestore;
    private DocumentSnapshot lastVisibleDocument;
    PostAdapter adapter;
    Query query;

    Boolean isOpen;
    private SmoothBottomBar bottomBar;
    VideoPostAdapter adapterVideo;
    pdfAdapter pdfAdapter;
    GalleryAdapter galleryAdapter;
    Uri imageUri;

    public HomeFragment(){}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        bottomBar = (SmoothBottomBar) getActivity().findViewById(R.id.bottomBar);
        bottomBar.setItemActiveIndex(0);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        firebaseFirestore = FirebaseFirestore.getInstance();

        postModelArrayList = new ArrayList<>();
        postVideoArrayList = new ArrayList<>();
        pdfModelArrayList = new ArrayList<>();
        galleryModelArrayList = new ArrayList<>();
        who = null;


        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
        try {
            FirebaseFirestore.getInstance().setFirestoreSettings(settings);
        }catch (Exception e){

        }


        adapter = new PostAdapter(getActivity(), postModelArrayList, getActivity());
        binding.nestedScroll.setNestedScrollingEnabled(false);
        binding.discoverRecyclerView.setAdapter(adapter);
//        binding.swipeRefresh.setNestedScrollingEnabled(false);
        collectionReference = firebaseFirestore.collection("post");
        binding.spinKit.setVisibility(View.VISIBLE);
        //initialPage(null);
        loadGallery();


        isOpen = false;

        //getting user information
        database.getReference().child("users").child(auth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && getActivity() != null){
                            UserModel userModel = snapshot.getValue(UserModel.class);
                            Glide.with(getActivity())
                                    .load(userModel.getImage())
                                    .placeholder(getActivity().getDrawable(R.drawable.profileplaceholder))
                                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                    .into(binding.myProfile);
                            long verifiedValid = userModel.getVerifiedValid();
                            if (verifiedValid < new Date().getTime() && userModel.isVerified()){
                                database.getReference().child("users").child(auth.getCurrentUser().getUid())
                                        .child("verified")
                                        .setValue(false);
                                database.getReference().child("users").child(auth.getCurrentUser().getUid())
                                        .child("verifiedValid").removeValue();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



//        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
//                getActivity().getSupportFragmentManager(), FragmentPagerItems.with(getActivity())
//                .add("For you", DiscoverFragment.class)
//                .add("From Admin", Admin.class)
//                .add("Video", Videos.class)
//                .create());
//
//        binding.viewpager.setAdapter(adapter);
//        binding.viewpagertab.setViewPager(binding.viewpager);


        //toolbar clicks
        binding.myProfile.setOnClickListener(v->{
            Intent i = new Intent(getActivity().getApplicationContext(), ProfileActivity.class);

            i.setFlags(i.FLAG_ACTIVITY_NEW_TASK);
            getActivity().startActivity(i);
        });

        binding.search.setOnClickListener(v->{
            Intent i = new Intent(getActivity().getApplicationContext(), SearchActivity.class);
            i.setFlags(i.FLAG_ACTIVITY_NEW_TASK);
            getActivity().startActivity(i);
        });



//        transaction.replace(R.id.content, new HomeFragment());
//        transaction.commit();



        // Add tabs to the TabLayout
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("AIMA GALLERY"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("PDFs"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("VIDEOS"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("FROM ADMIN"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("FOR YOU"));

        // Set the default selected tab
        TextView tv = (TextView) LayoutInflater.from(getActivity())
                .inflate(R.layout.custom_tab, null);
        binding.tabLayout.getTabAt(0).setCustomView(tv);
        binding.tabLayout.getTabAt(0).select();

        // Handle tab selection changes
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Show the corresponding content in the FrameLayout

                TextView tv = (TextView) LayoutInflater.from(getActivity())
                        .inflate(R.layout.custom_tab, null);
                binding.tabLayout.getTabAt(tab.getPosition()).setCustomView(tv);

                switch (tab.getPosition()) {
                    case 0:
                        who = "gallery";
                        loadGallery();
                        break;
                    case 1:
                        loadPdfs();
                        break;
                    case 2:
                        initialVideoPage();
                        break;
                    case 3:
                        postModelArrayList.clear();
                        adapter = new PostAdapter(getActivity(), postModelArrayList, getActivity());
                        binding.discoverRecyclerView.setAdapter(adapter);

                        collectionReference = firebaseFirestore.collection("post");
                        binding.spinKit.setVisibility(View.VISIBLE);
                        binding.nextBtn.setVisibility(View.GONE);

                        who = "Admin";
                        initialPage(who);
                        break;
                    case 4:
                        postModelArrayList.clear();

                        adapter = new PostAdapter(getActivity(), postModelArrayList, getActivity());
                        binding.discoverRecyclerView.setAdapter(adapter);
                        collectionReference = firebaseFirestore.collection("post");
                        binding.spinKit.setVisibility(View.VISIBLE);
                        binding.nextBtn.setVisibility(View.GONE);
                        who = "public";
                        initialPage(null);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        binding.swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                binding.spinKit.setVisibility(View.VISIBLE);
                if (who == null || who.equalsIgnoreCase("public")){
                    initialPage(who);
                }else if (who.equalsIgnoreCase("Admin")){
                    initialPage(who);
                }else if (who.equalsIgnoreCase("pdf")){
                    loadPdfs();
                }else if (who.equalsIgnoreCase("gallery")){
                    loadGallery();
                }else if (who.equalsIgnoreCase("Video")){
                    initialVideoPage();
                }
            }
        });


        binding.openClose.setOnClickListener(c->{
            if (isOpen) {
                binding.closeBtn.setVisibility(View.GONE);
                binding.openBtn.setVisibility(View.VISIBLE);
                binding.featuresOut.setVisibility(View.GONE);
                isOpen = false;
            }else {
                binding.openBtn.setVisibility(View.GONE);
                binding.closeBtn.setVisibility(View.VISIBLE);
                binding.featuresOut.setVisibility(View.VISIBLE);
                isOpen = true;
            }
        });

        binding.closeFeature.setOnClickListener(v->{
            binding.openClose.setVisibility(View.GONE);
        });



        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.discoverRecyclerView.setLayoutManager(layoutManager);
        binding.discoverRecyclerView.setHasFixedSize(true);


        binding.nextBtn.setOnClickListener(c->{
            binding.spinKit.setVisibility(View.VISIBLE);
            if (who == null || who.equalsIgnoreCase("public")){
                nextStore(null);
            }else if (who.equalsIgnoreCase("Admin")){
                nextStore(who);
            }else if (who.equalsIgnoreCase("Video")){
                nextVideoStore();
            }
        });

        binding.addDonation.setOnClickListener(v->{
            Intent intent = new Intent(getActivity().getApplicationContext(), DonationPage.class);
            intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
            getActivity().startActivity(intent);
        });

        binding.addMembership.setOnClickListener(c->{
            Intent i = new Intent(getActivity().getApplicationContext(), MemberShip_Act.class);
            i.setFlags(i.FLAG_ACTIVITY_NEW_TASK);
            getActivity().startActivity(i);
        });
        binding.addPost.setOnClickListener(c->{
            Intent i = new Intent(getActivity().getApplicationContext(), PostActivity.class);
            i.setFlags(i.FLAG_ACTIVITY_NEW_TASK);
            getActivity().startActivity(i);
        });

        binding.memberFeature.setOnClickListener(v->{
            Toast.makeText(getActivity(), "loading...", Toast.LENGTH_SHORT).show();
            binding.addMembership.performClick();
        });
        binding.donationFeature.setOnClickListener(v->{
            Toast.makeText(getActivity(), "loading...", Toast.LENGTH_SHORT).show();
            binding.addDonation.performClick();
        });




//        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.blink);
//        binding.notificationBadge.setAnimation(animation);

        binding.notification.setOnClickListener(v->{
            Intent intent = new Intent(getActivity().getApplicationContext(), NotificationAct.class);
            intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
            getActivity().startActivity(intent);
        });

        binding.camera.setOnClickListener(v->{
            ImagePicker.with(this)
                    .crop()//Crop image(Optional), Check Customization for more option
                    .cameraOnly()
                    .compress(3072)			//Final image size will be less than 3 MB(Optional)
                    .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                    .start(85);
            Toast.makeText(getActivity(), "opening camera...", Toast.LENGTH_SHORT).show();
        });

        database.getReference().child("notification")
                .child(auth.getCurrentUser().getUid())
                .orderByChild("notifyAt")
                .limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            for (DataSnapshot snapshot1 : snapshot.getChildren()){

                                Boolean seen = snapshot1.child("seen").getValue(Boolean.class);
                                if (!seen){
                                    binding.notificationBadge.setVisibility(View.VISIBLE);
                                }

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.tabLayout.getTabAt(1).isSelected()) {
                    binding.tabLayout.getTabAt(0).select();
                    who = "gallery";

                } else if (binding.tabLayout.getTabAt(2).isSelected()){
                    binding.tabLayout.getTabAt(0).select();
                    who = "gallery";

                }else if (binding.tabLayout.getTabAt(3).isSelected()){
                    binding.tabLayout.getTabAt(0).select();
                    who = "gallery";

                }else if (binding.tabLayout.getTabAt(4).isSelected()){
                    binding.tabLayout.getTabAt(0).select();
                    who = "gallery";

                }else {
                    ExitApp();

                }
            }
        });



        return binding.getRoot();
    }

    private void loadGallery() {

        galleryAdapter = new GalleryAdapter(getActivity(), getActivity(), galleryModelArrayList);
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

                                galleryModel.setId(snapshot1.getKey());
                                galleryModel.setCaption(galleryModel.getCaption());
                                galleryModel.setTime(galleryModel.getTime());
                                galleryModel.setImages(galleryModel.getImages());

                                galleryModelArrayList.add(galleryModel);
                            }

                            Collections.reverse(galleryModelArrayList);
                            binding.spinKit.setVisibility(View.GONE);
                            binding.swipeRefresh.setRefreshing(false);
                            galleryAdapter.notifyDataSetChanged();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.spinKit.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getData() != null && requestCode == 85){
            Toast.makeText(getActivity(), "please wait...", Toast.LENGTH_SHORT).show();
            imageUri = data.getData();
            Intent intent = new Intent(getActivity().getApplicationContext(), PostActivity.class);
            intent.putExtra("uri", imageUri.toString());
            getActivity().startActivity(intent);

        }
    }

    public void loadPdfs(){

        pdfAdapter = new pdfAdapter(getActivity(), pdfModelArrayList);
        binding.discoverRecyclerView.setAdapter(pdfAdapter);
        binding.spinKit.setVisibility(View.VISIBLE);
        binding.nextBtn.setVisibility(View.GONE);
        who = "pdf";

        database.getReference().child("pdfs")
                .orderByChild("time")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            pdfModelArrayList.clear();
                            for (DataSnapshot snapshot1 : snapshot.getChildren()){
                                pdfModel pdfModel = snapshot1.getValue(pdfModel.class);
                                pdfModel.setId(snapshot1.getKey());;
                                pdfModel.setCaption(pdfModel.getCaption());
                                pdfModel.setTime(pdfModel.getTime());
                                pdfModel.setLink(pdfModel.getLink());
                                pdfModelArrayList.add(pdfModel);
                            }
                            Collections.reverse(pdfModelArrayList);
                            binding.spinKit.setVisibility(View.GONE);
                            binding.swipeRefresh.setRefreshing(false);
                            pdfAdapter.notifyDataSetChanged();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.spinKit.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                    }
                });
    }

    public void initialPage(String who){

        if (who == null || who.equalsIgnoreCase("public")){
            query = collectionReference.orderBy("time", Query.Direction.DESCENDING)
                    .whereEqualTo("approved", true)
                    .limit(18);
        }else{
            query = collectionReference.orderBy("time", Query.Direction.DESCENDING)
                    .whereEqualTo("uploader", "Admin")
                    .limit(18);
        }

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

                        postModel.setId(snapshot.getId());
                        postModel.setImage(snapshot.getString("image"));
                        postModel.setUploader(snapshot.getString("uploader"));
                        if (snapshot.getString("caption") != null){
                            postModel.setCaption(snapshot.getString("caption"));
                        }

                        postModel.setTime(snapshot.getLong("time"));
                        postModel.setCommentsCount(postModel.getCommentsCount());
                        postModel.setLikesCount(postModel.getLikesCount());
                        postModel.setApproved(snapshot.getBoolean("approved"));
                        postModelArrayList.add(postModel);
                    }
                    adapter.notifyDataSetChanged();
                    binding.spinKit.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    binding.nextBtn.setVisibility(View.VISIBLE);
                }else {
                    binding.spinKit.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), "No Records!", Toast.LENGTH_SHORT).show();
                    binding.swipeRefresh.setRefreshing(false);
                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                binding.spinKit.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
            }
        });

    }
    public void nextStore(String who){
        if (who == null || who.equalsIgnoreCase("public")){
            query = collectionReference.orderBy("time", Query.Direction.DESCENDING)
                    .startAfter(lastVisibleDocument)
                    .whereEqualTo("approved", true)
                    .limit(18);
        }else {
            query = collectionReference.orderBy("time", Query.Direction.DESCENDING)
                    .startAfter(lastVisibleDocument)
                    .whereEqualTo("uploader", "Admin")
                    .limit(18);
        }
        query.get() // Order by a suitable field

                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        postModelArrayList.clear();
                        lastVisibleDocument = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);
                        // Process the data from the next page
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()){
                            PostModel postModel = snapshot.toObject(PostModel.class);

                            postModel.setId(snapshot.getId());
                            postModel.setImage(snapshot.getString("image"));
                            postModel.setUploader(snapshot.getString("uploader"));
                            if (snapshot.getString("caption") != null){
                                postModel.setCaption(snapshot.getString("caption"));
                            }

                            postModel.setTime(snapshot.getLong("time"));
                            postModel.setCommentsCount(postModel.getCommentsCount());
                            postModel.setLikesCount(postModel.getLikesCount());
                            postModelArrayList.add(postModel);
                        }
                        adapter.notifyDataSetChanged();
                        binding.nestedScroll.scrollTo(0, 0);
                        binding.spinKit.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                    }else {
                        binding.spinKit.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        binding.nextBtn.setVisibility(View.GONE);
                        Toast.makeText(getActivity(), "No Records!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    binding.spinKit.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                });
    }
    public void initialVideoPage(){
        adapterVideo = new VideoPostAdapter(getActivity(), postVideoArrayList, getActivity().getLifecycle(), getActivity());
        binding.discoverRecyclerView.setAdapter(adapterVideo);

        collectionReference = firebaseFirestore.collection("video");
        binding.spinKit.setVisibility(View.VISIBLE);
        binding.nextBtn.setVisibility(View.GONE);
        who = "Video";

        collectionReference.orderBy("time", Query.Direction.DESCENDING)
                .whereEqualTo("approved", true)
                .limit(18).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {
                            postVideoArrayList.clear();
                            lastVisibleDocument = queryDocumentSnapshots.getDocuments()
                                    .get(queryDocumentSnapshots.size() - 1);
                            // Process the data from the initial page
                            for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()){
                                VideoModel postModel = snapshot.toObject(VideoModel.class);

                                postModel.setUploader(snapshot.getString("uploader"));
                                if (snapshot.getString("caption") != null){
                                    postModel.setCaption(snapshot.getString("caption"));
                                }

                                postModel.setTime(snapshot.getLong("time"));
                                postVideoArrayList.add(postModel);
                            }
                            adapterVideo.notifyDataSetChanged();
                            binding.spinKit.setVisibility(View.GONE);
                            binding.swipeRefresh.setRefreshing(false);
                            binding.nextBtn.setVisibility(View.VISIBLE);
                        }else {
                            binding.spinKit.setVisibility(View.GONE);
                            Toast.makeText(getActivity(), "No Records!", Toast.LENGTH_SHORT).show();
                            binding.swipeRefresh.setRefreshing(false);
                        }


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        binding.spinKit.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                    }
                });
    }
    public void nextVideoStore(){
        collectionReference // Order by a suitable field
                .orderBy("time", Query.Direction.DESCENDING)
                .startAfter(lastVisibleDocument)
                .whereEqualTo("approved", true)
                .limit(18)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        postVideoArrayList.clear();
                        lastVisibleDocument = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);
                        // Process the data from the next page
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()){
                            VideoModel postModel = snapshot.toObject(VideoModel.class);

                            postModel.setUploader(snapshot.getString("uploader"));
                            if (snapshot.getString("caption") != null){
                                postModel.setCaption(snapshot.getString("caption"));
                            }

                            postModel.setTime(snapshot.getLong("time"));
                            postVideoArrayList.add(postModel);
                        }
                        adapterVideo.notifyDataSetChanged();
                        binding.nestedScroll.smoothScrollTo(0, 0);
                        binding.spinKit.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                    }else {
                        binding.spinKit.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        binding.nextBtn.setVisibility(View.GONE);
                        Toast.makeText(getActivity(), "No Records!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    binding.spinKit.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                });
    }

    public void ExitApp(){
        DialogNotLoginBinding dialogNotLoginBinding = DialogNotLoginBinding.inflate(getLayoutInflater());
        Dialog dialog1 = new Dialog(getActivity());
        dialog1.setContentView(dialogNotLoginBinding.getRoot());

        dialog1.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog1.getWindow().setGravity(Gravity.CENTER_VERTICAL);

        dialog1.setCancelable(false);
        dialog1.setCanceledOnTouchOutside(false);

        dialogNotLoginBinding.titleText.setText("Leave App");
        dialogNotLoginBinding.messageText.setText("Are you sure you want to quit this app?");
        dialogNotLoginBinding.yesBtnText.setText("Yes");
        dialogNotLoginBinding.noBtn.setVisibility(View.VISIBLE);

        dialogNotLoginBinding.loginBtn.setOnClickListener(c->{
            getActivity().finish();
        });

        dialogNotLoginBinding.noBtn.setOnClickListener(c->{
            dialog1.dismiss();
        });

        dialog1.show();
    }

}