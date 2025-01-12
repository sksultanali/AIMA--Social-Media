package com.developerali.aima.Helpers;

import com.developerali.aima.Model_Apis.ApiResponse;
import com.developerali.aima.Model_Apis.ApiService;

import java.util.LinkedList;
import java.util.Queue;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoApiReformOld {

    private Queue<VideoInsertTask> taskQueue = new LinkedList<>();
    private ApiService videoService;

    public VideoApiReformOld(ApiService videoService) {
        this.videoService = videoService;
    }

    public void enqueueInsertTask(String videoId, String uploader, String youTubeId, String caption, String status, Runnable actionOnSuccess) {
        taskQueue.add(new VideoInsertTask(videoId, uploader, youTubeId, caption, status, actionOnSuccess));
        if (taskQueue.size() == 1) {
            // Start processing only if no ongoing request
            processNextTask();
        }
    }

    private void processNextTask() {
        if (taskQueue.isEmpty()) return;  // No tasks to process

        VideoInsertTask currentTask = taskQueue.peek();  // Get the current task

        // Make the API call
        Call<ApiResponse> call = videoService.insertOldVideo(
                "insertOldVideos",
                currentTask.videoId,
                currentTask.uploader,
                currentTask.youTubeId,
                currentTask.caption,
                currentTask.status
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

    public class VideoInsertTask {
        String videoId;
        String uploader;
        String youTubeId;
        String caption;
        String status;
        Runnable actionOnSuccess;

        public VideoInsertTask(String videoId, String uploader, String youTubeId, String caption, String status, Runnable actionOnSuccess) {
            this.videoId = videoId;
            this.uploader = uploader;
            this.youTubeId = youTubeId;
            this.caption = caption;
            this.status = status;
            this.actionOnSuccess = actionOnSuccess;
        }
    }

}
