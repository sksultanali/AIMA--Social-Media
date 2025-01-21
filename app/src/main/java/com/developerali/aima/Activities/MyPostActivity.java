package com.developerali.aima.Activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.developerali.aima.Adapters.PostAdapter;
import com.developerali.aima.Adapters.VideoPostAdapter;
import com.developerali.aima.BottomBar.MeetingFragment;
import com.developerali.aima.Helpers.Helper;
import com.developerali.aima.HomeBottomBar.AimaAdmin;
import com.developerali.aima.HomeBottomBar.AimaVideos;
import com.developerali.aima.Model_Apis.ApiService;
import com.developerali.aima.Model_Apis.PostResponse;
import com.developerali.aima.Model_Apis.RetrofitClient;
import com.developerali.aima.Model_Apis.VideoResponse;
import com.developerali.aima.MyPostSection.NormalPost;
import com.developerali.aima.MyPostSection.VideoPost;
import com.developerali.aima.R;
import com.developerali.aima.databinding.ActivityMyPostBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyPostActivity extends AppCompatActivity {

    ActivityMyPostBinding binding;
    String profileId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Helper.changeStatusBarToDark(MyPostActivity.this, R.color.backgroundBottomColour);
        profileId = getIntent().getStringExtra("profileId");

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content, new NormalPost());
        transaction.commit();

        // Add tabs to the TabLayout
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Photos"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Videos"));

        // Set the default selected tab
        TextView tv = (TextView) LayoutInflater.from(MyPostActivity.this)
                .inflate(R.layout.custom_tab, null);
        binding.tabLayout.getTabAt(0).setCustomView(tv);
        binding.tabLayout.getTabAt(0).select();

        // Handle tab selection changes
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                TextView tv = (TextView) LayoutInflater.from(MyPostActivity.this)
                        .inflate(R.layout.custom_tab, null);
                binding.tabLayout.getTabAt(tab.getPosition()).setCustomView(tv);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                switch (tab.getPosition()) {
                    case 0:
                        transaction.replace(R.id.content, new NormalPost());
                        transaction.commit();
                        break;
                    case 1:
                        transaction.replace(R.id.content, new VideoPost());
                        transaction.commit();
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

        binding.goBack.setOnClickListener(v->{
            onBackPressed();
        });



    }

}