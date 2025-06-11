package com.kurakulas.app.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import java.net.URLEncoder
import java.io.File
import com.kurakulas.app.data.api.ApiConfig

data class Banker(
    val id: Int,
    val vendorBank: String,
    val bankerName: String,
    val phoneNumber: String,
    val emailId: String,
    val bankerDesignation: String,
    val loanType: String,
    val state: String,
    val location: String,
    val visitingCard: String,
    val address: String
)

@Singleton
class BankerRepository @Inject constructor() {
    private val baseUrl = ApiConfig.BASE_URL
    private val TAG = "BankerRepository"

    suspend fun getVendorBanks(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/get_vendor_banks.php")
            Log.d(TAG, "Attempting to connect to: ${url.toString()}")
            
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            Log.d(TAG, "Connection established, sending request...")
            
            val responseCode = connection.responseCode
            Log.d(TAG, "Response code: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d(TAG, "Raw response: $response")
                
                try {
                    val jsonResponse = JSONObject(response)
                    Log.d(TAG, "Parsed JSON: $jsonResponse")
                    
                    if (jsonResponse.getBoolean("success")) {
                        val dataArray = jsonResponse.getJSONArray("data")
                        val banksList = mutableListOf<String>()
                        
                        for (i in 0 until dataArray.length()) {
                            banksList.add(dataArray.getString(i))
                        }
                        
                        Log.d(TAG, "Successfully parsed ${banksList.size} banks: $banksList")
                        Result.Success(banksList)
                    } else {
                        val errorMessage = jsonResponse.getString("message")
                        Log.e(TAG, "API returned error: $errorMessage")
                        Result.Error(errorMessage)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing JSON response", e)
                    Result.Error("Error parsing response: ${e.message}")
                }
            } else {
                val errorStream = connection.errorStream?.bufferedReader()?.use { it.readText() }
                Log.e(TAG, "HTTP Error: $responseCode, Error stream: $errorStream")
                Result.Error("Failed to fetch vendor banks. Response code: $responseCode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error occurred", e)
            Result.Error("Network error: ${e.message}")
        }
    }

    suspend fun getBankerDesignations(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/get_banker_designations.php")
            Log.d(TAG, "Fetching banker designations from: ${url.toString()}")
            
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            val responseCode = connection.responseCode
            Log.d(TAG, "Response code for designations: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d(TAG, "Raw response for designations: $response")
                
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getBoolean("success")) {
                        val designationsArray = jsonResponse.getJSONArray("data")
                        val designations = mutableListOf<String>()
                        
                        for (i in 0 until designationsArray.length()) {
                            designations.add(designationsArray.getString(i))
                        }
                        
                        Log.d(TAG, "Successfully parsed ${designations.size} designations")
                        Result.Success(designations)
                    } else {
                        val errorMessage = jsonResponse.optString("message", "Unknown error occurred")
                        Log.e(TAG, "Error in response: $errorMessage")
                        Result.Error(errorMessage)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing JSON response", e)
                    Result.Error("Error parsing response: ${e.message}")
                }
            } else {
                val errorStream = connection.errorStream?.bufferedReader()?.use { it.readText() }
                Log.e(TAG, "HTTP Error: $responseCode, Error stream: $errorStream")
                Result.Error("Failed to fetch banker designations. Response code: $responseCode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error occurred", e)
            Result.Error("Network error: ${e.message}")
        }
    }

