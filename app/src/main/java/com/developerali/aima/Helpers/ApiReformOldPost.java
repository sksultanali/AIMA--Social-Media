package com.developerali.aima.Helpers;

import android.app.Activity;
import android.widget.Toast;

import com.developerali.aima.Model_Apis.ApiResponse;
import com.developerali.aima.Model_Apis.ApiService;

import java.util.LinkedList;
import java.util.Queue;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiReformOldPost {
    private Queue<InsertPostTask> taskQueue = new LinkedList<>();
    private ApiService postService;
    Activity activity;

    public ApiReformOldPost(ApiService postService, Activity activity) {
        this.activity = activity;
        this.postService = postService;
    }

    public class InsertPostTask {
        String id;
        String image;
        String uploader;
        String caption;
        String status;
        int commentsCount;
        int likesCount;
        Runnable actionOnSuccess;

        public InsertPostTask(String id, String image, String uploader, String caption, String status, int commentsCount, int likesCount, Runnable actionOnSuccess) {
            this.id = id;
            this.image = image;
            this.uploader = uploader;
            this.caption = caption;
            this.status = status;
            this.commentsCount = commentsCount;
            this.likesCount = likesCount;
            this.actionOnSuccess = actionOnSuccess;
        }
    }


    public void enqueueInsertTask(String id, String image, String uploader, String caption, String status, int commentsCount, int likesCount, Runnable actionOnSuccess) {
        taskQueue.add(new InsertPostTask(id, image, uploader, caption, status, commentsCount, likesCount, actionOnSuccess));
        if (taskQueue.size() == 1) {
            // Start processing only if no ongoing request
            processNextTask();
        }
    }

    private void processNextTask() {
        if (taskQueue.isEmpty()) return;  // No tasks to process

        InsertPostTask currentTask = taskQueue.peek();  // Get the current task

        // Make the API call
        Call<ApiResponse> call = postService.insertOldPost(
                "insertOldPost",
                currentTask.id,
                currentTask.image,
                currentTask.uploader,
                currentTask.caption,
                currentTask.status,
                currentTask.commentsCount,
                currentTask.likesCount
        );

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if ("success".equals(apiResponse.getStatus())) {
                        if (currentTask.actionOnSuccess != null) {
                            currentTask.actionOnSuccess.run();
                        }
                    }else {
                        Toast.makeText(activity, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    taskQueue.poll(); // Remove the task after successful completion
                    processNextTask(); // Process the next task
                } else {
                    // Handle failure (e.g., log error or retry)
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Handle the failure scenario (e.g., retry or log error)
            }
        });
    }
}
