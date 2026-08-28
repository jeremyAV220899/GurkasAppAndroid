package com.cloud.gurkasapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PerfilActivity : AppCompatActivity() {

    private val permisoCamaraLauncher =
        registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { concedido ->

            if (concedido) {

                Toast.makeText(
                    this,
                    "Permiso de cámara activado",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                Toast.makeText(
                    this,
                    "Permiso de cámara no concedido",
                    Toast.LENGTH_SHORT
                ).show()
            }

            actualizarEstadoCamara()
        }


    private val permisoNotificacionesLauncher =
        registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { concedido ->

            if (concedido) {

                Toast.makeText(
                    this,
                    "Notificaciones activadas",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                Toast.makeText(
                    this,
                    "Notificaciones no permitidas",
                    Toast.LENGTH_SHORT
                ).show()
            }

            actualizarEstadoNotificaciones()
        }


    private val permisoUbicacionLauncher =
        registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { concedido ->

            if (concedido) {

                Toast.makeText(
                    this,
                    "Permiso de ubicación activado",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                Toast.makeText(
                    this,
                    "Permiso de ubicación no concedido",
                    Toast.LENGTH_SHORT
                ).show()
            }

            actualizarEstadoGps()
        }

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

        // =============================
        // CERRAR PERFIL
        // =============================

        findViewById<View>(R.id.btnCerrar).setOnClickListener {
            finish()
        }

        // =============================
        // FOTO DE PERFIL
        // =============================

        val imgPerfil =
            findViewById<ImageView>(R.id.imgPerfil)

        imgPerfil.setOnClickListener {

            val intent = Intent(
                this,
                activity_registro_foto::class.java
            )

            startActivity(intent)
        }

        // =============================
        // ESTADOS DEL DISPOSITIVO
        // =============================

        configurarClicksEstados()
        actualizarEstados()

        // =============================
        // SUGERENCIA
        // =============================

        val edtSugerencia =
            findViewById<EditText>(R.id.edtSugerencia)

        val txtContador =
            findViewById<TextView>(R.id.txtContadorSugerencia)

        val btnEnviar =
            findViewById<TextView>(R.id.btnEnviarSugerencia)

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

                    val cantidad =
                        s?.length ?: 0

                    txtContador.text =
                        "$cantidad / 100"
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {
                }
            }
        )

        // =============================
        // VALIDAR Y ENVIAR
        // =============================

        btnEnviar.setOnClickListener {

            val sugerencia =
                edtSugerencia.text
                    .toString()
                    .trim()

            when {

                sugerencia.isEmpty() -> {

                    edtSugerencia.error =
                        "Escribe una sugerencia"

                    edtSugerencia.requestFocus()
                }

                sugerencia.length < 10 -> {

                    edtSugerencia.error =
                        "La sugerencia debe tener al menos 10 caracteres"

                    edtSugerencia.requestFocus()
                }

                else -> {

                    Toast.makeText(
                        this,
                        "Gracias por tu sugerencia",
                        Toast.LENGTH_SHORT
                    ).show()

                    edtSugerencia.text.clear()

                    txtContador.text =
                        "0 / 100"
                }
            }
        }
    }

    // =========================================
    // REFRESCAR AL REGRESAR A LA PANTALLA
    // =========================================

    override fun onResume() {
        super.onResume()

        actualizarEstados()
    }

    // =========================================
    // ACTUALIZAR TODOS LOS ESTADOS
    // =========================================

    private fun actualizarEstados() {

        actualizarEstadoInternet()
        actualizarEstadoGps()
        actualizarEstadoCamara()
        actualizarEstadoNotificaciones()
    }

    // =========================================
    // INTERNET
    // =========================================

    private fun actualizarEstadoInternet() {

        val txtEstado =
            findViewById<TextView>(
                R.id.txtEstadoInternet
            )

        val icono =
            findViewById<TextView>(
                R.id.iconEstadoInternet
            )

        val connectivityManager =
            getSystemService(
                Context.CONNECTIVITY_SERVICE
            ) as ConnectivityManager

        val network =
            connectivityManager.activeNetwork

        val capabilities =
            connectivityManager
                .getNetworkCapabilities(network)

        val conectado =
            capabilities?.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            ) == true &&
                    capabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_VALIDATED
                    )

        if (conectado) {

            mostrarDisponible(
                txtEstado,
                icono,
                "Conectado"
            )

        } else {

            mostrarError(
                txtEstado,
                icono,
                "Sin conexión"
            )
        }
    }

    // =========================================
    // GPS / UBICACIÓN
    // =========================================

    private fun actualizarEstadoGps() {

        val txtEstado =
            findViewById<TextView>(
                R.id.txtEstadoGps
            )

        val icono =
            findViewById<TextView>(
                R.id.iconEstadoGps
            )

        val locationManager =
            getSystemService(
                Context.LOCATION_SERVICE
            ) as LocationManager

        val gpsActivo =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

                locationManager.isLocationEnabled

            } else {

                locationManager.isProviderEnabled(
                    LocationManager.GPS_PROVIDER
                )
            }

        val permisoUbicacion =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        when {

            !gpsActivo -> {

                mostrarError(
                    txtEstado,
                    icono,
                    "GPS desactivado"
                )
            }

            !permisoUbicacion -> {

                mostrarAdvertencia(
                    txtEstado,
                    icono,
                    "Sin permiso"
                )
            }

            else -> {

                mostrarDisponible(
                    txtEstado,
                    icono,
                    "Disponible"
                )
            }
        }
    }

    // =========================================
    // CÁMARA
    // =========================================

    private fun actualizarEstadoCamara() {

        val txtEstado =
            findViewById<TextView>(
                R.id.txtEstadoCamara
            )

        val icono =
            findViewById<TextView>(
                R.id.iconEstadoCamara
            )

        val tieneCamara =
            packageManager.hasSystemFeature(
                PackageManager.FEATURE_CAMERA_ANY
            )

        val permiso =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

        when {

            !tieneCamara -> {

                mostrarError(
                    txtEstado,
                    icono,
                    "No disponible"
                )
            }

            !permiso -> {

                mostrarAdvertencia(
                    txtEstado,
                    icono,
                    "Sin permiso"
                )
            }

            else -> {

                mostrarDisponible(
                    txtEstado,
                    icono,
                    "Permitida"
                )
            }
        }
    }

    // =========================================
    // NOTIFICACIONES
    // =========================================

    private fun actualizarEstadoNotificaciones() {

        val txtEstado =
            findViewById<TextView>(
                R.id.txtEstadoNotificaciones
            )

        val icono =
            findViewById<TextView>(
                R.id.iconEstadoNotificaciones
            )

        val habilitadas =
            NotificationManagerCompat
                .from(this)
                .areNotificationsEnabled()

        if (habilitadas) {

            mostrarDisponible(
                txtEstado,
                icono,
                "Permitidas"
            )

        } else {

            mostrarAdvertencia(
                txtEstado,
                icono,
                "Desactivadas"
            )
        }
    }

    // =========================================
    // COLORES Y ESTADOS
    // =========================================

    private fun mostrarDisponible(
        texto: TextView,
        icono: TextView,
        mensaje: String
    ) {

        texto.text = mensaje
        texto.setTextColor(
            android.graphics.Color.parseColor(
                "#22A447"
            )
        )

        icono.text = "✓"
        icono.setTextColor(
            android.graphics.Color.parseColor(
                "#22A447"
            )
        )
    }

    private fun mostrarAdvertencia(
        texto: TextView,
        icono: TextView,
        mensaje: String
    ) {

        texto.text = mensaje
        texto.setTextColor(
            android.graphics.Color.parseColor(
                "#F59E0B"
            )
        )

        icono.text = "!"
        icono.setTextColor(
            android.graphics.Color.parseColor(
                "#F59E0B"
            )
        )
    }

    private fun mostrarError(
        texto: TextView,
        icono: TextView,
        mensaje: String
    ) {

        texto.text = mensaje
        texto.setTextColor(
            android.graphics.Color.parseColor(
                "#D71920"
            )
        )

        icono.text = "×"
        icono.setTextColor(
            android.graphics.Color.parseColor(
                "#D71920"
            )
        )
    }

    // =========================================
    // CLICK EN LOS ESTADOS
    // =========================================

    private fun configurarClicksEstados() {

        val rowGps =
            findViewById<View>(R.id.rowEstadoGps)

        val rowCamara =
            findViewById<View>(R.id.rowEstadoCamara)

        val rowNotificaciones =
            findViewById<View>(R.id.rowEstadoNotificaciones)

        val rowInternet =
            findViewById<View>(R.id.rowEstadoInternet)


        // =====================================
        // GPS / UBICACIÓN
        // =====================================

        rowGps.setOnClickListener {

            val locationManager =
                getSystemService(
                    Context.LOCATION_SERVICE
                ) as LocationManager

            val gpsActivo =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

                    locationManager.isLocationEnabled

                } else {

                    locationManager.isProviderEnabled(
                        LocationManager.GPS_PROVIDER
                    )
                }


            val permisoUbicacion =
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED


            when {

                // Primero necesitamos permiso
                !permisoUbicacion -> {

                    permisoUbicacionLauncher.launch(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                }

                // Tiene permiso pero GPS apagado
                !gpsActivo -> {

                    try {

                        val intent =
                            Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS
                            )

                        startActivity(intent)

                    } catch (e: Exception) {

                        Toast.makeText(
                            this,
                            "No se pudo abrir la configuración de ubicación",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                // Todo correcto
                else -> {

                    Toast.makeText(
                        this,
                        "La ubicación ya está activa",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


        // =====================================
        // CÁMARA
        // =====================================

        rowCamara.setOnClickListener {

            val permisoCamara =
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED


            if (permisoCamara) {

                Toast.makeText(
                    this,
                    "La cámara ya tiene permiso",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                permisoCamaraLauncher.launch(
                    Manifest.permission.CAMERA
                )
            }
        }


        // =====================================
        // NOTIFICACIONES
        // =====================================

        rowNotificaciones.setOnClickListener {

            val notificacionesActivas =
                NotificationManagerCompat
                    .from(this)
                    .areNotificationsEnabled()


            if (notificacionesActivas) {

                Toast.makeText(
                    this,
                    "Las notificaciones ya están activadas",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }


            // Android 13+
            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.TIRAMISU
            ) {

                val permiso =
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    )

                if (
                    permiso !=
                    PackageManager.PERMISSION_GRANTED
                ) {

                    permisoNotificacionesLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                    )

                } else {

                    abrirConfiguracionNotificaciones()
                }

            } else {

                // Android anteriores a 13
                abrirConfiguracionNotificaciones()
            }
        }


        // =====================================
        // INTERNET
        // =====================================

        rowInternet.setOnClickListener {

            val connectivityManager =
                getSystemService(
                    Context.CONNECTIVITY_SERVICE
                ) as ConnectivityManager

            val network =
                connectivityManager.activeNetwork

            val capabilities =
                connectivityManager
                    .getNetworkCapabilities(network)

            val conectado =
                capabilities?.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET
                ) == true &&
                        capabilities.hasCapability(
                            NetworkCapabilities.NET_CAPABILITY_VALIDATED
                        )


            if (conectado) {

                Toast.makeText(
                    this,
                    "La conexión a internet está disponible",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                abrirConfiguracionInternet()
            }
        }

    }

    private fun abrirConfiguracionNotificaciones() {

        try {

            val intent =
                Intent(
                    Settings.ACTION_APP_NOTIFICATION_SETTINGS
                ).apply {

                    putExtra(
                        Settings.EXTRA_APP_PACKAGE,
                        packageName
                    )
                }

            startActivity(intent)

        } catch (e: Exception) {

            abrirConfiguracionAplicacion()
        }
    }
    private fun abrirConfiguracionInternet() {

        try {

            val intent =
                Intent(
                    Settings.ACTION_WIFI_SETTINGS
                )

            startActivity(intent)

        } catch (e: Exception) {

            val intent =
                Intent(
                    Settings.ACTION_SETTINGS
                )

            startActivity(intent)
        }
    }
    private fun abrirConfiguracionAplicacion() {

        val intent =
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            ).apply {

                data =
                    android.net.Uri.parse(
                        "package:$packageName"
                    )
            }

        startActivity(intent)
    }
}