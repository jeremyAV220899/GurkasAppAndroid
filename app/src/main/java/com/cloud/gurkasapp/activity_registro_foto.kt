package com.cloud.gurkasapp

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class activity_registro_foto : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_registro_foto)

        val btnVolver = findViewById<TextView>(R.id.btnVolver)

        btnVolver.setOnClickListener {
            finish()
        }

        val headerRequisitos =
            findViewById<LinearLayout>(R.id.headerRequisitos)

        val contenidoRequisitos =
            findViewById<LinearLayout>(R.id.contenidoRequisitos)

        val flechaRequisitos =
            findViewById<TextView>(R.id.txtFlechaRequisitos)


        headerRequisitos.setOnClickListener {

            if (contenidoRequisitos.visibility == View.VISIBLE) {

                // CONTRAER
                contenidoRequisitos.visibility = View.GONE

                flechaRequisitos.text = "⌄"

            } else {

                // EXPANDIR
                contenidoRequisitos.visibility = View.VISIBLE

                flechaRequisitos.text = "⌃"
            }
        }

        // =====================================================
// CONTRAER / EXPANDIR CONSIDERACIONES
// =====================================================

        val headerConsideraciones =
            findViewById<LinearLayout>(R.id.headerConsideraciones)

        val contenidoConsideraciones =
            findViewById<LinearLayout>(R.id.contenidoConsideraciones)

        val flechaConsideraciones =
            findViewById<TextView>(R.id.txtFlechaConsideraciones)


        headerConsideraciones.setOnClickListener {

            if (contenidoConsideraciones.visibility == View.VISIBLE) {

                // CONTRAER
                contenidoConsideraciones.visibility = View.GONE

                flechaConsideraciones.text = "⌄"

            } else {

                // EXPANDIR
                contenidoConsideraciones.visibility = View.VISIBLE

                flechaConsideraciones.text = "⌃"
            }
        }
// ================================================
// CONTRAER / EXPANDIR PASOS A SEGUIR
// ================================================

        val headerPasos =
            findViewById<LinearLayout>(R.id.headerPasos)

        val contenidoPasos =
            findViewById<LinearLayout>(R.id.contenidoPasos)

        val flechaPasos =
            findViewById<TextView>(R.id.txtFlechaPasos)

        headerPasos.setOnClickListener {

            if (contenidoPasos.visibility == View.VISIBLE) {

                // CERRAR
                contenidoPasos.visibility = View.GONE
                flechaPasos.text = "⌄"

            } else {

                // ABRIR
                contenidoPasos.visibility = View.VISIBLE
                flechaPasos.text = "⌃"
            }
        }

    }
}