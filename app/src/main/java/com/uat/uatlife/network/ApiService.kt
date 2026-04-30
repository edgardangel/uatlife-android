package com.uat.uatlife.network

import com.uat.uatlife.network.models.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Definición de todos los endpoints de la API de UATLife v2.0
 * Base URL: https://bd-uat-bus-api-uatlife-xazfaa-1b2660-157-245-239-94.traefik.me/
 */
interface ApiService {

    // ==================== AUTENTICACIÓN ====================

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("api/auth/profile")
    suspend fun getProfile(): Response<UserProfile>

    // ==================== BUS TRACKER ====================

    @GET("api/bus/paradas")
    suspend fun getParadas(): Response<List<ParadaAutobus>>

    @POST("api/moderacion/reportes")
    suspend fun reportarBus(@Body request: ReporteBusRequest): Response<MensajeResponse>

    // ==================== PUBLICACIONES (FEED) ====================

    @GET("api/publicaciones")
    suspend fun getPublicaciones(
        @Query("comunidad_id") comunidadId: Int? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<List<Publicacion>>

    @POST("api/publicaciones")
    suspend fun crearPublicacion(@Body request: CrearPublicacionRequest): Response<CrearPublicacionResponse>

    @DELETE("api/publicaciones/{id}")
    suspend fun eliminarPublicacion(@Path("id") id: Int): Response<MensajeResponse>

    @POST("api/publicaciones/{id}/reaccion")
    suspend fun reaccionar(
        @Path("id") id: Int,
        @Body request: ReaccionRequest
    ): Response<ReaccionResponse>

    @GET("api/publicaciones/{id}/comentarios")
    suspend fun getComentarios(@Path("id") id: Int): Response<List<Comentario>>

    @POST("api/publicaciones/{id}/comentarios")
    suspend fun comentar(
        @Path("id") id: Int,
        @Body request: ComentarRequest
    ): Response<Comentario>

    // ==================== MARKETPLACE ====================

    @GET("api/productos")
    suspend fun getProductos(
        @Query("categoria_id") categoriaId: Int? = null,
        @Query("busqueda") busqueda: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<List<Producto>>

    @GET("api/productos/{id}")
    suspend fun getProductoById(@Path("id") id: Int): Response<Producto>

    @GET("api/productos/mis-productos")
    suspend fun getMisProductos(): Response<List<Producto>>

    @GET("api/productos/categorias")
    suspend fun getCategorias(): Response<List<Categoria>>

    @POST("api/productos")
    suspend fun crearProducto(@Body request: CrearProductoRequest): Response<CrearProductoResponse>

    @PUT("api/productos/{id}")
    suspend fun actualizarProducto(
        @Path("id") id: Int,
        @Body request: CrearProductoRequest
    ): Response<CrearProductoResponse>

    @DELETE("api/productos/{id}")
    suspend fun eliminarProducto(@Path("id") id: Int): Response<MensajeResponse>

    // ==================== MENSAJERÍA ====================

    @GET("api/mensajes/conversaciones")
    suspend fun getConversaciones(): Response<List<Conversacion>>

    @GET("api/mensajes/conversaciones/{convId}")
    suspend fun getMensajes(@Path("convId") convId: Int): Response<List<Mensaje>>

    @POST("api/mensajes")
    suspend fun enviarMensaje(@Body request: EnviarMensajeRequest): Response<Mensaje>

    // ==================== COMUNIDADES ====================

    @GET("api/comunidades")
    suspend fun getComunidades(): Response<List<Comunidad>>

    @POST("api/comunidades")
    suspend fun crearComunidad(@Body request: CrearComunidadRequest): Response<CrearComunidadResponse>

    @POST("api/comunidades/{id}/unirse")
    suspend fun unirseAComunidad(@Path("id") id: Int): Response<MensajeResponse>

    @DELETE("api/comunidades/{id}/salir")
    suspend fun salirDeComunidad(@Path("id") id: Int): Response<MensajeResponse>

    // ==================== MODERACIÓN ====================

    @POST("api/moderacion/reportes")
    suspend fun crearReporte(@Body request: CrearReporteRequest): Response<MensajeResponse>

    @GET("api/moderacion/estadisticas")
    suspend fun getEstadisticas(): Response<EstadisticasResponse>
}