    suspend fun getLoanTypes(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/get_loan_types.php")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val responseCode = connection.responseCode
            Log.d(TAG, "Response code for loan types: $responseCode")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d(TAG, "Raw response for loan types: $response")

                try {
                    val jsonResponse = JSONObject(response)
                    Log.d(TAG, "Parsed JSON response: $jsonResponse")

                    if (jsonResponse.getBoolean("success")) {
                        val loanTypesArray = jsonResponse.getJSONArray("data")
                        val loanTypes = mutableListOf<String>()
                        
                        for (i in 0 until loanTypesArray.length()) {
                            loanTypes.add(loanTypesArray.getString(i))
                        }
                        
                        Log.d(TAG, "Successfully parsed ${loanTypes.size} loan types: $loanTypes")
                        Result.Success(loanTypes)
                    } else {
                        val errorMessage = jsonResponse.getString("message")
                        Log.e(TAG, "API returned error for loan types: $errorMessage")
                        Result.Error(errorMessage)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing JSON response for loan types", e)
                    Result.Error("Error parsing response: ${e.message}")
                }
            } else {
                val errorStream = connection.errorStream?.bufferedReader()?.use { it.readText() }
                Log.e(TAG, "HTTP Error for loan types: $responseCode, Error stream: $errorStream")
                Result.Error("Failed to fetch loan types. Response code: $responseCode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error occurred while fetching loan types", e)
            Result.Error("Network error: ${e.message}")
        }
    }

