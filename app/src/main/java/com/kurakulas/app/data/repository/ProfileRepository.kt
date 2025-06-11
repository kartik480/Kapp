package com.kurakulas.app.data.repository

import android.content.Context
import com.kurakulas.app.data.api.ProfileApi
import com.kurakulas.app.data.model.UserProfile
import com.kurakulas.app.data.local.SessionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val profileApi: ProfileApi,
    private val sessionManager: SessionManager
) {
    suspend fun getUserProfile(): UserProfile {
        // Get user ID from stored login response
        val loginResponse = sessionManager.getLoginResponse()
        val userId = loginResponse?.user?.id ?: throw IOException("User not logged in")

        return try {
            val response = profileApi.getUserProfile(userId)
            if (response.success && response.data != null) {
                response.data
            } else {
                throw IOException(response.message ?: "Failed to fetch profile")
            }
        } catch (e: HttpException) {
            when (e.code()) {
                500 -> throw IOException("Server error: Please try again later")
                404 -> throw IOException("Profile not found")
                else -> throw IOException("Failed to fetch profile: ${e.message()}")
            }
        } catch (e: IOException) {
            throw IOException("Network error: Please check your connection")
        } catch (e: Exception) {
            throw IOException("Unexpected error: ${e.message}")
        }
    }
} 