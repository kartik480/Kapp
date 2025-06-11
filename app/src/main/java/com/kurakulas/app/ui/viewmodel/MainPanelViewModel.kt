package com.kurakulas.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kurakulas.app.data.api.ChangePasswordApi
import com.kurakulas.app.data.model.ChangePasswordResponse
import com.kurakulas.app.data.model.LoginResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.kurakulas.app.data.repository.LoginRepository
import com.kurakulas.app.data.local.AppointmentPointsManager
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.kurakulas.app.data.api.ApiConfig

@HiltViewModel
class MainPanelViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val appointmentPointsManager: AppointmentPointsManager
) : ViewModel() {
    private val _userFirstName = MutableStateFlow<String?>(null)
    val userFirstName: StateFlow<String?> = _userFirstName.asStateFlow()

    private val _userLastName = MutableStateFlow<String?>(null)
    val userLastName: StateFlow<String?> = _userLastName.asStateFlow()

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Add state flows for appointment points
    private val _salPoints = MutableStateFlow(0)
    val salPoints: StateFlow<Int> = _salPoints.asStateFlow()

    private val _senpPoints = MutableStateFlow(0)
    val senpPoints: StateFlow<Int> = _senpPoints.asStateFlow()

    private val _sepPoints = MutableStateFlow(0)
    val sepPoints: StateFlow<Int> = _sepPoints.asStateFlow()

    private val _nriPoints = MutableStateFlow(0)
    val nriPoints: StateFlow<Int> = _nriPoints.asStateFlow()

    private val _educationalPoints = MutableStateFlow(0)
    val educationalPoints: StateFlow<Int> = _educationalPoints.asStateFlow()

    init {
        // Check for existing session
        val storedLoginResponse = loginRepository.getStoredLoginResponse()
        if (storedLoginResponse != null) {
            updateUserData(storedLoginResponse)
        } else {
            clearUserData()
        }

        // Load saved points
        loadPoints()
    }

    private fun loadPoints() {
        _salPoints.value = appointmentPointsManager.getSalPoints()
        _senpPoints.value = appointmentPointsManager.getSenpPoints()
        _sepPoints.value = appointmentPointsManager.getSepPoints()
        _nriPoints.value = appointmentPointsManager.getNriPoints()
        _educationalPoints.value = appointmentPointsManager.getEducationalPoints()
    }

    fun incrementPoints(type: String) {
        when (type) {
            "Salaried-SAL" -> {
                appointmentPointsManager.incrementSalPoints()
                _salPoints.value = appointmentPointsManager.getSalPoints()
            }
            "Self Employed Non Professionals-SENP" -> {
                appointmentPointsManager.incrementSenpPoints()
                _senpPoints.value = appointmentPointsManager.getSenpPoints()
            }
            "Self Employed Professionals-SEP" -> {
                appointmentPointsManager.incrementSepPoints()
                _sepPoints.value = appointmentPointsManager.getSepPoints()
            }
            "NRI" -> {
                appointmentPointsManager.incrementNriPoints()
                _nriPoints.value = appointmentPointsManager.getNriPoints()
            }
            "Educational" -> {
                appointmentPointsManager.incrementEducationalPoints()
                _educationalPoints.value = appointmentPointsManager.getEducationalPoints()
            }
        }
    }

    fun updateUserData(loginResponse: LoginResponse) {
        Log.d("MainPanelViewModel", "Updating user data with response: $loginResponse")
        loginResponse.user?.let { user ->
            _userFirstName.value = user.firstName
            _userLastName.value = user.lastName
            _userEmail.value = user.emailId
            _userId.value = user.id
            Log.d("MainPanelViewModel", "Updated user data - First Name: ${user.firstName}, Last Name: ${user.lastName}, Email: ${user.emailId}, ID: ${user.id}")
        } ?: run {
            Log.e("MainPanelViewModel", "User data is null in the response")
        }
    }

    fun clearUserData() {
        _userFirstName.value = null
        _userLastName.value = null
        _userEmail.value = null
        _userId.value = null
        Log.d("MainPanelViewModel", "Cleared user data")
    }

    fun logout() {
        viewModelScope.launch {
            try {
                loginRepository.logout()
                clearUserData()
                Log.d("MainPanelViewModel", "Logout successful")
            } catch (e: Exception) {
                Log.e("MainPanelViewModel", "Logout failed", e)
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    val retrofit = Retrofit.Builder()
                        .baseUrl(ApiConfig.BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

                    val api = retrofit.create(ChangePasswordApi::class.java)
                    api.changePassword(
                        userId = _userId.value ?: throw Exception("User ID not found"),
                        currentPassword = currentPassword,
                        newPassword = newPassword
                    )
                }

                if (response.success) {
                    _uiState.value = _uiState.value.copy(
                        message = response.message ?: "Password changed successfully"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        message = response.message ?: "Failed to change password"
                    )
                }
            } catch (e: Exception) {
                Log.e("MainPanelViewModel", "Password change failed", e)
                _uiState.value = _uiState.value.copy(
                    message = "Failed to change password: ${e.message}"
                )
            }
        }
    }
}

data class UiState(
    val message: String? = null
) 
