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
import android.util.Log
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
import com.bumptech.glide.Glide
import com.cloud.gurkasapp.api.RetrofitClient
import com.cloud.gurkasapp.models.DatosResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PerfilActivity : AppCompatActivity() {


    // ==========================================================
    // PERMISO CÁMARA
    // ==========================================================

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


    // ==========================================================
    // PERMISO NOTIFICACIONES
    // ==========================================================

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


    // ==========================================================
    // PERMISO UBICACIÓN
    // ==========================================================

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


    // ==========================================================
    // ON CREATE
    // ==========================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(
            R.layout.activity_perfil
        )


        // ======================================================
        // INSETS
        // ======================================================

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v, insets ->

            val systemBars =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }


        // ======================================================
        // RECIBIR CÓDIGO DESDE MAIN ACTIVITY
        // ======================================================

        val codigoUsuario =
            intent
                .getStringExtra(
                    "codigo_usuario"
                )
                ?.trim()
                ?: ""


        Log.d(
            "DATOS_PERSONAL",
            "Código recibido: $codigoUsuario"
        )


        // ======================================================
        // CERRAR PERFIL
        // ======================================================

        findViewById<View>(
            R.id.btnCerrar
        ).setOnClickListener {

            finish()
        }


        // ======================================================
        // FOTO DE PERFIL
        // ======================================================

        val imgPerfil =
            findViewById<ImageView>(
                R.id.imgPerfil
            )

        imgPerfil.setOnClickListener {

            val intent =
                Intent(
                    this,
                    activity_registro_foto::class.java
                )

            startActivity(
                intent
            )
        }


        // ======================================================
        // CONSULTAR DATOS PERSONALES
        // ======================================================

        if (codigoUsuario.isNotBlank()) {

            obtenerDatosPersonal(
                codigoUsuario
            )

        } else {

            Toast.makeText(
                this,
                "No se recibió el código del usuario",
                Toast.LENGTH_SHORT
            ).show()
        }


        // ======================================================
        // ESTADOS DEL DISPOSITIVO
        // ======================================================

        configurarClicksEstados()

        actualizarEstados()


        // ======================================================
        // SUGERENCIA
        // ======================================================

        val edtSugerencia =
            findViewById<EditText>(
                R.id.edtSugerencia
            )

        val txtContador =
            findViewById<TextView>(
                R.id.txtContadorSugerencia
            )

        val btnEnviar =
            findViewById<TextView>(
                R.id.btnEnviarSugerencia
            )


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
                        s?.length
                            ?: 0

                    txtContador.text =
                        "$cantidad / 100"
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {
                }
            }
        )


        // ======================================================
        // VALIDAR Y ENVIAR SUGERENCIA
        // ======================================================

        btnEnviar.setOnClickListener {

            val sugerencia =
                edtSugerencia
                    .text
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


    // ==========================================================
    // CONSULTAR DATOS PERSONALES
    // ==========================================================

    private fun obtenerDatosPersonal(
        codigo: String
    ) {


        // ======================================================
        // VISTAS
        // ======================================================

        val imgPerfil =
            findViewById<ImageView>(
                R.id.imgPerfil
            )

        val txtNombre =
            findViewById<TextView>(
                R.id.txtNombre
            )

        val txtCarrera =
            findViewById<TextView>(
                R.id.txtCarrera
            )

        val txtCorreoInstitucional =
            findViewById<TextView>(
                R.id.txtCorreoInstitucional
            )

        val txtCodigo =
            findViewById<TextView>(
                R.id.txtCodigo
            )

        val txtDni =
            findViewById<TextView>(
                R.id.txtDni
            )

        val txtModalidad =
            findViewById<TextView>(
                R.id.txtModalidad
            )

        val txtCampus =
            findViewById<TextView>(
                R.id.txtCampus
            )

        val txtTelefono =
            findViewById<TextView>(
                R.id.txtTelefono
            )

        val txtCorreoPersonal =
            findViewById<TextView>(
                R.id.txtCorreoPersonal
            )

        val txtContactoNombre =
            findViewById<TextView>(
                R.id.txtContactoNombre
            )

        val txtContactoTelefono =
            findViewById<TextView>(
                R.id.txtContactoTelefono
            )

        val txtParentesco =
            findViewById<TextView>(
                R.id.txtParentesco
            )


        // ======================================================
        // MOSTRAR CÓDIGO
        // ======================================================

        txtCodigo.text =
            codigo


        // ======================================================
        // LOG
        // ======================================================

        Log.d(
            "DATOS_PERSONAL",
            "Consultando API con código: $codigo"
        )


        // ======================================================
        // LLAMADA API
        // ======================================================

        RetrofitClient
            .apiService
            .obtenerDatosPersonal(
                codigo
            )
            .enqueue(

                object :
                    Callback<DatosResponse> {


                    override fun onResponse(
                        call: Call<DatosResponse>,
                        response: Response<DatosResponse>
                    ) {


                        // =========================================
                        // ERROR HTTP
                        // =========================================

                        if (!response.isSuccessful) {

                            Log.e(
                                "DATOS_PERSONAL",
                                "Error HTTP: ${response.code()}"
                            )

                            Toast.makeText(
                                this@PerfilActivity,
                                "Error al obtener datos: ${response.code()}",
                                Toast.LENGTH_SHORT
                            ).show()

                            return
                        }


                        // =========================================
                        // LISTA
                        // =========================================

                        val lista =
                            response
                                .body()
                                ?.lista
                                ?: emptyList()


                        if (lista.isEmpty()) {

                            Toast.makeText(
                                this@PerfilActivity,
                                "No se encontraron datos del trabajador",
                                Toast.LENGTH_SHORT
                            ).show()

                            return
                        }


                        // =========================================
                        // PRIMER REGISTRO
                        // =========================================

                        val datos =
                            lista[0]


                        Log.d(
                            "DATOS_PERSONAL",
                            "Nombre: ${datos.nombrecompleto}"
                        )

                        Log.d(
                            "DATOS_PERSONAL",
                            "Puesto: ${datos.puesto}"
                        )

                        Log.d(
                            "DATOS_PERSONAL",
                            "Foto: ${datos.foto}"
                        )


                        // =========================================
                        // NOMBRE
                        // =========================================

                        txtNombre.text =
                            datos
                                .nombrecompleto
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?: "--"


                        // =========================================
                        // PUESTO CABECERA
                        // =========================================

                        txtCarrera.text =
                            datos
                                .puesto
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?: "--"


                        // =========================================
                        // CORREO CABECERA
                        // =========================================

                        txtCorreoInstitucional.text =
                            datos
                                .correo
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?: "--"


                        // =========================================
                        // DNI
                        // =========================================

                        txtDni.text =
                            datos
                                .doctidentidad
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?: "--"


                        // =========================================
                        // PUESTO
                        // =========================================

                        txtModalidad.text =
                            datos
                                .puesto
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?: "--"


                        // =========================================
                        // SEDE
                        // =========================================

                        txtCampus.text =
                            datos
                                .sede
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?: "--"


                        // =========================================
                        // TELÉFONO
                        // =========================================

                        txtTelefono.text =
                            datos
                                .celular
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?: "--"


                        // =========================================
                        // CORREO PERSONAL
                        // =========================================

                        txtCorreoPersonal.text =
                            datos
                                .correo
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?: "--"


                        // =========================================
                        // CONTACTO EMERGENCIA
                        // =========================================

                        txtContactoNombre.text =
                            datos
                                .nombre_contacto
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?: "--"


                        // =========================================
                        // TELÉFONO CONTACTO
                        // =========================================

                        txtContactoTelefono.text =
                            datos
                                .celular_c_1
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?: "--"


                        // =========================================
                        // PARENTESCO
                        //
                        // LA API NO LO DEVUELVE
                        // =========================================

                        txtParentesco.text =
                            "--"


                        // =========================================
                        // FOTO
                        // =========================================

                        val urlFoto =
                            datos
                                .foto
                                ?.trim()


                        if (
                            !urlFoto.isNullOrBlank()
                        ) {

                            Log.d(
                                "DATOS_PERSONAL",
                                "Cargando foto: $urlFoto"
                            )


                            Glide
                                .with(
                                    this@PerfilActivity
                                )
                                .load(
                                    urlFoto
                                )
                                .placeholder(
                                    R.drawable.ic_usuario
                                )
                                .error(
                                    R.drawable.ic_usuario
                                )
                                .centerCrop()
                                .into(
                                    imgPerfil
                                )

                        } else {

                            imgPerfil.setImageResource(
                                R.drawable.ic_usuario
                            )
                        }
                    }


                    override fun onFailure(
                        call: Call<DatosResponse>,
                        t: Throwable
                    ) {

                        Log.e(
                            "DATOS_PERSONAL",
                            "Error conexión: ${t.message}",
                            t
                        )

                        Toast.makeText(
                            this@PerfilActivity,
                            "Error de conexión: ${t.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )
    }


    // ==========================================================
    // REFRESCAR AL REGRESAR
    // ==========================================================

    override fun onResume() {
        super.onResume()

        actualizarEstados()
    }


    // ==========================================================
    // ACTUALIZAR TODOS LOS ESTADOS
    // ==========================================================

    private fun actualizarEstados() {

        actualizarEstadoInternet()
        actualizarEstadoGps()
        actualizarEstadoCamara()
        actualizarEstadoNotificaciones()
    }


    // ==========================================================
    // INTERNET
    // ==========================================================

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
                .getNetworkCapabilities(
                    network
                )


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


    // ==========================================================
    // GPS
    // ==========================================================

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
            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.P
            ) {

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


    // ==========================================================
    // CÁMARA
    // ==========================================================

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


    // ==========================================================
    // NOTIFICACIONES
    // ==========================================================

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


    // ==========================================================
    // MOSTRAR DISPONIBLE
    // ==========================================================

    private fun mostrarDisponible(
        texto: TextView,
        icono: TextView,
        mensaje: String
    ) {

        texto.text =
            mensaje

        texto.setTextColor(
            android.graphics.Color.parseColor(
                "#22A447"
            )
        )

        icono.text =
            "✓"

        icono.setTextColor(
            android.graphics.Color.parseColor(
                "#22A447"
            )
        )
    }


    // ==========================================================
    // MOSTRAR ADVERTENCIA
    // ==========================================================

    private fun mostrarAdvertencia(
        texto: TextView,
        icono: TextView,
        mensaje: String
    ) {

        texto.text =
            mensaje

        texto.setTextColor(
            android.graphics.Color.parseColor(
                "#F59E0B"
            )
        )

        icono.text =
            "!"

        icono.setTextColor(
            android.graphics.Color.parseColor(
                "#F59E0B"
            )
        )
    }


    // ==========================================================
    // MOSTRAR ERROR
    // ==========================================================

    private fun mostrarError(
        texto: TextView,
        icono: TextView,
        mensaje: String
    ) {

        texto.text =
            mensaje

        texto.setTextColor(
            android.graphics.Color.parseColor(
                "#D71920"
            )
        )

        icono.text =
            "×"

        icono.setTextColor(
            android.graphics.Color.parseColor(
                "#D71920"
            )
        )
    }


    // ==========================================================
    // CONFIGURAR CLICKS DE ESTADOS
    // ==========================================================

    private fun configurarClicksEstados() {

        val rowGps =
            findViewById<View>(
                R.id.rowEstadoGps
            )

        val rowCamara =
            findViewById<View>(
                R.id.rowEstadoCamara
            )

        val rowNotificaciones =
            findViewById<View>(
                R.id.rowEstadoNotificaciones
            )

        val rowInternet =
            findViewById<View>(
                R.id.rowEstadoInternet
            )


        // ======================================================
        // GPS
        // ======================================================

        rowGps.setOnClickListener {

            val locationManager =
                getSystemService(
                    Context.LOCATION_SERVICE
                ) as LocationManager


            val gpsActivo =
                if (
                    Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.P
                ) {

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

                !permisoUbicacion -> {

                    permisoUbicacionLauncher.launch(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                }


                !gpsActivo -> {

                    try {

                        val intent =
                            Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS
                            )

                        startActivity(
                            intent
                        )

                    } catch (e: Exception) {

                        Toast.makeText(
                            this,
                            "No se pudo abrir la configuración de ubicación",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }


                else -> {

                    Toast.makeText(
                        this,
                        "La ubicación ya está activa",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


        // ======================================================
        // CÁMARA
        // ======================================================

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


        // ======================================================
        // NOTIFICACIONES
        // ======================================================

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

                abrirConfiguracionNotificaciones()
            }
        }


        // ======================================================
        // INTERNET
        // ======================================================

        rowInternet.setOnClickListener {

            val connectivityManager =
                getSystemService(
                    Context.CONNECTIVITY_SERVICE
                ) as ConnectivityManager


            val network =
                connectivityManager.activeNetwork


            val capabilities =
                connectivityManager
                    .getNetworkCapabilities(
                        network
                    )


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


    // ==========================================================
    // CONFIGURACIÓN NOTIFICACIONES
    // ==========================================================

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


            startActivity(
                intent
            )

        } catch (e: Exception) {

            abrirConfiguracionAplicacion()
        }
    }


    // ==========================================================
    // CONFIGURACIÓN INTERNET
    // ==========================================================

    private fun abrirConfiguracionInternet() {

        try {

            val intent =
                Intent(
                    Settings.ACTION_WIFI_SETTINGS
                )


            startActivity(
                intent
            )

        } catch (e: Exception) {

            val intent =
                Intent(
                    Settings.ACTION_SETTINGS
                )


            startActivity(
                intent
            )
        }
    }


    // ==========================================================
    // CONFIGURACIÓN DE LA APP
    // ==========================================================

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


        startActivity(
            intent
        )
    }
}