package com.cloud.gurkasapp.models

data class ObtenerPersonalFacialResponse(
    val data: List<PersonalFacialItem>?
)

data class PersonalFacialItem(
    val idFacial: Int?,
    val codigo: String?,
    val embedding: String?,
    val cantidadMuestras: Int?,
    val modelo: String?,
    val dimensionEmbedding: Int?,
    val fotoUrl: String?,
    val fechaRegistro: String?,
    val fechaActualizacion: String?,
    val activo: Boolean?,
    val usuarioRegistro: String?
)