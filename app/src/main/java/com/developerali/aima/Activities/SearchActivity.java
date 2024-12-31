package com.developerali.aima.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.developerali.aima.Adapters.FollowAdapter;
import com.developerali.aima.Adapters.PostAdapter;
import com.developerali.aima.Adapters.SearchAdapter;
import com.developerali.aima.Adapters.VideoPostAdapter;
import com.developerali.aima.CommonFeatures;
import com.developerali.aima.DB_Helper;
import com.developerali.aima.Models.PostModel;
import com.developerali.aima.Models.RecentSearchModel;
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
//import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.Collections;

public class SearchActivity extends AppCompatActivity implements SearchAdapter.SelectedItem {

    private ActivitySearchBinding binding;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseFirestore firebaseFirestore;
    private ArrayList<UserModel> userModelArrayList;
    private SharedPreferences sharedPreferences;
    ArrayList<RecentSearchModel> recentSearches;
    private DB_Helper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initFirebase();
        //setupToolbar();

        dbHelper = new DB_Helper(this);
        ArrayList<RecentSearchModel> searchModelArrayList = dbHelper.getAllSearchQueries();
        recentSearches = new ArrayList<>();
        recentSearches.clear();
        for (RecentSearchModel searchQuery : searchModelArrayList) {
            // Do something with each search query
            RecentSearchModel recentSearchModel = new RecentSearchModel();
            recentSearchModel.setSearch_query(searchQuery.getSearch_query());
            recentSearchModel.setType(searchQuery.getType());
            recentSearches.add(recentSearchModel);

        }
        loadSearchHis(recentSearches);


        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchProfile(query);
                binding.progressBar4.setVisibility(View.VISIBLE);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


//        binding.searchView.setVoiceSearch(true);
//        binding.searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                searchProfile(query);
//                binding.progressBar4.setVisibility(View.VISIBLE);
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                return false;
//            }
//        });

//        binding.searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
//            @Override
//            public void onSearchViewShown() {
//                toggleSearchUIVisibility(true);
//            }
//
//            @Override
//            public void onSearchViewClosed() {
//                toggleSearchUIVisibility(false);
//            }
//        });

