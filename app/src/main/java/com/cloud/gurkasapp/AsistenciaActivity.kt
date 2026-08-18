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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AsistenciaActivity : AppCompatActivity() {

    private lateinit var calendarioSemana: LinearLayout
    private lateinit var scrollCalendarioCompleto: ScrollView
    private lateinit var gridMesActual: GridLayout
    private lateinit var gridMesSiguiente: GridLayout
    private lateinit var txtMesActual: TextView
    private lateinit var txtTituloMes: TextView
    private lateinit var txtMesSiguiente: TextView
    private lateinit var btnExpandir: TextView
    private lateinit var btnHoy: TextView
    private lateinit var recyclerDias: RecyclerView
    private var expandido = false
    private var fechaSeleccionada = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        // Permite dibujar detrás de las barras del sistema
        enableEdgeToEdge()

        setContentView(R.layout.activity_asistencia)

        calendarioSemana = findViewById(R.id.calendarioSemana)
        scrollCalendarioCompleto = findViewById(R.id.scrollCalendarioCompleto)
        gridMesActual = findViewById(R.id.gridMesActual)
        gridMesSiguiente = findViewById(R.id.gridMesSiguiente)
        txtMesActual = findViewById(R.id.txtMesActual)
        txtTituloMes = findViewById(R.id.txtTituloMes)
        txtMesSiguiente = findViewById(R.id.txtMesSiguiente)
        btnExpandir = findViewById(R.id.btnExpandirCalendario)
        btnHoy = findViewById(R.id.btnHoy)
        recyclerDias = findViewById(R.id.recyclerDias)


        // BARRAS DEL SISTEMA

        val root = findViewById<View>(R.id.main)
        val headerContent = findViewById<View>(R.id.headerContent)
        val menuInferior = findViewById<View>(R.id.menuInferior)

        val controller = WindowCompat.getInsetsController(
            window,
            window.decorView
        )

        // Status bar transparente
        window.statusBarColor = Color.TRANSPARENT

        // Navigation bar blanca
        window.navigationBarColor = Color.WHITE

        // Iconos superiores blancos
        controller.isAppearanceLightStatusBars = false

        // Iconos inferiores negros
        controller.isAppearanceLightNavigationBars = true


        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->

            val statusInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navigationInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

            // HEADER

            // Baja usuario e iconos para que no choquen
            // con hora, wifi y batería
            headerContent.setPadding(
                dp(22),
                statusInsets.top + dp(8),
                dp(22),
                0
            )

            // MENÚ INFERIOR

            val paramsMenu = menuInferior.layoutParams

            paramsMenu.height = dp(76) + navigationInsets.bottom

            menuInferior.layoutParams = paramsMenu

            menuInferior.setPadding(
                0,
                dp(5),
                0,
                navigationInsets.bottom + dp(6)
            )


            insets
        }


        // FECHA ACTUAL
        fechaSeleccionada = Calendar.getInstance()

        // CALENDARIO
        actualizarCalendario()

        // LISTA DESDE HOY HACIA ATRÁS
        configurarListaAsistencia()

        // ABRIR / CERRAR

        btnExpandir.setOnClickListener {
            expandirContraerCalendario()
        }

        // IR A HOY

        btnHoy.setOnClickListener {
            fechaSeleccionada = Calendar.getInstance()
            actualizarCalendario()
            recyclerDias.scrollToPosition(0)
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

    // ACTUALIZAR CALENDARIO
    private fun actualizarCalendario() {
        actualizarTitulo()
        generarSemana()
        generarCalendarioCompleto()
    }

    // TÍTULO AUTOMÁTICO
    private fun actualizarTitulo() {

        val formato =
            SimpleDateFormat(
                "MMMM yyyy",
                Locale("es", "ES")
            )

        val titulo =
            capitalizarMes(
                formato.format(
                    fechaSeleccionada.time
                )
            )

        txtMesActual.text = titulo
        txtTituloMes.text = titulo
    }

    // SEMANA COMPACTA

    private fun generarSemana() {

        calendarioSemana.removeAllViews()

        val inicioSemana = fechaSeleccionada.clone() as Calendar
        val diaSemana = inicioSemana.get(Calendar.DAY_OF_WEEK)

        val diferencia =
            when (diaSemana) {

                Calendar.SUNDAY ->
                    6
                else ->
                    diaSemana -
                            Calendar.MONDAY
            }

        inicioSemana.add(Calendar.DAY_OF_MONTH, -diferencia)


        val nombres = arrayOf("L", "M", "M", "J", "V", "S", "D")


        for (i in 0 until 7) {

            val fecha = inicioSemana.clone() as Calendar

            fecha.add(Calendar.DAY_OF_MONTH, i)

            calendarioSemana.addView(
                crearDiaSemana(
                    nombres[i],
                    fecha
                )
            )

        }
    }

    // CREAR DÍA SEMANA


    private fun crearDiaSemana(
        letra: String,
        fecha: Calendar
    ): LinearLayout {

        val contenedor = LinearLayout(this)
        contenedor.orientation = LinearLayout.VERTICAL
        contenedor.gravity = Gravity.CENTER
        contenedor.layoutParams =
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )

        // LETRA
        val txtLetra = TextView(this)

        txtLetra.text = letra
        txtLetra.gravity = Gravity.CENTER
        txtLetra.textSize = 11f
        txtLetra.setTextColor(Color.parseColor(
                "#A9B2BC"
            )
        )
        // NÚMERO
        val txtNumero = TextView(this)
        txtNumero.text = fecha.get(Calendar.DAY_OF_MONTH).toString()
        txtNumero.gravity = Gravity.CENTER
        txtNumero.textSize = 14f
        val size = dp(34)

        val parametros =
            LinearLayout.LayoutParams(
                size,
                size
            )

        parametros.topMargin = dp(6)
        txtNumero.layoutParams = parametros

        // FECHA PASADA

        if (esFechaPasada(fecha)) {
            txtNumero.background = null
            txtNumero.setTextColor(Color.parseColor("#DADDE1"))
            txtNumero.isEnabled = false
            txtNumero.isClickable = false
        }
        // FECHA SELECCIONADA
        else if (mismaFecha(fecha, fechaSeleccionada)) {
            txtNumero.setBackgroundResource(R.drawable.bg_dia_seleccionado)
            txtNumero.setTextColor(Color.WHITE)
        }
        // FECHA FUTURA
        else {
            txtNumero.background = null
            txtNumero.setTextColor(Color.parseColor("#333333"))
        }

        // SOLO HOY O FUTURO
        if (!esFechaPasada(fecha)) {
            txtNumero.setOnClickListener {
                fechaSeleccionada = fecha.clone() as Calendar
                actualizarCalendario()
            }
        }
        contenedor.addView(txtLetra)
        contenedor.addView(txtNumero)
        return contenedor
    }

    // CALENDARIO EXPANDIDO

    private fun generarCalendarioCompleto() {

        // MES ACTUAL
        val mesActual = fechaSeleccionada.clone() as Calendar

        generarMes(mesActual, gridMesActual)

        // MES ANTERIOR
        val mesAnterior = fechaSeleccionada.clone() as Calendar

        mesAnterior.add(Calendar.MONTH, -1)

        val formato =
            SimpleDateFormat(
                "MMMM yyyy",
                Locale("es", "ES")
            )

        val tituloAnterior =
            capitalizarMes(
                formato.format(
                    mesAnterior.time
                )
            )
        txtMesSiguiente.text = tituloAnterior
        generarMes(mesAnterior, gridMesSiguiente)
    }


    // =================================================
    // GENERAR MES
    // =================================================

    private fun generarMes(calendarioMes: Calendar, grid: GridLayout) {

        grid.removeAllViews()
        val primerDiaMes = calendarioMes.clone() as Calendar

        primerDiaMes.set(Calendar.DAY_OF_MONTH, 1)

        val diaSemana = primerDiaMes.get(Calendar.DAY_OF_WEEK)

        val diferencia =
            when (diaSemana) {

                Calendar.SUNDAY ->
                    6

                else ->
                    diaSemana -
                            Calendar.MONDAY
            }

        val inicio = primerDiaMes.clone() as Calendar

        inicio.add(
            Calendar.DAY_OF_MONTH,
            -diferencia
        )


        // 42 CELDAS
        for (i in 0 until 42) {

            val fecha = inicio.clone() as Calendar

            fecha.add(Calendar.DAY_OF_MONTH, i)

            val dia = TextView(this)

            dia.text = fecha.get(Calendar.DAY_OF_MONTH).toString()
            dia.gravity = Gravity.CENTER
            dia.textSize = 13f

            val parametros = GridLayout.LayoutParams()

            parametros.width = 0
            parametros.height = dp(42)
            parametros.columnSpec = GridLayout.spec(i % 7, 1f)
            parametros.rowSpec = GridLayout.spec(i / 7)
            dia.layoutParams = parametros

            // FECHA PASADA

            if (esFechaPasada(fecha)) {

                dia.background = null
                dia.setTextColor(Color.parseColor("#DADDE1"))
                dia.isEnabled = false
                dia.isClickable = false
            }
            // SELECCIONADA
            else if (mismaFecha(fecha, fechaSeleccionada)) {
                dia.setBackgroundResource(R.drawable.bg_dia_seleccionado)
                dia.setTextColor(Color.WHITE)
            }

            // FUTURA
            else
            {
                dia.background = null
                if (fecha.get(Calendar.MONTH) != calendarioMes.get(Calendar.MONTH)) {
                    dia.setTextColor(Color.parseColor("#E1E4E8"))
                } else {
                    dia.setTextColor(Color.parseColor("#333333"))
                }
            }
            // SOLO HOY Y FUTURO
            if (!esFechaPasada(fecha)) {
                dia.setOnClickListener {
                    fechaSeleccionada = fecha.clone() as Calendar
                    actualizarCalendario()
                }
            }
            grid.addView(dia)
        }
    }

    // LISTA DESDE HOY HACIA ATRÁS

    private fun configurarListaAsistencia() {
        recyclerDias.layoutManager = LinearLayoutManager(this)
        recyclerDias.adapter = DiasAsistenciaAdapter(generarHistorialDias())
    }

    // GENERAR HISTORIAL

    private fun generarHistorialDias(): List<DiaAsistencia>
    {
        val lista = mutableListOf<DiaAsistencia>()

        // SIEMPRE EMPIEZA EN HOY
        val fecha = Calendar.getInstance()
        val nombres = arrayOf("Dom.", "Lun.", "Mar.", "Mié.", "Jue.", "Vie.", "Sáb.")


        // 90 DÍAS HACIA ATRÁS
        for (i in 0 until 90) {

            val numero = fecha.get(Calendar.DAY_OF_MONTH).toString()
            val nombre = nombres[fecha.get(Calendar.DAY_OF_WEEK) - 1]

            lista.add(
                DiaAsistencia(
                    numero = numero,
                    nombre = nombre,
                    descripcion = "Día sin marcación"
                )
            )
            // RETROCEDER UN DÍA
            fecha.add(Calendar.DAY_OF_MONTH, -1)
        }

        return lista
    }

    // EXPANDIR / CONTRAER

    private fun expandirContraerCalendario() {

        expandido = !expandido

        if (expandido) {

            // OCULTAR SEMANA
            calendarioSemana.visibility = View.GONE

            // MOSTRAR CALENDARIO COMPLETO
            scrollCalendarioCompleto.visibility = View.VISIBLE
            val params = scrollCalendarioCompleto.layoutParams

            // ALTURA CUANDO ESTÁ ABIERTO
            params.height = dp(420)
            scrollCalendarioCompleto.layoutParams = params
            btnExpandir.text = "⌃"

        } else {

            // OCULTAR COMPLETO
            scrollCalendarioCompleto.visibility = View.GONE
            // MOSTRAR SEMANA
            calendarioSemana.visibility = View.VISIBLE
            btnExpandir.text = "⌄"
        }
    }

    // FECHA PASADA

    private fun esFechaPasada(fecha: Calendar): Boolean
    {
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

    private fun mismaFecha(fecha1: Calendar, fecha2: Calendar): Boolean
    {
        return (fecha1.get(Calendar.YEAR) == fecha2.get(Calendar.YEAR) &&
                fecha1.get(Calendar.MONTH) == fecha2.get(Calendar.MONTH) &&
                fecha1.get(Calendar.DAY_OF_MONTH) == fecha2.get(Calendar.DAY_OF_MONTH)
                )
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

    private fun dp(valor: Int): Int
    {
        return (valor * resources.displayMetrics.density).toInt()
    }
}