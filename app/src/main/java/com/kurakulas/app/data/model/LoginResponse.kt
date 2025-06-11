package com.kurakulas.app.data.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("user")
    val user: UserData?
)

data class UserData(
    @SerializedName("id")
    val id: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("mobile")
    val mobile: String,
    @SerializedName("email_id")
    val emailId: String,
    @SerializedName("rank")
    val rank: String,
    @SerializedName("avatar")
    val avatar: String
) 
