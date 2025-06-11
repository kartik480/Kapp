package com.kurakulas.app.ui.addappointment

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.kurakulas.app.data.api.ApiService
import com.kurakulas.app.data.model.PropertyDetailsRequest
import javax.inject.Inject

class AddAppointmentViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _state = MutableStateFlow<AddAppointmentState>(AddAppointmentState.Initial)
    val state: StateFlow<AddAppointmentState> = _state

    fun submitPropertyDetails(request: PropertyDetailsRequest) {
        viewModelScope.launch {
            _state.value = AddAppointmentState.Loading
            try {
                Log.d("AddAppointment", "Submitting property details: $request")
                val response = apiService.addPropertyDetails(request)
                Log.d("AddAppointment", "Response received: $response")
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        _state.value = AddAppointmentState.Success(apiResponse.message ?: "Property details added successfully")
                    } else {
                        _state.value = AddAppointmentState.Error(apiResponse?.message ?: "Failed to add property details")
                    }
                } else {
                    _state.value = AddAppointmentState.Error("Server error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("AddAppointment", "Error submitting property details", e)
                _state.value = AddAppointmentState.Error(e.message ?: "An error occurred")
            }
        }
    }
} 
