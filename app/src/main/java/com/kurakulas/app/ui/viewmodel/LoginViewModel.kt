package com.kurakulas.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.kurakulas.app.data.model.LoginResponse
import com.kurakulas.app.data.repository.LoginRepository
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository
) : ViewModel() {

    private val _loginResult = MutableStateFlow<LoginResult>(LoginResult.Initial)
    val loginResult: StateFlow<LoginResult> = _loginResult

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                Log.d("LoginViewModel", "=== Login Attempt ===")
                Log.d("LoginViewModel", "Username: $username")
                Log.d("LoginViewModel", "Password length: ${password.length}")
                
                val response = loginRepository.login(username, password)
                Log.d("LoginViewModel", "=== Server Response ===")
                Log.d("LoginViewModel", "Status: ${response.status}")
                Log.d("LoginViewModel", "User ID: ${response.user?.id}")
                Log.d("LoginViewModel", "Username from response: ${response.user?.username}")
                Log.d("LoginViewModel", "Full name: ${response.user?.firstName} ${response.user?.lastName}")
                
                if (response.status == "success" && response.user != null) {
                    if (response.user.firstName.isNotEmpty() && response.user.lastName.isNotEmpty()) {
                        Log.d("LoginViewModel", "Login successful for user: ${response.user.firstName} ${response.user.lastName}")
                        _loginResult.value = LoginResult.Success(response)
                    } else {
                        Log.e("LoginViewModel", "Invalid user data received - missing name information")
                        _loginResult.value = LoginResult.Error("Invalid user data")
                    }
                } else {
                    Log.e("LoginViewModel", "Login failed. Status: ${response.status}, User: ${response.user}")
                    _loginResult.value = LoginResult.Error("Invalid credentials")
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Login error: ${e.message}", e)
                _loginResult.value = LoginResult.Error(e.message ?: "An error occurred")
            }
        }
    }

    sealed class LoginResult {
        object Initial : LoginResult()
        data class Success(val response: LoginResponse) : LoginResult()
        data class Error(val message: String) : LoginResult()
    }
} 
