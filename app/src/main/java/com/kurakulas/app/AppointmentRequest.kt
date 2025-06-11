package pzn.api

data class AppointmentRequest(
    val database_id: String,
    val mobile_number: String,
    val lead_name: String,
    val email_id: String,
    val company_name: String,
    val alternative_mobile: String,
    val state: String,
    val location: String,
    val sub_location: String,
    val pin_code: String,
    val source: String,
    val visiting_card: String,
    val user_qualification: String,
    val residental_address: String,
    val customer_type: String,
    val appt_bank: String,
    val appt_product: String,
    val appt_status: String,
    val appt_sub_status: String
) 
