package com.cloud.gurkasapp

import android.graphics.Color
import android.os.Bundle
import android.view.View
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

        val controller = WindowCompat.getInsetsController(
            window,
            window.decorView
        )

        val headerContent = findViewById<View>(R.id.headerContent)
        val statusBarOverlay = findViewById<View>(R.id.statusBarOverlay)
        val scrollHome = findViewById<ScrollView>(R.id.scrollHome)

        // Estado inicial:
        // foto roja -> iconos blancos
        controller.isAppearanceLightStatusBars = false

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { _, insets ->

            val statusBarInsets =
                insets.getInsets(WindowInsetsCompat.Type.statusBars())

            // Alto exacto de la status bar
            val params = statusBarOverlay.layoutParams
            params.height = statusBarInsets.top
            statusBarOverlay.layoutParams = params

            // Contenido debajo de hora / wifi / batería
            headerContent.setPadding(
                dpToPx(22),
                statusBarInsets.top + dpToPx(8),
                dpToPx(22),
                0
            )

            insets
        }

        scrollHome.setOnScrollChangeListener { _, _, scrollY, _, _ ->

            if (scrollY > dpToPx(30)) {

                // =============================
                // FONDO BLANCO
                // =============================

                statusBarOverlay.setBackgroundColor(Color.WHITE)

                // Hora, señal, WiFi y batería NEGROS
                controller.isAppearanceLightStatusBars = true

            } else {

                // =============================
                // FOTO / FONDO ROJO
                // =============================

                statusBarOverlay.setBackgroundColor(Color.TRANSPARENT)

                // Hora, señal, WiFi y batería BLANCOS
                controller.isAppearanceLightStatusBars = false
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}