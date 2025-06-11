package pzn.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {
    @POST("add_appointment.php")
    suspend fun addAppointment(
        @Body request: com.kurakulas.app.data.model.AddAppointmentRequest
    ): Response<ApiResponse<Unit>>

    @POST("add_banker.php")
    suspend fun addBanker(
        @Body request: BankerRequest
    ): Response<ApiResponse<Unit>>

    @GET("get_customer_types.php")
    suspend fun getCustomerTypes(): Response<ApiResponse<List<String>>>

    @GET("get_states.php")
    suspend fun getStates(): Response<ApiResponse<List<String>>>

    @GET("get_locations.php")
    suspend fun getLocations(
        @Query("state_id") stateId: String
    ): Response<ApiResponse<List<String>>>

    @GET("get_sublocations.php")
    suspend fun getSublocations(
        @Query("location_id") locationId: String
    ): Response<ApiResponse<List<String>>>

    @GET("get_pincodes.php")
    suspend fun getPincodes(): Response<ApiResponse<List<String>>>

    @GET("get_banks.php")
    suspend fun getBanks(): Response<ApiResponse<List<String>>>

    @GET("get_portfolio_banks.php")
    suspend fun getPortfolioBanks(): Response<ApiResponse<List<String>>>

    @GET("get_loan_types.php")
    suspend fun getLoanTypes(): Response<ApiResponse<List<String>>>

    @GET("get_roi.php")
    suspend fun getRoiOptions(): Response<ApiResponse<List<String>>>

    @GET("get_tenure.php")
    suspend fun getTenureOptions(): Response<ApiResponse<List<String>>>

    @GET("get_vehicle_makes.php")
    suspend fun getVehicleMakes(): Response<ApiResponse<List<String>>>

    @GET("get_vehicle_models.php")
    suspend fun getVehicleModels(): Response<ApiResponse<List<String>>>

    @GET("get_manufacturing_years.php")
    suspend fun getManufacturingYears(): Response<ApiResponse<List<String>>>

    @GET("get_property_types.php")
    suspend fun getPropertyTypes(): Response<ApiResponse<List<String>>>

    @GET("get_appointment_banks.php")
    suspend fun getAppointmentBanks(): Response<ApiResponse<List<String>>>

    @GET("get_appointment_products.php")
    suspend fun getAppointmentProducts(): Response<ApiResponse<List<String>>>

    @GET("get_appointment_statuses.php")
    suspend fun getAppointmentStatuses(): Response<ApiResponse<List<String>>>

    @GET("get_appointment_sub_statuses.php")
    suspend fun getAppointmentSubStatuses(): Response<ApiResponse<List<String>>>

    @GET("get_bank_account_types.php")
    suspend fun getBankAccountTypes(): Response<ApiResponse<List<String>>>

    @GET("get_dsa_dropdown_options.php")
    suspend fun getDsaDropdownOptions(): Response<ApiResponse<DsaDropdownOptions>>

    @GET("get_filtered_dsa_codes.php")
    suspend fun getFilteredDsaCodes(
        @Query("vendor_bank") vendorBank: String,
        @Query("loan_type") loanType: String,
        @Query("state") state: String,
        @Query("location") location: String
    ): Response<ApiResponse<List<DsaCodeData>>>

    @POST("upload_visiting_card.php")
    suspend fun uploadVisitingCard(
        @Body requestBody: MultipartBody
    ): Response<ApiResponse<String>>

    @POST("add_bank_account.php")
    @Headers("Accept: application/json")
    suspend fun addBankAccount(
        @Body request: BankAccountRequest
    ): Response<ApiResponse<Unit>>

    @POST("add_relationship_with_bank.php")
    suspend fun addRelationshipWithBank(@Body request: RelationshipWithBankRequest): Response<ApiResponse<Unit>>

    @POST("add_vehicle_details.php")
    suspend fun addVehicleDetails(@Body request: VehicleDetailsRequest): Response<ApiResponse<Unit>>

    @POST("add_property_details.php")
    suspend fun addPropertyDetails(@Body request: PropertyDetailsRequest): Response<PropertyDetailsResponse>

    @POST("add_credit_card_details.php")
    suspend fun addCreditCardDetails(
        @Body request: CreditCardDetailsRequest
    ): Response<ApiResponse<Unit>>

    @POST("add_appointment_status.php")
    suspend fun addAppointmentStatus(
        @Body request: AppointmentStatusRequest
    ): Response<ApiResponse<Unit>>

    @POST("add_personal_info.php")
    suspend fun addPersonalInfo(
        @Body request: PersonalInfoRequest
    ): Response<ApiResponse<Unit>>

    @Multipart
    @POST("upload_banker_visiting_card.php")
    suspend fun uploadBankerVisitingCard(@Body requestBody: MultipartBody): Response<ApiResponse<String>>
}

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T
)

data class DsaDropdownOptions(
    val vendor_banks: List<String>,
    val loan_types: List<String>,
    val branch_states: List<String>,
    val branch_locations: List<String>
)

data class CustomerTypesResponse(
    val status: String,
    val data: List<String>
)

data class StatesResponse(
    val status: String,
    val data: List<String>
)

data class LocationsResponse(
    val status: String,
    val data: List<String>
)

data class SublocationsResponse(
    val status: String,
    val data: List<String>
)

data class PincodesResponse(
    val status: String,
    val data: List<String>
)

data class BankAccountRequest(
    val b_bank_name: String,
    val b_account_type: String,
    val b_account_no: String,
    val b_branch_name: String,
    val b_ifsc_code: String
)

data class BankAccountResponse(
    val success: Boolean,
    val message: String?,
    val data: String?
)

data class PropertyDetailsRequest(
    val database_id: Int,
    val p_property_type: String,
    val p_area: String,
    val p_lands: String,
    val p_sft: String,
    val p_market_value: String
)

data class PropertyDetailsResponse(
    val status: String,
    val message: String,
    val insert_id: Int? = null,
    val table: String? = null,
    val count: Int? = null,
    val data: List<PropertyDetailsData>? = null
)

data class PropertyDetailsData(
    val id: Int,
    val database_id: Int,
    val p_property_type: String,
    val p_area: String,
    val p_lands: String,
    val p_sft: String,
    val p_market_value: String
) 
