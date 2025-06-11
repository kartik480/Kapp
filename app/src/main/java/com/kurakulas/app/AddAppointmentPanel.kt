package pzn

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import pzn.api.RetrofitClient
import pzn.api.AppointmentRequest
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.ContentResolver
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import pzn.api.BankAccountRequest
import pzn.api.BankAccountResponse
import pzn.api.RelationshipWithBankRequest
import pzn.api.RelationshipWithBankDetails
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import pzn.api.VehicleDetailsRequest
import androidx.compose.foundation.background
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import pzn.api.CreditCardDetailsRequest
import com.kurakulas.app.data.api.ApiConfig
import pzn.api.PropertyDetailsRequest
import pzn.api.AppointmentStatusRequest

// Helper functions for file handling
private fun getFileName(contentResolver: ContentResolver, uri: Uri): String? {
    val cursor = contentResolver.query(uri, null, null, null, null)
    return cursor?.use {
        val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        it.moveToFirst()
        it.getString(nameIndex)
    }
}

private fun getFileExtension(contentResolver: ContentResolver, uri: Uri): String? {
    val mime = MimeTypeMap.getSingleton()
    return contentResolver.getType(uri)?.let { mime.getExtensionFromMimeType(it) }
}

private fun uriToFile(context: Context, uri: Uri, fileName: String?): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, fileName ?: "temp_file")
    FileOutputStream(file).use { outputStream ->
        inputStream?.copyTo(outputStream)
    }
    return file
}

// Add this data class after the existing AppointmentData class
data class BankAccountDetails(
    val bankName: String,
    val accountType: String,
    val accountNumber: String,
    val branchName: String,
    val ifscCode: String
)

// Add this data class after the existing data classes
data class VehicleDetails(
    val vehicleNumber: String,
    val vehicleMake: String,
    val vehicleModel: String,
    val manufacturingYear: String,
    val engineNumber: String,
    val chassisNumber: String
)

