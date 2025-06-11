package com.kurakulas.app.data.model

data class PropertyDetailsRequest(
    val database_id: Int,
    val p_property_type: String,
    val p_area: String,
    val p_lands: String,
    val p_sft: String,
    val p_market_value: String
) 
