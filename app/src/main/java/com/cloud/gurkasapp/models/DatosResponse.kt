package com.cloud.gurkasapp.models

data class DatosResponse(
    val lista: List<DatosItem>?
)

data class DatosItem(
    val nombrecompleto: String?,
    val puesto: String?,
    val correo: String?,
    val doctidentidad: String?,
    val sede: String?,
    val celular: String?,
    val nombre_contacto: String?,
    val celular_c_1: String?,
    val foto: String?
)