// Add this data class after the existing data classes
data class PropertyDetails(
    val propertyType: String,
    val area: String,
    val lands: String,
    val sft: String,
    val marketValue: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppointmentPanel(
    onNavigateBack: () -> Unit,
    context: Context,
    onPointsIncrement: (String) -> Unit = {} // Add callback for point increment
) {
    // Helper function for file handling
    fun getAppointmentFileName(contentResolver: ContentResolver, uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            it.getString(nameIndex)
        }
    }

    var creditCardBank by remember { mutableStateOf("") }
    var creditLimit by remember { mutableStateOf("") }
    var appointmentBank by remember { mutableStateOf("") }
    var appointmentProduct by remember { mutableStateOf("") }
    var appointmentStatus by remember { mutableStateOf("") }
    var appointmentSubStatus by remember { mutableStateOf("") }
    var appointmentNote by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    
    // Add state for customer types
    var customerTypes by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingCustomerTypes by remember { mutableStateOf(true) }
    var customerTypeError by remember { mutableStateOf<String?>(null) }

    // Add state for relationship with banks
    var relationshipWithBanks by remember { mutableStateOf<List<RelationshipWithBankDetails>>(emptyList()) }

    // Add state for states
    var states by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingStates by remember { mutableStateOf(true) }
    var statesError by remember { mutableStateOf<String?>(null) }
    var selectedState by remember { mutableStateOf("") }

    // Add state for locations
    var locations by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingLocations by remember { mutableStateOf(true) }
    var locationsError by remember { mutableStateOf<String?>(null) }
    var selectedLocation by remember { mutableStateOf("") }

    // Add state for sublocations
    var subLocations by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingSublocations by remember { mutableStateOf(true) }
    var sublocationsError by remember { mutableStateOf<String?>(null) }

    // Add state for pincodes
    var pincodes by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingPincodes by remember { mutableStateOf(true) }
    var pincodesError by remember { mutableStateOf<String?>(null) }
    var pincodeExpanded by remember { mutableStateOf(false) }

    // Add state for visiting card
    var visitingCardUri by remember { mutableStateOf<Uri?>(null) }
    var visitingCardFileName by remember { mutableStateOf<String?>(null) }

    // Add state for banks
    var banks by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingBanks by remember { mutableStateOf(true) }
    var banksError by remember { mutableStateOf<String?>(null) }
    var bankNameExpanded by remember { mutableStateOf(false) }

    // Add state for portfolio banks
    var portfolioBanks by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingPortfolioBanks by remember { mutableStateOf(true) }
    var portfolioBanksError by remember { mutableStateOf<String?>(null) }
    var loanBankNameExpanded by remember { mutableStateOf(false) }

    // Add state for loan types
    var loanTypes by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingLoanTypes by remember { mutableStateOf(true) }
    var loanTypesError by remember { mutableStateOf<String?>(null) }
    var loanTypeExpanded by remember { mutableStateOf(false) }

    // Add state for ROI options
    var roiOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingRoiOptions by remember { mutableStateOf(true) }
    var roiOptionsError by remember { mutableStateOf<String?>(null) }
    var roiExpanded by remember { mutableStateOf(false) }

    // Add state for Tenure options
    var tenureOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingTenureOptions by remember { mutableStateOf(true) }
    var tenureOptionsError by remember { mutableStateOf<String?>(null) }
    var tenureExpanded by remember { mutableStateOf(false) }

    // Add state for bank account details
    var bankAccounts by remember { mutableStateOf<List<BankAccountDetails>>(emptyList()) }

    // Add state for vehicle details
    var vehicleDetails by remember { mutableStateOf<List<VehicleDetails>>(emptyList()) }
    var isAddingVehicle by remember { mutableStateOf(false) }

    // Add state for property types
    var propertyTypes by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingPropertyTypes by remember { mutableStateOf(true) }
    var propertyTypesError by remember { mutableStateOf<String?>(null) }
    var propertyTypeExpanded by remember { mutableStateOf(false) }

    // Add state for appointment banks
    var appointmentBanks by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingAppointmentBanks by remember { mutableStateOf(true) }
    var appointmentBanksError by remember { mutableStateOf<String?>(null) }

    // Add state for appointment products
    var appointmentProducts by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingAppointmentProducts by remember { mutableStateOf(true) }
    var appointmentProductsError by remember { mutableStateOf<String?>(null) }

    // Add state for appointment statuses
    var appointmentStatuses by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingAppointmentStatuses by remember { mutableStateOf(true) }
    var appointmentStatusesError by remember { mutableStateOf<String?>(null) }

    // Add state for appointment sub statuses
    var appointmentSubStatuses by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingAppointmentSubStatuses by remember { mutableStateOf(true) }
    var appointmentSubStatusesError by remember { mutableStateOf<String?>(null) }

    // Add state variables for appointment dropdowns
    var appointmentBankExpanded by remember { mutableStateOf(false) }
    var appointmentProductExpanded by remember { mutableStateOf(false) }
    var appointmentStatusExpanded by remember { mutableStateOf(false) }
    var appointmentSubStatusExpanded by remember { mutableStateOf(false) }

    // Add state for bank account types
    var bankAccountTypes by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingBankAccountTypes by remember { mutableStateOf(true) }
    var bankAccountTypesError by remember { mutableStateOf<String?>(null) }

    // Add state for property details
    var propertyDetails by remember { mutableStateOf<List<PropertyDetails>>(emptyList()) }
    var isAddingProperty by remember { mutableStateOf(false) }
    var selectedPropertyType by remember { mutableStateOf("") }
    var propertyArea by remember { mutableStateOf("") }
    var propertyLands by remember { mutableStateOf("") }
    var propertySft by remember { mutableStateOf("") }
    var propertyMarketValue by remember { mutableStateOf("") }
    var databaseId by remember { mutableStateOf(0) }

    // Replace the second propertyArea declaration with propertyAreaInput
    var propertyAreaInput by remember { mutableStateOf("") }

    // Add state for customer type
    var
            customerType by remember { mutableStateOf("") }
    var customerTypeExpanded by remember { mutableStateOf(false) }

    // Fetch data when the panel is first displayed
    LaunchedEffect(Unit) {
        // Fetch bank account types
        try {
            Log.d("BankAccountTypesAPI", "=== Starting Bank Account Types Fetch ===")
            Log.d("BankAccountTypesAPI", "Making API call to get_bank_account_types.php...")
            Log.d("BankAccountTypesAPI", "Base URL: ${ApiConfig.BASE_URL}")
            Log.d("BankAccountTypesAPI", "Full URL: ${ApiConfig.BASE_URL}get_bank_account_types.php")
            
            val response = RetrofitClient.apiService.getBankAccountTypes()
            Log.d("BankAccountTypesAPI", "API Response Code: ${response.code()}")
            Log.d("BankAccountTypesAPI", "API Response Body: ${response.body()}")
            Log.d("BankAccountTypesAPI", "API Response Headers: ${response.headers()}")
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    Log.d("BankAccountTypesAPI", "Successfully fetched bank account types")
                    Log.d("BankAccountTypesAPI", "Number of account types: ${apiResponse.data.size}")
                    Log.d("BankAccountTypesAPI", "Account types: ${apiResponse.data}")
                    bankAccountTypes = apiResponse.data
                } else {
                    Log.e("BankAccountTypesAPI", "API returned success=false")
                    Log.e("BankAccountTypesAPI", "Error message: ${apiResponse?.message}")
                    bankAccountTypesError = apiResponse?.message ?: "Failed to load bank account types"
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("BankAccountTypesAPI", "=== Bank Account Types Fetch Failed ===")
                Log.e("BankAccountTypesAPI", "Error Response Code: ${response.code()}")
                Log.e("BankAccountTypesAPI", "Error Response Body: $errorBody")
                Log.e("BankAccountTypesAPI", "Error Headers: ${response.headers()}")
                Log.e("BankAccountTypesAPI", "Error Message: ${response.message()}")
                
                bankAccountTypesError = when (response.code()) {
                    404 -> "Bank account types endpoint not found. Please check server configuration."
                    500 -> "Server error occurred. Please try again later."
                    else -> "Failed to load bank account types: ${response.code()}"
                }
            }
        } catch (e: Exception) {
            Log.e("BankAccountTypesAPI", "=== Exception in Bank Account Types Fetch ===")
            Log.e("BankAccountTypesAPI", "Exception Type: ${e.javaClass.simpleName}")
            Log.e("BankAccountTypesAPI", "Exception Message: ${e.message}")
            Log.e("BankAccountTypesAPI", "Stack Trace:", e)
            
            bankAccountTypesError = when (e) {
                is java.net.UnknownHostException -> "Cannot connect to server. Please check your internet connection."
                is java.net.SocketTimeoutException -> "Connection timed out. Please try again."
                else -> "Error: ${e.message}"
            }
        } finally {
            isLoadingBankAccountTypes = false
            Log.d("BankAccountTypesAPI", "=== Bank Account Types Fetch Completed ===")
        }

        // Fetch customer types
        try {
            val response = RetrofitClient.apiService.getCustomerTypes()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    customerTypes = apiResponse.data
                } else {
                    customerTypeError = "Failed to load customer types"
                }
            } else {
                customerTypeError = "Error: ${response.code()}"
            }
        } catch (e: Exception) {
            customerTypeError = "Error: ${e.message}"
        } finally {
            isLoadingCustomerTypes = false
        }

        // Fetch states
        try {
            val response = RetrofitClient.apiService.getStates()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    states = apiResponse.data
                } else {
                    statesError = "Failed to load states"
                }
            } else {
                statesError = "Error: ${response.code()}"
            }
        } catch (e: Exception) {
            statesError = "Error: ${e.message}"
        } finally {
            isLoadingStates = false
        }

        // Fetch locations
        try {
            val response = RetrofitClient.apiService.getLocations(selectedState)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    locations = apiResponse.data
                } else {
                    locationsError = "Failed to load locations"
                }
            } else {
                locationsError = "Error: ${response.code()}"
            }
        } catch (e: Exception) {
            locationsError = "Error: ${e.message}"
        } finally {
            isLoadingLocations = false
        }

        // Fetch sublocations
        try {
            val response = RetrofitClient.apiService.getSublocations(selectedLocation)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    subLocations = apiResponse.data
                } else {
                    sublocationsError = "Failed to load sublocations"
                }
            } else {
                sublocationsError = "Error: ${response.code()}"
            }
        } catch (e: Exception) {
            sublocationsError = "Error: ${e.message}"
        } finally {
            isLoadingSublocations = false
        }

        // Fetch pincodes
        try {
            val response = RetrofitClient.apiService.getPincodes()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    pincodes = apiResponse.data
                } else {
                    pincodesError = "Failed to load pincodes"
                }
            } else {
                pincodesError = "Error: ${response.code()}"
            }
        } catch (e: Exception) {
            pincodesError = "Error: ${e.message}"
        } finally {
            isLoadingPincodes = false
        }

        // Fetch banks
        try {
            val response = RetrofitClient.apiService.getBanks()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    banks = apiResponse.data
                } else {
                    banksError = "Failed to load banks"
                }
            } else {
                banksError = "Error: ${response.code()}"
            }
        } catch (e: Exception) {
            banksError = "Error: ${e.message}"
        } finally {
            isLoadingBanks = false
        }

        // Fetch portfolio banks
        try {
            val response = RetrofitClient.apiService.getPortfolioBanks()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    portfolioBanks = apiResponse.data
                } else {
                    portfolioBanksError = "Failed to load portfolio banks"
                }
            } else {
                portfolioBanksError = "Error: ${response.code()}"
            }
        } catch (e: Exception) {
            portfolioBanksError = "Error: ${e.message}"
        } finally {
            isLoadingPortfolioBanks = false
        }

        // Fetch loan types
        try {
            val response = RetrofitClient.apiService.getLoanTypes()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    loanTypes = apiResponse.data
                } else {
                    loanTypesError = "Failed to load loan types"
                }
            } else {
                loanTypesError = "Error: ${response.code()}"
            }
        } catch (e: Exception) {
            loanTypesError = "Error: ${e.message}"
        } finally {
            isLoadingLoanTypes = false
        }

        // Fetch ROI options
        try {
            val response = RetrofitClient.apiService.getRoiOptions()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    roiOptions = apiResponse.data
                } else {
                    roiOptionsError = "Failed to load ROI options"
                }
            } else {
                roiOptionsError = "Error: ${response.code()}"
            }
        } catch (e: Exception) {
            roiOptionsError = "Error: ${e.message}"
        } finally {
            isLoadingRoiOptions = false
        }

        // Fetch Tenure options
        try {
            val response = RetrofitClient.apiService.getTenureOptions()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    tenureOptions = apiResponse.data
                } else {
                    tenureOptionsError = "Failed to load tenure options"
                }
            } else {
                tenureOptionsError = "Error: ${response.code()}"
            }
        } catch (e: Exception) {
            tenureOptionsError = "Error: ${e.message}"
        } finally {
            isLoadingTenureOptions = false
        }

        // Fetch property types
        try {
            Log.d("PropertyTypesAPI", "=== Starting Property Types Fetch ===")
            Log.d("PropertyTypesAPI", "Making API call to get_property_types.php...")
            Log.d("PropertyTypesAPI", "Base URL: ${ApiConfig.BASE_URL}")
            Log.d("PropertyTypesAPI", "Full URL: ${ApiConfig.BASE_URL}get_property_types.php")
            
            val response = RetrofitClient.apiService.getPropertyTypes()
            Log.d("PropertyTypesAPI", "API Response Code: ${response.code()}")
            Log.d("PropertyTypesAPI", "API Response Body: ${response.body()}")
            Log.d("PropertyTypesAPI", "API Response Headers: ${response.headers()}")
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    Log.d("PropertyTypesAPI", "Successfully fetched property types")
                    Log.d("PropertyTypesAPI", "Number of property types: ${apiResponse.data.size}")
                    Log.d("PropertyTypesAPI", "Property types: ${apiResponse.data}")
                    propertyTypes = apiResponse.data
                } else {
                    Log.e("PropertyTypesAPI", "API returned success=false")
                    Log.e("PropertyTypesAPI", "Error message: ${apiResponse?.message}")
                    propertyTypesError = "Failed to load property types: ${apiResponse?.message}"
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("PropertyTypesAPI", "=== Property Types Fetch Failed ===")
                Log.e("PropertyTypesAPI", "Error Response Code: ${response.code()}")
                Log.e("PropertyTypesAPI", "Error Response Body: $errorBody")
                Log.e("PropertyTypesAPI", "Error Headers: ${response.headers()}")
                Log.e("PropertyTypesAPI", "Error Message: ${response.message()}")
                
                propertyTypesError = when (response.code()) {
                    404 -> "Property types endpoint not found. Please check server configuration."
                    500 -> "Server error occurred. Please try again later."
                    else -> "Failed to load property types: ${response.code()}"
                }
            }
        } catch (e: Exception) {
            Log.e("PropertyTypesAPI", "=== Exception in Property Types Fetch ===")
            Log.e("PropertyTypesAPI", "Exception Type: ${e.javaClass.simpleName}")
            Log.e("PropertyTypesAPI", "Exception Message: ${e.message}")
            Log.e("PropertyTypesAPI", "Stack Trace:", e)
            
            propertyTypesError = when (e) {
                is java.net.UnknownHostException -> "Cannot connect to server. Please check your internet connection."
                is java.net.SocketTimeoutException -> "Connection timed out. Please try again."
                else -> "Error: ${e.message}"
            }
        } finally {
            isLoadingPropertyTypes = false
            Log.d("PropertyTypesAPI", "=== Property Types Fetch Completed ===")
        }

        // Fetch appointment banks
        try {
            val response = RetrofitClient.apiService.getAppointmentBanks()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    appointmentBanks = apiResponse.data
                } else {
                    appointmentBanksError = "Failed to load appointment banks"
                }
            } else {
                appointmentBanksError = "Error: ${response.code()}"
            }
        } catch (e: Exception) {
            appointmentBanksError = "Error: ${e.message}"
        } finally {
            isLoadingAppointmentBanks = false
        }

        // Fetch appointment products
        try {
            val response = RetrofitClient.apiService.getAppointmentProducts()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    appointmentProducts = apiResponse.data
                } else {
                    appointmentProductsError = "Failed to load appointment products"
                }
            } else {
                appointmentProductsError = "Error: ${response.code()}"
            }
        } catch (e: Exception) {
            appointmentProductsError = "Error: ${e.message}"
        } finally {
            isLoadingAppointmentProducts = false
        }

        // Fetch appointment statuses
        try {
            val response = RetrofitClient.apiService.getAppointmentStatuses()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    appointmentStatuses = apiResponse.data
                } else {
                    appointmentStatusesError = "Failed to load appointment statuses"
                }
            } else {
                appointmentStatusesError = "Error: ${response.code()}"
            }
        } catch (e: Exception) {
            appointmentStatusesError = "Error: ${e.message}"
        } finally {
            isLoadingAppointmentStatuses = false
        }

        // Fetch appointment sub statuses
        try {
            val response = RetrofitClient.apiService.getAppointmentSubStatuses()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    appointmentSubStatuses = apiResponse.data
                } else {
                    appointmentSubStatusesError = "Failed to load appointment sub statuses"
                }
            } else {
                appointmentSubStatusesError = "Error: ${response.code()}"
            }
        } catch (e: Exception) {
            appointmentSubStatusesError = "Error: ${e.message}"
        } finally {
            isLoadingAppointmentSubStatuses = false
        }
    }

    var mobileNumber by remember { mutableStateOf("") }
    var leadName by remember { mutableStateOf("") }
    var emailId by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var alternativeMobile by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var subLocation by remember { mutableStateOf("") }
    var pinCode by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("") }
    var userQualification by remember { mutableStateOf("") }
    var residentalAddress by remember { mutableStateOf("") }
    var callingTypeLoan by remember { mutableStateOf("") }
    var callingBank by remember { mutableStateOf("") }
    var callingStatus by remember { mutableStateOf("") }
    var callingSubStatus by remember { mutableStateOf("") }
    var officeAddress by remember { mutableStateOf("") }
    var branchAddress by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var accountType by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var branchName by remember { mutableStateOf("") }
    var ifscCode by remember { mutableStateOf("") }
    var loanBankName by remember { mutableStateOf("") }
    var loanType by remember { mutableStateOf("") }
    var loanAmount by remember { mutableStateOf("") }
    var roi by remember { mutableStateOf("") }
    var tenure by remember { mutableStateOf("") }
    var emi by remember { mutableStateOf("") }
    var firstEmiDate by remember { mutableStateOf("") }
    var lastEmiDate by remember { mutableStateOf("") }
    var loanAccountNumber by remember { mutableStateOf("") }
    var vehicleNumber by remember { mutableStateOf("") }
    var vehicleMake by remember { mutableStateOf("") }
    var vehicleModel by remember { mutableStateOf("") }
    var manufacturingYear by remember { mutableStateOf("") }
    var engineNumber by remember { mutableStateOf("") }
    var chassisNumber by remember { mutableStateOf("") }
    var propertyType by remember { mutableStateOf("") }
    var landArea by remember { mutableStateOf("") }
    var sftArea by remember { mutableStateOf("") }
    var marketValue by remember { mutableStateOf("") }

    // Dropdown options
    val customerTypeOptions = listOf("Type 1", "Type 2", "Type 3")

    var vehicleMakes by remember { mutableStateOf<List<String>>(emptyList()) }
    var vehicleModels by remember { mutableStateOf<List<String>>(emptyList()) }
    var manufacturingYears by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingVehicleData by remember { mutableStateOf(false) }

    // Add these state variables for dropdown expansion
    var vehicleMakeExpanded by remember { mutableStateOf(false) }
    var vehicleModelExpanded by remember { mutableStateOf(false) }
    var manufacturingYearExpanded by remember { mutableStateOf(false) }

    var bankAccountTypeExpanded by remember { mutableStateOf(false) }

    // Add these state variables at the top with other state variables
    var propertyDetailsList = remember { mutableStateListOf<Map<String, String>>() }

    fun addRelationshipWithBank(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            try {
                Log.d("RelationshipWithBankAPI", "Starting relationship with bank addition...")
                val request = RelationshipWithBankRequest(
                    r_bank_name = loanBankName,
                    r_loan_type = loanType,
                    r_loan_amount = loanAmount,
                    r_roi = roi,
                    r_tenure = tenure,
                    r_emi = emi,
                    first_emi_date = firstEmiDate,
                    last_emi_date = lastEmiDate,
                    loan_account_name = loanAccountNumber
                )
                Log.d("RelationshipWithBankAPI", "Request data: $request")
                
                withContext(Dispatchers.IO) {
                    try {
                        val response = RetrofitClient.apiService.addRelationshipWithBank(request)
                        Log.d("RelationshipWithBankAPI", "Response code: ${response.code()}")
                        Log.d("RelationshipWithBankAPI", "Response body: ${response.body()}")
                        
                        if (response.isSuccessful) {
                            withContext(Dispatchers.Main) {
                                // Clear the input fields
                                loanBankName = ""
                                loanType = ""
                                loanAmount = ""
                                roi = ""
                                tenure = ""
                                emi = ""
                                firstEmiDate = ""
                                lastEmiDate = ""
                                loanAccountNumber = ""
                                
                                // Add to the list of relationship with banks
                                relationshipWithBanks = relationshipWithBanks + RelationshipWithBankDetails(
                                    bankName = request.r_bank_name,
                                    loanType = request.r_loan_type,
                                    loanAmount = request.r_loan_amount,
                                    roi = request.r_roi,
                                    tenure = request.r_tenure,
                                    emi = request.r_emi,
                                    firstEmiDate = request.first_emi_date,
                                    lastEmiDate = request.last_emi_date,
                                    loanAccountName = request.loan_account_name
                                )
                                
                                Toast.makeText(context, "Relationship with bank added successfully", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            val errorBody = response.errorBody()?.string()
                            Log.e("RelationshipWithBankAPI", "Error response: $errorBody")
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Failed to add relationship with bank: ${response.code()}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("RelationshipWithBankAPI", "Network error occurred", e)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Network error: Please check your internet connection", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("RelationshipWithBankAPI", "Exception occurred", e)
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Add this function to fetch vehicle data
    fun fetchVehicleData(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            isLoadingVehicleData = true
            try {
                // Fetch vehicle makes
                val makesResponse = RetrofitClient.apiService.getVehicleMakes()
                if (makesResponse.isSuccessful && makesResponse.body()?.success == true) {
                    vehicleMakes = makesResponse.body()?.data ?: emptyList()
                }

                // Fetch vehicle models
                val modelsResponse = RetrofitClient.apiService.getVehicleModels()
                if (modelsResponse.isSuccessful && modelsResponse.body()?.success == true) {
                    vehicleModels = modelsResponse.body()?.data ?: emptyList()
                }

                // Fetch manufacturing years
                val yearsResponse = RetrofitClient.apiService.getManufacturingYears()
                if (yearsResponse.isSuccessful && yearsResponse.body()?.success == true) {
                    manufacturingYears = yearsResponse.body()?.data ?: emptyList()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error fetching vehicle data: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoadingVehicleData = false
            }
        }
    }

    // Call fetchVehicleData when the composable is first launched
    LaunchedEffect(Unit) {
        fetchVehicleData(coroutineScope)
    }

    // Add this function to handle adding vehicle details
    fun addVehicleDetails(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            try {
                Log.d("VehicleDetailsAPI", "=== Starting Vehicle Details Addition ===")
                Log.d("VehicleDetailsAPI", "Input Validation:")
                Log.d("VehicleDetailsAPI", "Vehicle Number: $vehicleNumber")
                Log.d("VehicleDetailsAPI", "Vehicle Make: $vehicleMake")
                Log.d("VehicleDetailsAPI", "Vehicle Model: $vehicleModel")
                Log.d("VehicleDetailsAPI", "Manufacturing Year: $manufacturingYear")
                Log.d("VehicleDetailsAPI", "Engine Number: $engineNumber")
                Log.d("VehicleDetailsAPI", "Chassis Number: $chassisNumber")

                isAddingVehicle = true
                Log.d("VehicleDetailsAPI", "Creating request object...")
                val request = VehicleDetailsRequest(
                    vehicle_number = vehicleNumber,
                    vehicle_make = vehicleMake,
                    vehical_modal = vehicleModel,
                    manufacture_year = manufacturingYear,
                    engine_number = engineNumber,
                    chases_number = chassisNumber
                )
                Log.d("VehicleDetailsAPI", "Request object created: $request")
                
                Log.d("VehicleDetailsAPI", "Making API call to add_vehicle_details.php...")
                Log.d("VehicleDetailsAPI", "Base URL: ${ApiConfig.BASE_URL}")
                Log.d("VehicleDetailsAPI", "Full URL: ${ApiConfig.BASE_URL}add_vehicle_details.php")
                
                val response = RetrofitClient.apiService.addVehicleDetails(request)
                Log.d("VehicleDetailsAPI", "API Response Code: ${response.code()}")
                Log.d("VehicleDetailsAPI", "API Response Body: ${response.body()}")
                
                if (response.isSuccessful) {
                    Log.d("VehicleDetailsAPI", "Vehicle details added successfully!")
                    Log.d("VehicleDetailsAPI", "Clearing input fields...")
                    
                    // Clear the input fields
                    vehicleNumber = ""
                    vehicleMake = ""
                    vehicleModel = ""
                    manufacturingYear = ""
                    engineNumber = ""
                    chassisNumber = ""
                    
                    Log.d("VehicleDetailsAPI", "Updating vehicle details list...")
                    // Add to the list of vehicle details
                    vehicleDetails = vehicleDetails + VehicleDetails(
                        vehicleNumber = request.vehicle_number,
                        vehicleMake = request.vehicle_make,
                        vehicleModel = request.vehical_modal,
                        manufacturingYear = request.manufacture_year,
                        engineNumber = request.engine_number,
                        chassisNumber = request.chases_number
                    )
                    
                    Log.d("VehicleDetailsAPI", "Vehicle details list updated. New size: ${vehicleDetails.size}")
                    Toast.makeText(context, "Vehicle details added successfully", Toast.LENGTH_SHORT).show()
                    Log.d("VehicleDetailsAPI", "=== Vehicle Details Addition Completed Successfully ===")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("VehicleDetailsAPI", "=== Vehicle Details Addition Failed ===")
                    Log.e("VehicleDetailsAPI", "Error Response Code: ${response.code()}")
                    Log.e("VehicleDetailsAPI", "Error Response Body: $errorBody")
                    Log.e("VehicleDetailsAPI", "Error Headers: ${response.headers()}")
                    Log.e("VehicleDetailsAPI", "Error Message: ${response.message()}")
                    
                    val errorMessage = when (response.code()) {
                        404 -> "Vehicle details endpoint not found. Please check server configuration."
                        500 -> "Server error occurred. Please try again later."
                        else -> "Failed to add vehicle details: ${response.code()}"
                    }
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("VehicleDetailsAPI", "=== Exception in Vehicle Details Addition ===")
                Log.e("VehicleDetailsAPI", "Exception Type: ${e.javaClass.simpleName}")
                Log.e("VehicleDetailsAPI", "Exception Message: ${e.message}")
                Log.e("VehicleDetailsAPI", "Stack Trace:", e)
                
                val errorMessage = when (e) {
                    is java.net.UnknownHostException -> "Cannot connect to server. Please check your internet connection."
                    is java.net.SocketTimeoutException -> "Connection timed out. Please try again."
                    else -> "Error: ${e.message}"
                }
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            } finally {
                isAddingVehicle = false
                Log.d("VehicleDetailsAPI", "isAddingVehicle set to false")
            }
        }
    }

    // Add this function to handle property submission
    fun submitPropertyDetails() {
        coroutineScope.launch {
            try {
                Log.d("PropertyDetailsAPI", "=== Starting Property Details Addition ===")
                Log.d("PropertyDetailsAPI", "Input Validation:")
                Log.d("PropertyDetailsAPI", "Property Type: $selectedPropertyType")
                Log.d("PropertyDetailsAPI", "Area: $propertyAreaInput")
                Log.d("PropertyDetailsAPI", "Lands: $propertyLands")
                Log.d("PropertyDetailsAPI", "SFT: $propertySft")
                Log.d("PropertyDetailsAPI", "Market Value: $propertyMarketValue")

                val request = PropertyDetailsRequest(
                    database_id = 1, // Changed from "1" to 1 (Int instead of String)
                    p_property_type = selectedPropertyType,
                    p_area = propertyAreaInput,
                    p_lands = propertyLands,
                    p_sft = propertySft,
                    p_market_value = propertyMarketValue
                )

                Log.d("PropertyDetailsAPI", "Making API call to add_property_details.php...")
                Log.d("PropertyDetailsAPI", "Request data: $request")
                
                val response = RetrofitClient.apiService.addPropertyDetails(request)
                Log.d("PropertyDetailsAPI", "API Response Code: ${response.code()}")
                Log.d("PropertyDetailsAPI", "API Response Body: ${response.body()}")
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.status == "success") {
                        // Add the property to the list
                        propertyDetails = propertyDetails + PropertyDetails(
                            propertyType = selectedPropertyType,
                            area = propertyAreaInput,
                            lands = propertyLands,
                            sft = propertySft,
                            marketValue = propertyMarketValue
                        )
                        // Clear the form
                        selectedPropertyType = ""
                        propertyAreaInput = ""
                        propertyLands = ""
                        propertySft = ""
                        propertyMarketValue = ""
                        isAddingProperty = false
                        Toast.makeText(context, "Property details added successfully", Toast.LENGTH_SHORT).show()
                        Log.d("PropertyDetailsAPI", "=== Property Details Addition Completed Successfully ===")
                    } else {
                        Log.e("PropertyDetailsAPI", "API returned success=false")
                        Log.e("PropertyDetailsAPI", "Error message: ${apiResponse?.message}")
                        Toast.makeText(context, apiResponse?.message ?: "Failed to add property details", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("PropertyDetailsAPI", "=== Property Details Addition Failed ===")
                    Log.e("PropertyDetailsAPI", "Error Response Code: ${response.code()}")
                    Log.e("PropertyDetailsAPI", "Error Response Body: $errorBody")
                    Log.e("PropertyDetailsAPI", "Error Headers: ${response.headers()}")
                    Log.e("PropertyDetailsAPI", "Error Message: ${response.message()}")
                    
                    val errorMessage = when (response.code()) {
                        404 -> "Property details endpoint not found. Please check server configuration."
                        500 -> "Server error occurred. Please try again later."
                        else -> "Failed to add property details: ${response.code()}"
                    }
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("PropertyDetailsAPI", "=== Exception in Property Details Addition ===")
                Log.e("PropertyDetailsAPI", "Exception Type: ${e.javaClass.simpleName}")
                Log.e("PropertyDetailsAPI", "Exception Message: ${e.message}")
                Log.e("PropertyDetailsAPI", "Stack Trace:", e)
                
                val errorMessage = when (e) {
                    is java.net.UnknownHostException -> "Cannot connect to server. Please check your internet connection."
                    is java.net.SocketTimeoutException -> "Connection timed out. Please try again."
                    else -> "Error: ${e.message}"
                }
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onNavigateBack,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Add Appointment",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Personal Information Section
            Text(
                text = "Personal Information",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            OutlinedTextField(
                value = mobileNumber,
                onValueChange = { if (it.length <= 10) mobileNumber = it },
                label = { Text("Mobile Number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = leadName,
                onValueChange = { leadName = it },
                label = { Text("Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = emailId,
                onValueChange = { emailId = it },
                label = { Text("Email ID") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = companyName,
                onValueChange = { companyName = it },
                label = { Text("Company Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = alternativeMobile,
                onValueChange = { if (it.length <= 10) alternativeMobile = it },
                label = { Text("Alternative Mobile") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // State Dropdown
            var stateExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = stateExpanded,
                onExpandedChange = { stateExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedState,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("State") },
                    trailingIcon = { 
                        if (isLoadingStates) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = stateExpanded)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .padding(bottom = 8.dp),
                    isError = statesError != null,
                    supportingText = {
                        if (statesError != null) {
                            Text(statesError!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                ExposedDropdownMenu(
                    expanded = stateExpanded,
                    onDismissRequest = { stateExpanded = false }
                ) {
                    if (isLoadingStates) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (states.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No states available") },
                            onClick = { }
                        )
                    } else {
                        states.forEach { state ->
                            DropdownMenuItem(
                                text = { Text(state) },
                                onClick = {
                                    selectedState = state
                                    selectedLocation = "" // Clear location when state changes
                                    subLocations = emptyList() // Clear sublocations when state changes
                                    stateExpanded = false
                                    // Trigger location fetch
                                    coroutineScope.launch {
                                        isLoadingLocations = true
                                        try {
                                            val response = RetrofitClient.apiService.getLocations(state)
                                            if (response.isSuccessful) {
                                                val apiResponse = response.body()
                                                if (apiResponse?.success == true) {
                                                    locations = apiResponse.data
                                                } else {
                                                    locationsError = "Failed to load locations"
                                                }
                                            } else {
                                                locationsError = "Error: ${response.code()}"
                                            }
                                        } catch (e: Exception) {
                                            locationsError = "Error: ${e.message}"
                                        } finally {
                                            isLoadingLocations = false
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Location Dropdown
            var locationExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = locationExpanded,
                onExpandedChange = { locationExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedLocation,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Location") },
                    trailingIcon = { 
                        if (isLoadingLocations) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationExpanded)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .padding(bottom = 8.dp),
                    isError = locationsError != null,
                    supportingText = {
                        if (locationsError != null) {
                            Text(locationsError!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                ExposedDropdownMenu(
                    expanded = locationExpanded,
                    onDismissRequest = { locationExpanded = false }
                ) {
                    if (isLoadingLocations) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (locations.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No locations available") },
                            onClick = { }
                        )
                    } else {
                        locations.forEach { location ->
                            DropdownMenuItem(
                                text = { Text(location) },
                                onClick = {
                                    selectedLocation = location
                                    subLocations = emptyList() // Clear sublocations when location changes
                                    locationExpanded = false
                                    // Trigger sublocation fetch
                                    coroutineScope.launch {
                                        isLoadingSublocations = true
                                        try {
                                            val response = RetrofitClient.apiService.getSublocations(location)
                                            if (response.isSuccessful) {
                                                val apiResponse = response.body()
                                                if (apiResponse?.success == true) {
                                                    subLocations = apiResponse.data
                                                } else {
                                                    sublocationsError = "Failed to load sublocations"
                                                }
                                            } else {
                                                sublocationsError = "Error: ${response.code()}"
                                            }
                                        } catch (e: Exception) {
                                            sublocationsError = "Error: ${e.message}"
                                        } finally {
                                            isLoadingSublocations = false
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Sub Location Dropdown
            var subLocationExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = subLocationExpanded,
                onExpandedChange = { subLocationExpanded = it }
            ) {
                OutlinedTextField(
                    value = subLocation,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Sub Location") },
                    trailingIcon = { 
                        if (isLoadingSublocations) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = subLocationExpanded)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .padding(bottom = 8.dp),
                    isError = sublocationsError != null,
                    supportingText = {
                        if (sublocationsError != null) {
                            Text(sublocationsError!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                ExposedDropdownMenu(
                    expanded = subLocationExpanded,
                    onDismissRequest = { subLocationExpanded = false }
                ) {
                    if (isLoadingSublocations) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (subLocations.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No sublocations available") },
                            onClick = { }
                        )
                    } else {
                        subLocations.forEach { sublocationName ->
                            DropdownMenuItem(
                                text = { Text(sublocationName) },
                                onClick = {
                                    subLocation = sublocationName
                                    subLocationExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Replace the existing pincode TextField with this dropdown
            ExposedDropdownMenuBox(
                expanded = pincodeExpanded,
                onExpandedChange = { pincodeExpanded = it }
            ) {
                OutlinedTextField(
                    value = pinCode,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Pin Code") },
                    trailingIcon = { 
                        if (isLoadingPincodes) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = pincodeExpanded)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .padding(bottom = 8.dp),
                    isError = pincodesError != null,
                    supportingText = {
                        if (pincodesError != null) {
                            Text(pincodesError!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                ExposedDropdownMenu(
                    expanded = pincodeExpanded,
                    onDismissRequest = { pincodeExpanded = false }
                ) {
                    if (isLoadingPincodes) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (pincodes.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No pincodes available") },
                            onClick = { }
                        )
                    } else {
                        pincodes.forEach { pincode ->
                            DropdownMenuItem(
                                text = { Text(pincode) },
                                onClick = {
                                    pinCode = pincode
                                    pincodeExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Additional Information Section
            Text(
                text = "Additional Information",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = source,
                onValueChange = { source = it },
                label = { Text("Source") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = userQualification,
                onValueChange = { userQualification = it },
                label = { Text("Qualification") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = residentalAddress,
                onValueChange = { residentalAddress = it },
                label = { Text("Residential Address") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // Update the customer type dropdown to show loading state
            ExposedDropdownMenuBox(
                expanded = customerTypeExpanded,
                onExpandedChange = { customerTypeExpanded = it }
            ) {
                OutlinedTextField(
                    value = customerType,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Customer Type") },
                    trailingIcon = { 
                        if (isLoadingCustomerTypes) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerTypeExpanded)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .padding(bottom = 8.dp),
                    isError = customerTypeError != null,
                    supportingText = {
                        if (customerTypeError != null) {
                            Text(customerTypeError!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                ExposedDropdownMenu(
                    expanded = customerTypeExpanded,
                    onDismissRequest = { customerTypeExpanded = false }
                ) {
                    if (isLoadingCustomerTypes) {
                        DropdownMenuItem(
                            text = { Text("Loading...") },
                            onClick = { }
                        )
                    } else if (customerTypes.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No customer types available") },
                            onClick = { }
                        )
                    } else {
                        customerTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    customerType = type
                                    customerTypeExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Bank Account Details Section
            Text(
                text = "Bank Account Details",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bank Accounts",
                    style = MaterialTheme.typography.titleMedium
                )
                Button(
                    onClick = {
                        if (bankName.isBlank() || accountType.isBlank() || accountNumber.isBlank() || 
                            branchName.isBlank() || ifscCode.isBlank()) {
                            Toast.makeText(context, "Please fill all bank account details", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        coroutineScope.launch {
                            try {
                                Log.d("BankAccountAPI", "Starting bank account addition...")
                                val request = BankAccountRequest(
                                    b_bank_name = bankName,
                                    b_account_type = accountType,
                                    b_account_no = accountNumber,
                                    b_branch_name = branchName,
                                    b_ifsc_code = ifscCode
                                )
                                Log.d("BankAccountAPI", "Request data: $request")
                                
                                val response = RetrofitClient.apiService.addBankAccount(request)
                                Log.d("BankAccountAPI", "Response code: ${response.code()}")
                                Log.d("BankAccountAPI", "Response body: ${response.body()}")
                                
                                if (response.isSuccessful) {
                                    // Clear the input fields
                                    bankName = ""
                                    accountType = ""
                                    accountNumber = ""
                                    branchName = ""
                                    ifscCode = ""
                                    
                                    // Add to the list of bank accounts
                                    bankAccounts = bankAccounts + BankAccountDetails(
                                        bankName = request.b_bank_name,
                                        accountType = request.b_account_type,
                                        accountNumber = request.b_account_no,
                                        branchName = request.b_branch_name,
                                        ifscCode = request.b_ifsc_code
                                    )
                                    
                                    Toast.makeText(context, "Bank account added successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    Log.e("BankAccountAPI", "Error response: ${response.errorBody()?.string()}")
                                    Toast.makeText(context, "Failed to add bank account", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Log.e("BankAccountAPI", "Exception occurred", e)
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add")
                }
            }

            // Display added bank accounts
            if (bankAccounts.isNotEmpty()) {
                Text(
                    text = "Added Bank Accounts",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                bankAccounts.forEach { account ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text("Bank: ${account.bankName}")
                            Text("Account Type: ${account.accountType}")
                            Text("Account No: ${account.accountNumber}")
                            Text("Branch: ${account.branchName}")
                            Text("IFSC: ${account.ifscCode}")
                        }
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = bankNameExpanded,
                onExpandedChange = { bankNameExpanded = it }
            ) {
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Bank Name") },
                    trailingIcon = { 
                        if (isLoadingBanks) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = bankNameExpanded)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .padding(bottom = 8.dp),
                    isError = banksError != null,
                    supportingText = {
                        if (banksError != null) {
                            Text(banksError!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                ExposedDropdownMenu(
                    expanded = bankNameExpanded,
                    onDismissRequest = { bankNameExpanded = false }
                ) {
                    if (isLoadingBanks) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (banks.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No banks available") },
                            onClick = { }
                        )
                    } else {
                        banks.forEach { bank ->
                            DropdownMenuItem(
                                text = { Text(bank) },
                                onClick = {
                                    bankName = bank
                                    bankNameExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = bankAccountTypeExpanded,
                onExpandedChange = { bankAccountTypeExpanded = it }
            ) {
                OutlinedTextField(
                    value = accountType,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Type of Account") },
                    trailingIcon = { 
                        if (isLoadingBankAccountTypes) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = bankAccountTypeExpanded)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .padding(bottom = 8.dp),
                    isError = bankAccountTypesError != null,
                    supportingText = {
                        if (bankAccountTypesError != null) {
                            Text(bankAccountTypesError!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                ExposedDropdownMenu(
                    expanded = bankAccountTypeExpanded,
                    onDismissRequest = { bankAccountTypeExpanded = false }
                ) {
                    if (isLoadingBankAccountTypes) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (bankAccountTypes.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No account types available") },
                            onClick = { }
                        )
                    } else {
                        bankAccountTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    accountType = type
                                    bankAccountTypeExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = accountNumber,
                onValueChange = { accountNumber = it },
                label = { Text("Account Number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = branchName,
                onValueChange = { branchName = it },
                label = { Text("Branch Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = ifscCode,
                onValueChange = { ifscCode = it },
                label = { Text("IFSC Code") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // Relationship with Bank Section
            Text(
                text = "Relationship with Bank",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bank Relationships",
                    style = MaterialTheme.typography.titleMedium
                )
                Button(
                    onClick = { 
                        if (loanBankName.isBlank() || loanType.isBlank() || loanAmount.isBlank() || 
                            roi.isBlank() || tenure.isBlank() || emi.isBlank() || 
                            firstEmiDate.isBlank() || lastEmiDate.isBlank() || loanAccountNumber.isBlank()) {
                            Toast.makeText(context, "Please fill all relationship with bank details", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        addRelationshipWithBank(coroutineScope)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add")
                }
            }

            ExposedDropdownMenuBox(
                expanded = loanBankNameExpanded,
                onExpandedChange = { loanBankNameExpanded = it }
            ) {
                OutlinedTextField(
                    value = loanBankName,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Bank Name") },
                    trailingIcon = { 
                        if (isLoadingPortfolioBanks) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = loanBankNameExpanded)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .padding(bottom = 8.dp),
                    isError = portfolioBanksError != null,
                    supportingText = {
                        if (portfolioBanksError != null) {
                            Text(portfolioBanksError!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                ExposedDropdownMenu(
                    expanded = loanBankNameExpanded,
                    onDismissRequest = { loanBankNameExpanded = false }
                ) {
                    if (isLoadingPortfolioBanks) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (portfolioBanks.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No portfolio banks available") },
                            onClick = { }
                        )
                    } else {
                        portfolioBanks.forEach { bank ->
                            DropdownMenuItem(
                                text = { Text(bank) },
                                onClick = {
                                    loanBankName = bank
                                    loanBankNameExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Replace the existing loan type dropdown with this updated version
            ExposedDropdownMenuBox(
                expanded = loanTypeExpanded,
                onExpandedChange = { loanTypeExpanded = it }
            ) {
                OutlinedTextField(
                    value = loanType,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Type of Loan") },
                    trailingIcon = { 
                        if (isLoadingLoanTypes) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = loanTypeExpanded)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .padding(bottom = 8.dp),
                    isError = loanTypesError != null,
                    supportingText = {
                        if (loanTypesError != null) {
                            Text(loanTypesError!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                ExposedDropdownMenu(
                    expanded = loanTypeExpanded,
                    onDismissRequest = { loanTypeExpanded = false }
                ) {
                    if (isLoadingLoanTypes) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (loanTypes.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No loan types available") },
                            onClick = { }
                        )
                    } else {
                        loanTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    loanType = type
                                    loanTypeExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = loanAmount,
                onValueChange = { loanAmount = it },
                label = { Text("Loan Amount") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // Replace the existing ROI dropdown with this updated version
            ExposedDropdownMenuBox(
                expanded = roiExpanded,
                onExpandedChange = { roiExpanded = it }
            ) {
                OutlinedTextField(
                    value = roi,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("ROI %") },
                    trailingIcon = { 
                        if (isLoadingRoiOptions) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = roiExpanded)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .padding(bottom = 8.dp),
                    isError = roiOptionsError != null,
                    supportingText = {
                        if (roiOptionsError != null) {
                            Text(roiOptionsError!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                ExposedDropdownMenu(
                    expanded = roiExpanded,
                    onDismissRequest = { roiExpanded = false }
                ) {
                    if (isLoadingRoiOptions) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (roiOptions.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No ROI options available") },
                            onClick = { }
                        )
                    } else {
                        roiOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    roi = option
                                    roiExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Replace the existing Tenure dropdown with this updated version
            ExposedDropdownMenuBox(
                expanded = tenureExpanded,
                onExpandedChange = { tenureExpanded = it }
            ) {
                OutlinedTextField(
                    value = tenure,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Tenure (months)") },
                    trailingIcon = { 
                        if (isLoadingTenureOptions) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = tenureExpanded)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .padding(bottom = 8.dp),
                    isError = tenureOptionsError != null,
                    supportingText = {
                        if (tenureOptionsError != null) {
                            Text(tenureOptionsError!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                ExposedDropdownMenu(
                    expanded = tenureExpanded,
                    onDismissRequest = { tenureExpanded = false }
                ) {
                    if (isLoadingTenureOptions) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (tenureOptions.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No tenure options available") },
                            onClick = { }
                        )
                    } else {
                        tenureOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    tenure = option
                                    tenureExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = emi,
                onValueChange = { emi = it },
                label = { Text("EMI") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // First EMI Date with Calendar
            var showFirstEmiDatePicker by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = firstEmiDate,
                onValueChange = { },
                readOnly = true,
                label = { Text("First EMI Date") },
                trailingIcon = {
                    IconButton(onClick = { showFirstEmiDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select date")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            if (showFirstEmiDatePicker) {
                val datePickerState = rememberDatePickerState()
                DatePickerDialog(
                    onDismissRequest = { showFirstEmiDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val date = java.time.Instant.ofEpochMilli(millis)
                                        .atZone(java.time.ZoneId.systemDefault())
                                        .toLocalDate()
                                    firstEmiDate = date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy"))
                                }
                                showFirstEmiDatePicker = false
                            }
                        ) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showFirstEmiDatePicker = false }) {
                            Text("Cancel")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            // Last EMI Date with Calendar
            var showLastEmiDatePicker by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = lastEmiDate,
                onValueChange = { },
                readOnly = true,
                label = { Text("Last EMI Date") },
                trailingIcon = {
                    IconButton(onClick = { showLastEmiDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select date")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            if (showLastEmiDatePicker) {
                val datePickerState = rememberDatePickerState()
                DatePickerDialog(
                    onDismissRequest = { showLastEmiDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val date = java.time.Instant.ofEpochMilli(millis)
                                        .atZone(java.time.ZoneId.systemDefault())
                                        .toLocalDate()
                                    lastEmiDate = date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy"))
                                }
                                showLastEmiDatePicker = false
                            }
                        ) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLastEmiDatePicker = false }) {
                            Text("Cancel")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            OutlinedTextField(
                value = loanAccountNumber,
                onValueChange = { loanAccountNumber = it },
                label = { Text("Loan Account Number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // Vehicle Section
            Text(
                text = "Vehicle Details",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Vehicle Details Input Fields
            OutlinedTextField(
                value = vehicleNumber,
                onValueChange = { vehicleNumber = it },
                label = { Text("Vehicle Number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // Vehicle Make Dropdown
            ExposedDropdownMenuBox(
                expanded = vehicleMakeExpanded,
                onExpandedChange = { vehicleMakeExpanded = it }
            ) {
                OutlinedTextField(
                    value = vehicleMake,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Make") },
                    trailingIcon = { 
                        if (isLoadingVehicleData) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehicleMakeExpanded)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .padding(bottom = 8.dp)
                )

                ExposedDropdownMenu(
                    expanded = vehicleMakeExpanded,
                    onDismissRequest = { vehicleMakeExpanded = false }
                ) {
                    if (isLoadingVehicleData) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (vehicleMakes.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No makes available") },
                            onClick = { }
                        )
                    } else {
                        vehicleMakes.forEach { make ->
                            DropdownMenuItem(
                                text = { Text(make) },
                                onClick = {
                                    vehicleMake = make
                                    vehicleMakeExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Vehicle Model Dropdown
            ExposedDropdownMenuBox(
                expanded = vehicleModelExpanded,
                onExpandedChange = { vehicleModelExpanded = it }
            ) {
                OutlinedTextField(
                    value = vehicleModel,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Model") },
                    trailingIcon = { 
                        if (isLoadingVehicleData) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehicleModelExpanded)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .padding(bottom = 8.dp)
                )

                ExposedDropdownMenu(
                    expanded = vehicleModelExpanded,
                    onDismissRequest = { vehicleModelExpanded = false }
                ) {
                    if (isLoadingVehicleData) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (vehicleModels.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No models available") },
                            onClick = { }
                        )
                    } else {
                        vehicleModels.forEach { model ->
                            DropdownMenuItem(
                                text = { Text(model) },
                                onClick = {
                                    vehicleModel = model
                                    vehicleModelExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Manufacturing Year Dropdown
            ExposedDropdownMenuBox(
                expanded = manufacturingYearExpanded,
                onExpandedChange = { manufacturingYearExpanded = it }
            ) {
                OutlinedTextField(
                    value = manufacturingYear,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Manufacturing Year") },
                    trailingIcon = { 
                        if (isLoadingVehicleData) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = manufacturingYearExpanded)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .padding(bottom = 8.dp)
                )

                ExposedDropdownMenu(
                    expanded = manufacturingYearExpanded,
                    onDismissRequest = { manufacturingYearExpanded = false }
                ) {
                    if (isLoadingVehicleData) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (manufacturingYears.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No years available") },
                            onClick = { }
                        )
                    } else {
                        manufacturingYears.forEach { year ->
                            DropdownMenuItem(
                                text = { Text(year) },
                                onClick = {
                                    manufacturingYear = year
                                    manufacturingYearExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = engineNumber,
                onValueChange = { engineNumber = it },
                label = { Text("Engine Number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = chassisNumber,
                onValueChange = { chassisNumber = it },
                label = { Text("Chassis Number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Vehicles",
                    style = MaterialTheme.typography.titleMedium
                )
                Button(
                    onClick = { 
                        if (vehicleNumber.isBlank() || vehicleMake.isBlank() || vehicleModel.isBlank() || 
                            manufacturingYear.isBlank() || engineNumber.isBlank() || chassisNumber.isBlank()) {
                            Toast.makeText(context, "Please fill all vehicle details", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        addVehicleDetails(coroutineScope)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add")
                }
            }

            // Vehicle Details Table
            if (vehicleDetails.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Table Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(8.dp)
                        ) {
                            Text(
                                "Vehicle Number",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Make",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Model",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Year",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Engine No.",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Chassis No.",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Action",
                                modifier = Modifier.weight(0.5f),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        // Table Content
                        vehicleDetails.forEach { vehicle ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    vehicle.vehicleNumber,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    vehicle.vehicleMake,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    vehicle.vehicleModel,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    vehicle.manufacturingYear,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    vehicle.engineNumber,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    vehicle.chassisNumber,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { /* TODO: Implement delete functionality */ },
                                    modifier = Modifier.weight(0.5f)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            }

            // Property Details Section
            Text(
                text = "Property Details",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Property Type Dropdown
            ExposedDropdownMenuBox(
                expanded = propertyTypeExpanded,
                onExpandedChange = { propertyTypeExpanded = it }
            ) {
                OutlinedTextField(
                    value = propertyType,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Type of Property") },
                    trailingIcon = { 
                        if (isLoadingPropertyTypes) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = propertyTypeExpanded)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .padding(bottom = 8.dp),
                    isError = propertyTypesError != null,
                    supportingText = {
                        if (propertyTypesError != null) {
                            Text(propertyTypesError!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                ExposedDropdownMenu(
                    expanded = propertyTypeExpanded,
                    onDismissRequest = { propertyTypeExpanded = false }
                ) {
                    if (isLoadingPropertyTypes) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (propertyTypes.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No property types available") },
                            onClick = { }
                        )
                    } else {
                        propertyTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    propertyType = type
                                    propertyTypeExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = propertyAreaInput,
                onValueChange = { propertyAreaInput = it },
                label = { Text("Area") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = landArea,
                onValueChange = { landArea = it },
                label = { Text("Land in SQ Yards") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = sftArea,
                onValueChange = { sftArea = it },
                label = { Text("SFT") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = marketValue,
                onValueChange = { marketValue = it },
                label = { Text("Market Value") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Button(
                onClick = {
                    if (propertyType.isNotEmpty() && propertyAreaInput.isNotEmpty() && 
                        landArea.isNotEmpty() && sftArea.isNotEmpty() && 
                        marketValue.isNotEmpty()) {
                        
                        coroutineScope.launch {
                            try {
                                Log.d("PropertyDetailsAPI", "=== Starting Property Details Addition ===")
                                Log.d("PropertyDetailsAPI", "Input Validation:")
                                Log.d("PropertyDetailsAPI", "Property Type: $propertyType")
                                Log.d("PropertyDetailsAPI", "Area: $propertyAreaInput")
                                Log.d("PropertyDetailsAPI", "Lands: $landArea")
                                Log.d("PropertyDetailsAPI", "SFT: $sftArea")
                                Log.d("PropertyDetailsAPI", "Market Value: $marketValue")

                                val request = PropertyDetailsRequest(
                                    database_id = 1, // Changed from "1" to 1 (Int instead of String)
                                    p_property_type = propertyType,
                                    p_area = propertyAreaInput,
                                    p_lands = landArea,
                                    p_sft = sftArea,
                                    p_market_value = marketValue
                                )

                                Log.d("PropertyDetailsAPI", "Making API call to add_property_details.php...")
                                Log.d("PropertyDetailsAPI", "Request data: $request")
                                
                                val response = RetrofitClient.apiService.addPropertyDetails(request)
                                Log.d("PropertyDetailsAPI", "API Response Code: ${response.code()}")
                                Log.d("PropertyDetailsAPI", "API Response Body: ${response.body()}")
                                
                                if (response.isSuccessful) {
                                    val apiResponse = response.body()
                                    if (apiResponse?.status == "success") {
                                        // Add to local list for UI display
                                        val propertyDetails = mapOf(
                                            "p_property_type" to propertyType,
                                            "p_area" to propertyAreaInput,
                                            "p_lands" to landArea,
                                            "p_sft" to sftArea,
                                            "p_market_value" to marketValue
                                        )
                                        propertyDetailsList.add(propertyDetails)
                                        
                                        // Clear the form
                                        propertyType = ""
                                        propertyAreaInput = ""
                                        landArea = ""
                                        sftArea = ""
                                        marketValue = ""
                                        
                                        Toast.makeText(context, "Property details added successfully", Toast.LENGTH_SHORT).show()
                                        Log.d("PropertyDetailsAPI", "=== Property Details Addition Completed Successfully ===")
                                    } else {
                                        Log.e("PropertyDetailsAPI", "API returned success=false")
                                        Log.e("PropertyDetailsAPI", "Error message: ${apiResponse?.message}")
                                        Toast.makeText(context, apiResponse?.message ?: "Failed to add property details", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    val errorBody = response.errorBody()?.string()
                                    Log.e("PropertyDetailsAPI", "=== Property Details Addition Failed ===")
                                    Log.e("PropertyDetailsAPI", "Error Response Code: ${response.code()}")
                                    Log.e("PropertyDetailsAPI", "Error Response Body: $errorBody")
                                    
                                    val errorMessage = when (response.code()) {
                                        404 -> "Property details endpoint not found. Please check server configuration."
                                        500 -> "Server error occurred. Please try again later."
                                        else -> "Failed to add property details: ${response.code()}"
                                    }
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Log.e("PropertyDetailsAPI", "=== Exception in Property Details Addition ===")
                                Log.e("PropertyDetailsAPI", "Exception Type: ${e.javaClass.simpleName}")
                                Log.e("PropertyDetailsAPI", "Exception Message: ${e.message}")
                                Log.e("PropertyDetailsAPI", "Stack Trace:", e)
                                
                                val errorMessage = when (e) {
                                    is java.net.UnknownHostException -> "Cannot connect to server. Please check your internet connection."
                                    is java.net.SocketTimeoutException -> "Connection timed out. Please try again."
                                    else -> "Error: ${e.message}"
                                }
                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Property")
            }

            // Add a table to display added properties
            if (propertyDetailsList.isNotEmpty()) {
                Text(
                    "Added Properties",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                
                PropertyDetailsTable(
                    propertyDetailsList = propertyDetailsList,
                    onDeleteProperty = { index ->
                        propertyDetailsList.removeAt(index)
                        Toast.makeText(context, "Property removed", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Credit Card Details Section
            Text(
                text = "Credit Card Details",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Credit Card Bank Dropdown
            var creditCardBankExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = creditCardBankExpanded,
                onExpandedChange = { creditCardBankExpanded = it }
            ) {
                OutlinedTextField(
                    value = creditCardBank,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Bank Name") },
                    trailingIcon = { 
                        if (isLoadingBanks) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = creditCardBankExpanded)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .padding(bottom = 8.dp),
                    isError = banksError != null,
                    supportingText = {
                        if (banksError != null) {
                            Text(banksError!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                ExposedDropdownMenu(
                    expanded = creditCardBankExpanded,
                    onDismissRequest = { creditCardBankExpanded = false }
                ) {
                    if (isLoadingBanks) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (banks.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No banks available") },
                            onClick = { }
                        )
                    } else {
                        banks.forEach { bank ->
                            DropdownMenuItem(
                                text = { Text(bank) },
                                onClick = {
                                    creditCardBank = bank
                                    creditCardBankExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Credit Limit Input Field
            OutlinedTextField(
                value = creditLimit,
                onValueChange = { creditLimit = it },
                label = { Text("Credit Limit") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // Add Credit Card Button
            Button(
                onClick = {
                    if (creditCardBank.isBlank() || creditLimit.isBlank()) {
                        Toast.makeText(context, "Please fill all credit card details", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    coroutineScope.launch {
                        try {
                            Log.d("CreditCardAPI", "Starting credit card details addition...")
                            val request = CreditCardDetailsRequest(
                                database_id = "1", // TODO: Get the actual database_id from the appointment
                                c_bank_name = creditCardBank,
                                c_limit = creditLimit
                            )
                            Log.d("CreditCardAPI", "Request data: $request")
                            
                            val response = RetrofitClient.apiService.addCreditCardDetails(request)
                            Log.d("CreditCardAPI", "Response code: ${response.code()}")
                            Log.d("CreditCardAPI", "Response body: ${response.body()}")
                            
                            if (response.isSuccessful) {
                                // Clear the input fields
                                creditCardBank = ""
                                creditLimit = ""
                                
                                Toast.makeText(context, "Credit card details added successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                val errorBody = response.errorBody()?.string()
                                Log.e("CreditCardAPI", "Error response: $errorBody")
                                Toast.makeText(context, "Failed to add credit card details: ${response.code()}", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Log.e("CreditCardAPI", "Exception occurred", e)
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Credit Card")
            }

            // Appointment Details Section
            Text(
                text = "Appointment Details",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Update the Appointment Bank dropdown
            ExposedDropdownMenuBox(
                expanded = appointmentBankExpanded,
                onExpandedChange = { appointmentBankExpanded = it }
            ) {
                OutlinedTextField(
                    value = appointmentBank,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Appointment Bank") },
                    trailingIcon = { 
                        if (isLoadingAppointmentBanks) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = appointmentBankExpanded)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .padding(bottom = 8.dp),
                    isError = appointmentBanksError != null,
                    supportingText = {
                        if (appointmentBanksError != null) {
                            Text(appointmentBanksError!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                ExposedDropdownMenu(
                    expanded = appointmentBankExpanded,
                    onDismissRequest = { appointmentBankExpanded = false }
                ) {
                    if (isLoadingAppointmentBanks) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (appointmentBanks.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No appointment banks available") },
                            onClick = { }
                        )
                    } else {
                        appointmentBanks.forEach { bank ->
                            DropdownMenuItem(
                                text = { Text(bank) },
                                onClick = {
                                    appointmentBank = bank
                                    appointmentBankExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Update the Appointment Product dropdown
            ExposedDropdownMenuBox(
                expanded = appointmentProductExpanded,
                onExpandedChange = { appointmentProductExpanded = it }
            ) {
                OutlinedTextField(
                    value = appointmentProduct,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Appointment Product") },
                    trailingIcon = { 
                        if (isLoadingAppointmentProducts) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = appointmentProductExpanded)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .padding(bottom = 8.dp),
                    isError = appointmentProductsError != null,
                    supportingText = {
                        if (appointmentProductsError != null) {
                            Text(appointmentProductsError!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                ExposedDropdownMenu(
                    expanded = appointmentProductExpanded,
                    onDismissRequest = { appointmentProductExpanded = false }
                ) {
                    if (isLoadingAppointmentProducts) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (appointmentProducts.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No appointment products available") },
                            onClick = { }
                        )
                    } else {
                        appointmentProducts.forEach { product ->
                            DropdownMenuItem(
                                text = { Text(product) },
                                onClick = {
                                    appointmentProduct = product
                                    appointmentProductExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Update the Appointment Status dropdown
            ExposedDropdownMenuBox(
                expanded = appointmentStatusExpanded,
                onExpandedChange = { appointmentStatusExpanded = it }
            ) {
                OutlinedTextField(
                    value = appointmentStatus,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Appointment Status") },
                    trailingIcon = { 
                        if (isLoadingAppointmentStatuses) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = appointmentStatusExpanded)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .padding(bottom = 8.dp),
                    isError = appointmentStatusesError != null,
                    supportingText = {
                        if (appointmentStatusesError != null) {
                            Text(appointmentStatusesError!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                ExposedDropdownMenu(
                    expanded = appointmentStatusExpanded,
                    onDismissRequest = { appointmentStatusExpanded = false }
                ) {
                    if (isLoadingAppointmentStatuses) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (appointmentStatuses.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No appointment statuses available") },
                            onClick = { }
                        )
                    } else {
                        appointmentStatuses.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status) },
                                onClick = {
                                    appointmentStatus = status
                                    appointmentStatusExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Update the Appointment Sub Status dropdown
            ExposedDropdownMenuBox(
                expanded = appointmentSubStatusExpanded,
                onExpandedChange = { appointmentSubStatusExpanded = it }
            ) {
                OutlinedTextField(
                    value = appointmentSubStatus,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Appointment Sub Status") },
                    trailingIcon = { 
                        if (isLoadingAppointmentSubStatuses) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = appointmentSubStatusExpanded)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .padding(bottom = 8.dp),
                    isError = appointmentSubStatusesError != null,
                    supportingText = {
                        if (appointmentSubStatusesError != null) {
                            Text(appointmentSubStatusesError!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                ExposedDropdownMenu(
                    expanded = appointmentSubStatusExpanded,
                    onDismissRequest = { appointmentSubStatusExpanded = false }
                ) {
                    if (isLoadingAppointmentSubStatuses) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (appointmentSubStatuses.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No appointment sub statuses available") },
                            onClick = { }
                        )
                    } else {
                        appointmentSubStatuses.forEach { subStatus ->
                            DropdownMenuItem(
                                text = { Text(subStatus) },
                                onClick = {
                                    appointmentSubStatus = subStatus
                                    appointmentSubStatusExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Note Field
            OutlinedTextField(
                value = appointmentNote,
                onValueChange = { appointmentNote = it },
                label = { Text("notes") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                minLines = 3
            )

            // Visiting Card Section
            Text(
                text = "Visiting Card",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            var visitingCardFileName by remember { mutableStateOf<String?>(null) }
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                uri?.let {
                    visitingCardUri = it
                    visitingCardFileName = getAppointmentFileName(context.contentResolver, it)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = visitingCardFileName ?: "",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Visiting Card") },
                    modifier = Modifier.weight(1f),
                    trailingIcon = {
                        IconButton(onClick = { launcher.launch("application/pdf,image/*") }) {
                            Icon(Icons.Default.AttachFile, contentDescription = "Attach File")
                        }
                    }
                )
            }

            // Submit Button
            Button(
                onClick = {
                    if (mobileNumber.isBlank() || leadName.isBlank()) {
                        Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    coroutineScope.launch {
                        try {
                            Log.d("AppointmentAPI", "Starting appointment submission...")
                            
                            // Create appointment request
                            val generatedUniqueId = "APT-" + System.currentTimeMillis()
                            val request = com.kurakulas.app.data.model.AddAppointmentRequest(
                                database_id = 1,
                                unique_id = generatedUniqueId,
                                mobile_number = mobileNumber,
                                lead_name = leadName,
                                email_id = emailId,
                                company_name = companyName,
                                alternative_mobile = alternativeMobile,
                                state = selectedState,
                                location = selectedLocation,
                                sub_location = subLocation,
                                pin_code = pinCode,
                                source = source,
                                visiting_card = "",
                                user_qualification = userQualification,
                                residental_address = residentalAddress,
                                customer_type = customerType
                            )
                            
                            Log.d("AppointmentAPI", "Request data: $request")
                            
                            Log.d("AppointmentAPI", "Submitting appointment request: $request")
                            val response = RetrofitClient.apiService.addAppointment(request)
                            Log.d("AppointmentAPI", "Response code: ${response.code()}")

                            if (response.isSuccessful) {
                                // Create appointment status request
                                val appointmentStatusRequest = AppointmentStatusRequest(
                                    appt_bank = appointmentBank,
                                    appt_product = appointmentProduct,
                                    appt_status = appointmentStatus,
                                    appt_sub_status = appointmentSubStatus,
                                    notes = appointmentNote
                                )

                                // Add appointment status
                                val statusResponse = RetrofitClient.apiService.addAppointmentStatus(appointmentStatusRequest)
                                if (statusResponse.isSuccessful) {
                                    Toast.makeText(context, "Appointment added successfully", Toast.LENGTH_SHORT).show()
                                    // Increment points based on customer type
                                    onPointsIncrement(customerType)
                                    onNavigateBack()
                                } else {
                                    Toast.makeText(context, "Error adding appointment status", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Error adding appointment", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Log.e("AppointmentAPI", "Exception occurred", e)
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Submit")
            }

            // Add this section in your UI where you want to show the property details form
            if (isAddingProperty) {
                Dialog(onDismissRequest = { isAddingProperty = false }) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "Add Property Details",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Property Type Dropdown
                            ExposedDropdownMenuBox(
                                expanded = propertyTypeExpanded,
                                onExpandedChange = { propertyTypeExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedPropertyType,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Property Type") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = propertyTypeExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )

                                ExposedDropdownMenu(
                                    expanded = propertyTypeExpanded,
                                    onDismissRequest = { propertyTypeExpanded = false }
                                ) {
                                    propertyTypes.forEach { type ->
                                        DropdownMenuItem(
                                            text = { Text(type) },
                                            onClick = {
                                                selectedPropertyType = type
                                                propertyTypeExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Area Field
                            OutlinedTextField(
                                value = propertyAreaInput,
                                onValueChange = { propertyAreaInput = it },
                                label = { Text("Area") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Lands Field
                            OutlinedTextField(
                                value = propertyLands,
                                onValueChange = { propertyLands = it },
                                label = { Text("Lands") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // SFT Field
                            OutlinedTextField(
                                value = propertySft,
                                onValueChange = { propertySft = it },
                                label = { Text("SFT") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Market Value Field
                            OutlinedTextField(
                                value = propertyMarketValue,
                                onValueChange = { propertyMarketValue = it },
                                label = { Text("Market Value") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { isAddingProperty = false }) {
                                    Text("Cancel")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { submitPropertyDetails() },
                                    enabled = selectedPropertyType.isNotEmpty() && 
                                            propertyAreaInput.isNotEmpty() && 
                                            propertyLands.isNotEmpty() && 
                                            propertySft.isNotEmpty() && 
                                            propertyMarketValue.isNotEmpty()
                                ) {
                                    Text("Add Property")
                                }
                            }
                        }
                    }
                }
            }

            // Add this section to show the list of added properties
            if (propertyDetails.isNotEmpty()) {
                Text(
                    text = "Property Details",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                propertyDetails.forEach { property ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text("Type: ${property.propertyType}")
                            Text("Area: ${property.area}")
                            Text("Lands: ${property.lands}")
                            Text("SFT: ${property.sft}")
                            Text("Market Value: ${property.marketValue}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

data class AppointmentData(
    val mobileNumber: String,
    val name: String,
    val emailId: String,
    val companyName: String,
    val alternativeMobile: String,
    val state: String,
    val location: String,
    val subLocation: String,
    val pincode: String,
    val source: String,
    val visitingCardUri: Uri?,
    val qualification: String,
    val residentialArea: String,
    val customerType: String,
    val appointmentBank: String,
    val appointmentProduct: String,
    val appointmentStatus: String,
    val appointmentSubStatus: String,
    val notes: String,
    val bankName: String,
    val accountType: String,
    val accountNumber: String,
    val branchName: String,
    val ifscCode: String,
    val loanBankName: String,
    val loanType: String,
    val loanAmount: String,
    val roi: String,
    val tenure: String,
    val emi: String,
    val firstEmiDate: String,
    val lastEmiDate: String,
    val loanAccountNumber: String,
    val vehicleNumber: String,
    val vehicleMake: String,
    val vehicleModel: String,
    val manufacturingYear: String,
    val engineNumber: String,
    val chassisNumber: String,
    val propertyType: String,
    val propertyArea: String,
    val landArea: String,
    val sftArea: String,
    val marketValue: String,
    val creditCardBank: String,
    val creditLimit: String
) 

@Composable
private fun PropertyDetailsTable(
    propertyDetailsList: List<Map<String, String>>,
    onDeleteProperty: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("Type", modifier = Modifier.weight(1f))
            Text("Area", modifier = Modifier.weight(1f))
            Text("Lands", modifier = Modifier.weight(1f))
            Text("SFT", modifier = Modifier.weight(1f))
            Text("Market Value", modifier = Modifier.weight(1f))
            Text("Action", modifier = Modifier.weight(0.5f))
        }
        
        // Data Rows
        propertyDetailsList.forEachIndexed { index, property ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    property["p_property_type"] ?: "",
                    modifier = Modifier.weight(1f)
                )
                Text(
                    property["p_area"] ?: "",
                    modifier = Modifier.weight(1f)
                )
                Text(
                    property["p_lands"] ?: "",
                    modifier = Modifier.weight(1f)
                )
                Text(
                    property["p_sft"] ?: "",
                    modifier = Modifier.weight(1f)
                )
                Text(
                    property["p_market_value"] ?: "",
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { onDeleteProperty(index) },
                    modifier = Modifier.weight(0.5f)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}