        binding.radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int checkedRadioButtonId = radioGroup.getCheckedRadioButtonId();
                RadioButton radioButton = findViewById(checkedRadioButtonId);
                String searchOn = radioButton.getText().toString();
                getSupportActionBar().setTitle("Searching on " + searchOn);
            }
        });
    }

    private void loadSearchHis(ArrayList<RecentSearchModel> recentSearches) {
        LinearLayoutManager lnm = new LinearLayoutManager(SearchActivity.this);
        binding.userRecyclerView.setLayoutManager(lnm);
        if (recentSearches != null){
            Collections.reverse(recentSearches);
            SearchAdapter adapter = new SearchAdapter(recentSearches, SearchActivity.this, SearchActivity.this);
            binding.userRecyclerView.setAdapter(adapter);
            binding.noData.setVisibility(View.GONE);
        }
        if (recentSearches.isEmpty()){
            binding.userRecyclerView.setVisibility(View.GONE);
            binding.noData.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Initialize Firebase instances for authentication, database, and Firestore.
     */
    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    /**
     * Sets up the toolbar with home button and title.
     */
    private void setupToolbar() {
        //setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Search for peoples");
            getSupportActionBar().setSubtitle("we always have something for you...");
        }
        CommonFeatures.lowerColour(getWindow(), getResources());
    }

    private void toggleSearchUIVisibility(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        binding.radioGroup.setVisibility(visibility);
        binding.spinKit.setVisibility(visibility);
    }

    /**
     * Search for profiles, posts, or videos based on user input and selected category.
     *
     * @param queryText The search query entered by the user.
     */
    private void searchProfile(String queryText) {
        int checkedRadioButtonId = binding.radioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = findViewById(checkedRadioButtonId);
        String searchOn = radioButton.getText().toString();
        dbHelper.addSearchQuery(new RecentSearchModel(queryText, searchOn));
        binding.noData.setVisibility(View.GONE);

        switch (searchOn.toLowerCase()) {
            case "people":
                searchPeople(queryText);
                break;
            case "video":
                searchVideos(queryText);
                break;
            default:
                searchPosts(queryText);
                break;
        }
    }

    /**
     * Searches for users based on their name from Firebase Realtime Database.
     *
     * @param queryText The search query entered by the user.
     */
    private void searchPeople(String queryText) {
        DatabaseReference reference = database.getReference().child("users");
        Query query = reference.orderByChild("name")
                .startAt(queryText).endAt(queryText + "\uf8ff");

        userModelArrayList = new ArrayList<>();
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userModelArrayList.clear();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String userId = childSnapshot.getKey();
                    if (!(userId != null && userId.equalsIgnoreCase(auth.getCurrentUser().getUid()))) {
                        UserModel userModel = childSnapshot.getValue(UserModel.class);
                        if (userModel != null) {
                            userModel.setUserId(userId);
                            userModelArrayList.add(userModel);
                        }
                    }
                }

                if (userModelArrayList.isEmpty()) {
                    loadDefaultUsers();
                } else {
                    displayPeopleResults();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SearchActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Loads a default set of users to display when no search results are found.
     */
    private void loadDefaultUsers() {
        DatabaseReference reference = database.getReference().child("users");
        reference.limitToFirst(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userModelArrayList.clear();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    UserModel userModel = childSnapshot.getValue(UserModel.class);
                    if (userModel != null) {
                        userModel.setUserId(childSnapshot.getKey());
                        userModelArrayList.add(userModel);
                    }
                }
                displayPeopleResults();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SearchActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Displays the search results for people in the RecyclerView.
     */
    private void displayPeopleResults() {
        FollowAdapter followAdapter = new FollowAdapter(SearchActivity.this, userModelArrayList);
        binding.userRecyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
        binding.userRecyclerView.setAdapter(followAdapter);
        followAdapter.notifyDataSetChanged();

        if (userModelArrayList.isEmpty()) {
            binding.noData.setVisibility(View.VISIBLE);
        }
        binding.progressBar4.setVisibility(View.GONE);
        binding.userRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * Searches for videos from Firestore based on their captions.
     *
     * @param queryText The search query entered by the user.
     */
    private void searchVideos(String queryText) {
        ArrayList<VideoModel> videoModelArrayList = new ArrayList<>();
        firebaseFirestore.collection("video")
                .whereEqualTo("caption", queryText)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                            VideoModel videoModel = snapshot.toObject(VideoModel.class);

                            videoModel.setUploader(snapshot.getString("uploader"));
                            if (snapshot.getString("caption") != null){
                                videoModel.setCaption(snapshot.getString("caption"));
                            }
                            videoModel.setTime(snapshot.getLong("time"));

                            if (videoModel != null) {
                                videoModelArrayList.add(videoModel);
                            }
                        }
                    }
                }).addOnFailureListener(e ->
                        Toast.makeText(SearchActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());

        if (videoModelArrayList.isEmpty()) {
            loadDefaultVideos();
        } else {
            displayVideoResults(videoModelArrayList);
        }
    }

    /**
     * Loads a default set of videos when no search results are found.
     */
    private void loadDefaultVideos() {
        ArrayList<VideoModel> videoModelArrayList = new ArrayList<>();
        firebaseFirestore.collection("video")
                .limit(10)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                        VideoModel videoModel = snapshot.toObject(VideoModel.class);

                        videoModel.setUploader(snapshot.getString("uploader"));
                        if (snapshot.getString("caption") != null){
                            videoModel.setCaption(snapshot.getString("caption"));
                        }
                        videoModel.setTime(snapshot.getLong("time"));

                        if (videoModel != null) {
                            videoModelArrayList.add(videoModel);
                        }
                    }
                    displayVideoResults(videoModelArrayList);
                }).addOnFailureListener(e ->
                        Toast.makeText(SearchActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Displays the search results for videos in the RecyclerView.
     *
     * @param videoModelArrayList The list of video models to display.
     */
    private void displayVideoResults(ArrayList<VideoModel> videoModelArrayList) {
        VideoPostAdapter videoPostAdapter = new VideoPostAdapter(videoModelArrayList, getLifecycle(), SearchActivity.this);
        binding.userRecyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
        binding.userRecyclerView.setAdapter(videoPostAdapter);
        videoPostAdapter.notifyDataSetChanged();

        if (videoModelArrayList.isEmpty()) {
            binding.noData.setVisibility(View.VISIBLE);
        }

        binding.progressBar4.setVisibility(View.GONE);
    }

    /**
     * Searches for posts from Firestore based on their descriptions.
     *
     * @param queryText The search query entered by the user.
     */
    private void searchPosts(String queryText) {
        ArrayList<PostModel> postModelArrayList = new ArrayList<>();
        firebaseFirestore.collection("posts")
                .whereEqualTo("description", queryText)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                            PostModel postModel = snapshot.toObject(PostModel.class);
                            postModel.setId(snapshot.getId());
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

                            if (postModel != null) {
                                postModelArrayList.add(postModel);
                            }
                        }

                        if (postModelArrayList.isEmpty()) {
                            loadDefaultPosts();
                        } else {
                            displayPostResults(postModelArrayList);
                        }
                    }else {
                        loadDefaultPosts();
                    }
                }).addOnFailureListener(e ->
                        Toast.makeText(SearchActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());


    }

    /**
     * Loads a default set of posts when no search results are found.
     */
    private void loadDefaultPosts() {
        ArrayList<PostModel> postModelArrayList = new ArrayList<>();

        firebaseFirestore.collection("post")
                .whereEqualTo("approved", true)  // Ensures we only fetch approved posts
                .limit(10)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {

                            postModelArrayList.clear();
                            for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                                PostModel postModel = snapshot.toObject(PostModel.class);
                                if (postModel != null) {
                                    postModel.setId(snapshot.getId());

                                    if (snapshot.getString("image") != null) {
                                        postModel.setImage(snapshot.getString("image"));
                                    } else {
                                        postModel.setImage("");  // Set default or empty image if null
                                    }

                                    if (snapshot.getString("uploader") != null) {
                                        postModel.setUploader(snapshot.getString("uploader"));
                                    } else {
                                        postModel.setUploader("Unknown");  // Handle missing uploader
                                    }

                                    if (snapshot.getString("caption") != null) {
                                        postModel.setCaption(snapshot.getString("caption"));
                                    } else {
                                        postModel.setCaption("");  // Set default caption if null
                                    }

                                    if (snapshot.getLong("time") != null) {
                                        postModel.setTime(snapshot.getLong("time"));
                                    }

                                    if (snapshot.getLong("commentsCount") != null) {
                                        postModel.setCommentsCount(Math.toIntExact(snapshot.getLong("commentsCount")));
                                    } else {
                                        postModel.setCommentsCount(Math.toIntExact(0L));  // Set default count if missing
                                    }

                                    if (snapshot.getLong("likesCount") != null) {
                                        postModel.setLikesCount(Math.toIntExact(snapshot.getLong("likesCount")));
                                    } else {
                                        postModel.setLikesCount(Math.toIntExact(0L));  // Set default count if missing
                                    }

                                    postModel.setApproved(snapshot.getBoolean("approved") != null
                                            ? snapshot.getBoolean("approved")
                                            : false);  // Set approved safely

                                    postModelArrayList.add(postModel);  // Add postModel to the list
                                }
                            }

                            displayPostResults(postModelArrayList);  // Show the posts in the UI

                        } else {
                            Toast.makeText(SearchActivity.this, "No approved posts found", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SearchActivity.this, "Error: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    /**
     * Displays the search results for posts in the RecyclerView.
     *
     * @param postModelArrayList The list of post models to display.
     */
    private void displayPostResults(ArrayList<PostModel> postModelArrayList) {
        PostAdapter postAdapter = new PostAdapter(postModelArrayList, SearchActivity.this);
        binding.userRecyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
        binding.userRecyclerView.setAdapter(postAdapter);
        postAdapter.notifyDataSetChanged();

        if (postModelArrayList.isEmpty()) {
            binding.noData.setVisibility(View.VISIBLE);
        }

        binding.progressBar4.setVisibility(View.GONE);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.serach_menu, menu);
//        MenuItem item = menu.findItem(R.id.action_search);
//        binding.searchView.setMenuItem(item);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        if (item.getItemId() == android.R.id.home) {
//            finish();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public void onShowAction(RecentSearchModel searchModel) {
        switch (searchModel.getType().toLowerCase()) {
            case "people":
                binding.people.setChecked(true);
                break;
            case "video":
                binding.video.setChecked(true);
                break;
            default:
                binding.post.setChecked(true);
                break;
        }
        searchProfile(searchModel.getSearch_query());
        binding.progressBar4.setVisibility(View.VISIBLE);
    }
}
