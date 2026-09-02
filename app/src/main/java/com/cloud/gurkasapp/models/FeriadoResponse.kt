package com.cloud.gurkasapp.models

data class FeriadoResponse(
    val lista: List<FeriadoItem>?
)

data class FeriadoItem(
    val codigoAsistencia: String?,
    val tipoAsistencia: String?
)