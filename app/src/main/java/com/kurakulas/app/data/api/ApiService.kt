package com.kurakulas.app.data.api

import com.kurakulas.app.data.model.AddAppointmentRequest

import com.kurakulas.app.data.model.LoginRequest
import com.kurakulas.app.data.model.LoginResponse
import com.kurakulas.app.data.model.PropertyDetailsRequest
import com.kurakulas.app.data.model.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("login.php")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("add_property_details.php")
    suspend fun addPropertyDetails(@Body request: PropertyDetailsRequest): Response<ApiResponse<Unit>>

    @POST("add_appointment.php")
    suspend fun addAppointment(@Body request: AddAppointmentRequest): Response<ApiResponse<Map<String, Any>>>
} 
