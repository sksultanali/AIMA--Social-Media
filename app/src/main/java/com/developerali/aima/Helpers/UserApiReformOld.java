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

public class UserApiReformOld {
    public class UserInsertTask {
        String userId;
        String name;
        String type;
        int verified;
        String verifiedValid;
        int stars;
        String email;
        String phone;
        String password;
        String image;
        String cover;
        String whatsapp;
        String facebook;
        String about;
        String bio;
        int follower;
        int following;
        int posts;
        String token;
        Runnable actionOnSuccess;

        public UserInsertTask(String userId, String name, String type, int verified, String verifiedValid, int stars,
                              String email, String phone, String password, String image, String cover,
                              String whatsapp, String facebook, String about, String bio, int follower, int following,
                              int posts, String token, Runnable actionOnSuccess) {
            this.userId = userId;
            this.name = name;
            this.type = type;
            this.verified = verified;
            this.verifiedValid = verifiedValid;
            this.stars = stars;
            this.email = email;
            this.phone = phone;
            this.password = password;
            this.image = image;
            this.cover = cover;
            this.whatsapp = whatsapp;
            this.facebook = facebook;
            this.about = about;
            this.bio = bio;
            this.follower = follower;
            this.following = following;
            this.posts = posts;
            this.token = token;
            this.actionOnSuccess = actionOnSuccess;
        }
    }


    private Queue<UserInsertTask> taskQueue = new LinkedList<>();
    private ApiService userService;
    Activity activity;

    public UserApiReformOld(ApiService userService, Activity activity) {
        this.userService = userService;
        this.activity = activity;
    }

    public void enqueueInsertTask(String userId, String name, String type, int verified, String verifiedValid, int stars,
                                  String email, String phone, String password, String image, String cover,
                                  String whatsapp, String facebook, String about, String bio, int follower, int following,
                                  int posts, String token, Runnable actionOnSuccess) {
        taskQueue.add(new UserInsertTask(userId, name, type, verified, verifiedValid, stars, email, phone, password, image,
                cover, whatsapp, facebook, about, bio, follower, following, posts, token, actionOnSuccess));
        if (taskQueue.size() == 1) {
            // Start processing if no ongoing request
            processNextTask();
        }
    }

    private void processNextTask() {
        if (taskQueue.isEmpty()) return;  // No tasks to process

        UserInsertTask currentTask = taskQueue.peek();  // Get the current task

        // Make the API call
        Call<ApiResponse> call = userService.insertOldUser(
                "insertOldUsers",
                currentTask.userId,
                currentTask.name,
                currentTask.type,
                currentTask.verified,
                currentTask.verifiedValid,
                currentTask.stars,
                currentTask.email,
                currentTask.phone,
                currentTask.password,
                currentTask.image,
                currentTask.cover,
                currentTask.whatsapp,
                currentTask.facebook,
                currentTask.about,
                currentTask.bio,
                currentTask.follower,
                currentTask.following,
                currentTask.posts,
                currentTask.token
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
