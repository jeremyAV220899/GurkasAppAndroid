package com.cloud.gurkasapp

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import com.cloud.gurkasapp.api.RetrofitClient
import com.cloud.gurkasapp.models.ResumenAsistenciaItem
import com.cloud.gurkasapp.models.ResumenAsistenciaResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class CalendarioFragment : Fragment() {


    // ==========================================================
    // VISTAS
    // ==========================================================

    private lateinit var txtMesActual: TextView
    private lateinit var txtTituloMesActual: TextView
    private lateinit var txtTituloMesAnterior: TextView

    private lateinit var gridMesActual: GridLayout
    private lateinit var gridMesAnterior: GridLayout

    private lateinit var btnHoy: TextView

    private lateinit var scrollCalendario: NestedScrollView


    // ==========================================================
    // FECHA SELECCIONADA
    // ==========================================================

    private var fechaSeleccionada: Calendar =
        Calendar.getInstance()


    // ==========================================================
    // DATOS RECIBIDOS DE LA API
    //
    // EJEMPLO:
    //
    // "2026-08-01" -> ResumenAsistenciaItem
    // ==========================================================

    private val resumenPorFecha =
        mutableMapOf<String, ResumenAsistenciaItem>()


    // ==========================================================
    // CREAR VISTA
    // ==========================================================

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(
            R.layout.fragment_calendario,
            container,
            false
        )
    }


    // ==========================================================
    // VISTA CREADA
    // ==========================================================

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(
            view,
            savedInstanceState
        )


        // ======================================================
        // VISTAS
        // ======================================================

        txtMesActual =
            view.findViewById(
                R.id.txtMesActual
            )

        txtTituloMesActual =
            view.findViewById(
                R.id.txtTituloMesActual
            )

        txtTituloMesAnterior =
            view.findViewById(
                R.id.txtTituloMesAnterior
            )

        gridMesActual =
            view.findViewById(
                R.id.gridMesActual
            )

        gridMesAnterior =
            view.findViewById(
                R.id.gridMesAnterior
            )

        btnHoy =
            view.findViewById(
                R.id.btnHoy
            )

        scrollCalendario =
            view.findViewById(
                R.id.scrollCalendario
            )


        // ======================================================
        // FECHA ACTUAL
        // ======================================================

        fechaSeleccionada =
            Calendar.getInstance()


        // ======================================================
        // PRIMER DIBUJO
        // SIN DATOS TODAVÍA
        // ======================================================

        actualizarCalendario()


        // ======================================================
        // CONSULTAR API
        // ======================================================

        obtenerResumenAsistencia()


        // ======================================================
        // BOTÓN HOY
        // ======================================================

        btnHoy.setOnClickListener {

            fechaSeleccionada =
                Calendar.getInstance()

            actualizarCalendario()

            scrollCalendario.post {

                scrollCalendario.smoothScrollTo(
                    0,
                    0
                )
            }
        }
    }


    // ==========================================================
    // OBTENER CÓDIGO DEL USUARIO
    //
    // LO TOMA DEL txtUsuario DE LA ACTIVITY
    // ==========================================================

    private fun obtenerCodigoUsuario(): String {

        val txtUsuario =
            requireActivity()
                .findViewById<TextView>(
                    R.id.txtUsuario
                )

        return txtUsuario
            .text
            .toString()
            .trim()
    }


    // ==========================================================
    // CONSUMIR API RESUMEN ASISTENCIA
    // ==========================================================

    private fun obtenerResumenAsistencia() {


        // ======================================================
        // CÓDIGO DEL USUARIO
        // ======================================================

        val codigo =
            obtenerCodigoUsuario()


        if (codigo.isBlank()) {

            Toast.makeText(
                requireContext(),
                "No se encontró el código del usuario",
                Toast.LENGTH_SHORT
            ).show()

            return
        }


        // ======================================================
        // FECHA ACTUAL
        // ======================================================

        val hoy =
            Calendar.getInstance()


        // ======================================================
        // PRIMER DÍA DEL MES ANTERIOR
        // ======================================================

        val inicio =
            hoy.clone() as Calendar


        inicio.add(
            Calendar.MONTH,
            -1
        )


        inicio.set(
            Calendar.DAY_OF_MONTH,
            1
        )


        // ======================================================
        // ÚLTIMO DÍA DEL MES ACTUAL
        // ======================================================

        val fin =
            hoy.clone() as Calendar


        fin.set(
            Calendar.DAY_OF_MONTH,
            fin.getActualMaximum(
                Calendar.DAY_OF_MONTH
            )
        )


        // ======================================================
        // FORMATO API
        // yyyy-MM-dd
        // ======================================================

        val formatoApi =
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
            )


        val fechaInicio =
            formatoApi.format(
                inicio.time
            )


        val fechaFin =
            formatoApi.format(
                fin.time
            )


        // ======================================================
        // LOG
        // ======================================================

        Log.d(
            "CALENDARIO_API",
            "================================"
        )

        Log.d(
            "CALENDARIO_API",
            "FECHA INICIO: $fechaInicio"
        )

        Log.d(
            "CALENDARIO_API",
            "FECHA FIN: $fechaFin"
        )

        Log.d(
            "CALENDARIO_API",
            "CODIGO: $codigo"
        )

        Log.d(
            "CALENDARIO_API",
            "================================"
        )


        // ======================================================
        // API
        // ======================================================

        RetrofitClient
            .apiService
            .obtenerResumenAsistencia(
                fechaInicio,
                fechaFin,
                codigo
            )
            .enqueue(

                object :
                    Callback<ResumenAsistenciaResponse> {


                    override fun onResponse(
                        call: Call<ResumenAsistenciaResponse>,
                        response: Response<ResumenAsistenciaResponse>
                    ) {


                        if (!isAdded) {
                            return
                        }


                        // ==========================================
                        // RESPUESTA CORRECTA
                        // ==========================================

                        if (response.isSuccessful) {


                            val lista =
                                response.body()
                                    ?.lista
                                    ?: emptyList()


                            Log.d(
                                "CALENDARIO_API",
                                "TOTAL REGISTROS: ${lista.size}"
                            )


                            // ======================================
                            // LIMPIAR MAPA
                            // ======================================

                            resumenPorFecha.clear()


                            // ======================================
                            // RECORRER RESPUESTA
                            // ======================================

                            lista.forEach { item ->


                                val fechaOriginal =
                                    item.fecha


                                val fechaConvertida =
                                    normalizarFechaApi(
                                        fechaOriginal
                                    )


                                Log.d(
                                    "CALENDARIO_API",
                                    "ORIGINAL=$fechaOriginal | CONVERTIDA=$fechaConvertida | CODIGO=${item.codigoasistencia}"
                                )


                                if (fechaConvertida != null) {


                                    resumenPorFecha[
                                        fechaConvertida
                                    ] =
                                        item
                                }
                            }


                            // ======================================
                            // LOG DE MAPA FINAL
                            // ======================================

                            Log.d(
                                "CALENDARIO_API",
                                "MAPA FINAL: ${resumenPorFecha.size}"
                            )


                            resumenPorFecha.forEach {

                                Log.d(
                                    "CALENDARIO_API",
                                    "MAPA => ${it.key} = ${it.value.codigoasistencia}"
                                )
                            }


                            // ======================================
                            // REDIBUJAR
                            // ======================================

                            actualizarCalendario()


                        } else {


                            Log.e(
                                "CALENDARIO_API",
                                "ERROR HTTP: ${response.code()}"
                            )


                            Toast.makeText(
                                requireContext(),
                                "Error del servidor: ${response.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }


                    override fun onFailure(
                        call: Call<ResumenAsistenciaResponse>,
                        t: Throwable
                    ) {


                        if (!isAdded) {
                            return
                        }


                        Log.e(
                            "CALENDARIO_API",
                            "ERROR: ${t.message}"
                        )


                        Toast.makeText(
                            requireContext(),
                            "Error: ${t.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )
    }


    // ==========================================================
    // NORMALIZAR FECHA API
    //
    // SOPORTA:
    //
    // 01 de agosto de 2026
    //
    // 1 de agosto de 2026
    //
    // 2026-08-01
    //
    // 2026-08-01T00:00:00
    //
    // Y DEVUELVE:
    //
    // 2026-08-01
    // ==========================================================

    private fun normalizarFechaApi(
        fecha: String?
    ): String? {


        if (fecha.isNullOrBlank()) {
            return null
        }


        val texto =
            fecha.trim()


        // ======================================================
        // SI YA VIENE COMO yyyy-MM-dd
        // ======================================================

        if (
            texto.matches(
                Regex(
                    """\d{4}-\d{2}-\d{2}.*"""
                )
            )
        ) {

            return texto.substring(
                0,
                10
            )
        }


        // ======================================================
        // VIENE COMO:
        // 01 de agosto de 2026
        // ======================================================

        return try {


            val formatoEntrada =
                SimpleDateFormat(
                    "d 'de' MMMM 'de' yyyy",
                    Locale(
                        "es",
                        "ES"
                    )
                )


            formatoEntrada.isLenient =
                false


            val formatoSalida =
                SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.US
                )


            val fechaConvertida =
                formatoEntrada.parse(
                    texto.lowercase(
                        Locale(
                            "es",
                            "ES"
                        )
                    )
                )


            if (fechaConvertida != null) {


                formatoSalida.format(
                    fechaConvertida
                )


            } else {


                Log.e(
                    "CALENDARIO_API",
                    "NO SE PUDO CONVERTIR: $texto"
                )


                null
            }


        } catch (e: Exception) {


            Log.e(
                "CALENDARIO_API",
                "ERROR CONVIRTIENDO FECHA: $texto",
                e
            )


            null
        }
    }


    // ==========================================================
    // ACTUALIZAR CALENDARIO
    // ==========================================================

    private fun actualizarCalendario() {


        val hoy =
            Calendar.getInstance()


        // ======================================================
        // MES ACTUAL
        // ======================================================

        val mesActual =
            hoy.clone() as Calendar


        mesActual.set(
            Calendar.DAY_OF_MONTH,
            1
        )


        // ======================================================
        // MES ANTERIOR
        // ======================================================

        val mesAnterior =
            mesActual.clone() as Calendar


        mesAnterior.add(
            Calendar.MONTH,
            -1
        )


        // ======================================================
        // TÍTULOS
        // ======================================================

        val tituloActual =
            obtenerTituloMes(
                mesActual
            )


        val tituloAnterior =
            obtenerTituloMes(
                mesAnterior
            )


        // ======================================================
        // FECHA SUPERIOR
        // ======================================================

        val formatoFechaActual =
            SimpleDateFormat(
                "d 'de' MMMM 'del' yyyy",
                Locale(
                    "es",
                    "ES"
                )
            )


        txtMesActual.text =
            formatoFechaActual.format(
                hoy.time
            )


        txtTituloMesActual.text =
            tituloActual


        txtTituloMesAnterior.text =
            tituloAnterior


        // ======================================================
        // GENERAR LOS DOS MESES
        // ======================================================

        generarMes(
            calendarioMes = mesActual,
            grid = gridMesActual
        )


        generarMes(
            calendarioMes = mesAnterior,
            grid = gridMesAnterior
        )
    }


    // ==========================================================
    // TÍTULO MES
    // ==========================================================

    private fun obtenerTituloMes(
        calendario: Calendar
    ): String {


        val formato =
            SimpleDateFormat(
                "MMMM yyyy",
                Locale(
                    "es",
                    "ES"
                )
            )


        return capitalizarMes(
            formato.format(
                calendario.time
            )
        )
    }


    // ==========================================================
    // GENERAR MES
    // ==========================================================

    private fun generarMes(
        calendarioMes: Calendar,
        grid: GridLayout
    ) {


        grid.removeAllViews()


        grid.columnCount =
            7


        // ======================================================
        // PRIMER DÍA DEL MES
        // ======================================================

        val primerDiaMes =
            calendarioMes.clone() as Calendar


        primerDiaMes.set(
            Calendar.DAY_OF_MONTH,
            1
        )


        val diaSemana =
            primerDiaMes.get(
                Calendar.DAY_OF_WEEK
            )


        // ======================================================
        // CALENDARIO COMIENZA LUNES
        // ======================================================

        val espaciosIniciales =
            when (diaSemana) {


                Calendar.SUNDAY ->
                    6


                else ->
                    diaSemana -
                            Calendar.MONDAY
            }


        // ======================================================
        // ESPACIOS VACÍOS
        // ======================================================

        for (
        i in 0 until espaciosIniciales
        ) {


            val espacio =
                View(
                    requireContext()
                )


            espacio.layoutParams =
                crearParametrosCelda()


            grid.addView(
                espacio
            )
        }


        // ======================================================
        // DÍAS DEL MES
        // ======================================================

        val cantidadDias =
            calendarioMes.getActualMaximum(
                Calendar.DAY_OF_MONTH
            )


        for (
        diaNumero in 1..cantidadDias
        ) {


            val fecha =
                calendarioMes.clone() as Calendar


            fecha.set(
                Calendar.DAY_OF_MONTH,
                diaNumero
            )


            limpiarHora(
                fecha
            )


            // ==================================================
            // CONTENEDOR DEL DÍA
            // ==================================================

            val contenedorDia =
                LinearLayout(
                    requireContext()
                ).apply {


                    orientation =
                        LinearLayout.VERTICAL


                    gravity =
                        Gravity.CENTER


                    layoutParams =
                        crearParametrosCelda()
                }


            // ==================================================
            // NÚMERO DEL DÍA
            // ==================================================

            val dia =
                TextView(
                    requireContext()
                ).apply {


                    text =
                        diaNumero.toString()


                    gravity =
                        Gravity.CENTER


                    textSize =
                        13f


                    layoutParams =
                        LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dp(30)
                        )
                }


            // ==================================================
            // ESTADO DEL NÚMERO
            // ==================================================

            configurarEstadoDia(
                dia,
                fecha
            )


            // ==================================================
            // RAYITA DE COLOR
            // ==================================================

            val indicador =
                crearIndicadorFecha(
                    fecha
                )


            contenedorDia.addView(
                dia
            )


            contenedorDia.addView(
                indicador
            )


            // ==================================================
            // VERIFICAR SI HAY REGISTRO
            // ==================================================

            val fechaClave =
                obtenerFechaClave(
                    fecha
                )


            val tieneRegistro =
                resumenPorFecha.containsKey(
                    fechaClave
                )


            // ==================================================
            // CLICK
            //
            // PERMITIR:
            // - FECHAS PASADAS CON REGISTRO
            // - HOY CON REGISTRO
            // ==================================================

            if (
                !esFechaFutura(fecha) &&
                tieneRegistro
            ) {

                contenedorDia.isEnabled =
                    true

                contenedorDia.isClickable =
                    true

                contenedorDia.setOnClickListener {

                    fechaSeleccionada =
                        fecha.clone() as Calendar

                    actualizarCalendario()

                    mostrarDetalleMarcacion(
                        fecha
                    )
                }

            } else {

                contenedorDia.isEnabled =
                    false

                contenedorDia.isClickable =
                    false
            }

            grid.addView(
                contenedorDia
            )
        }
    }


    // ==========================================================
    // CREAR RAYITA SEGÚN codigoasistencia
    // ==========================================================

    private fun crearIndicadorFecha(
        fecha: Calendar
    ): View {


        val indicador =
            View(
                requireContext()
            )


        val parametros =
            LinearLayout.LayoutParams(
                dp(20),
                dp(4)
            )


        parametros.topMargin =
            dp(1)


        indicador.layoutParams =
            parametros


        // ======================================================
        // FECHA DEL CALENDARIO
        // ======================================================

        val fechaTexto =
            obtenerFechaClave(
                fecha
            )


        // ======================================================
        // BUSCAR EN DATOS API
        // ======================================================

        val registro =
            resumenPorFecha[
                fechaTexto
            ]


        Log.d(
            "CALENDARIO_COLOR",
            "BUSCANDO=$fechaTexto EXISTE=${registro != null}"
        )


        // ======================================================
        // SIN RESULTADO = SIN COLOR
        // ======================================================

        if (registro == null) {


            indicador.background =
                null


            return indicador
        }


        // ======================================================
        // CÓDIGO
        // ======================================================

        val codigo =
            registro
                .codigoasistencia
                ?.trim()
                ?.uppercase(
                    Locale.getDefault()
                )
                ?: ""


        // ======================================================
        // COLORES
        //
        // A  = VERDE
        // D  = AZUL
        // F  = ROJO
        // FT = ROJO
        // V  = AMARILLO
        // OTRO = ANARANJADO
        // ======================================================

        val color =
            obtenerColorCodigo(
                codigo
            )


        Log.d(
            "CALENDARIO_COLOR",
            "FECHA=$fechaTexto CODIGO=$codigo"
        )


        // ======================================================
        // FONDO
        // ======================================================

        val fondo =
            GradientDrawable().apply {


                shape =
                    GradientDrawable.RECTANGLE


                cornerRadius =
                    dp(3).toFloat()


                setColor(
                    color
                )
            }


        indicador.background =
            fondo


        return indicador
    }


    // ==========================================================
    // COLOR SEGÚN CÓDIGO
    // ==========================================================

    private fun obtenerColorCodigo(
        codigo: String
    ): Int {


        return when (
            codigo.trim()
                .uppercase(
                    Locale.getDefault()
                )
        ) {


            // ASISTENCIA
            "A" ->
                Color.parseColor(
                    "#22C55E"
                )


            // DESCANSO
            "D" ->
                Color.parseColor(
                    "#64748B"
                )

            // FALTA
            "F" ->
                Color.parseColor(
                    "#EF4444"
                )

            // FERIADO
             "FT" ->
                Color.parseColor(
                    "#F59E0B"
                )


            // VACACIONES
            "V" ->
                Color.parseColor(
                    "#EAB308"
                )


            // OTRO
            else ->
                Color.parseColor(
                    "#94A3B8"
                )
        }
    }


    // ==========================================================
    // FECHA CALENDARIO yyyy-MM-dd
    // ==========================================================

    private fun obtenerFechaClave(
        fecha: Calendar
    ): String {


        val formato =
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
            )


        return formato.format(
            fecha.time
        )
    }


    // ==========================================================
    // ESTADO VISUAL DEL DÍA
    // ==========================================================

    private fun configurarEstadoDia(
        dia: TextView,
        fecha: Calendar
    ) {


        // ======================================================
        // HOY
        // ======================================================

        if (
            esHoy(
                fecha
            )
        ) {


            dia.setBackgroundResource(
                R.drawable.bg_dia_seleccionado
            )


            dia.setTextColor(
                Color.WHITE
            )


            return
        }


        // ======================================================
        // FECHA SELECCIONADA
        // ======================================================

        if (
            mismaFecha(
                fecha,
                fechaSeleccionada
            )
        ) {


            dia.setBackgroundResource(
                R.drawable.bg_dia_seleccionado
            )


            dia.setTextColor(
                Color.WHITE
            )


            return
        }


        // ======================================================
        // SIN FONDO
        // ======================================================

        dia.background =
            null


        // ======================================================
        // FUTURO
        // ======================================================

        if (
            esFechaFutura(
                fecha
            )
        ) {


            dia.setTextColor(
                Color.parseColor(
                    "#DADDE1"
                )
            )


            return
        }


        // ======================================================
        // PASADO
        // ======================================================

        dia.setTextColor(
            Color.parseColor(
                "#333333"
            )
        )
    }


    // ==========================================================
    // PARÁMETROS CELDA
    // ==========================================================

    private fun crearParametrosCelda():
            GridLayout.LayoutParams {


        val parametros =
            GridLayout.LayoutParams()


        parametros.width =
            0


        parametros.height =
            dp(44)


        parametros.columnSpec =
            GridLayout.spec(
                GridLayout.UNDEFINED,
                1f
            )


        parametros.setMargins(
            0,
            0,
            0,
            0
        )


        return parametros
    }


    // ==========================================================
    // ES FECHA PASADA
    // ==========================================================

    private fun esFechaPasada(
        fecha: Calendar
    ): Boolean {


        val hoy =
            Calendar.getInstance()


        limpiarHora(
            hoy
        )


        val comparar =
            fecha.clone() as Calendar


        limpiarHora(
            comparar
        )


        return comparar.before(
            hoy
        )
    }


    // ==========================================================
    // ES FUTURA
    // ==========================================================

    private fun esFechaFutura(
        fecha: Calendar
    ): Boolean {


        val hoy =
            Calendar.getInstance()


        limpiarHora(
            hoy
        )


        val comparar =
            fecha.clone() as Calendar


        limpiarHora(
            comparar
        )


        return comparar.after(
            hoy
        )
    }


    // ==========================================================
    // ES HOY
    // ==========================================================

    private fun esHoy(
        fecha: Calendar
    ): Boolean {


        val hoy =
            Calendar.getInstance()


        limpiarHora(
            hoy
        )


        val comparar =
            fecha.clone() as Calendar


        limpiarHora(
            comparar
        )


        return mismaFecha(
            comparar,
            hoy
        )
    }


    // ==========================================================
    // LIMPIAR HORA
    // ==========================================================

    private fun limpiarHora(
        fecha: Calendar
    ) {


        fecha.set(
            Calendar.HOUR_OF_DAY,
            0
        )


        fecha.set(
            Calendar.MINUTE,
            0
        )


        fecha.set(
            Calendar.SECOND,
            0
        )


        fecha.set(
            Calendar.MILLISECOND,
            0
        )
    }


    // ==========================================================
    // MISMA FECHA
    // ==========================================================

    private fun mismaFecha(
        fecha1: Calendar,
        fecha2: Calendar
    ): Boolean {


        return (
                fecha1.get(
                    Calendar.YEAR
                ) ==
                        fecha2.get(
                            Calendar.YEAR
                        )
                        &&
                        fecha1.get(
                            Calendar.MONTH
                        ) ==
                        fecha2.get(
                            Calendar.MONTH
                        )
                        &&
                        fecha1.get(
                            Calendar.DAY_OF_MONTH
                        ) ==
                        fecha2.get(
                            Calendar.DAY_OF_MONTH
                        )
                )
    }


    // ==========================================================
    // CAPITALIZAR MES
    // ==========================================================

    private fun capitalizarMes(
        texto: String
    ): String {


        return texto.replaceFirstChar {


            if (
                it.isLowerCase()
            ) {


                it.titlecase(
                    Locale(
                        "es",
                        "ES"
                    )
                )


            } else {


                it.toString()
            }
        }
    }


    // ==========================================================
    // DP
    // ==========================================================

    private fun dp(
        valor: Int
    ): Int {


        return (
                valor *
                        resources
                            .displayMetrics
                            .density
                ).toInt()
    }


    // ==========================================================
    // MOSTRAR DETALLE DEL REGISTRO
    // ==========================================================

    private fun mostrarDetalleMarcacion(
        fecha: Calendar
    ) {


        val fechaClave =
            obtenerFechaClave(
                fecha
            )


        val registro =
            resumenPorFecha[
                fechaClave
            ]


        if (registro == null) {


            Toast.makeText(
                requireContext(),
                "No hay marcación para esta fecha",
                Toast.LENGTH_SHORT
            ).show()


            return
        }


        // ======================================================
        // DIÁLOGO
        // ======================================================

        val dialog =
            Dialog(
                requireContext()
            )


        val vista =
            layoutInflater.inflate(
                R.layout.dialog_detalle_marcacion,
                null
            )


        dialog.setContentView(
            vista
        )


        dialog.window
            ?.setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT
                )
            )


        // ======================================================
        // VISTAS
        // ======================================================

        val txtFecha =
            vista.findViewById<TextView>(
                R.id.txtFechaDialog
            )


        val txtCliente =
            vista.findViewById<TextView>(
                R.id.txtClienteDialog
            )


        val txtSede =
            vista.findViewById<TextView>(
                R.id.txtSedeDialog
            )


        val txtHora =
            vista.findViewById<TextView>(
                R.id.txtHoraDialog
            )


        val txtTipo =
            vista.findViewById<TextView>(
                R.id.txtTipoDialog
            )


        val btnCerrar =
            vista.findViewById<TextView>(
                R.id.btnCerrarDialog
            )


        val btnAceptar =
            vista.findViewById<TextView>(
                R.id.btnAceptarDialog
            )


        // ======================================================
        // FECHA
        // ======================================================

        val formatoPantalla =
            SimpleDateFormat(
                "d 'de' MMMM 'de' yyyy",
                Locale(
                    "es",
                    "ES"
                )
            )


        txtFecha.text =
            formatoPantalla.format(
                fecha.time
            )


        // ======================================================
        // DATOS API
        // ======================================================

        txtCliente.text =
            "Cliente: ${registro.cliente ?: "--"}"


        txtSede.text =
            "Sede: ${registro.sede ?: "--"}"


        txtHora.text =
            "Hora de marcación: ${registro.hora ?: "--"}"


        txtTipo.text =
            "Tipo de marcación: ${registro.tipoAsistencia ?: "--"}"


        // ======================================================
        // COLOR DEL TIPO
        // ======================================================

        val codigo =
            registro
                .codigoasistencia
                ?.trim()
                ?.uppercase(
                    Locale.getDefault()
                )
                ?: ""


        txtTipo.setTextColor(
            obtenerColorCodigo(
                codigo
            )
        )


        // ======================================================
        // BOTONES
        // ======================================================

        btnCerrar.setOnClickListener {

            dialog.dismiss()
        }


        btnAceptar.setOnClickListener {

            dialog.dismiss()
        }


        // ======================================================
        // MOSTRAR
        // ======================================================

        dialog.show()


        dialog.window?.setLayout(
            (
                    resources
                        .displayMetrics
                        .widthPixels *
                            0.88
                    ).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}