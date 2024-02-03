package com.developerali.aima.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import com.developerali.aima.Adapters.FollowAdapter;
import com.developerali.aima.Adapters.PostAdapter;
import com.developerali.aima.Adapters.VideoPostAdapter;
import com.developerali.aima.CommonFeatures;
import com.developerali.aima.MainActivity;
import com.developerali.aima.Models.PostModel;
import com.developerali.aima.Models.UsagesModel;
import com.developerali.aima.Models.UserModel;
import com.developerali.aima.Models.VideoModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.ActivitySearchBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    ActivitySearchBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseFirestore firebaseFirestore;
    ArrayList<UserModel> userModelArrayList;
    private long startTime;
    private long totalSeconds;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        CommonFeatures.lowerColour(getWindow(), getResources());
        getSupportActionBar().setSubtitle("click on search button");


        binding.searchView.setVoiceSearch(true);
        binding.searchView.setEllipsize(true);
        binding.searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchProfile(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        binding.searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                binding.radioGroup.setVisibility(View.VISIBLE);
                binding.spinKit.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSearchViewClosed() {
                binding.radioGroup.setVisibility(View.GONE);
                binding.spinKit.setVisibility(View.GONE);
            }
        });



    }

    public void searchProfile(String queryText){
        int checkedRadioButtonId = binding.radioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = findViewById(checkedRadioButtonId);
        String searchOn = radioButton.getText().toString();

        if (searchOn.equalsIgnoreCase("people")){
            DatabaseReference reference = database.getReference().child("users");
//        Query query = reference.orderByChild("name").equalTo(queryText);
            Query query = reference.orderByChild("name")
                    .startAt(queryText).endAt(queryText + "\uf8ff");
            userModelArrayList = new ArrayList<>();


            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    userModelArrayList.clear();
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {

                        String userId = childSnapshot.getKey();

                        if (!userId.equalsIgnoreCase(auth.getCurrentUser().getUid()) ) {

                            UserModel userModel = childSnapshot.getValue(UserModel.class);

                            userModel.setUserId(childSnapshot.getKey());

                            userModel.setImage(userModel.getImage());
                            //userModel.setCover(userModel.getCover());

                            userModel.setName(userModel.getName());
                            userModel.setBio(userModel.getBio());
                            //userModel.setAbout(userModel.getAbout());

                            //userModel.setEmail(userModel.getEmail());
                            //userModel.setPhone(userModel.getPhone());
                            //userModel.setWhatsapp(userModel.getWhatsapp());
                            //userModel.setFacebook(userModel.getFacebook());

                            userModel.setFollower(userModel.getFollower());
                            userModel.setType(userModel.getType());
                            //userModel.setFollowing(userModel.getFollowing());

                            userModelArrayList.add(userModel);


                            // String about = childSnapshot.child("about").getValue(String.class);
                            // String bio = childSnapshot.child("bio").getValue(String.class);
                        }

                    }

                    binding.searchText.setVisibility(View.GONE);
                    FollowAdapter followAdapter = new FollowAdapter(SearchActivity.this, userModelArrayList);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SearchActivity.this);
                    binding.userRecyclerView.setLayoutManager(linearLayoutManager);
                    binding.userRecyclerView.setAdapter(followAdapter);
                    followAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle any errors

                    Toast.makeText(SearchActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }else if (searchOn.equalsIgnoreCase("video")){

            ArrayList<VideoModel> videoModelArrayList = new ArrayList<>();

            firebaseFirestore.collection("video")
                    .whereEqualTo("caption", queryText)
                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                            if (!queryDocumentSnapshots.isEmpty()) {
                                videoModelArrayList.clear();
                                for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()){
                                    VideoModel postModel = snapshot.toObject(VideoModel.class);

                                    postModel.setUploader(snapshot.getString("uploader"));
                                    if (snapshot.getString("caption") != null){
                                        postModel.setCaption(snapshot.getString("caption"));
                                    }

                                    postModel.setTime(snapshot.getLong("time"));
                                    videoModelArrayList.add(postModel);
                                }
                                binding.searchText.setVisibility(View.GONE);
                                VideoPostAdapter postAdapter = new VideoPostAdapter(SearchActivity.this, videoModelArrayList,
                                        getLifecycle(), SearchActivity.this);
                                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SearchActivity.this);
                                binding.userRecyclerView.setLayoutManager(linearLayoutManager);
                                binding.userRecyclerView.setAdapter(postAdapter);
                                postAdapter.notifyDataSetChanged();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SearchActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }else {
            ArrayList<PostModel> postModelArrayList = new ArrayList<>();
            firebaseFirestore.collection("post")
                    .whereEqualTo("caption", queryText)
                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                            if (!queryDocumentSnapshots.isEmpty()) {

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
                                binding.searchText.setVisibility(View.GONE);
                                PostAdapter postAdapter = new PostAdapter(SearchActivity.this, postModelArrayList, SearchActivity.this);
                                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SearchActivity.this);
                                binding.userRecyclerView.setLayoutManager(linearLayoutManager);
                                binding.userRecyclerView.setAdapter(postAdapter);
                                postAdapter.notifyDataSetChanged();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SearchActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.serach_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        binding.searchView.setMenuItem(item);

        return true;
    }




    @Override
    public void onBackPressed() {
        if (binding.searchView.isSearchOpen()) {
            binding.searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    binding.searchView.setQuery(searchWrd, false);
                }
            }

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sharedPreferences = getSharedPreferences("UsageTime", MODE_PRIVATE); //creating database
        totalSeconds = sharedPreferences.getLong("total_seconds", 0);  //getting previous value
        startTime = System.currentTimeMillis();  //get start time for counting
    }

    @Override
    protected void onPause() {
        long currentTime = System.currentTimeMillis();  //get stop time for counting
        long totalTime = currentTime - startTime;   //calculating watch time
        long newTime = totalSeconds + (totalTime/1000);    //add previous sec and now time converting in sec

        SharedPreferences.Editor editor = sharedPreferences.edit();  // updating in database
        editor.putLong("total_seconds", newTime);
        editor.apply();

        ArrayList<UsagesModel> arrayList = CommonFeatures.readListFromPref(this);
        UsagesModel usagesModel = new UsagesModel("Searching", startTime, currentTime);
        arrayList.add(usagesModel);
        CommonFeatures.writeListInPref(SearchActivity.this, arrayList);

        super.onPause();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}