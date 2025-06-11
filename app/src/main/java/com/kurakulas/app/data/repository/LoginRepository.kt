package com.kurakulas.app.data.repository

import android.util.Log
import com.kurakulas.app.data.local.SessionManager
import com.kurakulas.app.data.model.LoginResponse
import com.kurakulas.app.data.remote.LoginApi
import javax.inject.Inject

class LoginRepository @Inject constructor(
    private val loginApi: LoginApi,
    private val sessionManager: SessionManager
) {
    suspend fun login(username: String, password: String): LoginResponse {
        Log.d("LoginRepository", "=== Making API Request ===")
        Log.d("LoginRepository", "Username: $username")
        Log.d("LoginRepository", "Password: $password")
        Log.d("LoginRepository", "API Endpoint: login.php")
        
        val response = loginApi.login(username, password)
        
        Log.d("LoginRepository", "=== API Response ===")
        Log.d("LoginRepository", "Status: ${response.status}")
        Log.d("LoginRepository", "User ID: ${response.user?.id}")
        Log.d("LoginRepository", "Username: ${response.user?.username}")
        Log.d("LoginRepository", "First Name: ${response.user?.firstName}")
        Log.d("LoginRepository", "Last Name: ${response.user?.lastName}")
        Log.d("LoginRepository", "Email: ${response.user?.emailId}")
        Log.d("LoginRepository", "Mobile: ${response.user?.mobile}")
        Log.d("LoginRepository", "Rank: ${response.user?.rank}")
        
        // Save login response to session
        sessionManager.saveLoginResponse(response)
        
        return response
    }

    fun getStoredLoginResponse(): LoginResponse? {
        return sessionManager.getLoginResponse()
    }

    fun isLoggedIn(): Boolean {
        return sessionManager.isLoggedIn()
    }

    fun logout() {
        sessionManager.clearSession()
    }
} 
