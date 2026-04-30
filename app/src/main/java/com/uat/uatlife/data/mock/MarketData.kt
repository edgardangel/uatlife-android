package com.uat.uatlife.data.mock

data class ProductoMock(
    val nombre: String,
    val vendedor: String,
    val precio: String,
    val categoria: String,
    val rating: Float,
    val isPremium: Boolean,
    val location: String,
    val descripcion: String = "Excelente producto garantizado. Entrego en punto medio o facultades."
)

val categoriasMercado = listOf("Todos", "Comida", "Papeleria", "Servicios", "Tecnologia", "Ropa")

val productosMock = listOf(
    ProductoMock("Hamburguesa Clásica Doble", "Snack FCB", "$65", "Comida", 4.8f, true, "FACULTAD DE CIENCIAS", "Hamburguesa con doble carne de res, queso amarillo, tocino, lechuga y tomate. Va con papas a la francesa naturales."),
    ProductoMock("Libretas Universitarias 100...", "Ana López", "$120", "Papeleria", 5.0f, false, "ARQUITECTURA", "Libretas profesionales cuadro chico, portadas de colores neon y pasta dura resistente. Nuevas."),
    ProductoMock("Formateo de Laptops y PC", "Carlos Ing.", "$250", "Servicios", 4.9f, true, "INGENIERÍA", "Realizo formateo, instalación de Windows 10/11, Office y programas de ingeniería (AutoCAD, SolidWorks). Trabajos el mismo día."),
    ProductoMock("Orden de Tacos de Trompo (5)", "Kiosko Central", "$45", "Comida", 4.5f, false, "PLAZA PRINCIPAL", "Riquísimos tacos de trompo estilo regio con doble tortilla, incluye cebolla y cilantro aparte y salsa verde picosa."),
    ProductoMock("Playera Lobo Edición", "Tienda UAT", "$250", "Ropa", 4.3f, true, "GIMNASIO", "Playera tipo dry-fit con tecnología deportiva, ideal para ir al gimnasio o torneos universitarios. Tallas M y L."),
    ProductoMock("Cargador Tipo C Carga Rápida", "Pedro Tech", "$120", "Tecnologia", 4.0f, false, "FIT", "Adaptador y cable de 20W carga super rápida. Compatible con Samsung, Motorola y cubitos recientes. Nuevo en caja.")
)
