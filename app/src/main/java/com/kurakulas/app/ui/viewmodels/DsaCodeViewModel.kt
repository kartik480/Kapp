package com.kurakulas.app.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pzn.api.ApiService
import pzn.api.DsaDropdownOptions
import pzn.api.DsaCodeData
import javax.inject.Inject

data class DsaCodeState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val vendorBanks: List<String> = emptyList(),
    val loanTypes: List<String> = emptyList(),
    val branchStates: List<String> = emptyList(),
    val branchLocations: List<String> = emptyList(),
    val dsaCodes: List<DsaCodeData> = emptyList()
)

@HiltViewModel
class DsaCodeViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _state = MutableStateFlow(DsaCodeState())
    val state: StateFlow<DsaCodeState> = _state.asStateFlow()

    init {
        loadDropdownOptions()
    }

    fun loadDropdownOptions() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                Log.d("DsaCodeViewModel", "Loading dropdown options...")
                val response = apiService.getDsaDropdownOptions()
                Log.d("DsaCodeViewModel", "Dropdown options response code: ${response.code()}")
                Log.d("DsaCodeViewModel", "Dropdown options response body: ${response.body()}")
                
                if (response.isSuccessful) {
                    response.body()?.data?.let { options ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            vendorBanks = options.vendor_banks,
                            loanTypes = options.loan_types,
                            branchStates = options.branch_states,
                            branchLocations = options.branch_locations
                        )
                        Log.d("DsaCodeViewModel", "Successfully loaded dropdown options")
                    }
                } else {
                    Log.e("DsaCodeViewModel", "Failed to load dropdown options: ${response.code()}")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Failed to load dropdown options"
                    )
                }
            } catch (e: Exception) {
                Log.e("DsaCodeViewModel", "Error loading dropdown options", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred"
                )
            }
        }
    }

    fun filterDsaCodes(
        vendorBank: String,
        loanType: String,
        state: String,
        location: String
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                Log.d("DsaCodeViewModel", "=== Starting DSA Code Filter ===")
                Log.d("DsaCodeViewModel", "Filter parameters:")
                Log.d("DsaCodeViewModel", "Vendor Bank: $vendorBank")
                Log.d("DsaCodeViewModel", "Loan Type: $loanType")
                Log.d("DsaCodeViewModel", "State: $state")
                Log.d("DsaCodeViewModel", "Location: $location")

                val response = apiService.getFilteredDsaCodes(
                    vendorBank = vendorBank,
                    loanType = loanType,
                    state = state,
                    location = location
                )

                Log.d("DsaCodeViewModel", "API Response Code: ${response.code()}")
                Log.d("DsaCodeViewModel", "API Response Body: ${response.body()}")
                Log.d("DsaCodeViewModel", "API Response Headers: ${response.headers()}")

                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.success) {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                dsaCodes = apiResponse.data
                            )
                            Log.d("DsaCodeViewModel", "Successfully loaded ${apiResponse.data.size} DSA codes")
                        } else {
                            Log.e("DsaCodeViewModel", "API returned success=false: ${apiResponse.message}")
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = apiResponse.message
                            )
                        }
                    } ?: run {
                        Log.e("DsaCodeViewModel", "Response body is null")
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "No data received from server"
                        )
                    }
                } else {
                    Log.e("DsaCodeViewModel", "API call failed with code ${response.code()}")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Failed to load DSA codes: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                Log.e("DsaCodeViewModel", "Exception occurred while filtering DSA codes", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred"
                )
            }
        }
    }
} 
