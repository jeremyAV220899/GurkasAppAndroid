package com.cloud.gurkasapp.models

data class SedeResponse(
    val lista: List<Sede>
)

data class Sede(
    val codUnidad: String?,
    val codsede: String?,
    val nombreComercial: String?,
    val aliasSede: String?
)