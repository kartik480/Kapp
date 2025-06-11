package com.kurakulas.app.data.api

import com.kurakulas.app.data.model.ChangePasswordResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ChangePasswordApi {
    @FormUrlEncoded
    @POST("change_password.php")
    suspend fun changePassword(
        @Field("user_id") userId: String,
        @Field("current_password") currentPassword: String,
        @Field("new_password") newPassword: String
    ): ChangePasswordResponse
} 