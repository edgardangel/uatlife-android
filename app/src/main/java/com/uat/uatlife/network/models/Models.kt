package com.uat.uatlife.network.models

import com.google.gson.annotations.SerializedName

// ============================================================
// REQUESTS (enviados al backend)
// ============================================================

data class LoginRequest(
    val identificador: String,
    val password: String
)

data class RegisterRequest(
    @SerializedName("nombre_completo") val nombreCompleto: String,
    val matricula: String,
    @SerializedName("correo_institucional") val correoInstitucional: String,
    val password: String,
    @SerializedName("facultad_id") val facultadId: Int? = null
)

data class CrearPublicacionRequest(
    @SerializedName("contenido_texto") val contenidoTexto: String?,
    @SerializedName("url_multimedia") val urlMultimedia: String? = null,
    @SerializedName("comunidad_id") val comunidadId: Int? = null
)

data class ReaccionRequest(
    @SerializedName("tipo_reaccion") val tipoReaccion: String = "like"
)

data class ComentarRequest(
    val contenido: String,
    @SerializedName("parent_id") val parentId: Int? = null
)

data class CrearProductoRequest(
    val titulo: String,
    val descripcion: String?,
    val precio: Double,
    @SerializedName("categoria_id") val categoriaId: Int?,
    val condicion: String,
    @SerializedName("facultad_id") val facultadId: Int?,
    @SerializedName("url_foto_principal") val urlFotoPrincipal: String? = null,
    @SerializedName("hora_inicio") val horaInicio: String? = null,
    @SerializedName("hora_fin") val horaFin: String? = null
)

data class EnviarMensajeRequest(
    @SerializedName("destinatario_id") val destinatarioId: Int,
    val contenido: String,
    @SerializedName("producto_id") val productoId: Int? = null
)

data class CrearComunidadRequest(
    val nombre: String,
    val descripcion: String?,
    val tipo: String = "publica",
    @SerializedName("facultad_id") val facultadId: Int? = null
)

data class ReporteBusRequest(
    @SerializedName("tipo_objetivo") val tipoObjetivo: String = "bus",
    @SerializedName("objetivo_id") val objetivoId: Int = 0,
    val motivo: String = "otro",
    val descripcion: String
)

data class CrearReporteRequest(
    @SerializedName("tipo_objetivo") val tipoObjetivo: String,
    @SerializedName("objetivo_id") val objetivoId: Int,
    val motivo: String,
    val descripcion: String? = null
)

// ============================================================
// RESPONSES (recibidos del backend)
// ============================================================

data class AuthResponse(
    val mensaje: String,
    val token: String,
    val usuario: UserProfile
)

data class VerifyOtpResponse(
    val mensaje: String,
    val resetToken: String?
)

data class UserProfile(
    val id: Int,
    @SerializedName("nombre_completo") val nombreCompleto: String,
    val matricula: String,
    @SerializedName("correo_institucional") val correoInstitucional: String,
    @SerializedName("facultad_id") val facultadId: Int?,
    @SerializedName("facultad_nombre") val facultadNombre: String? = null,
    @SerializedName("puntos_confianza") val puntosConfianza: Int,
    @SerializedName("racha_dias") val rachaDias: Int? = 0,
    @SerializedName("estatus_validacion") val estatusValidacion: String,
    @SerializedName("tipo_usuario") val tipoUsuario: String,
    @SerializedName("url_ficha_pago") val urlFichaPago: String? = null,
    @SerializedName("url_horario_img") val urlHorario: String? = null,
    @SerializedName("url_foto_perfil") val urlFotoPerfil: String? = null,
    val bio: String? = null,
    @SerializedName("semestre_actual") val semestreActual: Int? = null,
    @SerializedName("correo_secundario") val correoSecundario: String? = null,
    val telefono: String? = null,
    @SerializedName("suspension_hasta") val suspensionHasta: String? = null,
    @SerializedName("ban_permanente") val banPermanente: Boolean = false,
    @SerializedName("created_at") val createdAt: String? = null
)

data class ParadaAutobus(
    val id: Int,
    val nombre: String,
    val abreviatura: String?,
    val latitud: Double?,
    val longitud: Double?
)

data class Publicacion(
    val id: Int,
    @SerializedName("contenido_texto") val contenidoTexto: String?,
    @SerializedName("url_multimedia") val urlMultimedia: String?,
    @SerializedName("estado_moderacion") val estadoModeracion: String,
    @SerializedName("es_fijada") val esFijada: Boolean,
    @SerializedName("fecha_creacion") val fechaCreacion: String,
    @SerializedName("autor_id") val autorId: Int,
    @SerializedName("autor_nombre") val autorNombre: String,
    @SerializedName("autor_foto") val autorFoto: String?,
    @SerializedName("autor_tipo") val autorTipo: String,
    @SerializedName("autor_facultad") val autorFacultad: String?,
    @SerializedName("total_reacciones") val totalReacciones: Int,
    @SerializedName("total_comentarios") val totalComentarios: Int,
    @SerializedName("mi_reaccion") val miReaccion: String?
)

