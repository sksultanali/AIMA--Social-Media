package com.developerali.aima.BottomBar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.developerali.aima.Adapters.ShortsAdapter;
import com.developerali.aima.Helpers.Helper;
import com.developerali.aima.Models.shortsModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.FragmentShortsBinding;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;

import me.ibrahimsn.lib.SmoothBottomBar;


public class ShortsFragment extends Fragment {

    FragmentShortsBinding binding;
    private SmoothBottomBar bottomBar;
    ShortsAdapter adapter;


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
        Helper.changeStatusBarColor(getActivity(), R.color.white);
        bottomBar = getActivity().findViewById(R.id.bottomBar);
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