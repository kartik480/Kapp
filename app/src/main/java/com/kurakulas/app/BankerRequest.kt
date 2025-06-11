package pzn.api

data class BankerRequest(
    val vendor_bank: String,
    val banker_name: String,
    val phone_number: String,
    val email_id: String,
    val banker_designation: String,
    val loan_type: String,
    val state: String,
    val location: String,
    val visiting_card: String,
    val address: String
) 
