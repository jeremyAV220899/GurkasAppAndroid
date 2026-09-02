package com.cloud.gurkasapp.api

import com.cloud.gurkasapp.models.DatosResponse
import com.cloud.gurkasapp.models.FeriadoResponse
import com.cloud.gurkasapp.models.ObtenerPersonalFacialResponse
import com.cloud.gurkasapp.models.ResumenAsistenciaResponse
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
        @Query("fecha") fecha: String
    ): Call<FeriadoResponse>

    @GET("apersonal/DatosPersonalMovil")
    fun obtenerDatosPersonal(
        @Query("codigo") codigo: String
    ): Call<DatosResponse>

    @GET("apersonal/ResumenAsistenciaMovil")
    fun obtenerResumenAsistencia(
        @Query("fechainicio") fechainicio: String,
        @Query("fechafin") fechafin: String,
        @Query("codigo") codigo: String
    ): Call<ResumenAsistenciaResponse>

    @GET("apersonal/ObtenerPersonalFacial")
    fun obtenerPersonalFacial(
        @Query("codigo") codigo: String
    ): Call<ObtenerPersonalFacialResponse>
}