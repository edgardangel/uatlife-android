package com.uat.uatlife.network

import android.content.Context
import com.uat.uatlife.data.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton que provee la instancia de Retrofit configurada
 * para comunicarse con el backend de UATLife en Dokploy.
 */
object RetrofitClient {

    // URL base del backend desplegado en Dokploy
    private const val BASE_URL = "https://bd-uat-bus-api-uatlife-xazfaa-1b2660-157-245-239-94.traefik.me/"

    private var apiService: ApiService? = null

    /**
     * Obtiene la instancia de ApiService.
     * Requiere Context para acceder al TokenManager (DataStore).
     */
    fun getApiService(context: Context): ApiService {
        if (apiService == null) {
            val tokenManager = TokenManager(context)

            // Interceptor para inyectar el token JWT en cada petición
            val authInterceptor = Interceptor { chain ->
                val token = runBlocking {
                    tokenManager.getToken().first()
                }
                val request = if (token != null) {
                    chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                } else {
                    chain.request()
                }
                chain.proceed(request)
            }

            // Interceptor de logging para debug
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiService = retrofit.create(ApiService::class.java)
        }
        return apiService!!
    }
}
