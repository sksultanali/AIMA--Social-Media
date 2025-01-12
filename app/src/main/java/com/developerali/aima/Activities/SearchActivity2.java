package com.developerali.aima.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.developerali.aima.Adapters.KeywordAdapter;
import com.developerali.aima.Adapters.SearchAdapter;
import com.developerali.aima.Helpers.DB_Helper;
import com.developerali.aima.Model_Apis.ApiService;
import com.developerali.aima.Model_Apis.KeywordResponse;
import com.developerali.aima.Model_Apis.RetrofitClient;
import com.developerali.aima.Models.RecentSearchModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.ActivitySearch2Binding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity2 extends AppCompatActivity {

    ActivitySearch2Binding binding;
    ApiService apiService;
    Call<KeywordResponse> call;
    private List<String> textList = Arrays.asList(
            "Search people you know...",
            "Remember? that last post you seen...",
            "Search videos caption",
            "Ask anything you want!"
    );
    private int listIndex = 0;
    private int charIndex = 0;
    private long typingDelay = 100;
    private long deletingDelay = 50;
    private long showDelay = 2000;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isDeleting = false;
    private DB_Helper dbHelper;
    ArrayList<RecentSearchModel> recentSearches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearch2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        apiService = RetrofitClient.getClient().create(ApiService.class);
        startTypingAnimation();

        binding.goBack.setOnClickListener(v->{
            onBackPressed();
        });


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

        LinearLayoutManager lnm = new LinearLayoutManager(SearchActivity2.this);
        binding.searchRec.setLayoutManager(lnm);
        loadSearchHis(recentSearches);

        binding.searchBtn.setOnClickListener(v->{
            String keyword = binding.keyword.getText().toString();
            if (keyword.isEmpty()){
                binding.keyword.setError("*");
            }else {
                dbHelper.addSearchQuery(new RecentSearchModel(keyword, "people"));

                Intent i = new Intent(SearchActivity2.this, SearchResults.class);
                i.putExtra("type", "people");
                i.putExtra("keyword", keyword);
                startActivity(i);
            }
        });

        binding.cancelBtn.setOnClickListener(v->{
            binding.keyword.setText("");
        });

        binding.keyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() == 0){
                    binding.cancelBtn.setVisibility(View.GONE);
                    binding.searchBtn.setVisibility(View.GONE);
                }else {
                    binding.cancelBtn.setVisibility(View.VISIBLE);
                    binding.searchBtn.setVisibility(View.VISIBLE);

                    call = apiService.searchKeyword(
                            "searchKeyword", charSequence.toString());

                    call.enqueue(new Callback<KeywordResponse>() {
                        @Override
                        public void onResponse(Call<KeywordResponse> call, Response<KeywordResponse> response) {
                            if (response.isSuccessful() && response.body() != null){
                                KeywordResponse apiResponse = response.body();
                                if (apiResponse.getStatus().equalsIgnoreCase("success") && apiResponse.getData() != null &&
                                        !apiResponse.getData().isEmpty()){
                                    KeywordAdapter adapter = new KeywordAdapter(SearchActivity2.this, apiResponse.getData());
                                    binding.searchRec.setAdapter(adapter);
                                }
                                checkRecData();
                            }
                        }

                        @Override
                        public void onFailure(Call<KeywordResponse> call, Throwable t) {
                            Toast.makeText(SearchActivity2.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });


                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });




    }

    private void startTypingAnimation() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentText = textList.get(listIndex);
                if (!isDeleting) {
                    if (charIndex < currentText.length()) {
                        binding.keyword.setHint(currentText.substring(0, charIndex + 1));
                        charIndex++;
                        handler.postDelayed(this, typingDelay);
                    } else {
                        handler.postDelayed(() -> isDeleting = true, showDelay);
                        handler.postDelayed(this, showDelay);
                    }
                } else {
                    if (charIndex > 0) {
                        binding.keyword.setHint(currentText.substring(0, charIndex - 1));
                        charIndex--;
                        handler.postDelayed(this, deletingDelay);
                    } else {
                        isDeleting = false;
                        listIndex = (listIndex + 1) % textList.size();
                        handler.postDelayed(this, 0);
                    }
                }
            }
        }, typingDelay);
    }

    private void loadSearchHis(ArrayList<RecentSearchModel> recentSearches) {
        if (recentSearches != null){
            Collections.reverse(recentSearches);
            SearchAdapter adapter = new SearchAdapter(recentSearches, SearchActivity2.this);
            binding.searchRec.setAdapter(adapter);
        }
        checkRecData();
    }

    void checkRecData(){
        if (binding.searchRec.getAdapter() != null) {
            int itemCount = binding.searchRec.getAdapter().getItemCount();
            if (itemCount > 0) {
                binding.noData.setVisibility(View.GONE);
            } else {
                binding.noData.setVisibility(View.VISIBLE);
            }
        } else {
            binding.noData.setVisibility(View.VISIBLE);
        }
    }
}