    suspend fun getBranchStates(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/get_branch_states.php")
            Log.d(TAG, "Attempting to connect to: ${url.toString()}")
            
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            Log.d(TAG, "Connection established, sending request...")
            
            val responseCode = connection.responseCode
            Log.d(TAG, "Response code for branch states: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d(TAG, "Raw response for branch states: $response")
                
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getBoolean("success")) {
                        val dataArray = jsonResponse.getJSONArray("data")
                        val states = mutableListOf<String>()
                        
                        for (i in 0 until dataArray.length()) {
                            states.add(dataArray.getString(i))
                        }
                        
                        Result.Success(states)
                    } else {
                        Result.Error(jsonResponse.getString("message"))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing branch states response", e)
                    Result.Error("Error parsing response: ${e.message}")
                }
            } else {
                val errorStream = connection.errorStream?.bufferedReader()?.use { it.readText() }
                Log.e(TAG, "HTTP Error for branch states: $responseCode, Error stream: $errorStream")
                Result.Error("Failed to fetch branch states: HTTP $responseCode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching branch states", e)
            Result.Error("Error fetching branch states: ${e.message}")
        }
    }

    suspend fun getBranchLocations(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/get_branch_locations.php")
            Log.d(TAG, "Attempting to connect to: ${url.toString()}")
            
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            Log.d(TAG, "Connection established, sending request...")
            
            val responseCode = connection.responseCode
            Log.d(TAG, "Response code for branch locations: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d(TAG, "Raw response for branch locations: $response")
                
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getBoolean("success")) {
                        val dataArray = jsonResponse.getJSONArray("data")
                        val locations = mutableListOf<String>()
                        
                        for (i in 0 until dataArray.length()) {
                            locations.add(dataArray.getString(i))
                        }
                        
                        Result.Success(locations)
                    } else {
                        Result.Error(jsonResponse.getString("message"))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing branch locations response", e)
                    Result.Error("Error parsing response: ${e.message}")
                }
            } else {
                val errorStream = connection.errorStream?.bufferedReader()?.use { it.readText() }
                Log.e(TAG, "HTTP Error for branch locations: $responseCode, Error stream: $errorStream")
                Result.Error("Failed to fetch branch locations: HTTP $responseCode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching branch locations", e)
            Result.Error("Error fetching branch locations: ${e.message}")
        }
    }

    suspend fun getFilteredBankers(
        vendorBank: String,
        loanType: String,
        state: String,
        location: String
    ): Result<List<Banker>> = withContext(Dispatchers.IO) {
        try {
            val encodedVendorBank = URLEncoder.encode(vendorBank, "UTF-8")
            val encodedLoanType = URLEncoder.encode(loanType, "UTF-8")
            val encodedState = URLEncoder.encode(state, "UTF-8")
            val encodedLocation = URLEncoder.encode(location, "UTF-8")
            
            val url = URL("$baseUrl/get_filtered_bankers.php?vendor_bank=$encodedVendorBank&loan_type=$encodedLoanType&state=$encodedState&location=$encodedLocation")
            Log.d(TAG, "Fetching filtered bankers from: ${url.toString()}")
            
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            val responseCode = connection.responseCode
            Log.d(TAG, "Response code for filtered bankers: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d(TAG, "Raw response for filtered bankers: $response")
                
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getBoolean("success")) {
                        val bankersArray = jsonResponse.getJSONArray("data")
                        val bankers = mutableListOf<Banker>()
                        
                        for (i in 0 until bankersArray.length()) {
                            val bankerObj = bankersArray.getJSONObject(i)
                            bankers.add(
                                Banker(
                                    id = bankerObj.getInt("id"),
                                    vendorBank = bankerObj.getString("vendor_bank"),
                                    bankerName = bankerObj.getString("banker_name"),
                                    phoneNumber = bankerObj.getString("phone_number"),
                                    emailId = bankerObj.getString("email_id"),
                                    bankerDesignation = bankerObj.getString("banker_designation"),
                                    loanType = bankerObj.getString("loan_type"),
                                    state = bankerObj.getString("state"),
                                    location = bankerObj.getString("location"),
                                    visitingCard = bankerObj.getString("visiting_card"),
                                    address = bankerObj.getString("address")
                                )
                            )
                        }
                        
                        Log.d(TAG, "Successfully parsed ${bankers.size} bankers")
                        return@withContext Result.Success(bankers)
                    } else {
                        val errorMessage = jsonResponse.getString("message")
                        Log.e(TAG, "API returned error for filtered bankers: $errorMessage")
                        return@withContext Result.Error(errorMessage)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing JSON response for filtered bankers", e)
                    return@withContext Result.Error("Error parsing response: ${e.message}")
                }
            } else {
                val errorStream = connection.errorStream?.bufferedReader()?.use { it.readText() }
                Log.e(TAG, "HTTP Error for filtered bankers: $responseCode, Error stream: $errorStream")
                return@withContext Result.Error("Failed to fetch filtered bankers. Response code: $responseCode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error occurred while fetching filtered bankers", e)
            return@withContext Result.Error("Network error: ${e.message}")
        }
    }

    suspend fun getBankersByVendorBank(vendorBank: String): Result<List<Banker>> = withContext(Dispatchers.IO) {
        try {
            val encodedVendorBank = URLEncoder.encode(vendorBank, "UTF-8")
            val url = URL("$baseUrl/get_bankers_by_vendor.php?vendor_bank=$encodedVendorBank")
            Log.d(TAG, "Fetching bankers for vendor bank from: ${url.toString()}")
            
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            val responseCode = connection.responseCode
            Log.d(TAG, "Response code for bankers by vendor bank: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d(TAG, "Raw response for bankers by vendor bank: $response")
                
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getBoolean("success")) {
                        val bankersArray = jsonResponse.getJSONArray("data")
                        val bankers = mutableListOf<Banker>()
                        
                        for (i in 0 until bankersArray.length()) {
                            val bankerObj = bankersArray.getJSONObject(i)
                            bankers.add(
                                Banker(
                                    id = bankerObj.getInt("id"),
                                    vendorBank = bankerObj.getString("vendor_bank"),
                                    bankerName = bankerObj.getString("banker_name"),
                                    phoneNumber = bankerObj.getString("phone_number"),
                                    emailId = bankerObj.getString("email_id"),
                                    bankerDesignation = bankerObj.getString("banker_designation"),
                                    loanType = bankerObj.getString("loan_type"),
                                    state = bankerObj.getString("state"),
                                    location = bankerObj.getString("location"),
                                    visitingCard = bankerObj.getString("visiting_card"),
                                    address = bankerObj.getString("address")
                                )
                            )
                        }
                        
                        Log.d(TAG, "Successfully parsed ${bankers.size} bankers for vendor bank")
                        Result.Success(bankers)
                    } else {
                        val errorMessage = jsonResponse.getString("message")
                        Log.e(TAG, "API returned error for bankers by vendor bank: $errorMessage")
                        Result.Error(errorMessage)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing JSON response for bankers by vendor bank", e)
                    Result.Error("Error parsing response: ${e.message}")
                }
            } else {
                val errorStream = connection.errorStream?.bufferedReader()?.use { it.readText() }
                Log.e(TAG, "HTTP Error for bankers by vendor bank: $responseCode, Error stream: $errorStream")
                Result.Error("Failed to fetch bankers by vendor bank. Response code: $responseCode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error occurred while fetching bankers by vendor bank", e)
            Result.Error("Network error: ${e.message}")
        }
    }

    private suspend fun getVendorBankId(name: String): Int? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$baseUrl/get_vendor1_bank_id.php?name=${URLEncoder.encode(name, "UTF-8")}")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getBoolean("success")) {
                        jsonResponse.getInt("id")
                    } else null
                } else null
            } catch (e: Exception) {
                Log.e(TAG, "Error getting vendor bank ID", e)
                null
            }
        }
    }

    private suspend fun getLoanTypeId(name: String): Int? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$baseUrl/get_loan1_type_id.php?name=${URLEncoder.encode(name, "UTF-8")}")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getBoolean("success")) {
                        jsonResponse.getInt("id")
                    } else null
                } else null
            } catch (e: Exception) {
                Log.e(TAG, "Error getting loan type ID", e)
                null
            }
        }
    }

    private suspend fun getStateId(name: String): Int? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$baseUrl/get_state1_id.php?name=${URLEncoder.encode(name, "UTF-8")}")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getBoolean("success")) {
                        jsonResponse.getInt("id")
                    } else null
                } else null
            } catch (e: Exception) {
                Log.e(TAG, "Error getting state ID", e)
                null
            }
        }
    }

    private suspend fun getLocationId(name: String): Int? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$baseUrl/get_location1_id.php?name=${URLEncoder.encode(name, "UTF-8")}")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getBoolean("success")) {
                        jsonResponse.getInt("id")
                    } else null
                } else null
            } catch (e: Exception) {
                Log.e(TAG, "Error getting location ID", e)
                null
            }
        }
    }

    suspend fun addBanker(
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
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/add_banker.php")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Connection", "close")
            connection.doOutput = true
            connection.connectTimeout = 15000 // Increased timeout
            connection.readTimeout = 15000 // Increased timeout
            connection.useCaches = false // Disable caching

            val requestBody = JSONObject().apply {
                put("vendor_bank", vendorBank)
                put("banker_name", bankerName)
                put("phone_number", phoneNumber)
                put("email_id", emailId)
                put("banker_designation", bankerDesignation)
                put("loan_type", loanType)
                put("state", state)
                put("location", location)
                put("visiting_card", visitingCard)
                put("address", address)
            }.toString()

            Log.d(TAG, "Sending request to add banker: $requestBody")

            try {
                connection.outputStream.use { os ->
                    os.write(requestBody.toByteArray(Charsets.UTF_8))
                    os.flush()
                }

                val responseCode = connection.responseCode
                Log.d(TAG, "Response code: $responseCode")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d(TAG, "Raw response: $response")

                    try {
                        val jsonResponse = JSONObject(response)
                        if (jsonResponse.getBoolean("success")) {
                            val bankerId = jsonResponse.getJSONObject("data").getInt("id")
                            Log.d(TAG, "Successfully added banker with ID: $bankerId")
                            Result.Success(bankerId)
                        } else {
                            val errorMessage = jsonResponse.getString("message")
                            Log.e(TAG, "API returned error: $errorMessage")
                            Result.Error(errorMessage)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing JSON response", e)
                        Result.Error("Error parsing response: ${e.message}")
                    }
                } else {
                    val errorStream = connection.errorStream?.bufferedReader()?.use { it.readText() }
                    Log.e(TAG, "HTTP Error: $responseCode, Error stream: $errorStream")
                    Result.Error("Failed to add banker. Response code: $responseCode")
                }
            } finally {
                connection.disconnect()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error occurred", e)
            Result.Error("Network error: ${e.message}")
        }
    }

    private fun getMimeType(file: File): String {
        return when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "pdf" -> "application/pdf"
            else -> "application/octet-stream"
        }
    }
} 
