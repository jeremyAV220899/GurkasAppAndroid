package com.cloud.gurkasapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    // =====================================================
    // VISTAS GENERALES
    // =====================================================

    private lateinit var headerContent: View
    private lateinit var statusBarOverlay: View
    private lateinit var menuInferior: View

    private lateinit var txtTituloPantalla: TextView

    // MENÚ
    private lateinit var btnInicio: LinearLayout
    private lateinit var btnCalendario: LinearLayout
    private lateinit var btnNoticias: LinearLayout
    private lateinit var btnBoleta: LinearLayout

    // LÍNEAS
    private lateinit var lineaInicio: View
    private lateinit var lineaCalendario: View
    private lateinit var lineaNoticias: View
    private lateinit var lineaBoleta: View

    // ICONOS
    private lateinit var iconInicio: ImageView
    private lateinit var iconCalendario: ImageView
    private lateinit var iconNoticias: ImageView
    private lateinit var iconBoleta: ImageView

    // TEXTOS
    private lateinit var textInicio: TextView
    private lateinit var textCalendario: TextView
    private lateinit var textNoticias: TextView
    private lateinit var textBoleta: TextView


    // =====================================================
    // COLORES
    // =====================================================

    private val colorRojo = Color.parseColor("#D71920")
    private val colorNormal = Color.parseColor("#3C414A")
    private val colorBlanco = Color.WHITE


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_main)


        // =====================================================
        // REFERENCIAS
        // =====================================================

        val root = findViewById<View>(R.id.main)

        headerContent = findViewById(R.id.headerContent)

        statusBarOverlay = findViewById(R.id.statusBarOverlay)

        menuInferior = findViewById(R.id.menuInferior)

        txtTituloPantalla = findViewById(R.id.txtTituloPantalla)


        // =====================================================
        // BOTONES DEL MENÚ
        // =====================================================

        btnInicio = findViewById(R.id.btnInicio)
        btnCalendario = findViewById(R.id.btnCalendario)
        btnNoticias = findViewById(R.id.btnNoticias)
        btnBoleta = findViewById(R.id.btnBoleta)


        // =====================================================
        // LÍNEAS
        // =====================================================

        lineaInicio = findViewById(R.id.lineaInicio)
        lineaCalendario = findViewById(R.id.lineaCalendario)
        lineaNoticias = findViewById(R.id.lineaNoticias)
        lineaBoleta = findViewById(R.id.lineaBoleta)


        // =====================================================
        // ICONOS
        // =====================================================

        iconInicio = findViewById(R.id.iconInicio)
        iconCalendario = findViewById(R.id.iconCalendario)
        iconNoticias = findViewById(R.id.iconNoticias)
        iconBoleta = findViewById(R.id.iconBoleta)


        // =====================================================
        // TEXTOS
        // =====================================================

        textInicio = findViewById(R.id.textInicio)
        textCalendario = findViewById(R.id.textCalendario)
        textNoticias = findViewById(R.id.textNoticias)
        textBoleta = findViewById(R.id.textBoleta)


        // =====================================================
        // CONFIGURAR BARRAS DEL SISTEMA
        // =====================================================

        configurarBarrasSistema(root)


        // =====================================================
        // PERFIL
        // =====================================================

        findViewById<View>(R.id.btnPerfil).setOnClickListener {

            val intent = Intent(
                this,
                PerfilActivity::class.java
            )

            startActivity(intent)
        }


        // =====================================================
        // NAVEGACIÓN INFERIOR
        // =====================================================

        btnInicio.setOnClickListener {
            mostrarInicio()
        }


        btnCalendario.setOnClickListener {
            mostrarCalendario()
        }


        btnNoticias.setOnClickListener {
            mostrarNoticias()
        }

    /*
                btnBoleta.setOnClickListener {

                    mostrarBoleta()
                }
        */

        // =====================================================
        // PANTALLA INICIAL
        // =====================================================

        if (savedInstanceState == null) {

            mostrarInicio()
        }
    }


    // =====================================================
    // MOSTRAR HOME
    // =====================================================

    private fun mostrarInicio() {

        txtTituloPantalla.text = "¡Hola!"

        seleccionarMenu(
            menu = MenuSeleccionado.INICIO
        )

        mostrarFragment(
            HomeFragment()
        )

        mostrarHeaderRojo()
    }


    // =====================================================
    // MOSTRAR CALENDARIO
    // =====================================================

    private fun mostrarCalendario() {

        txtTituloPantalla.text = "Calendario"

        seleccionarMenu(
            menu = MenuSeleccionado.CALENDARIO
        )

        mostrarFragment(
            CalendarioFragment()
        )

        mostrarHeaderRojo()
    }


    // =====================================================
    // MOSTRAR NOTICIAS
    // =====================================================

    private fun mostrarNoticias() {

        txtTituloPantalla.text = "Noticias"

        seleccionarMenu(
            menu = MenuSeleccionado.NOTICIAS
        )

        mostrarFragment(
            NoticiasFragment()
        )

        mostrarHeaderRojo()
    }


    // =====================================================
    // MOSTRAR BOLETA
    // =====================================================
