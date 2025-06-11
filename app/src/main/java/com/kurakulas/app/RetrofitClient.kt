package pzn.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.google.gson.GsonBuilder
import com.google.gson.Gson
import com.kurakulas.app.data.api.ApiConfig
import java.util.concurrent.TimeUnit
import android.util.Log

object RetrofitClient {
    private val TAG = "RetrofitClient"
    
    private val gson: Gson = GsonBuilder()
        .setLenient()
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .create()

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d(TAG, "OkHttp: $message")
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val request = chain.request()
            Log.d(TAG, "=== Making Request ===")
            Log.d(TAG, "URL: ${request.url}")
            Log.d(TAG, "Method: ${request.method}")
            Log.d(TAG, "Headers: ${request.headers}")
            
            val response = chain.proceed(request)
            Log.d(TAG, "=== Received Response ===")
            Log.d(TAG, "Code: ${response.code}")
            Log.d(TAG, "Message: ${response.message}")
            Log.d(TAG, "Headers: ${response.headers}")
            
            response
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
