package com.cloud.gurkasapp

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.os.Handler
import android.widget.FrameLayout

class MainActivity : AppCompatActivity() {
    private lateinit var edtUsuario: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnIngresar: Button

    private lateinit var loadingOverlay: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        edtUsuario = findViewById(R.id.edtUsuario)
        edtPassword = findViewById(R.id.edtPassword)
        btnIngresar = findViewById(R.id.btnIngresar)
        loadingOverlay = findViewById(R.id.loadingOverlay)

        val btnMostrarPassword =
            findViewById<ImageButton>(R.id.btnMostrarPassword)

        var passwordVisible = false

        btnMostrarPassword.setOnClickListener {

            if (passwordVisible) {

                // Ocultar contraseña
                edtPassword.transformationMethod =
                    PasswordTransformationMethod.getInstance()

                btnMostrarPassword.setImageResource(
                    R.drawable.vista
                )

                btnMostrarPassword.contentDescription =
                    "Mostrar contraseña"

                passwordVisible = false

            } else {

                // Mostrar contraseña
                edtPassword.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()

                btnMostrarPassword.setImageResource(
                    R.drawable.esconder
                )

                btnMostrarPassword.contentDescription =
                    "Ocultar contraseña"

                passwordVisible = true
            }

            // Mantener cursor al final
            edtPassword.setSelection(edtPassword.text.length)
        }



        btnIngresar.isEnabled = false

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                validarFormularioEnTiempoReal()
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        edtUsuario.addTextChangedListener(watcher)
        edtPassword.addTextChangedListener(watcher)

        btnIngresar.setOnClickListener {
            if (validarCampos()) {

                // Mostrar pantalla de carga
                loadingOverlay.visibility = View.VISIBLE
                loadingOverlay.bringToFront()

                btnIngresar.isEnabled = false

                Handler(Looper.getMainLooper()).postDelayed({

                    // Ocultar pantalla de carga
                    loadingOverlay.visibility = View.GONE

                   // btnIngresar.isEnabled = true

                    Toast.makeText(
                        this,
                        "Inicio de sesión exitoso",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Ir a la pantalla principal
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)

                    // Cerrar el Login
                    finish()

                }, 5000)
            }
        }
    }

    private fun validarFormularioEnTiempoReal() {
        val usuario = edtUsuario.text.toString().trim()
        val password = edtPassword.text.toString()

        btnIngresar.isEnabled =
            usuario.isNotEmpty() &&
                    password.isNotEmpty()
    }

    private fun validarCampos(): Boolean {

        val usuario = edtUsuario.text.toString().trim()
        val password = edtPassword.text.toString()

        var esValido = true

        // USUARIO
        if (usuario.isEmpty()) {
            edtUsuario.error = "Ingresa tu código de usuario"
            edtUsuario.requestFocus()
            esValido = false

        } else if (!validarUsuario(usuario)) {
            edtUsuario.error = "Código de usuario no válido"
            edtUsuario.requestFocus()
            esValido = false
        }

        // CONTRASEÑA
        if (password.isEmpty()) {
            edtPassword.error = "Ingresa tu contraseña"

            if (esValido) {
                edtPassword.requestFocus()
            }

            esValido = false

        } else if (password.length < 6) {
            edtPassword.error =
                "La contraseña debe tener mínimo 6 caracteres"

            if (esValido) {
                edtPassword.requestFocus()
            }

            esValido = false
        }

        return esValido
    }

    private fun validarUsuario(usuario: String): Boolean {

        // Permite letras y números
        // Mínimo 3 caracteres
        // Máximo 20 caracteres

        val patron = Regex("""^[a-zA-Z0-9]{3,20}$""")

        return patron.matches(usuario)
    }


}