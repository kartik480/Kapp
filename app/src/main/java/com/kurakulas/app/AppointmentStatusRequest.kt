package pzn.api

data class AppointmentStatusRequest(
    val appt_id: Int? = null,
    val appt_bank: String,
    val appt_product: String,
    val appt_status: String,
    val appt_sub_status: String,
    val notes: String
) 