/*
    private fun mostrarBoleta() {

        txtTituloPantalla.text = "Boleta"

        seleccionarMenu(
            menu = MenuSeleccionado.BOLETA
        )

        mostrarFragment(
            BoletaFragment()
        )

        mostrarHeaderRojo()
    }
*/

    // =====================================================
    // CAMBIAR FRAGMENT
    // =====================================================

    private fun mostrarFragment(
        fragment: Fragment
    ) {

        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.contenedorFragment,
                fragment
            )
            .commit()
    }


    // =====================================================
    // ESTADO DEL MENÚ
    // =====================================================

    private fun seleccionarMenu(
        menu: MenuSeleccionado
    ) {

        // -------------------------------------------------
        // PRIMERO RESETEAMOS TODO
        // -------------------------------------------------

        lineaInicio.setBackgroundColor(colorBlanco)
        lineaCalendario.setBackgroundColor(colorBlanco)
        lineaNoticias.setBackgroundColor(colorBlanco)
        lineaBoleta.setBackgroundColor(colorBlanco)


        iconInicio.setColorFilter(colorNormal)
        iconCalendario.setColorFilter(colorNormal)
        iconNoticias.setColorFilter(colorNormal)
        iconBoleta.setColorFilter(colorNormal)


        textInicio.setTextColor(colorNormal)
        textCalendario.setTextColor(colorNormal)
        textNoticias.setTextColor(colorNormal)
        textBoleta.setTextColor(colorNormal)


        textInicio.setTypeface(null)
        textCalendario.setTypeface(null)
        textNoticias.setTypeface(null)
        textBoleta.setTypeface(null)


        // -------------------------------------------------
        // MARCAMOS EL SELECCIONADO
        // -------------------------------------------------

        when (menu) {

            MenuSeleccionado.INICIO -> {

                lineaInicio.setBackgroundColor(colorRojo)
                iconInicio.setColorFilter(colorRojo)
                textInicio.setTextColor(colorRojo)

                textInicio.setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )
            }


            MenuSeleccionado.CALENDARIO -> {

                lineaCalendario.setBackgroundColor(colorRojo)
                iconCalendario.setColorFilter(colorRojo)
                textCalendario.setTextColor(colorRojo)

                textCalendario.setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )
            }


            MenuSeleccionado.NOTICIAS -> {

                lineaNoticias.setBackgroundColor(colorRojo)
                iconNoticias.setColorFilter(colorRojo)
                textNoticias.setTextColor(colorRojo)

                textNoticias.setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )
            }


            MenuSeleccionado.BOLETA -> {

                lineaBoleta.setBackgroundColor(colorRojo)
                iconBoleta.setColorFilter(colorRojo)
                textBoleta.setTextColor(colorRojo)

                textBoleta.setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )
            }
        }
    }


    // =====================================================
    // CONFIGURAR STATUS BAR Y NAVIGATION BAR
    // =====================================================

    private fun configurarBarrasSistema(
        root: View
    ) {

        val controller =
            WindowCompat.getInsetsController(
                window,
                window.decorView
            )


        // Status bar transparente
        window.statusBarColor =
            Color.TRANSPARENT


        // Navigation bar blanca
        window.navigationBarColor =
            Color.WHITE


        // Header rojo -> iconos superiores blancos
        controller.isAppearanceLightStatusBars =
            false


        // Barra inferior -> iconos negros
        controller.isAppearanceLightNavigationBars =
            true


        ViewCompat.setOnApplyWindowInsetsListener(
            root
        ) { _, insets ->


            // =================================================
            // STATUS BAR
            // =================================================

            val statusInsets =
                insets.getInsets(
                    WindowInsetsCompat.Type.statusBars()
                )


            val statusParams =
                statusBarOverlay.layoutParams

            statusParams.height =
                statusInsets.top

            statusBarOverlay.layoutParams =
                statusParams


            // Usuario debajo del notch/status bar
            headerContent.setPadding(
                dpToPx(22),
                statusInsets.top + dpToPx(8),
                dpToPx(22),
                0
            )


            // =================================================
            // NAVIGATION BAR
            // =================================================

            val navigationInsets =
                insets.getInsets(
                    WindowInsetsCompat.Type.navigationBars()
                )


            val menuParams =
                menuInferior.layoutParams


            menuParams.height =
                dpToPx(76) +
                        navigationInsets.bottom


            menuInferior.layoutParams =
                menuParams


            menuInferior.setPadding(
                0,
                dpToPx(5),
                0,
                navigationInsets.bottom +
                        dpToPx(6)
            )


            insets
        }
    }


    // =====================================================
    // HEADER ROJO
    // =====================================================

    fun mostrarHeaderRojo() {

        statusBarOverlay.setBackgroundColor(
            Color.TRANSPARENT
        )

        val controller =
            WindowCompat.getInsetsController(
                window,
                window.decorView
            )

        controller.isAppearanceLightStatusBars =
            false
    }


    // =====================================================
    // STATUS BAR BLANCA
    // =====================================================

    fun mostrarStatusBarBlanca() {

        statusBarOverlay.setBackgroundColor(
            Color.WHITE
        )

        val controller =
            WindowCompat.getInsetsController(
                window,
                window.decorView
            )

        controller.isAppearanceLightStatusBars =
            true
    }


    // =====================================================
    // DP -> PX
    // =====================================================

    private fun dpToPx(
        dp: Int
    ): Int {

        return (
                dp *
                        resources.displayMetrics.density
                ).toInt()
    }


    // =====================================================
    // OPCIONES DEL MENÚ
    // =====================================================

    private enum class MenuSeleccionado {
        INICIO,
        CALENDARIO,
        NOTICIAS,
        BOLETA
    }
}