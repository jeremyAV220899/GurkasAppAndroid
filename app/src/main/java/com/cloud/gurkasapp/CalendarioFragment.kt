package com.cloud.gurkasapp

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarioFragment : Fragment() {

    // VISTAS
    private lateinit var txtMesActual: TextView
    private lateinit var txtTituloMesActual: TextView
    private lateinit var txtTituloMesAnterior: TextView
    private lateinit var gridMesActual: GridLayout
    private lateinit var gridMesAnterior: GridLayout
    private lateinit var btnHoy: TextView
    private lateinit var scrollCalendario: NestedScrollView

    // FECHA SELECCIONADA
    private var fechaSeleccionada: Calendar = Calendar.getInstance()

    // CREAR VISTA
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

    // VISTA CREADA
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // OBTENER VISTAS
        txtMesActual = view.findViewById(R.id.txtMesActual)
        txtTituloMesActual = view.findViewById(R.id.txtTituloMesActual)
        txtTituloMesAnterior = view.findViewById(R.id.txtTituloMesAnterior)
        gridMesActual = view.findViewById(R.id.gridMesActual)
        gridMesAnterior = view.findViewById(R.id.gridMesAnterior)
        btnHoy = view.findViewById(R.id.btnHoy)
        scrollCalendario = view.findViewById(R.id.scrollCalendario)

        // FECHA ACTUAL
        fechaSeleccionada = Calendar.getInstance()

        // GENERAR CALENDARIOS
        actualizarCalendario()

        // IR A HOY
        btnHoy.setOnClickListener {
            fechaSeleccionada = Calendar.getInstance()
            actualizarCalendario()
            scrollCalendario.post {
                scrollCalendario.smoothScrollTo(
                    0,
                    0
                )
            }
        }

        // STATUS BAR
        scrollCalendario.setOnScrollChangeListener {
                _,
                _,
                scrollY,
                _,
                _ ->

            val mainActivity = requireActivity() as MainActivity

            if (scrollY > dp(30)) {
                mainActivity.mostrarStatusBarBlanca()
            } else {
                mainActivity.mostrarHeaderRojo()
            }
        }
    }

    // ACTUALIZAR CALENDARIO
    private fun actualizarCalendario() {

        /* Los meses siempre se calculan desde la fecha real del dispositivo. */
        val hoy = Calendar.getInstance()

        // MES ACTUAL
        val mesActual = hoy.clone() as Calendar

        mesActual.set(Calendar.DAY_OF_MONTH,1)

        // MES ANTERIOR
        val mesAnterior = mesActual.clone() as Calendar

        mesAnterior.add(Calendar.MONTH, -1)

        // TÍTULOS
        val tituloActual = obtenerTituloMes(mesActual)
        val tituloAnterior = obtenerTituloMes(mesAnterior)

        // FECHA ACTUAL COMPLETA
        val formatoFechaActual = SimpleDateFormat("d 'de' MMMM 'del' yyyy", Locale("es", "ES"))
        val fechaActualTexto = formatoFechaActual.format(hoy.time)

        // TARJETA SUPERIOR
        txtMesActual.text = fechaActualTexto

        // TÍTULO MES ACTUAL
        txtTituloMesActual.text = tituloActual

        // TÍTULO MES ANTERIOR
        txtTituloMesAnterior.text = tituloAnterior

        // GENERAR MESES
        generarMes(calendarioMes = mesActual, grid = gridMesActual)
        generarMes(calendarioMes = mesAnterior, grid = gridMesAnterior)
    }

    // OBTENER TÍTULO DEL MES
    private fun obtenerTituloMes(calendario: Calendar): String {

        val formato =
            SimpleDateFormat(
                "MMMM yyyy",
                Locale(
                    "es",
                    "ES"
                )
            )
        return capitalizarMes(formato.format(calendario.time)
        )
    }

    // GENERAR MES
    private fun generarMes(calendarioMes: Calendar, grid: GridLayout) {

        grid.removeAllViews()
        grid.columnCount = 7

        // PRIMER DÍA DEL MES
        val primerDiaMes = calendarioMes.clone() as Calendar

        primerDiaMes.set(Calendar.DAY_OF_MONTH, 1)

        // DÍA DE LA SEMANA
        val diaSemana = primerDiaMes.get(Calendar.DAY_OF_WEEK)

        /* Nuestro calendario comienza en lunes.*/
        val espaciosIniciales =
            when (diaSemana) {
                Calendar.SUNDAY ->
                    6
                else ->
                    diaSemana -
                            Calendar.MONDAY
            }

        // ESPACIOS ANTES DEL DÍA 1
        for (i in 0 until espaciosIniciales) {
            val espacio = TextView(requireContext())
            val parametros = crearParametrosCelda()
            espacio.layoutParams = parametros
            grid.addView(espacio)
        }

        // CANTIDAD DE DÍAS
        val cantidadDias = calendarioMes.getActualMaximum(Calendar.DAY_OF_MONTH)

        // GENERAR DÍAS
        for (diaNumero in 1..cantidadDias) {

            val fecha = calendarioMes.clone() as Calendar
            fecha.set(Calendar.DAY_OF_MONTH, diaNumero)

            limpiarHora(fecha)

            // CREAR TEXTO DEL DÍA

            val dia = TextView(requireContext())
            dia.text = diaNumero.toString()
            dia.gravity = Gravity.CENTER
            dia.textSize = 13f
            dia.layoutParams = crearParametrosCelda()

            // ESTADO VISUAL
            configurarEstadoDia(
                dia = dia,
                fecha = fecha,
                calendarioMes = calendarioMes
            )

            // CLICK
            if (!esFechaPasada(fecha)) {
                dia.isEnabled = true
                dia.isClickable = true
                dia.setOnClickListener {
                    fechaSeleccionada = fecha.clone() as Calendar
                    actualizarCalendario()
                }
            } else {
                dia.isEnabled = false
                dia.isClickable = false
            }
            grid.addView(dia)
        }
    }

    // ESTADO VISUAL DEL DÍA
    private fun configurarEstadoDia(dia: TextView, fecha: Calendar, calendarioMes: Calendar) {

        // FECHA SELECCIONADA
        if (mismaFecha(fecha, fechaSeleccionada)) {
            dia.setBackgroundResource(R.drawable.bg_dia_seleccionado)
            dia.setTextColor(Color.WHITE)
            return
        }

        // FECHA PASADA
        if (esFechaPasada(fecha)) {
            dia.background = null

            dia.setTextColor(
                Color.parseColor(
                    "#DADDE1"
                )
            )
            return
        }

        // HOY O FECHA FUTURA
        dia.background = null

        if (fecha.get(Calendar.MONTH) == calendarioMes.get(Calendar.MONTH)) {
            dia.setTextColor(
                Color.parseColor(
                    "#333333"
                )
            )
        } else {
            dia.setTextColor(Color.parseColor("#E1E4E8"))
        }
    }

    // PARÁMETROS DE CADA CELDA
    private fun crearParametrosCelda(): GridLayout.LayoutParams {

        val parametros = GridLayout.LayoutParams()

        /* Cada día ocupa 1/7 del ancho disponible.*/

        parametros.width = 0
        parametros.height = dp(42)
        parametros.columnSpec =
            GridLayout.spec(
                GridLayout.UNDEFINED,
                1f
            )

        parametros.setMargins(0, 0, 0, 0)
        return parametros
    }

    // VERIFICAR FECHA PASADA
    private fun esFechaPasada(fecha: Calendar): Boolean {
        val hoy = Calendar.getInstance()
        limpiarHora(hoy)
        val comparar = fecha.clone() as Calendar
        limpiarHora(comparar)
        return comparar.before(hoy)
    }

    // QUITAR HORA
    private fun limpiarHora(fecha: Calendar) {

        fecha.set(Calendar.HOUR_OF_DAY, 0)
        fecha.set(Calendar.MINUTE, 0)
        fecha.set(Calendar.SECOND, 0)
        fecha.set(Calendar.MILLISECOND, 0)
    }

    // COMPARAR DOS FECHAS
    private fun mismaFecha(fecha1: Calendar, fecha2: Calendar): Boolean {
        return (fecha1.get(Calendar.YEAR) == fecha2.get(Calendar.YEAR) &&
                fecha1.get(Calendar.MONTH) == fecha2.get(Calendar.MONTH) &&
                fecha1.get(Calendar.DAY_OF_MONTH) == fecha2.get(Calendar.DAY_OF_MONTH))
    }

    // CAPITALIZAR MES
    private fun capitalizarMes(texto: String): String {

        return texto.replaceFirstChar {
            if (it.isLowerCase()) {
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

    // DP A PX
    private fun dp(valor: Int): Int {
        return (valor * resources.displayMetrics.density).toInt()
    }
}