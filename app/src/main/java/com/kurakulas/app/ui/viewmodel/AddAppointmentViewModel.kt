package com.kurakulas.app.ui.viewmodel

import com.kurakulas.app.data.model.AddAppointmentRequest

import pzn.api.RetrofitClient

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.kurakulas.app.data.model.State
import com.kurakulas.app.data.model.Location
import com.kurakulas.app.data.model.Sublocation
import com.kurakulas.app.data.repository.LocationRepository
import pzn.api.AppointmentStatusRequest
import pzn.api.PersonalInfoRequest
import pzn.api.PropertyDetailsRequest
import javax.inject.Inject

data class AddAppointmentState(
    val databaseId: Int = 0,
    val mobileNumber: String = "",
    val name: String = "",
    val emailId: String = "",
    val companyName: String = "",
    val alternativeNumber: String = "",
    val selectedState: String = "",
    val selectedLocation: String = "",
    val selectedSubLocation: String = "",
    val selectedPincode: String = "",
    val selectedSource: String = "",
    val visitingCardUri: String? = null,
    val qualifications: String = "",
    val residentialAddress: String = "",
    val selectedCustomerType: String = "",
    // Bank Account Details
    val selectedBankName: String = "",
    val selectedAccountType: String = "",
    val accountNumber: String = "",
    val branchName: String = "",
    val ifscCode: String = "",
    // Loan Details
    val selectedLoanBankName: String = "",
    val selectedLoanType: String = "",
    val loanAmount: String = "",
    val selectedROI: String = "",
    val selectedTenure: String = "",
    val emi: String = "",
    val firstEmiDate: String = "",
    val lastEmiDate: String = "",
    val loanAccountNumber: String = "",
    // Vehicle Details
    val vehicleNumber: String = "",
    val selectedMake: String = "",
    val selectedModel: String = "",
    val selectedManYear: String = "",
    val engineNumber: String = "",
    val chassisNumber: String = "",
    // Property Details
    val selectedPropertyType: String = "",
    val area: String = "",
    val landInSqYards: String = "",
    val sft: String = "",
    val marketValue: String = "",
    // Credit Card Details
    val selectedCreditCardBank: String = "",
    val creditCardLimit: String = "",
    // Appointment Details
    val selectedAppointmentBank: String = "",
    val selectedAppointmentProduct: String = "",
    val selectedAppointmentStatus: String = "",
    val selectedAppointmentSubStatus: String = "",
    val appointmentNote: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddAppointmentViewModel @Inject constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AddAppointmentState())
    val state: StateFlow<AddAppointmentState> = _state.asStateFlow()

    private val _states = MutableStateFlow<List<State>>(emptyList())
    val states: StateFlow<List<State>> = _states.asStateFlow()

    private val _locations = MutableStateFlow<List<Location>>(emptyList())
    val locations: StateFlow<List<Location>> = _locations.asStateFlow()

    private val _sublocations = MutableStateFlow<List<Sublocation>>(emptyList())
    val sublocations: StateFlow<List<Sublocation>> = _sublocations.asStateFlow()

    private val _selectedState = MutableStateFlow<State?>(null)
    val selectedState: StateFlow<State?> = _selectedState.asStateFlow()

    private val _selectedLocation = MutableStateFlow<Location?>(null)
    val selectedLocation: StateFlow<Location?> = _selectedLocation.asStateFlow()

    private val _selectedSublocation = MutableStateFlow<Sublocation?>(null)
    val selectedSublocation: StateFlow<Sublocation?> = _selectedSublocation.asStateFlow()

    init {
        loadStates()
    }

    private fun loadStates() {
        viewModelScope.launch {
            _states.value = locationRepository.getStates()
        }
    }

    fun onStateSelected(state: State) {
        viewModelScope.launch {
            _selectedState.value = state
            _selectedLocation.value = null
            _selectedSublocation.value = null
            val locationsForState = locationRepository.getLocationsForState(state.id)
            _locations.value = locationsForState
            // Only clear sublocations if there are no locations
            if (locationsForState.isEmpty()) {
                _sublocations.value = emptyList()
            }
        }
    }

    fun onLocationSelected(location: Location) {
        viewModelScope.launch {
            _selectedLocation.value = location
            _selectedSublocation.value = null
            val sublocationsForLocation = locationRepository.getSublocationsForLocation(location.id)
            _sublocations.value = sublocationsForLocation
        }
    }

    fun onSublocationSelected(sublocation: Sublocation) {
        _selectedSublocation.value = sublocation
    }

    fun updateMobileNumber(number: String) {
        _state.update { it.copy(mobileNumber = number) }
    }

    fun updateName(name: String) {
        _state.update { it.copy(name = name) }
    }

    fun updateEmailId(email: String) {
        _state.update { it.copy(emailId = email) }
    }

    fun updateCompanyName(company: String) {
        _state.update { it.copy(companyName = company) }
    }

    fun updateAlternativeNumber(number: String) {
        _state.update { it.copy(alternativeNumber = number) }
    }

    fun updateState(state: String) {
        _state.update { it.copy(selectedState = state) }
    }

    fun updateLocation(location: String) {
        _state.update { it.copy(selectedLocation = location) }
    }

    fun updateSubLocation(subLocation: String) {
        _state.update { it.copy(selectedSubLocation = subLocation) }
    }

    fun updatePincode(pincode: String) {
        _state.update { it.copy(selectedPincode = pincode) }
    }

    fun updateSource(source: String) {
        _state.update { it.copy(selectedSource = source) }
    }

    fun updateVisitingCardUri(uri: String?) {
        _state.update { it.copy(visitingCardUri = uri) }
    }

    fun updateQualifications(qualifications: String) {
        _state.update { it.copy(qualifications = qualifications) }
    }

    fun updateResidentialAddress(address: String) {
        _state.update { it.copy(residentialAddress = address) }
    }

    fun updateCustomerType(type: String) {
        _state.update { it.copy(selectedCustomerType = type) }
    }

    // Bank Account Details
    fun updateBankName(bankName: String) {
        _state.update { it.copy(selectedBankName = bankName) }
    }

    fun updateAccountType(accountType: String) {
        _state.update { it.copy(selectedAccountType = accountType) }
    }

    fun updateAccountNumber(accountNumber: String) {
        _state.update { it.copy(accountNumber = accountNumber) }
    }

    fun updateBranchName(branchName: String) {
        _state.update { it.copy(branchName = branchName) }
    }

    fun updateIfscCode(ifscCode: String) {
        _state.update { it.copy(ifscCode = ifscCode) }
    }

    fun addBankAccount() {
        // TODO: Implement bank account addition logic
    }

    // --- APPOINTMENT SUBMISSION ---
    fun submitAppointmentToServer(uniqueId: String, visitingCard: String? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val request = AddAppointmentRequest(
                    database_id = state.value.databaseId,
                    unique_id = uniqueId,
                    mobile_number = state.value.mobileNumber,
                    lead_name = state.value.name,
                    email_id = state.value.emailId,
                    company_name = state.value.companyName,
                    alternative_mobile = state.value.alternativeNumber,
                    state = state.value.selectedState,
                    location = state.value.selectedLocation,
                    sub_location = state.value.selectedSubLocation,
                    pin_code = state.value.selectedPincode,
                    source = state.value.selectedSource,
                    visiting_card = visitingCard,
                    user_qualification = state.value.qualifications,
                    residental_address = state.value.residentialAddress,
                    customer_type = state.value.selectedCustomerType
                )
                val response = RetrofitClient.apiService.addAppointment(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _state.update { it.copy(isLoading = false, error = null) }
                } else {
                    _state.update { it.copy(isLoading = false, error = response.body()?.message ?: "Failed to add appointment") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "An error occurred") }
            }
        }
    }

    // Loan Details
    fun updateLoanBankName(bankName: String) {
        _state.update { it.copy(selectedLoanBankName = bankName) }
    }

    fun updateLoanType(loanType: String) {
        _state.update { it.copy(selectedLoanType = loanType) }
    }

    fun updateLoanAmount(amount: String) {
        _state.update { it.copy(loanAmount = amount) }
    }

    fun updateROI(roi: String) {
        _state.update { it.copy(selectedROI = roi) }
    }

    fun updateTenure(tenure: String) {
        _state.update { it.copy(selectedTenure = tenure) }
    }

    fun updateEMI(emi: String) {
        _state.update { it.copy(emi = emi) }
    }

    fun updateFirstEmiDate(date: String) {
        _state.update { it.copy(firstEmiDate = date) }
    }

    fun updateLastEmiDate(date: String) {
        _state.update { it.copy(lastEmiDate = date) }
    }

    fun updateLoanAccountNumber(accountNumber: String) {
        _state.update { it.copy(loanAccountNumber = accountNumber) }
    }

    fun addLoanDetails() {
        // TODO: Implement loan details addition logic
    }

    fun submitAppointment() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                
                // Create personal info request
                val personalInfoRequest = PersonalInfoRequest(
                    mobile_number = state.value.mobileNumber,
                    lead_name = state.value.name,
                    email_id = state.value.emailId,
                    company_name = state.value.companyName,
                    alternative_mobile = state.value.alternativeNumber,
                    state = state.value.selectedState,
                    location = state.value.selectedLocation,
                    sub_location = state.value.selectedSubLocation,
                    pin_code = state.value.selectedPincode,
                    source = state.value.selectedSource,
                    visiting_card = state.value.visitingCardUri ?: "",
                    user_qualification = state.value.qualifications,
                    residental_address = state.value.residentialAddress,
                    customer_type = state.value.selectedCustomerType
                )
                
                // Create appointment status request
                val appointmentStatusRequest = AppointmentStatusRequest(
                    appt_id = 0, // Default to 0 since we're handling ID in the panel
                    appt_bank = state.value.selectedAppointmentBank,
                    appt_product = state.value.selectedAppointmentProduct,
                    appt_status = state.value.selectedAppointmentStatus,
                    appt_sub_status = state.value.selectedAppointmentSubStatus,
                    notes = state.value.appointmentNote
                )
                
                // Make API calls
                val personalInfoResponse = RetrofitClient.apiService.addPersonalInfo(personalInfoRequest)
                if (personalInfoResponse.isSuccessful) {
                    val appointmentStatusResponse = RetrofitClient.apiService.addAppointmentStatus(appointmentStatusRequest)
                    if (!appointmentStatusResponse.isSuccessful) {
                        throw Exception("Failed to add appointment status")
                    }
                } else {
                    throw Exception("Failed to add personal info")
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    // Vehicle Details
    fun updateVehicleNumber(number: String) {
        _state.update { it.copy(vehicleNumber = number) }
    }

    fun updateMake(make: String) {
        _state.update { it.copy(selectedMake = make) }
    }

    fun updateModel(model: String) {
        _state.update { it.copy(selectedModel = model) }
    }

    fun updateManYear(year: String) {
        _state.update { it.copy(selectedManYear = year) }
    }

    fun updateEngineNumber(number: String) {
        _state.update { it.copy(engineNumber = number) }
    }

    fun updateChassisNumber(number: String) {
        _state.update { it.copy(chassisNumber = number) }
    }

    fun addVehicleDetails() {
        // TODO: Implement vehicle details addition logic
    }

    // Property Details
    fun updatePropertyType(type: String) {
        _state.update { it.copy(selectedPropertyType = type) }
    }

    fun updateArea(area: String) {
        _state.update { it.copy(area = area) }
    }

    fun updateLandInSqYards(land: String) {
        _state.update { it.copy(landInSqYards = land) }
    }

    fun updateSFT(sft: String) {
        _state.update { it.copy(sft = sft) }
    }

    fun updateMarketValue(value: String) {
        _state.update { it.copy(marketValue = value) }
    }

    fun addPropertyDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                Log.d("AddAppointment", "Current database_id in state: ${state.value.databaseId}")
                val request = PropertyDetailsRequest(
                    database_id = state.value.databaseId,
                    p_property_type = state.value.selectedPropertyType,
                    p_area = state.value.area,
                    p_lands = state.value.landInSqYards,
                    p_sft = state.value.sft,
                    p_market_value = state.value.marketValue
                )
                Log.d("AddAppointment", "Created PropertyDetailsRequest with database_id: ${request.database_id}")
                
                val response = RetrofitClient.apiService.addPropertyDetails(request)
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.status == "success") {
                        // Clear the form after successful submission
                        _state.update { 
                            it.copy(
                                selectedPropertyType = "",
                                area = "",
                                landInSqYards = "",
                                sft = "",
                                marketValue = "",
                                isLoading = false
                            )
                        }
                        Log.d("AddAppointment", "Property details added successfully. Insert ID: ${apiResponse.insert_id}")
                    } else {
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                error = apiResponse?.message ?: "Failed to add property details"
                            )
                        }
                        Log.e("AddAppointment", "Failed to add property details: ${apiResponse?.message}")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("AddAppointment", "Server error: ${response.code()}, Body: $errorBody")
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = "Server error: ${response.code()}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("AddAppointment", "Error adding property details", e)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "An error occurred"
                    )
                }
            }
        }
    }

    // Credit Card Details
    fun updateCreditCardBank(bank: String) {
        _state.update { it.copy(selectedCreditCardBank = bank) }
    }

    fun updateCreditCardLimit(limit: String) {
        _state.update { it.copy(creditCardLimit = limit) }
    }

    fun addCreditCardDetails() {
        // TODO: Implement credit card details addition logic
    }

    // Appointment Details
    fun updateAppointmentBank(bank: String) {
        _state.update { it.copy(selectedAppointmentBank = bank) }
    }

    fun updateAppointmentProduct(product: String) {
        _state.update { it.copy(selectedAppointmentProduct = product) }
    }

    fun updateAppointmentStatus(status: String) {
        _state.update { it.copy(selectedAppointmentStatus = status) }
    }

    fun updateAppointmentSubStatus(subStatus: String) {
        _state.update { it.copy(selectedAppointmentSubStatus = subStatus) }
    }

    fun updateAppointmentNote(note: String) {
        _state.update { it.copy(appointmentNote = note) }
    }

    fun setDatabaseId(id: Int) {
        Log.d("AddAppointment", "Setting database_id in ViewModel: $id")
        _state.update { it.copy(databaseId = id) }
    }
} 
