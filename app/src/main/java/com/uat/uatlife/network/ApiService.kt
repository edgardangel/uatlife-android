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

    @Multipart
    @POST("api/auth/register")
    suspend fun register(
        @Part("nombre_completo") nombre: okhttp3.RequestBody,
        @Part("matricula") matricula: okhttp3.RequestBody,
        @Part("correo_institucional") correo: okhttp3.RequestBody,
        @Part("password") password: okhttp3.RequestBody,
        @Part("facultad_id") facultadId: okhttp3.RequestBody?,
        @Part ficha: okhttp3.MultipartBody.Part?
    ): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("api/auth/profile")
    suspend fun getProfile(): Response<UserProfile>

    @Multipart
    @PUT("api/auth/profile")
    suspend fun updateProfile(
        @Part("nombre_completo") nombreCompleto: okhttp3.RequestBody?,
        @Part("bio") bio: okhttp3.RequestBody?,
        @Part foto: okhttp3.MultipartBody.Part?
    ): Response<com.uat.uatlife.network.models.UpdateProfileResponse>

    @Multipart
    @POST("api/auth/profile/documento")
    suspend fun uploadDocumento(
        @Part imagen: okhttp3.MultipartBody.Part
    ): Response<MensajeResponse>

    @PUT("api/auth/security")
    suspend fun updateSecurityInfo(@Body request: okhttp3.RequestBody): Response<MensajeResponse>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: okhttp3.RequestBody): Response<MensajeResponse>

    @POST("api/auth/verify-otp")
    suspend fun verifyOtp(@Body request: okhttp3.RequestBody): Response<VerifyOtpResponse>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: okhttp3.RequestBody): Response<MensajeResponse>

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

    @Multipart
    @POST("api/publicaciones")
    suspend fun crearPublicacion(
        @Part("contenido_texto") contenido: okhttp3.RequestBody?,
        @Part("comunidad_id") comunidadId: okhttp3.RequestBody?,
        @Part imagen: okhttp3.MultipartBody.Part?
    ): Response<CrearPublicacionResponse>

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

    @Multipart
    @POST("api/productos")
    suspend fun crearProducto(
        @Part("titulo") titulo: okhttp3.RequestBody,
        @Part("descripcion") descripcion: okhttp3.RequestBody,
        @Part("precio") precio: okhttp3.RequestBody,
        @Part("condicion") condicion: okhttp3.RequestBody,
        @Part("categoria_id") categoriaId: okhttp3.RequestBody?,
        @Part foto: okhttp3.MultipartBody.Part?
    ): Response<CrearProductoResponse>

    @Multipart
    @PUT("api/productos/{id}")
    suspend fun actualizarProducto(
        @Path("id") id: Int,
        @Part("titulo") titulo: okhttp3.RequestBody,
        @Part("descripcion") descripcion: okhttp3.RequestBody?,
        @Part("precio") precio: okhttp3.RequestBody,
        @Part("condicion") condicion: okhttp3.RequestBody,
        @Part("categoria_id") categoriaId: okhttp3.RequestBody?,
        @Part foto: okhttp3.MultipartBody.Part? = null
    ): Response<CrearProductoResponse>

    @PATCH("api/productos/{id}/status")
    suspend fun patchProductStatus(
        @Path("id") id: Int,
        @Body body: StatusRequest
    ): Response<MensajeResponse>

    @DELETE("api/productos/{id}")
    suspend fun eliminarProducto(@Path("id") id: Int): Response<MensajeResponse>

    // ==================== MENSAJERÍA ====================

    @GET("api/mensajes/conversaciones")
    suspend fun getConversaciones(): Response<List<Conversacion>>

    @GET("api/mensajes/conversaciones/{convId}")
    suspend fun getMensajes(@Path("convId") convId: Int): Response<List<Mensaje>>

    @DELETE("api/mensajes/conversaciones/{convId}")
    suspend fun eliminarConversacion(@Path("convId") convId: Int): Response<MensajeResponse>

    @POST("api/mensajes")
    suspend fun enviarMensaje(@Body request: EnviarMensajeRequest): Response<Mensaje>

    @GET("api/mensajes/usuarios/buscar")
    suspend fun buscarUsuarios(@Query("q") query: String): Response<List<UsuarioBusqueda>>

    // ==================== COMUNIDADES ====================

    @GET("api/comunidades")
    suspend fun getComunidades(): Response<List<Comunidad>>

    @GET("api/comunidades/{id}")
    suspend fun getComunidadById(@Path("id") id: Int): Response<Comunidad>

    @GET("api/comunidades/{id}/miembros")
    suspend fun getMiembros(@Path("id") id: Int): Response<List<MiembroComunidad>>

    @POST("api/comunidades")
    suspend fun crearComunidad(@Body request: CrearComunidadRequest): Response<CrearComunidadResponse>

    @POST("api/comunidades/{id}/unirse")
    suspend fun unirseAComunidad(@Path("id") id: Int): Response<MensajeResponse>

    @DELETE("api/comunidades/{id}/salir")
    suspend fun salirDeComunidad(@Path("id") id: Int): Response<MensajeResponse>

    @DELETE("api/comunidades/{id}")
    suspend fun eliminarComunidad(@Path("id") id: Int): Response<MensajeResponse>

    // ==================== MODERACIÓN ====================

    @POST("api/moderacion/reportes")
    suspend fun crearReporte(@Body request: CrearReporteRequest): Response<MensajeResponse>

    @GET("api/moderacion/reportes")
    suspend fun getReportes(@Query("estado") estado: String = "pendiente"): Response<List<Reporte>>

    @PUT("api/moderacion/reportes/{id}/resolver")
    suspend fun resolverReporte(
        @Path("id") id: Int,
        @Body request: ResolverReporteRequest
    ): Response<MensajeResponse>

    @GET("api/moderacion/estadisticas")
    suspend fun getEstadisticas(): Response<EstadisticasResponse>

    @GET("api/moderacion/validaciones")
    suspend fun getValidacionesPendientes(): Response<List<ValidacionPendiente>>

    @PUT("api/moderacion/validaciones/{id}")
    suspend fun resolverValidacion(
        @Path("id") id: Int,
        @Body request: ResolverValidacionRequest
    ): Response<MensajeResponse>

    @GET("api/moderacion/sancionados")
    suspend fun getSancionados(): Response<List<UsuarioSancionado>>

    @POST("api/moderacion/sancionar")
    suspend fun sancionarUsuario(@Body request: SancionarRequest): Response<MensajeResponse>
}
