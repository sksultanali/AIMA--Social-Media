package com.developerali.aima.BottomBar;

import static com.facebook.react.bridge.UiThreadUtil.runOnUiThread;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.developerali.aima.Adapters.ShortsAdapter;
import com.developerali.aima.Models.shortsModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.FragmentShortsBinding;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;

import me.ibrahimsn.lib.SmoothBottomBar;


public class ShortsFragment extends Fragment {

    FragmentShortsBinding binding;
    private SmoothBottomBar bottomBar;
//    Adapter adapter;
//    ArrayList<shortsModel> arrayList;
    ShortsAdapter adapter;
    YouTubePlayer youTubePlayer;


    public ShortsFragment(){}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentShortsBinding.inflate(inflater, container, false);
        bottomBar = (SmoothBottomBar) getActivity().findViewById(R.id.bottomBar);
        bottomBar.setItemActiveIndex(3);


        FirebaseRecyclerOptions<shortsModel> options = new FirebaseRecyclerOptions.Builder<shortsModel>()
                .setQuery(FirebaseDatabase.getInstance().getReference()
                        .child("shorts").orderByChild("time"), shortsModel.class)
                .build();

        adapter = new ShortsAdapter(options);
        binding.viewPager.setAdapter(adapter);


        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
//                Toast.makeText(getActivity(), "page "+ position, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });



//        arrayList = new ArrayList<>();
//        FirebaseDatabase.getInstance()
//                .getReference().child("shorts")
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        arrayList.clear();
//                        for (DataSnapshot snapshot1 : snapshot.getChildren()){
//                            shortsModel shortsModel = snapshot1.getValue(shortsModel.class);
//                            arrayList.add(shortsModel);
//                        }
//
//                        binding.viewPager.setAdapter(new Adapter(getActivity(), arrayList));
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
















        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}