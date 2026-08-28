package com.cloud.gurkasapp

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
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
    }

    // ACTUALIZAR CALENDARIO
    private fun actualizarCalendario() {

        /* LOS MESES SIEMPRE SE CALCULAN DESDE LA FECHA REAL DEL DISPOSITIVO */
        val hoy = Calendar.getInstance()

        // MES ACTUAL
        val mesActual = hoy.clone() as Calendar
        mesActual.set(Calendar.DAY_OF_MONTH, 1)

        // MES ANTERIOR
        val mesAnterior = mesActual.clone() as Calendar
        mesAnterior.add(Calendar.MONTH, -1)

        // TÍTULOS
        val tituloActual = obtenerTituloMes(mesActual)
        val tituloAnterior = obtenerTituloMes(mesAnterior)

        // FECHA ACTUAL COMPLETA
        val formatoFechaActual =
            SimpleDateFormat(
                "d 'de' MMMM 'del' yyyy",
                Locale(
                    "es",
                    "ES"
                )
            )

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

        return capitalizarMes(
            formato.format(
                calendario.time
            )
        )
    }

    // GENERAR MES
    private fun generarMes(calendarioMes: Calendar, grid: GridLayout) {

        grid.removeAllViews()
        grid.columnCount = 7

        // PRIMER DÍA DEL MES
        val primerDiaMes = calendarioMes.clone() as Calendar
        primerDiaMes.set( Calendar.DAY_OF_MONTH, 1)

        // DÍA DE LA SEMANA
        val diaSemana = primerDiaMes.get(Calendar.DAY_OF_WEEK)

        /* NUESTRO CALENDARIO COMIENZA EN LUNES */
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
            val espacio =View(requireContext())
            espacio.layoutParams = crearParametrosCelda()
            grid.addView(
                espacio
            )
        }

        // CANTIDAD DE DÍAS
        val cantidadDias = calendarioMes.getActualMaximum(Calendar.DAY_OF_MONTH)

        // GENERAR DÍAS
        for (diaNumero in 1..cantidadDias) {
            val fecha = calendarioMes.clone() as Calendar

            fecha.set( Calendar.DAY_OF_MONTH,diaNumero )
            limpiarHora(fecha)

            // CONTENEDOR COMPLETO DEL DÍA
            val contenedorDia = LinearLayout( requireContext()
                ).apply {
                    orientation =LinearLayout.VERTICAL
                    gravity =Gravity.CENTER
                    layoutParams =crearParametrosCelda()
                }

            // NÚMERO DEL DÍA
            val dia =TextView(  requireContext()).apply {
                    text =diaNumero.toString()
                    gravity = Gravity.CENTER
                    textSize =  13f
                    layoutParams =
                        LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dp(30)
                        )
                }

            // CONFIGURAR ESTADO VISUAL
            configurarEstadoDia(
                dia = dia,
                fecha = fecha,
                calendarioMes = calendarioMes
            )

            // CREAR RAYITA
            val indicador =  crearIndicadorFecha(fecha)

            // AGREGAR AL CONTENEDOR
            contenedorDia.addView( dia )
            contenedorDia.addView( indicador )

            // C LICK  SOLO LAS FECHAS PASADAS SE PUEDEN PRESIONAR
            if ( esFechaPasada( fecha ) ) {
                contenedorDia.isEnabled = true
                contenedorDia.isClickable =  true
                contenedorDia.setOnClickListener {
                    fechaSeleccionada = fecha.clone() as Calendar
                    actualizarCalendario()
                    mostrarDetalleMarcacion(
                        fecha
                    )
                }
            } else {
                /* HOY Y FUTURO
                 * BLOQUEADOS*/
                contenedorDia.isEnabled =  false
                contenedorDia.isClickable = false
            }

            // AGREGAR AL GRID
            grid.addView(
                contenedorDia
            )
        }
    }

    // CREAR INDICADOR DE COLOR
    private fun crearIndicadorFecha( fecha: Calendar ): View {

        val indicador =  View( requireContext() )

        // TAMAÑO DE LA RAYITA
        val parametros =
            LinearLayout.LayoutParams(
                dp(14),
                dp(3)
            )

        parametros.topMargin = dp(1)
        indicador.layoutParams = parametros

        // HOY Y FUTURO
        // SIN COLOR

        if ( !esFechaPasada(fecha) ) {
            indicador.background = null
            return indicador
        }

        // COLOR ALEATORIO ESTABLE
        // 0 = VERDE
        // 1 = ROJO
        // 2 = AMARILLO
        // 3 = SIN COLOR

        val semilla =
            fecha.get(
                Calendar.YEAR
            ) * 10000 +
                    (
                            fecha.get(
                                Calendar.MONTH
                            ) + 1
                            ) * 100 +
                    fecha.get(
                        Calendar.DAY_OF_MONTH
                    )

        val tipo = kotlin.math.abs( semilla.hashCode() ) % 4

        // SIN INDICADOR
        if (tipo == 3) {
            indicador.background =  null
            return indicador
        }

        // COLOR
        val color =
            when (tipo) {

                // VERDE
                0 ->
                    Color.parseColor(
                        "#28A745"
                    )

                // ROJO
                1 ->
                    Color.parseColor(
                        "#D71920"
                    )

                // AMARILLO
                else ->
                    Color.parseColor(
                        "#F4C430"
                    )
            }

        // FONDO CON BORDES REDONDEADOS
        val fondo =
            GradientDrawable().apply {
                shape =  GradientDrawable.RECTANGLE
                cornerRadius =  dp(2).toFloat()
                setColor(
                    color
                )
            }
        indicador.background =   fondo
        return indicador
    }

    // ESTADO VISUAL DEL DÍA
    private fun configurarEstadoDia( dia: TextView,fecha: Calendar,  calendarioMes: Calendar) {

        // HOY CÍRCULO ROJO SIN RAYITA BLOQUEADO
        if ( esHoy( fecha))
        {
            dia.setBackgroundResource( R.drawable.bg_dia_seleccionado )
            dia.setTextColor( Color.WHITE)
            return
        }

        // FECHA PASADA SELECCIONADA
        if ( mismaFecha(fecha, fechaSeleccionada )
        ) {
            dia.setBackgroundResource( R.drawable.bg_dia_seleccionado )
            dia.setTextColor( Color.WHITE)
            return
        }

        // SIN FONDO
        dia.background = null

        // FUTURO GRIS Y BLOQUEADO
        if ( esFechaFutura( fecha )) {
            dia.setTextColor(
                Color.parseColor(
                    "#DADDE1"
                )
            )
            return
        }

        // PASADO TEXTO NORMAL
        dia.setTextColor(
            Color.parseColor(
                "#333333"
            )
        )
    }

    // PARÁMETROS DE CADA CELDA
    private fun crearParametrosCelda(): GridLayout.LayoutParams {
        val parametros = GridLayout.LayoutParams()

        /* CADA DÍA OCUPA 1/7 DEL ANCHO */
        parametros.width =    0
        parametros.height = dp(42)
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

    // VERIFICAR FECHA PASADA
    private fun esFechaPasada( fecha: Calendar): Boolean {

        val hoy =  Calendar.getInstance()
        limpiarHora(  hoy)
        val comparar =  fecha.clone() as Calendar
        limpiarHora( comparar )

        return comparar.before( hoy)
    }

    // VERIFICAR FECHA FUTURA
    private fun esFechaFutura(fecha: Calendar ): Boolean {
        val hoy = Calendar.getInstance()
        limpiarHora(hoy )
        val comparar = fecha.clone() as Calendar
        limpiarHora(comparar )
        return comparar.after(
            hoy
        )
    }

    // VERIFICAR SI ES HOY
    private fun esHoy( fecha: Calendar ): Boolean {
        val hoy = Calendar.getInstance()
        limpiarHora(hoy)
        val comparar =fecha.clone() as Calendar
        limpiarHora( comparar )
        return mismaFecha(
            comparar,
            hoy
        )
    }

    // QUITAR HORA
    private fun limpiarHora( fecha: Calendar ) {
        fecha.set(  Calendar.HOUR_OF_DAY, 0)
        fecha.set( Calendar.MINUTE, 0)
        fecha.set( Calendar.SECOND, 0)
        fecha.set(Calendar.MILLISECOND, 0)
    }

    // COMPARAR DOS FECHAS
    private fun mismaFecha( fecha1: Calendar,fecha2: Calendar): Boolean {
        return (
                fecha1.get(Calendar.YEAR) == fecha2.get(Calendar.YEAR )&&
                        fecha1.get( Calendar.MONTH ) == fecha2.get(Calendar.MONTH) &&
                        fecha1.get(Calendar.DAY_OF_MONTH) == fecha2.get( Calendar.DAY_OF_MONTH)
                )
    }

    // CAPITALIZAR MES
    private fun capitalizarMes(texto: String ): String {
        return texto.replaceFirstChar {
            if (it.isLowerCase() ) {
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
    private fun dp( valor: Int ): Int {
        return ( valor * resources.displayMetrics.density ).toInt()
    }

    private fun mostrarDetalleMarcacion( fecha: Calendar ) {

        val dialog = Dialog(requireContext())
        val vista =  layoutInflater.inflate( R.layout.dialog_detalle_marcacion,null )

        dialog.setContentView(vista)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val txtFecha = vista.findViewById<TextView>( R.id.txtFechaDialog )
        val txtCliente =vista.findViewById<TextView>(R.id.txtClienteDialog )
        val txtSede =vista.findViewById<TextView>( R.id.txtSedeDialog )
        val txtHora = vista.findViewById<TextView>( R.id.txtHoraDialog)
        val txtTipo = vista.findViewById<TextView>( R.id.txtTipoDialog )
        val btnCerrar =vista.findViewById<TextView>(R.id.btnCerrarDialog )
        val btnAceptar =  vista.findViewById<TextView>( R.id.btnAceptarDialog)

        val formato =
            SimpleDateFormat(
                "d 'de' MMMM 'de' yyyy",
                Locale("es", "ES")
            )

        txtFecha.text = formato.format(fecha.time)
        txtCliente.text = "Cliente: Grupo Gurkas"
        txtSede.text = "Sede: Sede Central"
        txtHora.text ="Hora de marcación: 08:05"

        val tipo = obtenerTipoMarcacion(fecha)

        txtTipo.text = "Tipo de marcación: ${tipo.first}"
        txtTipo.setTextColor( tipo.second )

        btnCerrar.setOnClickListener { dialog.dismiss()  }
        btnAceptar.setOnClickListener {  dialog.dismiss() }

        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.88).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun obtenerTipoMarcacion(
        fecha: Calendar
    ): Pair<String, Int> {

        val semilla =
            fecha.get(Calendar.YEAR) * 10000 +
                    (fecha.get(Calendar.MONTH) + 1) * 100 +
                    fecha.get(Calendar.DAY_OF_MONTH)

        val tipo =
            kotlin.math.abs(
                semilla.hashCode()
            ) % 4

        return when (tipo) {

            0 ->
                Pair(
                    "Asistencia",
                    Color.parseColor("#28A745")
                )

            1 ->
                Pair(
                    "Falta",
                    Color.parseColor("#D71920")
                )

            2 ->
                Pair(
                    "Tardanza",
                    Color.parseColor("#F4C430")
                )

            else ->
                Pair(
                    "Sin registro",
                    Color.parseColor("#8A9299")
                )
        }
    }
}