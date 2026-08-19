package com.cloud.gurkasapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AsistenciaActivity : AppCompatActivity() {

    // VISTAS
    private lateinit var txtMesActual: TextView
    private lateinit var txtTituloMesActual: TextView
    private lateinit var txtTituloMesAnterior: TextView
    private lateinit var gridMesActual: GridLayout
    private lateinit var gridMesAnterior: GridLayout
    private lateinit var btnHoy: TextView
    private lateinit var scrollCalendario: ScrollView

    // FECHA
    private var fechaSeleccionada: Calendar = Calendar.getInstance()

    // ON CREATE
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // Permite dibujar detrás de las barras del sistema
        enableEdgeToEdge()

        setContentView(R.layout.activity_asistencia)

        // OBTENER VISTAS

        txtMesActual = findViewById(R.id.txtMesActual)
        txtTituloMesActual = findViewById(R.id.txtTituloMesActual)
        txtTituloMesAnterior = findViewById(R.id.txtTituloMesAnterior)
        gridMesActual = findViewById(R.id.gridMesActual)
        gridMesAnterior = findViewById(R.id.gridMesAnterior)
        btnHoy = findViewById(R.id.btnHoy)
        scrollCalendario = findViewById(R.id.scrollCalendario)

        // BARRAS DEL SISTEMA
        configurarBarrasSistema()

        // FECHA ACTUAL
        fechaSeleccionada = Calendar.getInstance()

        // GENERAR CALENDARIOS
        actualizarCalendario()

        // IR A HOY
        btnHoy.setOnClickListener {
            fechaSeleccionada = Calendar.getInstance()
            actualizarCalendario()
            scrollCalendario.post { scrollCalendario.smoothScrollTo(0, 0) }
        }

        // BOTÓN INICIO
        val btnInicio = findViewById<LinearLayout>(R.id.btnInicio)
        btnInicio.setOnClickListener {
            finish()
        }

        // ABRIR PERFIL
        findViewById<View>(R.id.btnPerfil).setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java)
            startActivity(intent)
        }
    }


    // BARRAS DEL SISTEMA
    private fun configurarBarrasSistema() {

        val root = findViewById<View>(R.id.main)
        val headerContent = findViewById<View>(R.id.headerContent)
        val menuInferior = findViewById<View>(R.id.menuInferior)
        val controller = WindowCompat.getInsetsController(window, window.decorView)

        // Barra de estado transparente
        window.statusBarColor = Color.TRANSPARENT

        // Barra de navegación blanca
        window.navigationBarColor = Color.WHITE

        // Iconos superiores blancos
        controller.isAppearanceLightStatusBars = false

        // Iconos inferiores oscuros
        controller.isAppearanceLightNavigationBars = true

        ViewCompat.setOnApplyWindowInsetsListener(
            root
        ) { _, insets ->

            val statusInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navigationInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

            // HEADER
            headerContent.setPadding(dp(22), statusInsets.top + dp(8), dp(22), 0)

            // MENÚ INFERIOR
            val paramsMenu = menuInferior.layoutParams
            paramsMenu.height = dp(76) + navigationInsets.bottom
            menuInferior.layoutParams = paramsMenu
            menuInferior.setPadding(0, dp(5), 0, navigationInsets.bottom + dp(6))
            insets
        }
    }

    // ACTUALIZAR CALENDARIO
    private fun actualizarCalendario() {

        /*
         * IMPORTANTE:
         * Los meses que mostramos SIEMPRE se calculan desde la fecha real del dispositivo.
         * No dependen de fechaSeleccionada. De esta manera siempre tendremos:
         * MES ACTUAL
         * MES ANTERIOR
         */

        val hoy = Calendar.getInstance()
        val mesActual = hoy.clone() as Calendar

        mesActual.set(Calendar.DAY_OF_MONTH, 1)
        val mesAnterior = mesActual.clone() as Calendar

        mesAnterior.add(Calendar.MONTH, -1)

        // TÍTULOS
        val tituloActual = obtenerTituloMes(mesActual)

        val tituloAnterior = obtenerTituloMes(mesAnterior)

        // "Estás viendo: Agosto 2026"
        txtMesActual.text = tituloActual

        // Título sobre calendario actual
        txtTituloMesActual.text = tituloActual

        // Título sobre calendario anterior
        txtTituloMesAnterior.text = tituloAnterior

        // GENERAR MESES
        generarMes(calendarioMes = mesActual, grid = gridMesActual)
        generarMes(calendarioMes = mesAnterior, grid = gridMesAnterior)
    }

    // OBTENER NOMBRE DEL MES
    private fun obtenerTituloMes(calendario: Calendar): String
    {
        val formato = SimpleDateFormat("MMMM yyyy",
                Locale("es", "ES"))

        return capitalizarMes(
            formato.format(
                calendario.time
            )
        )
    }

    // GENERAR MES
    private fun generarMes(calendarioMes: Calendar, grid: GridLayout)
    {
        grid.removeAllViews()
        grid.columnCount = 7

        // PRIMER DÍA DEL MES
        val primerDiaMes = calendarioMes.clone() as Calendar
        primerDiaMes.set(Calendar.DAY_OF_MONTH, 1)

        // CALCULAR DÍA DE SEMANA
        // Nuestro calendario empieza en lunes.

        val diaSemana = primerDiaMes.get(Calendar.DAY_OF_WEEK)

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
            val espacio = TextView(this)
            val parametros = crearParametrosCelda()
            espacio.layoutParams = parametros
            grid.addView(
                espacio
            )
        }

        // CANTIDAD DE DÍAS DEL MES
        val cantidadDias = calendarioMes.getActualMaximum(Calendar.DAY_OF_MONTH)

        // GENERAR DÍAS

        for (diaNumero in 1..cantidadDias) {
            val fecha = calendarioMes.clone() as Calendar

            fecha.set(Calendar.DAY_OF_MONTH, diaNumero)
            limpiarHora(fecha)

            val dia = TextView(this)
            dia.text = diaNumero.toString()
            dia.gravity = Gravity.CENTER
            dia.textSize = 13f
            dia.layoutParams = crearParametrosCelda()

            // ESTADO VISUAL DEL DÍA
            configurarEstadoDia(dia = dia, fecha = fecha, calendarioMes = calendarioMes)

            // CLICK
            /*
             * Mantengo la lógica anterior:
             * solamente HOY o fechas futuras
             * se pueden seleccionar.
             */

            if (!esFechaPasada(fecha)) {

                dia.isEnabled = true
                dia.isClickable = true

                dia.setOnClickListener { fechaSeleccionada = fecha.clone() as Calendar
                    /*
                     * Solo regeneramos los calendarios para actualizar el círculo seleccionado.
                     * NO cambiamos de mes.
                     */
                    actualizarCalendario()
                }

            } else {
                dia.isEnabled = false
                dia.isClickable = false
            }
            grid.addView(
                dia
            )
        }
    }

    // ESTADO VISUAL DEL DÍA
    private fun configurarEstadoDia(dia: TextView, fecha: Calendar, calendarioMes: Calendar)
    {
        // FECHA SELECCIONADA
        if (mismaFecha(fecha, fechaSeleccionada))
        {
            dia.setBackgroundResource(R.drawable.bg_dia_seleccionado)
            dia.setTextColor(Color.WHITE)
            return
        }

        // FECHA PASADA
        if (esFechaPasada(fecha)) {
            dia.background = null
            dia.setTextColor(Color.parseColor("#DADDE1"))
            return
        }

        // HOY O FECHA FUTURA
        dia.background = null


        if (fecha.get(Calendar.MONTH) == calendarioMes.get(Calendar.MONTH)) {
            dia.setTextColor(Color.parseColor("#333333"))
        } else {
            dia.setTextColor(Color.parseColor("#E1E4E8"))
        }
    }

    // PARÁMETROS DE CADA DÍA
    private fun crearParametrosCelda():
            GridLayout.LayoutParams {

        val parametros = GridLayout.LayoutParams()

        /* Cada celda ocupa 1/7 del ancho.*/
        parametros.width = 0
        parametros.height = dp(42)
        parametros.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        parametros.setMargins(0, 0, 0, 0)

        return parametros
    }

    // FECHA PASADA
    private fun esFechaPasada(
        fecha: Calendar
    ): Boolean {
        val hoy = Calendar.getInstance()
        limpiarHora(hoy)
        val comparar = fecha.clone() as Calendar
        limpiarHora(comparar)
        return comparar.before(hoy)
    }

    // QUITAR HORA
    private fun limpiarHora(fecha: Calendar)
    {
        fecha.set(Calendar.HOUR_OF_DAY, 0)
        fecha.set(Calendar.MINUTE, 0)
        fecha.set(Calendar.SECOND, 0)
        fecha.set(Calendar.MILLISECOND, 0)
    }

    // COMPARAR FECHAS
    private fun mismaFecha(fecha1: Calendar, fecha2: Calendar): Boolean {
        return (fecha1.get(Calendar.YEAR) == fecha2.get(Calendar.YEAR) &&
                fecha1.get(Calendar.MONTH) == fecha2.get(Calendar.MONTH) &&
                fecha1.get(Calendar.DAY_OF_MONTH) == fecha2.get(Calendar.DAY_OF_MONTH))
    }

    // CAPITALIZAR MES
    private fun capitalizarMes(texto: String): String
    {
        return texto.replaceFirstChar {
            if (it.isLowerCase())
            {
                it.titlecase(Locale("es", "ES"))
            } else {
                it.toString()
            }
        }
    }

    // DP
    private fun dp(valor: Int): Int {
        return (valor * resources.displayMetrics.density).toInt()
    }
}