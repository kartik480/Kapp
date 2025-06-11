package com.kurakulas.app.data.api

import com.kurakulas.app.data.model.UserProfileResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ProfileApi {
    @FormUrlEncoded
    @POST("get_user_profile.php")
    suspend fun getUserProfile(
        @Field("user_id") userId: String
    ): UserProfileResponse
} 