data class Comentario(
    val id: Int,
    val contenido: String,
    @SerializedName("fecha_creacion") val fechaCreacion: String,
    @SerializedName("parent_id") val parentId: Int?,
    @SerializedName("autor_id") val autorId: Int,
    @SerializedName("autor_nombre") val autorNombre: String,
    @SerializedName("autor_foto") val autorFoto: String?
)

data class Producto(
    val id: Int,
    val titulo: String,
    val descripcion: String?,
    val precio: Double,
    val condicion: String,
    @SerializedName("url_foto_principal") val urlFotoPrincipal: String?,
    @SerializedName("es_premium") val esPremium: Boolean,
    @SerializedName("esta_vendido") val estaVendido: Boolean,
    @SerializedName("fecha_publicacion") val fechaPublicacion: String,
    @SerializedName("vendedor_id") val vendedorId: Int,
    @SerializedName("vendedor_nombre") val vendedorNombre: String,
    @SerializedName("vendedor_confianza") val vendedorConfianza: Int,
    @SerializedName("vendedor_foto") val vendedorFoto: String?,
    val categoria: String?,
    @SerializedName("facultad_entrega") val facultadEntrega: String?,
    @SerializedName("calificacion_promedio") val calificacionPromedio: Double,
    @SerializedName("total_resenas") val totalResenas: Int,
    @SerializedName("hora_inicio") val horaInicio: String? = null,
    @SerializedName("hora_fin") val horaFin: String? = null
)

data class Categoria(
    val id: Int,
    val nombre: String
)

data class Conversacion(
    val id: Int,
    @SerializedName("ultima_actividad") val ultimaActividad: String,
    @SerializedName("otro_usuario_id") val otroUsuarioId: Int,
    @SerializedName("otro_usuario_nombre") val otroUsuarioNombre: String,
    @SerializedName("otro_usuario_foto") val otroUsuarioFoto: String?,
    @SerializedName("ultimo_mensaje") val ultimoMensaje: String?,
    @SerializedName("mensajes_no_leidos") val mensajesNoLeidos: Int
)

data class Mensaje(
    val id: Int,
    val contenido: String,
    val tipo: String,
    val leido: Boolean,
    @SerializedName("fecha_envio") val fechaEnvio: String,
    @SerializedName("emisor_id") val emisorId: Int,
    @SerializedName("emisor_nombre") val emisorNombre: String,
    @SerializedName("emisor_foto") val emisorFoto: String?
)

data class Comunidad(
    val id: Int,
    val nombre: String,
    val descripcion: String?,
    val tipo: String,
    @SerializedName("es_oficial") val esOficial: Boolean,
    @SerializedName("url_banner") val urlBanner: String?,
    @SerializedName("total_miembros") val totalMiembros: Int,
    @SerializedName("es_miembro") val esMiembro: Boolean
)

// Respuestas genéricas
data class MensajeResponse(val mensaje: String)

data class ReaccionResponse(val total: Int)

data class CrearPublicacionResponse(
    val mensaje: String,
    val publicacion: Publicacion
)

data class CrearProductoResponse(
    val mensaje: String,
    val producto: Producto
)

data class CrearComunidadResponse(
    val mensaje: String,
    val comunidad: Comunidad
)

data class EstadisticasResponse(
    val estadisticas: EstadisticasPlataforma,
    @SerializedName("top_emprendedores") val topEmprendedores: List<TopEmprendedor>
)

data class EstadisticasPlataforma(
    @SerializedName("total_usuarios") val totalUsuarios: Int,
    @SerializedName("total_alumnos") val totalAlumnos: Int,
    @SerializedName("pendientes_validacion") val pendientesValidacion: Int,
    @SerializedName("publicaciones_activas") val publicacionesActivas: Int,
    @SerializedName("productos_en_venta") val productosEnVenta: Int,
    @SerializedName("reportes_bus_hoy") val reportesBusHoy: Int,
    @SerializedName("reportes_pendientes") val reportesPendientes: Int,
    @SerializedName("total_comunidades") val totalComunidades: Int
)

data class TopEmprendedor(
    val id: Int,
    @SerializedName("nombre_completo") val nombreCompleto: String,
    val facultad: String?,
    @SerializedName("puntos_confianza") val puntosConfianza: Int,
    @SerializedName("total_productos_activos") val totalProductosActivos: Int,
    @SerializedName("total_productos_vendidos") val totalProductosVendidos: Int,
    @SerializedName("promedio_calificacion") val promedioCalificacion: Double,
    @SerializedName("volumen_ventas_estimado") val volumenVentasEstimado: Double,
    @SerializedName("score_emprendedor") val scoreEmprendedor: Double
)

data class ErrorResponse(val error: String)
data class ApiInfoResponse(val mensaje: String, val version: String)
