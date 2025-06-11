package com.kurakulas.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.kurakulas.app.data.model.LoginResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val context: Context
) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREF_NAME = "KurakulasSession"
        private const val KEY_LOGIN_RESPONSE = "login_response"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    fun saveLoginResponse(loginResponse: LoginResponse) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_LOGIN_RESPONSE, gson.toJson(loginResponse))
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.apply()
    }

    fun getLoginResponse(): LoginResponse? {
        val json = sharedPreferences.getString(KEY_LOGIN_RESPONSE, null)
        return json?.let { gson.fromJson(it, LoginResponse::class.java) }
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun clearSession() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
} 
