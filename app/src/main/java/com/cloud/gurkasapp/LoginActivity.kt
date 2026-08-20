package com.cloud.gurkasapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

class LoginActivity : AppCompatActivity() {
    private lateinit var edtUsuario: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnIngresar: Button
    private lateinit var loadingOverlay: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // BARRA DE ESTADO TRANSPARENTE
        // Permite que el diseño se dibuje detrás
        // de la barra donde está la hora, WiFi y batería.
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Barra de estado transparente
        window.statusBarColor = Color.TRANSPARENT

        // Hora, WiFi, señal y batería en color BLANCO
        val controller = WindowCompat.getInsetsController(
            window,
            window.decorView
        )

        controller.isAppearanceLightStatusBars = false

        // CARGAR DISEÑO
        setContentView(R.layout.activity_login)

        // VINCULAR CONTROLES

        edtUsuario = findViewById(R.id.edtUsuario)
        edtPassword = findViewById(R.id.edtPassword)
        btnIngresar = findViewById(R.id.btnIngresar)
        loadingOverlay = findViewById(R.id.loadingOverlay)

        val btnMostrarPassword = findViewById<ImageButton>(R.id.btnMostrarPassword)

        // MOSTRAR / OCULTAR CONTRASEÑA
        var passwordVisible = false

        btnMostrarPassword.setOnClickListener {

            if (passwordVisible) {
                // Ocultar contraseña
                edtPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                btnMostrarPassword.setImageResource(R.drawable.vista)
                btnMostrarPassword.contentDescription = "Mostrar contraseña"
                passwordVisible = false

            } else {
                // Mostrar contraseña
                edtPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                btnMostrarPassword.setImageResource(R.drawable.esconder)
                btnMostrarPassword.contentDescription = "Ocultar contraseña"
                passwordVisible = true
            }
            // Mantener cursor al final
            edtPassword.setSelection(edtPassword.text.length)
        }

        // BOTÓN INGRESAR DESACTIVADO AL INICIO
        btnIngresar.isEnabled = false

        // VALIDAR CAMPOS MIENTRAS EL USUARIO ESCRIBE
        val watcher = object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // No hacemos nada aquí
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                validarFormularioEnTiempoReal()
            }


            override fun afterTextChanged(
                s: Editable?
            ) {
                // No hacemos nada aquí
            }
        }

        edtUsuario.addTextChangedListener(watcher)
        edtPassword.addTextChangedListener(watcher)

        // BOTÓN INGRESAR
        btnIngresar.setOnClickListener {
            if (validarCampos()) {
                // Mostrar pantalla de carga
                loadingOverlay.visibility = View.VISIBLE
                loadingOverlay.bringToFront()
                // Desactivar botón mientras carga
                btnIngresar.isEnabled = false

                // Esperar 5 segundos
                Handler(Looper.getMainLooper()).postDelayed({

                    // Ocultar pantalla de carga
                    loadingOverlay.visibility = View.GONE


                    // Mensaje
                    Toast.makeText(
                        this,
                        "Inicio de sesión exitoso",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Ir al Home
                    val intent = Intent(
                        this,
                        MainActivity::class.java
                    )
                    startActivity(intent)
                    // Cerrar Login
                    finish()
                }, 5000)
            }
        }
    }

    // VALIDAR FORMULARIO EN TIEMPO REAL
    private fun validarFormularioEnTiempoReal() {

        val usuario = edtUsuario.text.toString().trim()
        val password = edtPassword.text.toString()

        btnIngresar.isEnabled = usuario.isNotEmpty() && password.isNotEmpty()
    }

    // VALIDAR CAMPOS
    private fun validarCampos(): Boolean {

        val usuario = edtUsuario.text.toString().trim()
        val password = edtPassword.text.toString()
        var esValido = true

        // VALIDAR USUARIO
        if (usuario.isEmpty()) {
            edtUsuario.error = "Ingresa tu código de usuario"
            edtUsuario.requestFocus()
            esValido = false
        } else if (!validarUsuario(usuario)) {
            edtUsuario.error = "Código de usuario no válido"
            edtUsuario.requestFocus()
            esValido = false
        }

        // VALIDAR CONTRASEÑA
        if (password.isEmpty()) {

            edtPassword.error = "Ingresa tu contraseña"

            if (esValido) {
                edtPassword.requestFocus()
            }

            esValido = false

        } else if (password.length < 6) {

            edtPassword.error = "La contraseña debe tener mínimo 6 caracteres"
            if (esValido) {
                edtPassword.requestFocus()
            }
            esValido = false
        }
        return esValido
    }

    // VALIDAR FORMATO DEL USUARIO

    private fun validarUsuario(
        usuario: String
    ): Boolean {

        // Permite letras y números
        // Mínimo 3 caracteres
        // Máximo 20 caracteres

        val patron = Regex("""^[a-zA-Z0-9]{3,20}$""")
        return patron.matches(usuario)
    }
}