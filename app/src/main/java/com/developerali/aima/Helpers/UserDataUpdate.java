package com.developerali.aima.Helpers;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.developerali.aima.Model_Apis.ApiResponse;
import com.developerali.aima.Model_Apis.ApiService;
import com.developerali.aima.Model_Apis.RetrofitClient;
import com.developerali.aima.Model_Apis.UserDetails;
import com.developerali.aima.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.LinkedList;
import java.util.Queue;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserDataUpdate {
    private Queue<userUpdateTask> updateTaskQueue = new LinkedList<userUpdateTask>();
    ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

    public void enqueueUpdateTask(String uId, String fieldName, String fieldValue, Runnable actionOnSuccess) {
        updateTaskQueue.add(new userUpdateTask(uId, fieldName, fieldValue, actionOnSuccess));
        if (updateTaskQueue.size() == 1) {
            // Only start processing if the queue was empty (i.e., no ongoing request)
            processNextTask();
        }
    }

    public class userUpdateTask {
        String uId;
        String fieldName;
        String fieldValue;
        Runnable actionOnSuccess;

        public userUpdateTask(String uId, String fieldName, String fieldValue, Runnable actionOnSuccess) {
            this.uId = uId;
            this.fieldName = fieldName;
            this.fieldValue = fieldValue;
            this.actionOnSuccess = actionOnSuccess;
        }
    }

    private void processNextTask() {
        if (updateTaskQueue.isEmpty()){
            return;
        }

        userUpdateTask currentTask = updateTaskQueue.peek();  // Peek without removing

        Call<ApiResponse> call = apiService.updateUserData(
                "updateUserField",
                currentTask.uId,
                currentTask.fieldName,
                currentTask.fieldValue
        );

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();

                    //Toast.makeText(activity, "Updated: " + currentTask.fieldName, Toast.LENGTH_SHORT).show();
                    currentTask.actionOnSuccess.run();
                    updateTaskQueue.poll();
                    processNextTask();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                //Toast.makeText(activity, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}
