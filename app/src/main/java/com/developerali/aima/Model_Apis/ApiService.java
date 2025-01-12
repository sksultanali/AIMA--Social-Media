package com.developerali.aima.Model_Apis;

import androidx.annotation.Nullable;

import com.developerali.aima.Models.NotificationRequest;
import com.developerali.aima.Notifications.NotificationRequestTopic;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiService {

    @GET("aimaAppData/api.php")
    Call<ApiResponse> updateUserData(
            @Query("action") String action,
            @Query("user_id") String user_id,
            @Query("field") String field,
            @Query("value") String value
    );

    @POST("v1/projects/aima-6a424/messages:send")
    Call<Void> sendNotificationToken(
            @Header("Authorization") String authorization,
            @Body NotificationRequest notificationRequest
    );

    @POST("v1/projects/aima-6a424/messages:send")
    Call<Void> sendNotificationTopic(
            @Header("Authorization") String authorization,
            @Body NotificationRequestTopic notificationRequest
    );

    @GET("aimaAppData/api.php")
    Call<ApiResponse> addNewUser(
            @Query("action") String action,
            @Query("user_id") String user_id,
            @Query("name") String name,
            @Query("email") String email,
            @Query("password") @Nullable String password,
            @Query("token") @Nullable String token,
            @Query("image") @Nullable String image
    );

    @GET("aimaAppData/api.php")
    Call<UserDetails> getUserDetails(
            @Query("action") String action,
            @Query("user_id") String user_id
    );

    @GET("aimaAppData/api.php")
    Call<ApiResponse> insertPost(
            @Query("action") String action,
            @Query("uploader") String uploader,
            @Query("image") @Nullable String image,
            @Query("caption") @Nullable String caption
    );

    @GET("aimaAppData/api.php")
    Call<ApiResponse> updateMembership(
            @Query("action") String action,
            @Query("userId") String userId,
            @Query("fieldName") String fieldName,
            @Query("value") String value
    );

    @GET("aimaAppData/api.php")
    Call<MapPointerResponse> fetchMapPointers(
            @Query("action") String action,
            @Query("keyword") @Nullable String keyword
    );

    @GET("aimaAppData/api.php")
    Call<BoundaryData> fetchBoundaryData(
            @Query("action") String action
    );

    @GET("aimaAppData/api.php") // Update with the actual endpoint
    Call<ApiResponse> insertOldPost(
            @Query("action") String action,
            @Query("id") String id,
            @Query("image") String image,
            @Query("uploader") String uploader,
            @Query("caption") String caption,
            @Query("status") String status,
            @Query("commentsCount") int commentsCount,
            @Query("likesCount") int likesCount
    );

    @GET("aimaAppData/api.php")
    Call<ApiResponse> insertOldVideo(
            @Query("action") String action,
            @Query("videoId") String videoId,
            @Query("uploader") String uploader,
            @Query("youTubeId") String youTubeId,
            @Query("caption") @Nullable String caption,
            @Query("status") @Nullable String status
    );

    @GET("aimaAppData/api.php")
    Call<ApiResponse> insertOldUser(
            @Query("action") String action,
            @Query("userId") String userId,
            @Query("name") String name,
            @Query("type") String type,
            @Query("verified") int verified,
            @Query("verifiedValid") String verifiedValid,
            @Query("stars") @Nullable int stars,
            @Query("email") String email,
            @Query("phone") @Nullable String phone,
            @Query("password") @Nullable String password,
            @Query("image") @Nullable String image,
            @Query("cover") @Nullable String cover,
            @Query("whatsapp") @Nullable String whatsapp,
            @Query("facebook") @Nullable String facebook,
            @Query("about") @Nullable String about,
            @Query("bio") @Nullable String bio,
            @Query("follower") int follower,
            @Query("following") int following,
            @Query("posts") int posts,
            @Query("token") String token
    );

    @GET("aimaAppData/api.php")
    Call<ApiResponse> insertVideo(
            @Query("action") String action,
            @Query("uploader") String uploader,
            @Query("youTubeId") String youTubeId,
            @Query("caption") @Nullable String caption
    );

    @POST("v1/projects/bong-stay/messages:send")
    Call<Void> sendNotification(
            @Header("Authorization") String authorization,
            @Body NotificationRequest notificationRequest
    );

    @GET("aimaAppData/api.php")
    Call<ApiResponse> updatePostField(
            @Query("action") String action,
            @Query("id") String id,
            @Query("field") String field,
            @Query("value") String value
    );

    @GET("aimaAppData/api.php")
    Call<CountResponse> getCounts(
            @Query("action") String action
    );

    @GET("aimaAppData/api.php")
    Call<PostResponse> getAllPost(
            @Query("action") String action,
            @Query("status") String status,
            @Query("nextToken") int nextToken
    );

    @GET("aimaAppData/api.php")
    Call<KeywordResponse> searchKeyword(
            @Query("action") String action,
            @Query("keyword") String keyword
    );

    @GET("aimaAppData/api.php")
    Call<PostResponse> searchAllPosts(
            @Query("action") String action,
            @Query("keyword") String keyword,
            @Query("nextToken") int nextToken
    );

    @GET("aimaAppData/api.php")
    Call<UsersResponse> searchPeople(
            @Query("action") String action,
            @Query("keyword") String keyword,
            @Query("user_id") String user_id,
            @Query("nextToken") int nextToken
    );

    @GET("aimaAppData/api.php")
    Call<VideoResponse> searchAllVideos(
            @Query("action") String action,
            @Query("keyword") String keyword,
            @Query("nextToken") int nextToken
    );

    @GET("aimaAppData/api.php")
    Call<ApiResponse> insertMembership(
            @Query("action") String action,
            @Query("userId") String userId,
            @Query("name") String name,
            @Query("father_name") String father_name,
            @Query("image") String image,
            @Query("dob") String dob,
            @Query("address") String address,
            @Query("valid_till") int validTill
    );

    @GET("aimaAppData/api.php")
    Call<MembershipResponse> fetchMembershipByUserId(
            @Query("action") String action,
            @Query("userId") String userId
    );

    @GET("aimaAppData/api.php")
    Call<PostResponse> getAllAdminPost(
            @Query("action") String action,
            @Query("status") String status,
            @Query("uploader") String uploader,
            @Query("nextToken") int nextToken
    );

    @GET("aimaAppData/api.php")
    Call<SinglePostData> getSinglePost(
            @Query("action") String action,
            @Query("postId") String postId
    );

    @GET("aimaAppData/api.php")
    Call<PostResponse> getMyPosts(
            @Query("action") String action,
            @Query("uploader") String uploader,
            @Query("nextToken") int nextToken
    );

    @GET("aimaAppData/api.php")
    Call<VideoResponse> getMyVideos(
            @Query("action") String action,
            @Query("uploader") String uploader,
            @Query("nextToken") int nextToken
    );

    @GET("aimaAppData/api.php")
    Call<VideoResponse> getAllVideo(
            @Query("action") String action,
            @Query("uploader") String uploader,
            @Query("nextToken") int nextToken
    );

    @Multipart
    @POST("aimaAppData/api.php") // Replace with your endpoint
    Call<ImageUploadResponse> uploadImage(
            @Query("action") String action,
            @Query("folder_location") String folder_location,
            @Part MultipartBody.Part image
    );
}
