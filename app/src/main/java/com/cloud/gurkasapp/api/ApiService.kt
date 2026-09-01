package com.cloud.gurkasapp.api

import android.R
import com.cloud.gurkasapp.models.SedeResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("apersonal/UbicacionSedemovil")
    fun obtenerSedes(
        @Query("latitud") latitud: Double,
        @Query("longitud") longitud: Double
    ): Call<SedeResponse>

    @GET("apersonal/FeriadoMovil")
    fun obtenerFeriado(
        @Query("fecha") fecha: R.string
    ): Call<SedeResponse>
}