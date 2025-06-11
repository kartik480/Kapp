package com.kurakulas.app.data.repository

import com.kurakulas.app.data.model.State
import com.kurakulas.app.data.model.Location
import com.kurakulas.app.data.model.Sublocation
import pzn.api.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getStates(): List<State> {
        return try {
            val response = apiService.getStates()
            if (response.isSuccessful) {
                response.body()?.data?.mapIndexed { index, name ->
                    State(index.toString(), name)
                } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getLocationsForState(stateId: String): List<Location> {
        return try {
            val response = apiService.getLocations(stateId)
            if (response.isSuccessful) {
                response.body()?.data?.mapIndexed { index, name ->
                    Location(index.toString(), stateId, name)
                } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getSublocationsForLocation(locationId: String): List<Sublocation> {
        return try {
            val response = apiService.getSublocations(locationId)
            if (response.isSuccessful) {
                response.body()?.data?.mapIndexed { index, name ->
                    Sublocation(index.toString(), locationId, name)
                } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
} 
