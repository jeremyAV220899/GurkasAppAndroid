package com.cloud.gurkasapp.models

data class ResumenAsistenciaResponse(
    val lista: List<ResumenAsistenciaItem>?
)

data class ResumenAsistenciaItem(
    val fecha: String?,
    val cliente: String?,
    val sede: String?,
    val hora: String?,
    val tipoAsistencia: String?,
    val codigoasistencia: String?
)