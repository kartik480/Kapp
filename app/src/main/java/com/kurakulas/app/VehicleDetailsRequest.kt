package pzn.api

data class VehicleDetailsRequest(
    val vehicle_number: String,
    val vehicle_make: String,
    val vehical_modal: String,
    val manufacture_year: String,
    val engine_number: String,
    val chases_number: String
) 
