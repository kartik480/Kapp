package com.kurakulas.app.data.model

import com.google.gson.annotations.SerializedName

data class ChangePasswordResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String?
) 