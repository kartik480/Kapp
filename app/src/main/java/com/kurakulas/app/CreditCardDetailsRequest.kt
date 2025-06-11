package pzn.api

data class CreditCardDetailsRequest(
    val database_id: String,
    val c_bank_name: String,
    val c_limit: String
) 
