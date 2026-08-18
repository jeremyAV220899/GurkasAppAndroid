package com.cloud.gurkasapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_home)


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
        // ASISTENCIA
        // =====================================================

        val btnCalendario =
            findViewById<LinearLayout>(R.id.btnCalendario)

        btnCalendario.setOnClickListener {

            val intent = Intent(
                this,
                AsistenciaActivity::class.java
            )

            startActivity(intent)
        }


        // =====================================================
        // REFERENCIAS
        // =====================================================

        val root =
            findViewById<View>(R.id.main)

        val headerContent =
            findViewById<View>(R.id.headerContent)

        val statusBarOverlay =
            findViewById<View>(R.id.statusBarOverlay)

        val scrollHome =
            findViewById<ScrollView>(R.id.scrollHome)

        val menuInferior =
            findViewById<View>(R.id.menuInferior)


        // =====================================================
        // CONTROL STATUS BAR
        // =====================================================

        val controller =
            WindowCompat.getInsetsController(
                window,
                window.decorView
            )


        // Al inicio tenemos imagen roja,
        // por eso los iconos deben ser blancos.
        controller.isAppearanceLightStatusBars =
            false


        // =====================================================
        // STATUS BAR + NAVIGATION BAR
        // =====================================================

        ViewCompat.setOnApplyWindowInsetsListener(
            root
        ) { _, insets ->


            // -------------------------------------------------
            // BARRA SUPERIOR
            // -------------------------------------------------

            val statusInsets =
                insets.getInsets(
                    WindowInsetsCompat.Type.statusBars()
                )


            // Overlay exactamente del tamaño
            // de la barra superior
            val statusParams =
                statusBarOverlay.layoutParams

            statusParams.height =
                statusInsets.top

            statusBarOverlay.layoutParams =
                statusParams


            // Colocar usuario / iconos debajo
            // de la hora y batería
            headerContent.setPadding(
                dpToPx(22),
                statusInsets.top + dpToPx(8),
                dpToPx(22),
                0
            )


            // -------------------------------------------------
            // BARRA INFERIOR DEL TELÉFONO
            // -------------------------------------------------

            val navigationInsets =
                insets.getInsets(
                    WindowInsetsCompat.Type.navigationBars()
                )


            // Aumentamos la altura del menú para incluir
            // el espacio ocupado por Android
            val menuParams =
                menuInferior.layoutParams

            menuParams.height =
                dpToPx(76) +
                        navigationInsets.bottom

            menuInferior.layoutParams =
                menuParams


            // Dejamos los botones por encima
            // de la navegación del sistema
            menuInferior.setPadding(
                0,
                dpToPx(5),
                0,
                navigationInsets.bottom + dpToPx(6)
            )


            insets
        }


        // =====================================================
        // CAMBIAR STATUS BAR AL HACER SCROLL
        // =====================================================

        scrollHome.setOnScrollChangeListener {
                _,
                _,
                scrollY,
                _,
                _ ->


            if (scrollY > dpToPx(30)) {

                // Fondo blanco
                statusBarOverlay.setBackgroundColor(
                    Color.WHITE
                )

                // Hora, WiFi, batería negros
                controller.isAppearanceLightStatusBars =
                    true

            } else {

                // Imagen roja visible detrás
                statusBarOverlay.setBackgroundColor(
                    Color.TRANSPARENT
                )

                // Iconos blancos
                controller.isAppearanceLightStatusBars =
                    false
            }
        }
    }


    private fun dpToPx(dp: Int): Int {

        return (
                dp *
                        resources.displayMetrics.density
                ).toInt()
    }
}