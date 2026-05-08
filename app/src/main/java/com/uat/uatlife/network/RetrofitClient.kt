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
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import coil.ImageLoader

/**
 * Singleton que provee la instancia de Retrofit configurada
 * para comunicarse con el backend de UATLife en Dokploy.
 *
 * NOTA: El TrustManager permisivo es solo para debug con el
 * certificado de Traefik/Dokploy en entorno de desarrollo.
 */
object RetrofitClient {

    // URL base por IP para evitar caídas de DNS de traefik.me
    const val BASE_URL = "https://157.245.239.94/"
    private const val ORIGINAL_HOST = "bd-uat-bus-api-uatlife-xazfaa-1b2660-157-245-239-94.traefik.me"

    private var apiService: ApiService? = null

    /**
     * TrustManager que acepta cualquier certificado SSL.
     */
    val unsafeTrustManager = object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }

    fun getUnsafeOkHttpClient(): OkHttpClient {
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, arrayOf<TrustManager>(unsafeTrustManager), SecureRandom())
        }
        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, unsafeTrustManager)
            .hostnameVerifier { _, _ -> true }
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Host", ORIGINAL_HOST)
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    private fun buildUnsafeSslClient(context: Context): OkHttpClient {
        val tokenManager = TokenManager(context)

        // Interceptor JWT y control de expiración global
        val authInterceptor = Interceptor { chain ->
            val token = runBlocking { tokenManager.getToken().first() }
            val requestBuilder = chain.request().newBuilder()
                .header("Host", ORIGINAL_HOST)
            
            if (token != null) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
            
            val response = chain.proceed(requestBuilder.build())
            
            // Si el backend rechaza el token por inválido (403) o expirado (401), limpiamos la sesión
            if (response.code == 401 || response.code == 403) {
                runBlocking { tokenManager.clearSession() }
            }
            
            response
        }

        // Logging para debug
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // SSL Context que confía en cualquier certificado
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, arrayOf<TrustManager>(unsafeTrustManager), SecureRandom())
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .sslSocketFactory(sslContext.socketFactory, unsafeTrustManager)
            .hostnameVerifier { _, _ -> true }
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Obtiene la instancia de ApiService.
     */
    fun getApiService(context: Context): ApiService {
        if (apiService == null) {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(buildUnsafeSslClient(context))
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiService = retrofit.create(ApiService::class.java)
        }
        return apiService!!
    }

    /**
     * Provee un ImageLoader de Coil que confía en certificados inseguros.
     */
    fun getImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .okHttpClient { getUnsafeOkHttpClient() }
            .build()
    }
}
