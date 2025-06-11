package com.kurakulas.app.data.local

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppointmentPointsManager @Inject constructor(
    private val context: Context
) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "AppointmentPoints"
        private const val KEY_SAL_POINTS = "sal_points"
        private const val KEY_SENP_POINTS = "senp_points"
        private const val KEY_SEP_POINTS = "sep_points"
        private const val KEY_NRI_POINTS = "nri_points"
        private const val KEY_EDUCATIONAL_POINTS = "educational_points"
    }

    fun getSalPoints(): Int = sharedPreferences.getInt(KEY_SAL_POINTS, 0)
    fun getSenpPoints(): Int = sharedPreferences.getInt(KEY_SENP_POINTS, 0)
    fun getSepPoints(): Int = sharedPreferences.getInt(KEY_SEP_POINTS, 0)
    fun getNriPoints(): Int = sharedPreferences.getInt(KEY_NRI_POINTS, 0)
    fun getEducationalPoints(): Int = sharedPreferences.getInt(KEY_EDUCATIONAL_POINTS, 0)

    fun incrementSalPoints() {
        val currentPoints = getSalPoints()
        sharedPreferences.edit().putInt(KEY_SAL_POINTS, currentPoints + 1).apply()
    }

    fun incrementSenpPoints() {
        val currentPoints = getSenpPoints()
        sharedPreferences.edit().putInt(KEY_SENP_POINTS, currentPoints + 1).apply()
    }

    fun incrementSepPoints() {
        val currentPoints = getSepPoints()
        sharedPreferences.edit().putInt(KEY_SEP_POINTS, currentPoints + 1).apply()
    }

    fun incrementNriPoints() {
        val currentPoints = getNriPoints()
        sharedPreferences.edit().putInt(KEY_NRI_POINTS, currentPoints + 1).apply()
    }

    fun incrementEducationalPoints() {
        val currentPoints = getEducationalPoints()
        sharedPreferences.edit().putInt(KEY_EDUCATIONAL_POINTS, currentPoints + 1).apply()
    }

    fun clearPoints() {
        sharedPreferences.edit().clear().apply()
    }
} 