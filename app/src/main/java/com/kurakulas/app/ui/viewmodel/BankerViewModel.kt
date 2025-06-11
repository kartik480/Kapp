package com.kurakulas.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.kurakulas.app.data.BankerRepository
import com.kurakulas.app.data.Result
import com.kurakulas.app.ui.BankerData
import javax.inject.Inject

data class BankerState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val vendorBanks: List<String> = emptyList(),
    val loanTypes: List<String> = emptyList(),
    val branchStates: List<String> = emptyList(),
    val branchLocations: List<String> = emptyList(),
    val filteredBankers: List<BankerData> = emptyList(),
    val designations: List<String> = emptyList(),
    val isAddingBanker: Boolean = false,
    val addBankerError: String? = null
)

@HiltViewModel
class BankerViewModel @Inject constructor(
    private val repository: BankerRepository
) : ViewModel() {
    private val TAG = "BankerViewModel"
    private val _state = MutableStateFlow(BankerState())
    val state: StateFlow<BankerState> = _state.asStateFlow()

    init {
        loadVendorBanks()
        loadLoanTypes()
        loadBranchStates()
        loadBranchLocations()
        loadDesignations()
    }

    fun loadVendorBanks() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getVendorBanks()) {
                is Result.Success -> {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            vendorBanks = result.data,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    fun loadLoanTypes() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getLoanTypes()) {
                is Result.Success -> {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            loanTypes = result.data,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    fun loadBranchStates() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getBranchStates()) {
                is Result.Success -> {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            branchStates = result.data,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    fun loadBranchLocations() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getBranchLocations()) {
                is Result.Success -> {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            branchLocations = result.data,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    fun loadDesignations() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getBankerDesignations()) {
                is Result.Success -> {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            designations = result.data,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    fun filterBankers(
        vendorBank: String,
        loanType: String,
        state: String,
        location: String
    ) {
        viewModelScope.launch {
            Log.d(TAG, "Starting filterBankers with params: vendorBank=$vendorBank, loanType=$loanType, state=$state, location=$location")
            _state.update { 
                Log.d(TAG, "Updating state to loading")
                it.copy(isLoading = true, error = null) 
            }
            
            try {
                when (val result = repository.getFilteredBankers(
                    vendorBank,
                    loanType,
                    state,
                    location
                )) {
                    is Result.Success -> {
                        Log.d(TAG, "Received ${result.data.size} bankers from repository")
                        val mappedBankers = result.data.map { banker ->
                            Log.d(TAG, "Mapping banker: ${banker.bankerName}")
                            BankerData(
                                id = banker.id,
                                vendorBank = banker.vendorBank,
                                bankerName = banker.bankerName,
                                bankerDesignation = banker.bankerDesignation,
                                mobileNumber = banker.phoneNumber,
                                email = banker.emailId,
                                loanType = banker.loanType,
                                state = banker.state,
                                location = banker.location,
                                visitingCard = banker.visitingCard,
                                address = banker.address
                            )
                        }
                        _state.update { 
                            Log.d(TAG, "Updating state with ${mappedBankers.size} bankers")
                            it.copy(
                                isLoading = false,
                                filteredBankers = mappedBankers,
                                error = null
                            )
                        }
                    }
                    is Result.Error -> {
                        Log.e(TAG, "Error filtering bankers: ${result.message}")
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in filterBankers", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    fun addBanker(
        vendorBank: String,
        bankerName: String,
        phoneNumber: String,
        emailId: String,
        bankerDesignation: String,
        loanType: String,
        state: String,
        location: String,
        visitingCard: String,
        address: String
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isAddingBanker = true, addBankerError = null) }
            try {
                // Call repository to add banker
                val result = repository.addBanker(
                    vendorBank = vendorBank,
                    bankerName = bankerName,
                    phoneNumber = phoneNumber,
                    emailId = emailId,
                    bankerDesignation = bankerDesignation,
                    loanType = loanType,
                    state = state,
                    location = location,
                    visitingCard = visitingCard,
                    address = address
                )
                
                when (result) {
                    is Result.Success -> {
                        _state.update { 
                            it.copy(
                                isAddingBanker = false,
                                addBankerError = null
                            )
                        }
                    }
                    is Result.Error -> {
                        _state.update { 
                            it.copy(
                                isAddingBanker = false,
                                addBankerError = result.message
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isAddingBanker = false,
                        addBankerError = e.message ?: "Failed to add banker"
                    )
                }
            }
        }
    }
} 
