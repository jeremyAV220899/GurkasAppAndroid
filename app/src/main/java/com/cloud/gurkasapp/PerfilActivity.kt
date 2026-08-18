package com.cloud.gurkasapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PerfilActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil)

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v, insets ->

            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        // CERRAR PERFIL Y VOLVER AL HOME
        findViewById<View>(R.id.btnCerrar).setOnClickListener {
            finish()
        }
        // =============================
        // SUGERENCIA
        // =============================

        val edtSugerencia = findViewById<EditText>(R.id.edtSugerencia)

        val txtContador = findViewById<TextView>(R.id.txtContadorSugerencia)

        val btnEnviar = findViewById<TextView>(R.id.btnEnviarSugerencia)

        // CONTADOR DE CARACTERES
        edtSugerencia.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {

                    val cantidad = s?.length ?: 0

                    txtContador.text =
                        "$cantidad / 100"
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {
                }
            }
        )

        val imgPerfil = findViewById<ImageView>(R.id.imgPerfil)

        imgPerfil.setOnClickListener {

            val intent = Intent(
                this, activity_registro_foto::class.java
            )

            startActivity(intent)
        }

        // =============================
        // VALIDAR Y ENVIAR
        // =============================

        btnEnviar.setOnClickListener {

            val sugerencia =
                edtSugerencia.text
                    .toString()
                    .trim()

            when {

                // VACÍO
                sugerencia.isEmpty() -> {

                    edtSugerencia.error =
                        "Escribe una sugerencia"

                    edtSugerencia.requestFocus()
                }

                // DEMASIADO CORTO
                sugerencia.length < 10 -> {

                    edtSugerencia.error =
                        "La sugerencia debe tener al menos 10 caracteres"

                    edtSugerencia.requestFocus()
                }

                // CORRECTO
                else -> {

                    Toast.makeText(
                        this,
                        "Gracias por tu sugerencia",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Limpiar campo
                    edtSugerencia.text.clear()

                    // El contador regresará a 0 automáticamente
                    txtContador.text = "0 / 100"
                }
            }
        }


    }
}