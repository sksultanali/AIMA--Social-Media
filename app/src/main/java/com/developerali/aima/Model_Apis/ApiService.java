package com.developerali.aima.Model_Apis;

import androidx.annotation.Nullable;

import com.developerali.aima.Models.NotificationRequest;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiService {

    @GET("testApis/api.php")
    Call<ApiResponse> updateUserData(
            @Query("action") String action,
            @Query("user_id") String user_id,
            @Query("field") String field,
            @Query("value") String value
    );

    @POST("v1/projects/bong-stay/messages:send")
    Call<Void> sendNotificationTopic(
            @Header("Authorization") String authorization,
            @Body NotificationRequest notificationRequest
    );

    @GET("testApis/api.php")
    Call<ApiResponse> addNewUser(
            @Query("action") String action,
            @Query("user_id") String user_id,
            @Query("name") String name,
            @Query("email") String email,
            @Query("password") @Nullable String password,
            @Query("token") @Nullable String token,
            @Query("image") @Nullable String image
    );




//    @Multipart
//    @POST("testApis/api.php") // Replace with your endpoint
//    Call<ImageUploadResponse> uploadImage(
//            @Query("action") String action,
//            @Query("folder_location") String folder_location,
//            @Part MultipartBody.Part image
//    );
}
