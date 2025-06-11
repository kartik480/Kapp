package com.kurakulas.app.data.model

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
