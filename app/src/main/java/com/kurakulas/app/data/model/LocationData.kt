package com.kurakulas.app.data.model

data class State(
    val id: String,
    val name: String
)

data class Location(
    val id: String,
    val stateId: String,
    val name: String
)

data class Sublocation(
    val id: String,
    val locationId: String,
    val name: String
) 
